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
}
