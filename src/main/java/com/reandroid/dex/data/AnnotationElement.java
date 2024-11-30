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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.StringUle128Reference;
import com.reandroid.dex.smali.model.SmaliAnnotationElement;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.NullValue;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class AnnotationElement extends DataItem implements KeyReference,
        Comparable<AnnotationElement>, SmaliFormat {

    private final StringUle128Reference elementName;

    public AnnotationElement() {
        super(2);
        this.elementName = new StringUle128Reference(StringId.USAGE_METHOD_NAME);
        addChildBlock(0, elementName);
    }

    @Override
    public AnnotationElementKey getKey(){
        return new AnnotationElementKey(getName(), getValue());
    }
    @Override
    public void setKey(Key key) {
        AnnotationElementKey elementKey = (AnnotationElementKey) key;
        setName(elementKey.getName());
        setValue(elementKey.getValue());
    }
    public Key getValue() {
        DexValueBlock<?> valueBlock = getValueBlock();
        if (valueBlock != null) {
            return valueBlock.getKey();
        }
        return null;
    }
    public void setValue(Key value) {
        DexValueBlock<?> valueBlock = DexValueType.forKey(value).newInstance();
        setValue(valueBlock);
        valueBlock.setKey(value);
    }
    public DexValueBlock<?> getValueBlock(){
        return (DexValueBlock<?>) getChildBlockAt(1);
    }

    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getValue(DexValueType<T1> valueType){
        DexValueBlock<?> value = getValueBlock();
        if(value != null && value.is(valueType)){
            return (T1) value;
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DexValueBlock<?>> T1 getOrCreateValue(DexValueType<T1> valueType){
        DexValueBlock<?> value = getValueBlock();
        if(value == null || value == NullValue.PLACE_HOLDER || value.getValueType() != valueType){
            value = valueType.newInstance();
            setValue(value);
        }
        return (T1) value;
    }
    public void setValue(DexValueBlock<?> dexValue){
        addChildBlock(1, dexValue);
    }
    public boolean is(DexValueType<?> valueType){
        return getValueType() == valueType;
    }
    public boolean is(MethodKey methodKey) {
        return methodKey != null &&
                methodKey.equalsIgnoreReturnType(getMethodKey());
    }
    public DexValueType<?> getValueType(){
        DexValueBlock<?> value = getValueBlock();
        if(value != null){
            return value.getValueType();
        }
        return null;
    }
    public String getName(){
        return elementName.getString();
    }
    public void setName(String name){
        elementName.setString(name);
    }
    public StringId getNameId(){
        return elementName.getItem();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        this.elementName.onReadBytes(reader);
        DexValueBlock<?> value = DexValueType.create(reader);
        setValue(value);
        value.onReadBytes(reader);
    }

    public void replaceKeys(Key search, Key replace){
        getValueBlock().replaceKeys(search, replace);
    }
    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.singleOne(getNameId(), getValueBlock().usedIds());
    }
    public void merge(AnnotationElement element){
        if(element == this){
            return;
        }
        setName(element.getName());
        DexValueBlock<?> coming = element.getValueBlock();
        DexValueBlock<?> value = getOrCreateValue(coming.getValueType());
        value.merge(coming);
    }
    public void fromSmali(SmaliAnnotationElement element){
        setName(element.getName());
        SmaliValue smaliValue = element.getValue();
        DexValueBlock<?> value = getOrCreateValue(smaliValue.getValueType());
        value.fromSmali(smaliValue);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(" = ");
        getValueBlock().append(writer);
    }


    @Override
    public int compareTo(AnnotationElement other) {
        if(other == null){
            return -1;
        }
        if(other == this){
            return 0;
        }
        return SectionTool.compareIdx(getNameId(), other.getNameId());
    }
    public TypeKey getDataTypeKey(){
        DexValueBlock<?> valueBlock = getValueBlock();
        if(valueBlock != null){
            return valueBlock.getDataTypeKey();
        }
        return null;
    }
    public TypeKey getParentType(){
        AnnotationItem parent = getParentInstance(AnnotationItem.class);
        if(parent != null){
            return parent.getType();
        }
        return null;
    }
    public MethodKey getMethodKey() {
        return MethodKey.create(getParentType(), getName(),
                ProtoKey.emptyParameters(getDataTypeKey()));
    }

    @Override
    public Iterator<Key> usedKeys() {
        return CombiningIterator.singleOne(getMethodKey(), super.usedKeys());
    }
    @Override
    public int hashCode() {
        int hash = 1;
        Object obj = getName();
        hash = hash * 31;
        if(obj != null){
            hash += obj.hashCode();
        }
        obj = getValueBlock();
        hash = hash * 31;
        if(obj != null){
            hash = hash + obj.hashCode();
        }
        return hash;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnnotationElement element = (AnnotationElement) obj;
        if(!Objects.equals(getName(), element.getName())){
            return false;
        }
        return Objects.equals(getValueBlock(), element.getValueBlock());
    }

    @Override
    public String toString() {
        return getName() + " = " + getValueBlock();
    }

    public static final Creator<AnnotationElement> CREATOR = new Creator<AnnotationElement>() {
        @Override
        public AnnotationElement[] newArrayInstance(int length) {
            if(length == 0){
                return EMPTY;
            }
            return new AnnotationElement[length];
        }
        @Override
        public AnnotationElement newInstance() {
            return new AnnotationElement();
        }
    };
    private static final AnnotationElement[] EMPTY = new AnnotationElement[0];
}
