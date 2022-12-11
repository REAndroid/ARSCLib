package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipArchive;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

public class UncompressedFiles implements JSONConvert<JSONObject> {
    private final Set<String> mPathList;
    private final Set<String> mExtensionList;
    private String mResRawDir;
    public UncompressedFiles(){
        this.mPathList=new HashSet<>();
        this.mExtensionList=new HashSet<>();
    }
    public void setResRawDir(String resRawDir){
        this.mResRawDir=resRawDir;
    }
    public void apply(ZipArchive archive){
        for(InputSource inputSource:archive.listInputSources()){
            apply(inputSource);
        }
    }
    public void apply(InputSource inputSource){
        if(isUncompressed(inputSource.getAlias()) || isUncompressed(inputSource.getName())){
            inputSource.setMethod(ZipEntry.STORED);
        }else {
            inputSource.setMethod(ZipEntry.DEFLATED);
        }
    }
    public boolean isUncompressed(String path){
        if(path==null){
            return false;
        }
        if(containsPath(path)||containsExtension(path)||isResRawDir(path)){
            return true;
        }
        int i=path.indexOf('.');
        if(i<0){
            return false;
        }
        String extension=path.substring(i);
        return containsExtension(extension);
    }
    private boolean isResRawDir(String path){
        String dir=mResRawDir;
        if(dir==null||dir.length()==0){
            return false;
        }
        return path.startsWith(dir);
    }
    public boolean containsExtension(String extension){
        if(extension==null){
            return false;
        }
        if(mExtensionList.contains(extension)){
            return true;
        }
        if(!extension.startsWith(".")){
            return mExtensionList.contains("."+extension);
        }
        return mExtensionList.contains(extension.substring(1));
    }
    public boolean containsPath(String path){
        path=sanitizePath(path);
        if(path==null){
            return false;
        }
        return mPathList.contains(path);
    }
    public void addPath(ZipArchive zipArchive){
        for(InputSource inputSource: zipArchive.listInputSources()){
            addPath(inputSource);
        }
    }
    public void addPath(InputSource inputSource){
        if(inputSource.getMethod()!=ZipEntry.STORED){
            return;
        }
        addPath(inputSource.getAlias());
    }
    public void addPath(String path){
        path=sanitizePath(path);
        if(path==null){
            return;
        }
        mPathList.add(path);
    }
    public void removePath(String path){
        path=sanitizePath(path);
        if(path==null){
            return;
        }
        mPathList.remove(path);
    }
    public void replacePath(String path, String rep){
        path=sanitizePath(path);
        rep=sanitizePath(rep);
        if(path==null||rep==null){
            return;
        }
        if(!mPathList.contains(path)){
            return;
        }
        mPathList.remove(path);
        mPathList.add(rep);
    }
    public void addCommonExtensions(){
        for(String ext:COMMON_EXTENSIONS){
            addExtension(ext);
        }
    }
    public void addExtension(String extension){
        if(extension==null || extension.length()==0){
            return;
        }
        mExtensionList.add(extension);
    }
    public void clearPaths(){
        mPathList.clear();
    }
    public void clearExtensions(){
        mExtensionList.clear();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_extensions, new JSONArray(mExtensionList));
        jsonObject.put(NAME_paths, new JSONArray(mPathList));
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        clearExtensions();
        clearPaths();
        if(json==null){
            return;
        }
        JSONArray extensions = json.optJSONArray(NAME_extensions);
        if(extensions!=null){
            int length = extensions.length();
            for(int i=0;i<length;i++){
                this.addExtension(extensions.getString(i));
            }
        }
        JSONArray paths = json.optJSONArray(NAME_paths);
        if(paths!=null){
            int length = paths.length();
            for(int i=0;i<length;i++){
                this.addPath(paths.getString(i));
            }
        }
    }
    public void fromJson(File jsonFile) throws IOException {
        if(!jsonFile.isFile()){
            return;
        }
        JSONObject jsonObject=new JSONObject(new FileInputStream(jsonFile));
        fromJson(jsonObject);
    }
    private static String sanitizePath(String path){
        if(path==null || path.length()==0){
            return null;
        }
        path=path.replace(File.separatorChar, '/').trim();
        while (path.startsWith("/")){
            path=path.substring(1);
        }
        if(path.length()==0){
            return null;
        }
        return path;
    }
    public static final String JSON_FILE = "uncompressed-files.json";
    public static final String NAME_paths = "paths";
    public static final String NAME_extensions = "extensions";
    public static String[] COMMON_EXTENSIONS=new String[]{
            ".png",
            ".jpg",
            ".mp3",
            ".mp4",
            ".wav",
            ".webp",
    };
}
