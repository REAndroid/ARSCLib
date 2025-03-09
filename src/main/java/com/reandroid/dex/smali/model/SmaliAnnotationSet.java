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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class SmaliAnnotationSet extends SmaliSet<SmaliAnnotationItem>{ 

    public SmaliAnnotationSet() {
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
        AnnotationSetKey annotationSetKey = (AnnotationSetKey)  key;
        for (AnnotationItemKey itemKey : annotationSetKey) {
            createNew().setKey(itemKey);
        }
    }
    public void addAnnotations(Iterator<? extends AnnotationItemKey> iterator) {
        while (iterator.hasNext()) {
            addAnnotation(iterator.next());
        }
    }
    public void addAnnotation(AnnotationItemKey key) {
        getOrCreate(key.getType()).setKey(key);
    }
    public SmaliAnnotationItem getOrCreate(TypeKey typeKey) {
        SmaliAnnotationItem item = get(typeKey);
        if (item == null) {
            item = createNew();
            item.setType(typeKey);
        }
        return item;
    }
    public boolean hasAnnotation(TypeKey typeKey) {
        return get(typeKey) != null;
    }
    public SmaliAnnotationItem get(TypeKey typeKey) {
        int size = size();
        for (int i = 0; i < size; i++) {
            SmaliAnnotationItem item = get(i);
            if (ObjectsUtil.equals(item.getType(), typeKey)) {
                return item;
            }
        }
        return null;
    }
    public SmaliAnnotationItem createNew() {
        SmaliAnnotationItem item = new SmaliAnnotationItem();
        add(item);
        return item;
    }
    public AnnotatedProgram asAnnotatedProgram() {
        final SmaliAnnotationSet smaliAnnotationSet = this;
        return new AnnotatedProgram() {
            @Override
            public AnnotationSetKey getAnnotation() {
                return smaliAnnotationSet.getKey();
            }
            @Override
            public void setAnnotation(AnnotationSetKey annotationSet) {
                smaliAnnotationSet.setKey(annotationSet);
            }
            @Override
            public void clearAnnotations() {
                smaliAnnotationSet.clear();
            }
            @Override
            public boolean hasAnnotation(TypeKey typeKey) {
                return smaliAnnotationSet.hasAnnotation(typeKey);
            }
        };
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAll(iterator());
    }
    @Override
    SmaliAnnotationItem createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive != SmaliDirective.ANNOTATION && directive != SmaliDirective.SUB_ANNOTATION) {
            return null;
        }
        if (directive.isEnd(reader)) {
            SmaliDirective.parse(reader);
            return null;
        }
        return new SmaliAnnotationItem();
    }
    public static SmaliAnnotationSet read(SmaliReader reader) throws IOException {
        SmaliAnnotationSet smali = new SmaliAnnotationSet();
        smali.parse(reader);
        if (!smali.isEmpty()) {
            return smali;
        }
        return null;
    }
}
