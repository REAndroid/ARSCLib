package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ResFile {
    private final List<EntryBlock> entryBlockList;
    private final InputSource inputSource;
    private boolean mBinXml;
    private boolean mBinXmlChecked;
    public ResFile(InputSource inputSource, List<EntryBlock> entryBlockList){
        this.inputSource=inputSource;
        this.entryBlockList=entryBlockList;
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
    private EntryBlock pickOne(){
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
        try {
            mBinXml=ResXmlBlock.isResXmlBlock(getInputSource().openStream());
        } catch (IOException exception) {
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
    @Override
    public String toString(){
        return getFilePath();
    }

    public static final String DIR_NAME="res";
}
