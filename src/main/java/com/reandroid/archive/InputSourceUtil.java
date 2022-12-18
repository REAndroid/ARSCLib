package com.reandroid.archive;

import java.io.File;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class InputSourceUtil {

    public static String toRelative(File rootDir, File file){
        int len=rootDir.getAbsolutePath().length();
        String path=file.getAbsolutePath();
        path=path.substring(len);
        path=sanitize(path);
        return path;
    }
    public static String sanitize(String path){
        path=path.replace('\\', '/');
        while (path.startsWith("./")){
            path=path.substring(2);
        }
        while (path.startsWith("/")){
            path=path.substring(1);
        }
        while (path.startsWith(File.separator)){
            path=path.substring(1);
        }
        return path;
    }

    public static Map<String, InputSource> mapZipFileSources(ZipFile zipFile){
        Map<String, InputSource> results=new HashMap<>();
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        int i=0;
        while (entriesEnum.hasMoreElements()){
            ZipEntry zipEntry = entriesEnum.nextElement();
            if(zipEntry.isDirectory()){
                continue;
            }
            ZipEntrySource source=new ZipEntrySource(zipFile, zipEntry);
            source.setSort(i);
            results.put(source.getName(), source);
            i++;
        }
        return results;
    }
    public static List<InputSource> listZipFileSources(ZipFile zipFile){
        List<InputSource> results=new ArrayList<>();
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        int i=0;
        while (entriesEnum.hasMoreElements()){
            ZipEntry zipEntry = entriesEnum.nextElement();
            if(zipEntry.isDirectory()){
                continue;
            }
            ZipEntrySource source=new ZipEntrySource(zipFile, zipEntry);
            source.setSort(i);
            results.add(source);
        }
        return results;
    }
    public static List<InputSource> listDirectory(File dir){
        List<InputSource> results=new ArrayList<>();
        recursiveDirectory(results, dir, dir);
        return results;
    }
    private static void recursiveDirectory(List<InputSource> results, File rootDir, File dir){
        if(dir.isFile()){
            String name;
            if(rootDir.equals(dir)){
                name=dir.getName();
            }else {
                name=toRelative(rootDir, dir);
            }
            results.add(new FileInputSource(dir, name));
            return;
        }
        File[] childFiles=dir.listFiles();
        if(childFiles==null){
            return;
        }
        for(File file:childFiles){
            recursiveDirectory(results, rootDir, file);
        }
    }
    public static List<String> sortString(List<String> stringList){
        Comparator<String> cmp=new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        };
        stringList.sort(cmp);
        return stringList;
    }

    public static List<InputSource> sort(List<InputSource> sourceList){
        Comparator<InputSource> cmp=new Comparator<InputSource>() {
            @Override
            public int compare(InputSource in1, InputSource in2) {
                return Integer.compare(in1.getSort(), in2.getSort());
            }
        };
        sourceList.sort(cmp);
        return sourceList;
    }
}
