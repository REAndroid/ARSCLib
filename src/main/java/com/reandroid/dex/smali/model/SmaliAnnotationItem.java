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

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.*;
import com.reandroid.dex.smali.*;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class SmaliAnnotationItem extends SmaliSet<SmaliAnnotationElement>
        implements KeyReference, SmaliRegion {

    private SmaliDirective smaliDirective;
    private AnnotationVisibility visibility;
    private TypeKey type;

    public SmaliAnnotationItem() {
        super();
        this.smaliDirective = SmaliDirective.ANNOTATION;
        this.visibility = AnnotationVisibility.BUILD;
    }

    @Override
    public AnnotationItemKey getKey() {
        TypeKey typeKey = getType();
        if (typeKey != null) {
            int length = this.size();
            AnnotationElementKey[] elements = new AnnotationElementKey[length];
            for (int i = 0; i < length; i++) {
                elements[i] = get(i).getKey();
            }
            return AnnotationItemKey.create(getVisibility(), typeKey, elements);
        }
        return null;
    }
    @Override
    public void setKey(Key key) {
        clear();
        AnnotationItemKey itemKey = (AnnotationItemKey) key;
        setVisibility(itemKey.getVisibility());
        setType(itemKey.getType());
        for (AnnotationElementKey elementKey : itemKey) {
            newElement().setKey(elementKey);
        }
    }
    public AnnotationVisibility getVisibility() {
        return visibility;
    }
    public void setVisibility(AnnotationVisibility visibility) {
        if (visibility == null) {
            if (getParentAnnotationSet() != null) {
                throw new NullPointerException("Null AnnotationVisibility");
            }
            this.smaliDirective = SmaliDirective.SUB_ANNOTATION;
        } else {
            this.smaliDirective = SmaliDirective.ANNOTATION;
        }
        this.visibility = visibility;
    }

    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    public boolean hasElement(String name) {
        return get(name) != null;
    }
    public SmaliAnnotationElement get(String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            SmaliAnnotationElement element = get(i);
            if (ObjectsUtil.equals(element.getName(), name)) {
                return element;
            }
        }
        return null;
    }

    public SmaliAnnotationElement newElement() {
        SmaliAnnotationElement element = new SmaliAnnotationElement();
        add(element);
        return element;
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return smaliDirective;
    }
    public SmaliAnnotationSet getParentAnnotationSet() {
        Smali parent = getParent();
        if (parent instanceof SmaliAnnotationSet) {
            return (SmaliAnnotationSet) parent;
        }
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        AnnotationItemKey key = getKey();
        if (key != null) {
            key.append(writer);
        } else {
            getSmaliDirective().append(writer);
            writer.appendOptional(getVisibility());
            writer.appendOptional(getType());
            writer.appendAllWithIndent(iterator());
            getSmaliDirective().appendEnd(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        int position = reader.position();

        AnnotationItemKey itemKey = AnnotationItemKey.read(reader);

        SmaliDirective directive = getSmaliDirective();
        SmaliAnnotationSet parent = getParentAnnotationSet();
        if (parent != null) {
            directive = SmaliDirective.ANNOTATION;
        }
        if (directive != itemKey.getSmaliDirective()) {
            reader.position(position);
            throw new SmaliParseException("Expecting: " + directive, reader);
        }
        if (parent != null) {
            TypeKey typeKey = itemKey.getType();
            SmaliAnnotationItem duplicate = parent.get(typeKey);
            if (duplicate != null && duplicate != this) {
                reader.position(position);
                throw new SmaliParseException("Duplicate annotation: " + typeKey, reader);
            }
        }
        setKey(itemKey);
    }
    @Override
    SmaliAnnotationElement createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        if (reader.finished()) {
            return null;
        }
        if (getSmaliDirective().isEnd(reader)) {
            return null;
        }
        return new SmaliAnnotationElement();
    }
}
