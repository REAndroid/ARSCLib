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
package com.reandroid.archive;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
        return path;
    }

    public static Map<String, InputSource> mapZipFileSources(ZipFile zipFile){
        Map<String, InputSource> results=new LinkedHashMap<>();
        Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
        int i=0;
        while (entriesEnum.hasMoreElements()){
            ZipEntry zipEntry = entriesEnum.nextElement();
            if(zipEntry.isDirectory()){
                continue;
            }
            ZipEntrySource source=new ZipEntrySource(zipFile, zipEntry);
            source.setSort(i);
            source.setMethod(zipEntry.getMethod());
            results.put(source.getName(), source);
            i++;
        }
        return results;
    }
    public static Map<String, ByteInputSource> mapInputStreamAsBuffer(InputStream inputStream) throws IOException {
        Map<String, ByteInputSource> results = new LinkedHashMap<>();
        ZipInputStream zin = new ZipInputStream(inputStream);
        ZipEntry zipEntry;
        int i=0;
        while ((zipEntry=zin.getNextEntry())!=null){
            if(zipEntry.isDirectory()){
                continue;
            }
            byte[] buffer = loadBuffer(zin);
            String name = sanitize(zipEntry.getName());
            ByteInputSource source = new ByteInputSource(buffer, name);
            source.setSort(i);
            source.setMethod(zipEntry.getMethod());
            results.put(name, source);
            i++;
        }
        zin.close();
        return results;
    }
    private static byte[] loadBuffer(InputStream in) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buff=new byte[40960];
        int len;
        while((len=in.read(buff))>0){
            outputStream.write(buff, 0, len);
        }
        outputStream.close();
        return outputStream.toByteArray();
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
