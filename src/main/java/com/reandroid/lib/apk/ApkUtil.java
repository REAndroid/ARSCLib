package com.reandroid.lib.apk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    public static String toArchiveResourcePath(File dir, File file){
        String path = toArchivePath(dir, file);
        if(path.endsWith(ApkUtil.JSON_FILE_EXTENSION)){
            int i2=path.length()-ApkUtil.JSON_FILE_EXTENSION.length();
            path=path.substring(0, i2);
        }
        return path;
    }
    public static String toArchivePath(File dir, File file){
        String dirPath = dir.getAbsolutePath()+File.separator;
        String path = file.getAbsolutePath().substring(dirPath.length());
        path=path.replace(File.separatorChar, '/');
        return path;
    }
    public static List<File> recursiveFiles(File dir, String ext){
        List<File> results=new ArrayList<>();
        if(dir.isFile()){
            results.add(dir);
            return results;
        }
        if(!dir.isDirectory()){
            return results;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(File file:files){
            if(file.isFile()){
                if(ext!=null && !file.getName().endsWith(ext)){
                    continue;
                }
                results.add(file);
                continue;
            }
            results.addAll(recursiveFiles(file));
        }
        return results;
    }
    public static List<File> recursiveFiles(File dir){
        List<File> results=new ArrayList<>();
        if(dir.isFile()){
            results.add(dir);
            return results;
        }
        if(!dir.isDirectory()){
            return results;
        }
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(File file:files){
            if(file.isFile()){
                results.add(file);
                continue;
            }
            results.addAll(recursiveFiles(file));
        }
        return results;
    }
    public static List<File> listDirectories(File dir){
        List<File> results=new ArrayList<>();
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(File file:files){
            if(file.isDirectory()){
                results.add(file);
            }
        }
        return results;
    }
    public static List<File> listFiles(File dir, String ext){
        List<File> results=new ArrayList<>();
        File[] files=dir.listFiles();
        if(files==null){
            return results;
        }
        for(File file:files){
            if(file.isFile()){
                if(ext!=null && !file.getName().endsWith(ext)){
                    continue;
                }
                results.add(file);
            }
        }
        return results;
    }
    public static final String JSON_FILE_EXTENSION=".json";
    public static final String RES_JSON_NAME="res-json";
    public static final String ROOT_NAME="root";
    public static final String PACKAGE_JSON_FILE="package.json";
    public static final String SPLIT_JSON_DIRECTORY="resources";
    public static final String DEF_MODULE_NAME="base";
}
