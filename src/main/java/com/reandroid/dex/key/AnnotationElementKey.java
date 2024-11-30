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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationElementKey implements Key {

    private final String name;
    private final Key value;

    public AnnotationElementKey(String name, Key value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }
    public StringKey getNameKey() {
        return StringKey.create(getName());
    }
    public Key getValue() {
        return value;
    }
    public AnnotationElementKey changeName(StringKey name) {
        return changeName((name == null ? null : name.getString()));
    }
    public AnnotationElementKey changeName(String name) {
        if (ObjectsUtil.equals(getName(), name)) {
            return this;
        }
        return new AnnotationElementKey(name, getValue());
    }
    public AnnotationElementKey changeValue(Key value) {
        if (ObjectsUtil.equals(getValue(), value)) {
            return this;
        }
        return new AnnotationElementKey(getName(), value);
    }

    @Override
    public AnnotationElementKey replaceKey(Key search, Key replace) {
        if (this.equals(search)) {
            return (AnnotationElementKey) replace;
        }
        AnnotationElementKey result = this;
        if (ObjectsUtil.equals(this.getNameKey(), search)) {
            result = changeName((StringKey) replace);
        }
        Key value = getValue();
        if (value != null) {
            value = value.replaceKey(search, replace);
            if (value != this.getValue()) {
                result = result.changeValue(value);
            }
        }
        return result;
    }
    @Override
    public Iterator<? extends Key> mentionedKeys() {
        Key value = getValue();
        Iterator<? extends Key> valueMentions;
        if (value == null) {
            valueMentions = EmptyIterator.of();
        } else {
            valueMentions = value.mentionedKeys();
        }
        return CombiningIterator.singleThree(
                this,
                SingleIterator.of(getNameKey()),
                SingleIterator.of(value),
                valueMentions);
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof AnnotationElementKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        AnnotationElementKey elementKey = (AnnotationElementKey) obj;
        int i = CompareUtil.compare(getName(), elementKey.getName());
        if (i == 0) {
            i = CompareUtil.compare(getValue(), elementKey.getValue());
        }
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AnnotationElementKey)) {
            return false;
        }
        AnnotationElementKey other = (AnnotationElementKey) obj;
        return ObjectsUtil.equals(getName(), other.getName()) &&
                ObjectsUtil.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getName(), getValue());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(" = ");
        getValue().append(writer);
    }

    @Override
    public String toString() {
        return getName() + " = " + getValue();
    }
}
