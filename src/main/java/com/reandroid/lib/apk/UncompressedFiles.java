package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipArchive;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONConvert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

public class UncompressedFiles implements JSONConvert<JSONArray> {
    private final Set<String> mPathList;
    public UncompressedFiles(){
        this.mPathList=new HashSet<>();
    }
    public void apply(ZipArchive archive){
        for(InputSource inputSource:archive.listInputSources()){
            apply(inputSource);
        }
    }
    public void apply(InputSource inputSource){
        if(contains(inputSource.getName()) || contains(inputSource.getAlias())){
            inputSource.setMethod(ZipEntry.STORED);
        }else {
            inputSource.setMethod(ZipEntry.DEFLATED);
        }
    }
    public boolean contains(String path){
        return mPathList.contains(path);
    }
    public void add(ZipArchive zipArchive){
        for(InputSource inputSource: zipArchive.listInputSources()){
            add(inputSource);
        }
    }
    public void add(InputSource inputSource){
        if(inputSource.getMethod()!=ZipEntry.STORED){
            return;
        }
        add(inputSource.getAlias());
    }
    public void add(String path){
        if(path==null || path.length()==0){
            return;
        }
        path=path.replace(File.separatorChar, '/').trim();
        while (path.startsWith("/")){
            path=path.substring(1);
        }
        mPathList.add(path);
    }
    public void clear(){
        mPathList.clear();
    }
    @Override
    public JSONArray toJson() {
        return new JSONArray(mPathList);
    }
    @Override
    public void fromJson(JSONArray json) {
        if(json==null){
            return;
        }
        int length = json.length();
        for(int i=0;i<length;i++){
            this.add(json.getString(i));
        }
    }
    public void fromJson(File jsonFile) throws IOException {
        if(!jsonFile.isFile()){
            return;
        }
        JSONArray jsonArray=new JSONArray(new FileInputStream(jsonFile));
        fromJson(jsonArray);
    }
    public static final String JSON_FILE="uncompressed-files.json";
}
