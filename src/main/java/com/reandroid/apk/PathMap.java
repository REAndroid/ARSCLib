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

import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipArchive;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.util.*;

public class PathMap implements JSONConvert<JSONArray> {
    private final Object mLock = new Object();
    private final Map<String, String> mNameAliasMap;
    private final Map<String, String> mAliasNameMap;

    public PathMap(){
        this.mNameAliasMap = new HashMap<>();
        this.mAliasNameMap = new HashMap<>();
    }

    public void restore(ApkModule apkModule){
        restoreResFile(apkModule.listResFiles());
        restore(apkModule.getApkArchive().listInputSources());
    }
    public List<String> restoreResFile(Collection<ResFile> files){
        List<String> results = new ArrayList<>();
        if(files == null){
            return results;
        }
        for(ResFile resFile:files){
            String alias = restoreResFile(resFile);
            if(alias==null){
                continue;
            }
            results.add(alias);
        }
        return results;
    }
    public String restoreResFile(ResFile resFile){
        InputSource inputSource = resFile.getInputSource();
        String alias = restore(inputSource);
        if(alias==null){
            return null;
        }
        resFile.setFilePath(alias);
        return alias;
    }
    public List<String> restore(Collection<InputSource> sources){
        List<String> results = new ArrayList<>();
        if(sources == null){
            return results;
        }
        for(InputSource inputSource:sources){
            String alias = restore(inputSource);
            if(alias==null){
                continue;
            }
            results.add(alias);
        }
        return results;
    }
    public String restore(InputSource inputSource){
        if(inputSource==null){
            return null;
        }
        String name = inputSource.getName();
        String alias = getName(name);
        if(alias==null){
            name = inputSource.getAlias();
            alias = getName(name);
        }
        if(alias==null || alias.equals(inputSource.getAlias())){
            return null;
        }
        inputSource.setAlias(alias);
        return alias;
    }

    public String getAlias(String name){
        synchronized (mLock){
            return mNameAliasMap.get(name);
        }
    }
    public String getName(String alias){
        synchronized (mLock){
            return mAliasNameMap.get(alias);
        }
    }
    public int size(){
        synchronized (mLock){
            return mNameAliasMap.size();
        }
    }
    public void clear(){
        synchronized (mLock){
            mNameAliasMap.clear();
            mAliasNameMap.clear();
        }
    }
    public void add(ZipArchive archive){
        if(archive == null){
            return;
        }
        add(archive.listInputSources());
    }
    public void add(Collection<? extends InputSource> sources){
        if(sources==null){
            return;
        }
        for(InputSource inputSource:sources){
            add(inputSource);
        }
    }
    public void add(InputSource inputSource){
        if(inputSource==null){
            return;
        }
        add(inputSource.getName(), inputSource.getAlias());
    }
    public void add(String name, String alias){
        if(name==null || alias==null){
            return;
        }
        if(name.equals(alias)){
            return;
        }
        synchronized (mLock){
            mNameAliasMap.remove(name);
            mNameAliasMap.put(name, alias);
            mAliasNameMap.remove(alias);
            mAliasNameMap.put(alias, name);
        }
    }

    private void add(JSONObject json){
        if(json==null){
            return;
        }
        add(json.optString(NAME_name), json.optString(NAME_alias));
    }

    @Override
    public JSONArray toJson() {
        JSONArray jsonArray = new JSONArray();
        Map<String, String> nameMap = this.mNameAliasMap;
        List<String> nameList = toSortedList(nameMap.keySet());
        for(String name:nameList){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(NAME_name, name);
            jsonObject.put(NAME_alias, nameMap.get(name));
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clear();
        if(json==null){
            return;
        }
        int length = json.length();
        for(int i=0;i<length;i++){
            add(json.optJSONObject(i));
        }
    }
    @Override
    public String toString(){
        return "PathMap size="+size();
    }

    private static List<String> toSortedList(Collection<String> stringCollection){
        List<String> results;
        if(stringCollection instanceof List){
            results = (List<String>) stringCollection;
        }else {
            results = new ArrayList<>(stringCollection);
        }
        Comparator<String> cmp = new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        };
        results.sort(cmp);
        return results;
    }

    public static final String NAME_name = "name";
    public static final String NAME_alias = "alias";
    public static final String JSON_FILE = "path-map.json";
}
