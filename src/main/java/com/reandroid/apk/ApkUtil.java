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
import com.reandroid.archive2.block.ApkSignatureBlock;

import java.io.File;
import java.util.*;

public class ApkUtil {
    public static String sanitizeForFileName(String name){
        if(name==null){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = name.toCharArray();
        boolean skipNext = true;
        int length = 0;
        int lengthMax = MAX_FILE_NAME_LENGTH;
        for(int i=0;i<chars.length;i++){
            if(length>=lengthMax){
                break;
            }
            char ch = chars[i];
            if(isGoodFileNameSymbol(ch)){
                if(!skipNext){
                    builder.append(ch);
                    length++;
                }
                skipNext=true;
                continue;
            }
            if(!isGoodFileNameChar(ch)){
                skipNext = true;
                continue;
            }
            builder.append(ch);
            length++;
            skipNext=false;
        }
        if(length==0){
            return null;
        }
        return builder.toString();
    }
    private static boolean isGoodFileNameSymbol(char ch){
        return ch == '.'
                || ch == '+'
                || ch == '-'
                || ch == '_'
                || ch == '#';
    }
    private static boolean isGoodFileNameChar(char ch){
        return (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z');
    }
    public static int parseHex(String hex){
        long l=Long.decode(hex);
        return (int) l;
    }
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
            int i2=path.length()- ApkUtil.JSON_FILE_EXTENSION.length();
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
            if(hasExtension(dir, ext)){
                results.add(dir);
            }
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
                if(!hasExtension(file, ext)){
                    continue;
                }
                results.add(file);
                continue;
            }
            results.addAll(recursiveFiles(file, ext));
        }
        return results;
    }
    public static List<File> recursiveFiles(File dir){
        return recursiveFiles(dir, null);
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
                if(!hasExtension(file, ext)){
                    continue;
                }
                results.add(file);
            }
        }
        return results;
    }
    private static boolean hasExtension(File file, String ext){
        if(ext==null){
            return true;
        }
        String name=file.getName().toLowerCase();
        ext=ext.toLowerCase();
        return name.endsWith(ext);
    }
    public static String toModuleName(File file){
        String name=file.getName();
        int i=name.lastIndexOf('.');
        if(i>0){
            name=name.substring(0,i);
        }
        return name;
    }
    public static Map<String, InputSource> toAliasMap(Collection<InputSource> sourceList){
        Map<String, InputSource> results=new HashMap<>();
        for(InputSource inputSource:sourceList){
            results.put(inputSource.getAlias(), inputSource);
        }
        return results;
    }
    public static final String JSON_FILE_EXTENSION=".json";
    public static final String RES_JSON_NAME="res-json";
    public static final String ROOT_NAME="root";
    public static final String SPLIT_JSON_DIRECTORY="resources";
    public static final String DEF_MODULE_NAME="base";
    public static final String NAME_value_type="value_type";
    public static final String NAME_data="data";
    public static final String RES_DIR_NAME="res";
    public static final String FILE_NAME_PUBLIC_XML ="public.xml";

    public static final String TAG_STRING_ARRAY = "string-array";
    public static final String TAG_INTEGER_ARRAY = "integer-array";

    public static final String SIGNATURE_FILE_NAME = "signatures" + ApkSignatureBlock.FILE_EXT;
    public static final String SIGNATURE_DIR_NAME = "signatures";

    private static final int MAX_FILE_NAME_LENGTH = 50;
}
