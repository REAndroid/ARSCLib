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

import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class SmaliAnnotationSet extends SmaliSet<SmaliAnnotationItem>{

    public SmaliAnnotationSet(){
        super();
    }

    public AnnotationSetKey getKey() {
        int size = size();
        AnnotationItemKey[] elements = new AnnotationItemKey[size];
        for (int i = 0; i < size; i++) {
            elements[i] = get(i).getKey();
        }
        return AnnotationSetKey.of(elements);
    }
    public void setKey(Key key) {
        clear();
        addAllKeys((AnnotationSetKey) key);
    }

    public void addAllKeys(Iterable<? extends AnnotationItemKey> iterable) {
        if (iterable != null) {
            addAllKeys(iterable.iterator());
        }
    }
    public void addAllKeys(Iterator<? extends AnnotationItemKey> iterator) {
        while (iterator.hasNext()) {
            addKey(iterator.next());
        }
    }
    public void addKey(AnnotationItemKey key) {
        createNew().setKey(key);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAll(iterator());
    }

    public SmaliAnnotationItem createNew() {
        SmaliAnnotationItem item = new SmaliAnnotationItem();
        add(item);
        return item;
    }
    @Override
    SmaliAnnotationItem createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != SmaliDirective.ANNOTATION && directive != SmaliDirective.SUB_ANNOTATION){
            return null;
        }
        if(directive.isEnd(reader)){
            SmaliDirective.parse(reader);
            return null;
        }
        return new SmaliAnnotationItem();
    }
    public static SmaliAnnotationSet read(SmaliReader reader) throws IOException {
        SmaliAnnotationSet smali = new SmaliAnnotationSet();
        smali.parse(reader);
        if(!smali.isEmpty()) {
            return smali;
        }
        return null;
    }
}
