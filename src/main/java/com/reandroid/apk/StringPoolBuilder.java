/*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apk;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.ValueHeader;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class StringPoolBuilder {
    private final Map<Integer, Set<String>> mSpecNameMap;
    private final Set<String> mTableStrings;
    private int mCurrentPackageId;
    private JSONArray mStyledStrings;
    public StringPoolBuilder(){
        this.mSpecNameMap = new HashMap<>();
        this.mTableStrings = new HashSet<>();
    }
    public void apply(TableBlock tableBlock){
        applyTableString(tableBlock.getTableStringPool());
        for(int pkgId:mSpecNameMap.keySet()){
            PackageBlock packageBlock=tableBlock.getPackageArray().getOrCreate(pkgId);
            applySpecString(packageBlock.getSpecStringPool());
        }
    }
    private void applyTableString(TableStringPool stringPool){
        stringPool.fromJson(mStyledStrings);
        stringPool.addStrings(getTableString());
        stringPool.refresh();
    }
    private void applySpecString(SpecStringPool stringPool){
        int pkgId = stringPool.getPackageBlock().getId();
        stringPool.addStrings(getSpecString(pkgId));
        stringPool.refresh();
    }
    public void scanDirectory(File resourcesDir) throws IOException {
        mCurrentPackageId=0;
        List<File> pkgDirList=ApkUtil.listDirectories(resourcesDir);
        for(File dir:pkgDirList){
            File pkgFile=new File(dir, PackageBlock.JSON_FILE_NAME);
            scanFile(pkgFile);
            List<File> jsonFileList=ApkUtil.recursiveFiles(dir, ".json");
            for(File file:jsonFileList){
                if(file.equals(pkgFile)){
                    continue;
                }
                scanFile(file);
            }
        }
    }
    public void scanFile(File jsonFile) throws IOException {
        try{
            FileInputStream inputStream=new FileInputStream(jsonFile);
            JSONObject jsonObject=new JSONObject(inputStream);
            build(jsonObject);
        }catch (JSONException ex){
            throw new IOException(jsonFile+": "+ex.getMessage());
        }
    }
    public void build(JSONObject jsonObject){
        scan(jsonObject);
    }
    public Set<String> getTableString(){
        return mTableStrings;
    }
    public Set<String> getSpecString(int pkgId){
        return mSpecNameMap.get(pkgId);
    }
    private void scan(JSONObject jsonObject){
        if(jsonObject.has(ValueHeader.NAME_entry_name)){
            addSpecName(jsonObject.optString(ValueHeader.NAME_entry_name));
        }
        if(jsonObject.has(ApkUtil.NAME_value_type)){
            if(ValueType.STRING.name().equals(jsonObject.getString(ApkUtil.NAME_value_type))){
                String data= jsonObject.optString(ApkUtil.NAME_data, "");
                addTableString(data);
            }
            return;
        }else if(jsonObject.has(PackageBlock.NAME_package_id)){
            mCurrentPackageId = jsonObject.getInt(PackageBlock.NAME_package_id);
        }
        Set<String> keyList = jsonObject.keySet();
        for(String key:keyList){
            Object obj=jsonObject.get(key);
            if(obj instanceof JSONObject){
                scan((JSONObject) obj);
                continue;
            }
            if(obj instanceof JSONArray){
                JSONArray jsonArray = (JSONArray) obj;
                if(TableBlock.NAME_styled_strings.equals(key)){
                    this.mStyledStrings = jsonArray;
                }else {
                    scan(jsonArray);
                }
            }
        }
    }
    private void scan(JSONArray jsonArray){
        if(jsonArray==null){
            return;
        }
        for(Object obj:jsonArray.getArrayList()){
            if(obj instanceof JSONObject){
                scan((JSONObject) obj);
                continue;
            }
            if(obj instanceof JSONArray){
                scan((JSONArray) obj);
            }
        }
    }
    private void addTableString(String name){
        if(name==null){
            return;
        }
        mTableStrings.add(name);
    }
    private void addSpecName(String name){
        if(name==null){
            return;
        }
        int pkgId=mCurrentPackageId;
        if(pkgId==0){
            throw new IllegalArgumentException("Current package id is 0");
        }
        Set<String> specNames=mSpecNameMap.get(pkgId);
        if(specNames==null){
            specNames=new HashSet<>();
            mSpecNameMap.put(pkgId, specNames);
        }
        specNames.add(name);
    }
}
