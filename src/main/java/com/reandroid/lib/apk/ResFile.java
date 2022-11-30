package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.util.List;

public class ResFile {
    private final List<EntryBlock> entryBlockList;
    private final InputSource inputSource;
    public ResFile(InputSource inputSource, List<EntryBlock> entryBlockList){
        this.inputSource=inputSource;
        this.entryBlockList=entryBlockList;
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
    @Override
    public String toString(){
        return getFilePath();
    }

    public static final String DIR_NAME="res";
}
