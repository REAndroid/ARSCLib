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
package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.lib.apk.xmlencoder.XMLEncodeSource;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ResFile {
    private final List<EntryBlock> entryBlockList;
    private final InputSource inputSource;
    private boolean mBinXml;
    private boolean mBinXmlChecked;
    private String mFileExtension;
    private boolean mFileExtensionChecked;
    public ResFile(InputSource inputSource, List<EntryBlock> entryBlockList){
        this.inputSource=inputSource;
        this.entryBlockList=entryBlockList;
    }
    public List<EntryBlock> getEntryBlockList(){
        return entryBlockList;
    }
    public String validateTypeDirectoryName(){
        EntryBlock entryBlock=pickOne();
        if(entryBlock==null){
            return null;
        }
        String path=getFilePath();
        String root="";
        int i=path.indexOf('/');
        if(i>0){
            i++;
            root=path.substring(0, i);
            path=path.substring(i);
        }
        String name=path;
        i=path.lastIndexOf('/');
        if(i>0){
            i++;
            name=path.substring(i);
        }
        TypeBlock typeBlock=entryBlock.getTypeBlock();
        String typeName=typeBlock.getTypeName()+typeBlock.getResConfig().getQualifiers();
        return root+typeName+"/"+name;
    }
    public EntryBlock pickOne(){
        List<EntryBlock> entryList = entryBlockList;
        if(entryList.size()==0){
            return null;
        }
        for(EntryBlock entryBlock:entryList){
            if(!entryBlock.isNull() && entryBlock.isDefault()){
                return entryBlock;
            }
        }
        for(EntryBlock entryBlock:entryList){
            if(!entryBlock.isNull()){
                return entryBlock;
            }
        }
        for(EntryBlock entryBlock:entryList){
            if(entryBlock.isDefault()){
                return entryBlock;
            }
        }
        return entryList.get(0);
    }
    public String getFilePath(){
        return getInputSource().getAlias();
    }
    public void setFilePath(String filePath){
        getInputSource().setAlias(filePath);
        for(EntryBlock entryBlock:entryBlockList){
            entryBlock.getValueAsTableString().set(filePath);
        }
    }
    public InputSource getInputSource() {
        return inputSource;
    }
    public boolean isBinaryXml(){
        if(mBinXmlChecked){
            return mBinXml;
        }
        mBinXmlChecked=true;
        InputSource inputSource=getInputSource();
        if((inputSource instanceof XMLEncodeSource)
                || (inputSource instanceof JsonXmlInputSource)){
            mBinXml=true;
        }else{
            try {
                InputStream inputStream=getInputSource().openStream();
                mBinXml=ResXmlBlock.isResXmlBlock(inputStream);
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
        return mBinXml;
    }
    public boolean dumpToJson(File rootDir) throws IOException {
        if(!isBinaryXml()){
            return false;
        }
        String fileName=getFilePath()+ApkUtil.JSON_FILE_EXTENSION;
        fileName=fileName.replace('/', File.separatorChar);
        File file=new File(rootDir, fileName);
        ResXmlBlock resXmlBlock=new ResXmlBlock();
        resXmlBlock.readBytes(getInputSource().openStream());
        JSONObject jsonObject=resXmlBlock.toJson();
        jsonObject.write(file);
        return true;
    }
    public File buildOutFile(File dir){
        String path=getFilePath();
        path=path.replace('/', File.separatorChar);
        return new File(dir, path);
    }
    public String buildPath(){
        EntryBlock entryBlock=pickOne();
        TypeBlock typeBlock=entryBlock.getTypeBlock();
        StringBuilder builder=new StringBuilder();
        String type=typeBlock.getTypeName();
        builder.append(type);
        if(!type.equals("plurals") && !type.endsWith("s")){
            builder.append('s');
        }
        builder.append(typeBlock.getQualifiers());
        builder.append('/');
        builder.append(entryBlock.getName());
        String ext=getFileExtension();
        if(ext!=null){
            builder.append(ext);
        }
        return builder.toString();
    }
    private String getFileExtension(){
        if(!mFileExtensionChecked){
            mFileExtensionChecked=true;
            mFileExtension=readFileExtension();
        }
        return mFileExtension;
    }
    private String readFileExtension(){
        if(isBinaryXml()){
            return ".xml";
        }
        String path=getFilePath();
        int i=path.lastIndexOf('.');
        if(i>0){
            return path.substring(i);
        }
        try {
            String magicExt=FileMagic.getExtensionFromMagic(getInputSource());
            if(magicExt!=null){
                return magicExt;
            }
        } catch (IOException ignored) {
        }
        return null;
    }
    @Override
    public String toString(){
        return getFilePath();
    }

    public static final String DIR_NAME="res";
}
