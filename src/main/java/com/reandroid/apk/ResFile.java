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

import com.reandroid.apk.xmlencoder.XMLEncodeSource;
import com.reandroid.archive.BlockInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.arsc.value.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResFile {
    private final List<Entry> entryList;
    private final InputSource inputSource;
    private boolean mBinXml;
    private boolean mBinXmlChecked;
    private String mFileExtension;
    private boolean mFileExtensionChecked;
    private Entry mSelectedEntry;
    public ResFile(InputSource inputSource, List<Entry> entryList){
        this.inputSource=inputSource;
        this.entryList = entryList;
    }
    public List<Entry> getEntryList(){
        return entryList;
    }
    public PackageBlock getPackageBlock(){
        Entry entry = pickOne();
        if(entry != null){
            return entry.getPackageBlock();
        }
        return null;
    }
    public ResXmlDocument readAsXmlDocument() throws IOException {
        InputSource inputSource = getInputSource();
        if(inputSource instanceof BlockInputSource){
            BlockInputSource<?> bis = (BlockInputSource<?>) inputSource;
            Chunk<?> chunk = bis.getBlock();
            if(chunk instanceof ResXmlDocument){
                return (ResXmlDocument) chunk;
            }
        }
        ResXmlDocument xmlDocument = new ResXmlDocument();
        xmlDocument.setPackageBlock(getPackageBlock());
        xmlDocument.readBytes(getInputSource().openStream());
        return xmlDocument;
    }
    public String validateTypeDirectoryName(){
        Entry entry =pickOne();
        if(entry ==null){
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
        TypeBlock typeBlock= entry.getTypeBlock();
        String typeName=typeBlock.getTypeName()+typeBlock.getResConfig().getQualifiers();
        return root+typeName+"/"+name;
    }
    public Entry pickOne(){
        if(mSelectedEntry ==null){
            mSelectedEntry =selectOne();
        }
        return mSelectedEntry;
    }
    private Entry selectOne(){
        List<Entry> entryList = this.entryList;
        if(entryList.size() == 0){
            return null;
        }
        if(entryList.size() == 1){
            return entryList.get(0);
        }
        String name = getInputSource().getName();
        String alias = getInputSource().getAlias();
        for(Entry entry : entryList){
            ResValue resValue = entry.getResValue();
            if(resValue == null || resValue.getValueType() != ValueType.STRING){
                continue;
            }
            String value = resValue.getValueAsString();
            if(name.equals(value) || alias.equals(value)){
                return entry;
            }
        }
        for(Entry entry : entryList){
            if(!entry.isNull() && entry.isDefault()){
                return entry;
            }
        }
        for(Entry entry : entryList){
            if(!entry.isNull()){
                return entry;
            }
        }
        for(Entry entry : entryList){
            if(entry.isDefault()){
                return entry;
            }
        }
        return entryList.get(0);
    }
    public String getFilePath(){
        return getInputSource().getAlias();
    }
    public void setFilePath(String filePath){
        getInputSource().setAlias(filePath);
        for(Entry entry : entryList){
            TableEntry<?, ?> tableEntry = entry.getTableEntry();
            if(!(tableEntry instanceof ResTableEntry)){
                continue;
            }
            ResValue resValue = ((ResTableEntry) tableEntry).getValue();
            resValue.setValueAsString(filePath);
        }
    }
    public InputSource getInputSource() {
        return inputSource;
    }
    public boolean isBinaryXml(){
        if(mBinXmlChecked){
            return mBinXml;
        }
        mBinXmlChecked = true;
        InputSource inputSource = getInputSource();
        if((inputSource instanceof XMLEncodeSource)
                || (inputSource instanceof JsonXmlInputSource)){
            mBinXml = true;
        }else if (inputSource instanceof BlockInputSource){
            BlockInputSource<?> bis = (BlockInputSource<?>) inputSource;
            Chunk<?> chunk = bis.getBlock();
            if(chunk instanceof ResXmlDocument){
                mBinXml = true;
            }
        }
        if(!mBinXml){
            try {
                mBinXml = ResXmlDocument.isResXmlBlock(
                        inputSource.getBytes(InfoHeader.INFO_MIN_SIZE));
            } catch (IOException ignored) {
            }
            // Header could be obfuscated lets try load the whole document
            if(!mBinXml && getFilePath().endsWith(".xml")){
                try {
                    ResXmlDocument resXmlDocument = readAsXmlDocument();
                    mBinXml = !resXmlDocument.getStringPool().isEmpty();
                } catch (IOException ignored) {
                }
            }
        }
        return mBinXml;
    }
    public File buildOutFile(File dir){
        String path=getFilePath();
        path=path.replace('/', File.separatorChar);
        return new File(dir, path);
    }
    public String buildPath(){
        return buildPath(null);
    }
    public String buildPath(String parent){
        Entry entry = pickOne();
        StringBuilder builder = new StringBuilder();
        if(parent!=null){
            builder.append(parent);
            if(!parent.endsWith("/")){
                builder.append('/');
            }
        }
        TypeBlock typeBlock = entry.getTypeBlock();
        builder.append(typeBlock.getTypeName());
        builder.append(typeBlock.getQualifiers());
        builder.append('/');
        builder.append(entry.getName());
        String ext = getFileExtension();
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
        String path = getFilePath();
        if(path.endsWith(".9.png")){
            return ".9.png";
        }
        int i = path.lastIndexOf('.');
        if(i > 0){
            return path.substring(i);
        }
        try {
            String magicExt = FileMagic.getExtensionFromMagic(getInputSource());
            if(magicExt != null){
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
}
