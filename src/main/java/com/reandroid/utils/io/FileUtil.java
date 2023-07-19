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
package com.reandroid.utils.io;

import com.reandroid.arsc.BuildInfo;
import com.reandroid.utils.StringsUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FileUtil {

    public static String toReadableFileSize(long size){
        if(size < 0){
            return Long.toString(size);
        }
        String[] sizeUnits = FILE_SIZE_UNITS;
        String unit = "";
        long result = size;
        long dec = 0;
        for(int i = 0; i < sizeUnits.length; i++){
            long div;
            if(i == 0){
                div = 1024;
            }else {
                div = 1000;
            }
            unit = sizeUnits[i];
            size = size / div;
            if(size == 0){
                break;
            }
            dec = (result - (size * div));
            result = size;
        }
        if(dec == 0){
            return result + unit;
        }
        return result + "." + dec + unit;
    }
    public static void deleteDirectory(File dir){
        if(dir.isFile()){
            dir.delete();
            return;
        }
        if(!dir.isDirectory()){
            return;
        }
        File[] files = dir.listFiles();
        if(files == null){
            dir.delete();
            return;
        }
        for(File file : files){
            deleteDirectory(file);
        }
        dir.delete();
    }
    public static void deleteEmptyDirectory(File dir){
        if(dir.isFile()){
            return;
        }
        if(!dir.isDirectory()){
            return;
        }
        File[] files = dir.listFiles();
        if(files == null || files.length == 0){
            dir.delete();
        }
    }
    public static File getTempDir(){
        return getTempDir(getDefPrefix());
    }
    public static File getTempDir(String prefix) {
        synchronized (FileUtil.class){
            if(prefix == null){
                prefix = "";
            }
            File dir = TEMP_DIRS.get(prefix);
            if(dir == null){
                dir = createTempDir(prefix);
                TEMP_DIRS.put(prefix, dir);
            }else if(!dir.exists()){
                dir.mkdir();
                dir.deleteOnExit();
            }
            return dir;
        }
    }
    private static File createTempDir(String prefix) {
        String path = System.getProperty("java.io.tmpdir", null);
        if(path == null){
            path = System.getProperty("user.home", null);
        }
        if(path == null){
            path = "path";
            File file = new File(path).getParentFile();
            path = file.getAbsolutePath();
        }
        return createTempDir(new File(path), prefix);
    }
    private static File createTempDir(File baseDir, String prefix) {
        String baseName = System.currentTimeMillis() + "-";
        if(baseName.length() > 12){
            baseName = baseName.substring(6);
        }
        if(prefix == null){
            prefix = "";
        }
        int max = 9999;
        int i;
        for (i = 0; i < max; i++) {
            String name = prefix + baseName + StringsUtil.formatNumber(i, max);
            File tempDir = new File(baseDir, name);
            if (tempDir.mkdir()) {
                tempDir.deleteOnExit();
                return tempDir;
            }
        }
        throw new IllegalStateException(
                "Failed to create temp directory, trials = " + i + ", base = " + baseName);
    }

    public static void setDefaultTempPrefix(String prefix) {
        synchronized (FileUtil.class){
            if(Objects.equals(prefix, def_prefix)){
                return;
            }
            if(def_prefix != null){
                TEMP_DIRS.remove(def_prefix);
            }
            def_prefix = prefix;
        }
    }

    private static String getDefPrefix() {
        if(def_prefix == null){
            def_prefix = BuildInfo.getName() + "-";
        }
        return def_prefix;
    }

    private static String def_prefix;

    private static final Map<String, File> TEMP_DIRS = new HashMap<>();

    private static final String[] FILE_SIZE_UNITS = new String[]{
            " bytes",
            " Kb",
            " Mb",
            " Gb",
            " Pb"
    };
}
