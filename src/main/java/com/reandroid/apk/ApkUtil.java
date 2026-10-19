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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ApkUtil {

    public static Comparator<File> getValuesXmlComparator() {
        return (file1, file2) -> {
            String name1 = sanitizeValuesXmlName(file1);
            String name2 = sanitizeValuesXmlName(file2);
            int i = CompareUtil.compare(getValuesXmlOrder(name1),
                    getValuesXmlOrder(name2));
            if (i == 0) {
                i = CompareUtil.compare(name1, name2);
            }
            return i;
        };
    }
    static String sanitizeValuesXmlName(File file) {
        String name = file.getName().toLowerCase();
        if (name.startsWith("ids")) {
            return "ids.xml";
        }
        if (name.contains("attr")) {
            return "attrs.xml";
        }
        return name;
    }
    static int getValuesXmlOrder(String name) {
        if (name.equals("public.xml")) {
            return 0;
        }
        if (name.equals("ids.xml")) {
            return 1;
        }
        if (name.equals("attrs.xml")) {
            return 2;
        }
        return 3;
    }

    public static String replaceRootDir(String path, String dirName) {
        int i = path.indexOf('/');
        if (i >= 0) {
            i = i + 1;
            path = path.substring(i);
        }
        if (dirName != null && dirName.length() > 0) {
            if (!dirName.endsWith("/")) {
                dirName = dirName + "/";
            }
            path = dirName + path;
        }
        return path;
    }
    public static String jsonToArchiveResourcePath(File dir, File jsonFile) {
        String path = toArchivePath(dir, jsonFile);
        String ext = ApkUtil.JSON_FILE_EXTENSION;
        if (path.endsWith(ext)) {
            int i2 = path.length() - ext.length();
            path = path.substring(0, i2);
        }
        return path;
    }
    public static String toArchivePath(File dir, File file) {
        String dirPath = dir.getAbsolutePath()+File.separator;
        String path = file.getAbsolutePath().substring(dirPath.length());
        path=path.replace(File.separatorChar, '/');
        return path;
    }
    public static List<File> recursiveFiles(File dir, String ext) {
        List<File> results = new ArrayList<>();
        recursiveFiles(results, dir, ext);
        results.sort(FileUtil.getIgnoreCasePathComparator());
        return results;
    }
    private static void recursiveFiles(List<File> results, File dir, String ext) {
        if (dir.isFile()) {
            if (hasExtension(dir, ext)) {
                results.add(dir);
            }
            return;
        }
        if (!dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file:files) {
            if (file.isFile()) {
                if (hasExtension(file, ext)) {
                    results.add(file);
                }
                continue;
            }
            recursiveFiles(results, file, ext);
        }
    }
    public static List<File> recursiveFiles(File dir) {
        return recursiveFiles(dir, null);
    }
    public static List<File> listDirectories(File dir) {
        List<File> results = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return results;
        }
        for (File file:files) {
            if (file.isDirectory()) {
                results.add(file);
            }
        }
        return results;
    }
    public static List<File> listPackageDirectories(File resourcesDirectory) {
        List<File> results = new ArrayList<>();
        File[] files = resourcesDirectory.listFiles();
        if (files == null) {
            return results;
        }
        for (File dir:files) {
            if (isPackageDirectory(dir)) {
                results.add(dir);
            }
        }
        results.sort(CompareUtil.getComparableComparator());
        return results;
    }
    public static List<File> listPublicXmlFiles(File resourcesDirectory) {
        List<File> results = new ArrayList<>();
        File[] files = resourcesDirectory.listFiles();
        if (files == null) {
            return results;
        }
        for (File dir:files) {
            List<File> resDirList = listDirectories(dir);
            for (File resDir : resDirList) {
                File file = getPublicXmlFile(resDir);
                if (file != null) {
                    results.add(file);
                }
            }
        }
        results.sort(CompareUtil.getComparableComparator());
        return results;
    }
    private static File getPublicXmlFile(File resDir) {
        if (!resDir.isDirectory()) {
            return null;
        }
        File valuesDir = new File(resDir, PackageBlock.DIRECTORY_NAME_VALUES);
        if (!valuesDir.isDirectory()) {
            return null;
        }
        File file = new File(valuesDir, PackageBlock.FILE_NAME_PUBLIC_XML);
        if (!file.isFile()) {
            return null;
        }
        return file;
    }
    public static List<File> listValuesDirectory(File resDir) {
        return listValuesDirectory(resDir, true);
    }
    public static List<File> listValuesDirectory(File resDir, boolean includeVariants) {
        List<File> results = new ArrayList<>();
        if (!resDir.isDirectory()) {
            return results;
        }
        File[] files = resDir.listFiles();
        if (files == null) {
            return results;
        }
        for (File dir:files) {
            if (!dir.isDirectory()) {
                continue;
            }
            if (isValuesDirectoryName(dir.getName(), includeVariants)) {
                results.add(dir);
            }
        }
        results.sort(CompareUtil.getComparableComparator());
        return results;
    }
    public static boolean isValuesDirectoryName(String name, boolean checkVariant) {
        if (PackageBlock.DIRECTORY_NAME_VALUES.equals(name)) {
            return true;
        }
        if (!checkVariant) {
            return false;
        }
        return name.startsWith(PackageBlock.DIRECTORY_NAME_VALUES + "-");
    }
    private static boolean isPackageDirectory(File dir) {
        if (!dir.isDirectory()) {
            return false;
        }
        if (new File(dir, PackageBlock.FILE_NAME_JSON).isFile()) {
            return true;
        }
        dir = new File(dir, PackageBlock.DIRECTORY_NAME_VALUES);
        return new File(dir, PackageBlock.FILE_NAME_PUBLIC_XML).isFile();
    }
    public static List<File> listFiles(File dir, String ext) {
        List<File> results = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) {
            return results;
        }
        for (File file:files) {
            if (file.isFile()) {
                if (!hasExtension(file, ext)) {
                    continue;
                }
                results.add(file);
            }
        }
        return results;
    }
    private static boolean hasExtension(File file, String ext) {
        if (ext == null) {
            return true;
        }
        String name = file.getName().toLowerCase();
        ext = ext.toLowerCase();
        return name.endsWith(ext);
    }
    public static String toModuleName(File file) {
        return FileUtil.getNameWoExtension(file);
    }

    public static final String JSON_FILE_EXTENSION = ".json";
    public static final String ROOT_NAME = "root";
    public static final String DEF_MODULE_NAME = "base";

    public static final String SIGNATURE_DIR_NAME = "signatures";
}
