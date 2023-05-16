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
package com.reandroid.identifiers;

import java.util.*;

class IdentifierMap<CHILD extends Identifier> extends Identifier
        implements Comparator<CHILD> {
    private final Object mLock = new Object();
    private final Map<Integer, CHILD> idMap;
    private final Map<String, CHILD> nameMap;

    public IdentifierMap(int id, String name){
        super(id, name);
        this.idMap = new HashMap<>();
        this.nameMap = new HashMap<>();
    }
    public List<CHILD> listDuplicates(){
        List<CHILD> results = new ArrayList<>(getItems());
        Set<String> uniques = new HashSet<>();
        for(CHILD item : getItems()){
            String name = item.getName();
            if(uniques.contains(name)){
                results.add(item);
            }else {
                uniques.add(name);
            }
        }
        results.sort(this);
        return results;
    }
    public boolean hasDuplicates(){
        Set<String> uniques = new HashSet<>();
        for(CHILD item : getItems()){
            String name = item.getName();
            if(uniques.contains(name)){
                return true;
            }else {
                uniques.add(name);
            }
        }
        return false;
    }
    public List<String> listNames(){
        List<String> results = new ArrayList<>(size());
        for(CHILD item : list()){
            results.add(item.getName());
        }
        return results;
    }
    public List<CHILD> list(){
        List<CHILD> childList = new ArrayList<>(getItems());
        childList.sort(this);
        return childList;
    }
    public Collection<CHILD> getItems(){
        synchronized (mLock){
            return this.idMap.values();
        }
    }
    public void clear(){
        synchronized (mLock){
            this.idMap.clear();
            this.nameMap.clear();
        }
    }
    public CHILD getByTag(Object tag){
        for(CHILD item : getItems()){
            if(Objects.equals(tag, item.getTag())){
                return item;
            }
        }
        return null;
    }
    public int size(){
        synchronized (mLock){
            return this.idMap.size();
        }
    }
    public CHILD get(String childName){
        synchronized (mLock){
            return this.nameMap.get(childName);
        }
    }
    public CHILD get(int childId){
        synchronized (mLock){
            return this.idMap.get(childId);
        }
    }
    public void remove(CHILD entry){
        synchronized (mLock){
            if(entry == null){
                return;
            }
            this.idMap.remove(entry.getId());
            this.nameMap.remove(entry.getName());
        }
    }
    public CHILD add(CHILD child){
        synchronized (mLock){
            if(child == null){
                return null;
            }
            child.setParent(this);
            Integer entryId = child.getId();
            CHILD exist = this.idMap.get(entryId);
            if(exist != null){
                if(exist.getName() == null){
                    exist.setName(child.getName());
                    addNameMap(exist);
                }
                return exist;
            }
            this.idMap.put(entryId, child);
            addNameMap(child);
            return child;
        }
    }
    private void addNameMap(CHILD child){
        String childName = child.getName();
        if(childName == null){
            return;
        }
        CHILD exist = this.nameMap.get(childName);
        if(exist != null){
            return;
        }
        this.nameMap.put(childName, child);
    }
    @Override
    public int compare(CHILD child1, CHILD child2) {
        return child1.compareTo(child2);
    }
    @Override
    public String toString(){
        return super.toString() + " entries = " + size();
    }
}
