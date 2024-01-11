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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SmaliSet<T extends Smali> extends Smali{

    private final ArrayCollection<T> body;

    public SmaliSet(){
        super();
        body = new ArrayCollection<>();
    }

    public Iterator<T> iterator() {
        return body.iterator();
    }
    public Iterator<T> iterator(int start) {
        return body.iterator(start);
    }
    public<T2> Iterator<T2> iterator(Class<T2> instance) {
        return body.iterator(instance);
    }
    public int size(){
        return body.size();
    }
    public boolean isEmpty(){
        return size() == 0;
    }

    public int indexOf(T smali) {
        return body.indexOf(smali);
    }
    public T get(int i) {
        return body.get(i);
    }
    public boolean add(T smali){
        boolean added = body.add(smali);
        if(smali != null){
            smali.setParent(this);
        }
        return added;
    }
    public boolean contains(T smali){
        return body.contains(smali);
    }
    public boolean remove(T smali){
        boolean removed = body.remove(smali);
        if(removed && smali != null){
            smali.setParent(null);
        }
        return removed;
    }
    public T remove(int i){
        T smali = body.remove(i);
        if(smali != null){
            smali.setParent(null);
        }
        return smali;
    }
    public void clear(){
        for (T smali : body) {
            smali.setParent(null);
        }
        body.clear();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAllWithDoubleNewLine(iterator());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {

    }
}
