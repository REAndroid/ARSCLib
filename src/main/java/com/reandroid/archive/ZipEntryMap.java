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

import java.util.*;
import java.util.regex.Pattern;

public class ZipEntryMap implements Comparator<InputSource>{
    private final Object mLock = new Object();
    private final LinkedHashMap<String, InputSource> mSourceMap;
    private InputSource[] sourcesArray;
    public ZipEntryMap(LinkedHashMap<String, InputSource> entriesMap){
        this.mSourceMap = entriesMap;
    }
    public ZipEntryMap(){
        this(new LinkedHashMap<>());
    }

    public PathTree<InputSource> getPathTree(){
        return Archive.buildPathTree(toArray());
    }
    public LinkedHashMap<String, InputSource> toAliasMap(){
        InputSource[] sources = toArray();
        int length = sources.length;
        LinkedHashMap<String, InputSource> map = new LinkedHashMap<>(length);
        for(int i = 0; i < length; i++){
            InputSource inputSource = sources[i];
            map.put(inputSource.getAlias(), inputSource);
        }
        return map;
    }
    public InputSource[] toArray(boolean sort){
        InputSource[] sources = toArray();
        if(sort){
            Arrays.sort(sources, this);
        }
        return sources;
    }
    public InputSource[] toArray(){
        synchronized (mLock){
            if(sourcesArray != null){
                return sourcesArray;
            }
            LinkedHashMap<String, InputSource> map = this.mSourceMap;
            InputSource[] sources = new InputSource[map.size()];
            int index = 0;
            for(InputSource inputSource : map.values()){
                sources[index] = inputSource;
                index++;
            }
            this.sourcesArray = sources;
            return sources;
        }
    }
    private void onChanged(boolean changed){
        if(changed){
            this.sourcesArray = null;
        }
    }
    public int size(){
        synchronized (mLock){
            return mSourceMap.size();
        }
    }
    public void removeDir(String dirName){
        if(!dirName.endsWith("/")){
            dirName = dirName + "/";
        }
        synchronized (mLock){
            boolean changed = false;
            for(InputSource inputSource:toArray()){
                if(inputSource.getName().startsWith(dirName)){
                    inputSource = mSourceMap.remove(inputSource.getName());
                    if(!changed){
                        changed = inputSource != null;
                    }
                }
            }
            onChanged(changed);
        }
    }
    public void clear(){
        synchronized (mLock){
            mSourceMap.clear();
            onChanged(true);
        }
    }
    public void removeAll(Pattern pattern){
        synchronized (mLock){
            boolean removed = false;
            LinkedHashMap<String, InputSource> map = this.mSourceMap;
            for(InputSource inputSource : toArray()){
                String name = inputSource.getAlias();
                if(pattern.matcher(name).matches()){
                    if(map.remove(name) != null){
                        removed = true;
                    }
                }
            }
            onChanged(removed);
        }
    }
    public InputSource remove(String name){
        synchronized (mLock){
            InputSource inputSource = mSourceMap.remove(name);
            onChanged(inputSource != null);
            return inputSource;
        }
    }
    public void addAll(InputSource[] sources){
        if(sources == null){
            return;
        }
        synchronized (mLock){
            int length = sources.length;
            LinkedHashMap<String, InputSource> map = this.mSourceMap;
            boolean added = false;
            for(int i = 0; i < length; i++){
                InputSource inputSource = sources[i];
                if(inputSource == null){
                    continue;
                }
                String name = inputSource.getName();
                map.remove(name);
                map.put(name, inputSource);
                if(!added){
                    onChanged(true);
                }
                added = true;
            }
            onChanged(added);
        }
    }
    public void add(InputSource inputSource){
        if(inputSource == null){
            return;
        }
        synchronized (mLock){
            String name = inputSource.getName();
            LinkedHashMap<String, InputSource> map = this.mSourceMap;
            map.remove(name);
            map.put(name, inputSource);
            onChanged(true);
        }
    }
    public List<InputSource> listInputSources(){
        return new ArrayList<>(mSourceMap.values());
    }
    public InputSource getInputSource(String name){
        synchronized (mLock){
            return mSourceMap.get(name);
        }
    }
    public void refresh(){
        InputSource[] inputSourceList = toArray(true);
        set(inputSourceList);
    }
    public void autoSortApkFiles(){
        InputSource[] sources = toArray();
        int length = sources.length;

        for(int i = 0; i < length; i++){
            InputSource inputSource = sources[i];
            inputSource.setSort(-1);
        }

        Arrays.sort(sources, InputSource.ALIAS_COMPARATOR);

        for(int i = 0; i < length; i++){
            InputSource inputSource = sources[i];
            inputSource.setSort(i);
        }
        set(sources);
    }
    private void set(InputSource[] sources){
        clear();
        addAll(sources);
    }
    @Override
    public int compare(InputSource inputSource1, InputSource inputSource2) {
        if(inputSource1 == inputSource2){
            return 0;
        }
        if(inputSource1 == null){
            return 1;
        }
        if(inputSource2 == null){
            return -1;
        }
        return Integer.compare(inputSource1.getSort(), inputSource2.getSort());
    }
}
