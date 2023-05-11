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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class APKArchive extends ZipArchive {
    public APKArchive(Map<String, InputSource> entriesMap){
        super(entriesMap);
    }
    public APKArchive(){
        super();
    }

    public void refresh(){
        List<InputSource> inputSourceList = listInputSources();
        applySort(inputSourceList);
        set(inputSourceList);
    }
    public void autoSortApkFiles(){
        List<InputSource> inputSourceList = listInputSources();
        autoSortApkFiles(inputSourceList);
        set(inputSourceList);
    }
    public long writeApk(File outApk) throws IOException{
        ZipSerializer serializer=new ZipSerializer(listInputSources());
        return serializer.writeZip(outApk);
    }
    public long writeApk(OutputStream outputStream) throws IOException{
        ZipSerializer serializer=new ZipSerializer(listInputSources());
        return serializer.writeZip(outputStream);
    }
    public static APKArchive loadZippedApk(File zipFile) throws IOException {
        return loadZippedApk(new ZipFile(zipFile));
    }
    public static APKArchive loadZippedApk(ZipFile zipFile) {
        Map<String, InputSource> entriesMap = InputSourceUtil.mapZipFileSources(zipFile);
        return new APKArchive(entriesMap);
    }
    public static void repackApk(File apkFile) throws IOException{
        APKArchive apkArchive =loadZippedApk(apkFile);
        apkArchive.writeApk(apkFile);
    }
    public static void applySort(List<InputSource> sourceList){
        Comparator<InputSource> cmp=new Comparator<InputSource>() {
            @Override
            public int compare(InputSource in1, InputSource in2) {
                return Integer.compare(in1.getSort(), in2.getSort());
            }
        };
        sourceList.sort(cmp);
    }
    public static void autoSortApkFiles(List<InputSource> sourceList){
        Comparator<InputSource> cmp=new Comparator<InputSource>() {
            @Override
            public int compare(InputSource in1, InputSource in2) {
                return getSortName(in1).compareTo(getSortName(in2));
            }
        };
        sourceList.sort(cmp);
        int i=0;
        for(InputSource inputSource:sourceList){
            inputSource.setSort(i);
            i++;
        }
    }
    private static String getSortName(InputSource inputSource){
        String name=inputSource.getAlias();
        StringBuilder builder=new StringBuilder();
        if(name.equals("AndroidManifest.xml")){
            builder.append("0 ");
        }else if(name.startsWith("META-INF/")){
            builder.append("1 ");
        }else if(name.equals("resources.arsc")){
            builder.append("2 ");
        }else if(name.startsWith("classes")){
            builder.append("3 ");
        }else if(name.startsWith("res/")){
            builder.append("4 ");
        }else if(name.startsWith("lib/")){
            builder.append("5 ");
        }else if(name.startsWith("assets/")){
            builder.append("6 ");
        }else {
            builder.append("7 ");
        }
        builder.append(name.toLowerCase());
        return builder.toString();
    }
}
