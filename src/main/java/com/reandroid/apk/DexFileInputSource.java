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

import com.reandroid.archive.InputSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

 public class DexFileInputSource extends RenamedInputSource<InputSource> implements Comparable<DexFileInputSource>{
    public DexFileInputSource(String name, InputSource inputSource){
        super(name, inputSource);
    }
    public int getDexNumber(){
        return getDexNumber(getAlias());
    }
    @Override
    public int compareTo(DexFileInputSource source) {
        return Integer.compare(getDexNumber(), source.getDexNumber());
    }

    public static void sort(List<DexFileInputSource> sourceList){
        sourceList.sort(new Comparator<DexFileInputSource>() {
            @Override
            public int compare(DexFileInputSource s1, DexFileInputSource s2) {
                return s1.compareTo(s2);
            }
        });
    }

    public static void sortDexFiles(List<File> fileList){
        fileList.sort(new Comparator<File>() {
            @Override
            public int compare(File file1, File file2) {
                int i1 = getDexNumber(file1.getName());
                int i2 = getDexNumber(file2.getName());
                if(i1 == i2){
                    return 0;
                }
                if(i1 < 0 || i1 < i2){
                    return -1;
                }
                return 1;
            }
        });
    }
    public static List<File> listDexFiles(File dir){
        List<File> results = new ArrayList<>();
        if(!dir.isDirectory()){
            return results;
        }
        File[] files = dir.listFiles();
        if(files == null){
            return results;
        }
        for(File file : files){
            if(file.isFile() && isDexName(file.getName())){
                results.add(file);
            }
        }
        sortDexFiles(results);
        return results;
    }

    public static boolean isDexName(String name){
        return getDexNumber(name)>=0;
    }
    static String getDexName(int i){
        if(i==0){
            return "classes.dex";
        }
        return "classes"+i+".dex";
    }
    static int getDexNumber(String name){
        if(name.equals("classes.dex")){
            return 0;
        }
        String prefix = "classes";
        String ext = ".dex";
        if(!name.startsWith(prefix) || !name.endsWith(ext)){
            return -1;
        }
        String num = name.substring(prefix.length(), name.length() - ext.length());
        try {
            return Integer.parseInt(num);
        }catch (NumberFormatException ignored){
            return -1;
        }
    }

    public static final String DEX_DIRECTORY_NAME = "dex";
}
