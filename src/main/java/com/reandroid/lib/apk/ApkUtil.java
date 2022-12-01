package com.reandroid.lib.apk;

public class ApkUtil {
    public static String replaceRootDir(String path, String dirName){
        int i=path.indexOf('/')+1;
        path=path.substring(i);
        if(dirName != null && dirName.length()>0){
            if(!dirName.endsWith("/")){
                dirName=dirName+"/";
            }
            path=dirName+path;
        }
        return path;
    }
    public static final String JSON_FILE_EXTENSION=".a.json";
}
