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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class ZipArchive {
    private final Map<String, InputSource> mEntriesMap;
    public ZipArchive(Map<String, InputSource> entriesMap){
        this.mEntriesMap=entriesMap;
    }
    public ZipArchive(){
        this(new LinkedHashMap<>());
    }

    public int size(){
        return mEntriesMap.size();
    }
    public void extract(File outDir) throws IOException {
        for(InputSource inputSource:listInputSources()){
            extract(outDir, inputSource);
        }
    }
    private void extract(File outDir, InputSource inputSource) throws IOException {
        File file=toOutFile(outDir, inputSource.getAlias());
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        inputSource.write(outputStream);
        outputStream.close();
        inputSource.disposeInputSource();
    }
    private File toOutFile(File outDir, String path){
        path=path.replace('/', File.separatorChar);
        return new File(outDir, path);
    }
    public void removeDir(String dirName){
        if(!dirName.endsWith("/")){
            dirName=dirName+"/";
        }
        for(InputSource inputSource:listInputSources()){
            if(inputSource.getName().startsWith(dirName)){
                remove(inputSource.getName());
            }
        }
    }
    public void removeAll(Pattern patternAlias){
        for(InputSource inputSource:listInputSources()){
            Matcher matcher = patternAlias.matcher(inputSource.getAlias());
            if(matcher.matches()){
                mEntriesMap.remove(inputSource.getName());
            }
        }
    }
    public void clear(){
        mEntriesMap.clear();
    }
    public int entriesCount(){
        return mEntriesMap.size();
    }
    public InputSource remove(String name){
        InputSource inputSource=mEntriesMap.remove(name);
        if(inputSource==null){
            return null;
        }
        return inputSource;
    }
    public void addArchive(File archiveFile) throws IOException {
        ZipFile zipFile=new ZipFile(archiveFile);
        add(zipFile);
    }
    public void addDirectory(File dir){
        addAll(InputSourceUtil.listDirectory(dir));
    }
    public void add(ZipFile zipFile){
        List<InputSource> sourceList = InputSourceUtil.listZipFileSources(zipFile);
        this.addAll(sourceList);
    }
    public void set(Collection<? extends InputSource> inputSourceList){
        clear();
        addAll(inputSourceList);
    }
    public void addAll(Collection<? extends InputSource> inputSourceList){
        for(InputSource inputSource:inputSourceList){
            add(inputSource);
        }
    }
    public void add(InputSource inputSource){
        if(inputSource==null){
            return;
        }
        String name=inputSource.getName();
        Map<String, InputSource> map=mEntriesMap;
        map.remove(name);
        map.put(name, inputSource);
    }
    public List<InputSource> listInputSources(){
        return new ArrayList<>(mEntriesMap.values());
    }
    public InputSource getInputSource(String name){
        return mEntriesMap.get(name);
    }
}
