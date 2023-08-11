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
package com.reandroid.dex.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.AnnotationItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DexValue<T extends Block> extends FixedBlockContainer implements SmaliFormat {
    private final ByteItem valueType;
    private final SingleBlockContainer<T> valueContainer;
    DexValue(T value){
        super(2);
        valueType = new ByteItem();
        valueContainer = new SingleBlockContainer<>();
        valueContainer.setItem(value);
        addChild(0, valueType);
        addChild(1, valueContainer);
    }
    DexValue(){
        this(null);
    }

    public DexFile getDexFile() {
        return getParentInstance(DexFile.class);
    }
    public SectionList getSectionList() {
        return getParentInstance(SectionList.class);
    }
    public<T1 extends Block> Section<T1> getSection(SectionType<T1> sectionType) {
        SectionList sectionList = getSectionList();
        if(sectionList != null){
            return (Section<T1>) sectionList.get(sectionType);
        }
        return null;
    }
    public<T1 extends Block> T1 getItem(SectionType<T1> sectionType, int i) {
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.get(i);
        }
        return null;
    }
    protected StringData getStringIndex(int index){
        SectionList dexFile = getSectionList();
        if(dexFile != null){
            Section<?> section= dexFile.get(SectionType.STRING_DATA);
            return (StringData) section.get(index);
        }
        return null;
    }
    protected TypeId getTypeIndex(int index){
        return getItem(SectionType.TYPE_ID, index);
    }
    protected FieldId getFieldId(int index){
        return getItem(SectionType.FIELD_ID, index);
    }
    public T getValue(){
        return valueContainer.getItem();
    }
    public void setValue(T value){
        valueContainer.setItem(value);
    }
    public DexValueType getValueType(){
        return DexValueType.fromFlag(valueType.unsignedInt());
    }
    int getValueSize(){
        return DexValueType.decodeSize(valueType.unsignedInt());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        T value = getValue();
        if(value instanceof SmaliFormat){
            ((SmaliFormat)value).append(writer);
        }
    }
    @Override
    public String toString() {
        return String.valueOf(getValue());
    }

    public static DexValue<?> createFor(DexValueType valueType){
        if(valueType == DexValueType.ENUM){
            return new EnumValue();
        }
        if(valueType == DexValueType.ARRAY){
            return new ArrayValue();
        }
        if(valueType == DexValueType.ANNOTATION){
            return new DexValue<>(new AnnotationItem(true));
        }
        if(valueType == DexValueType.NULL){
            return new NullValue();
        }
        if(valueType == DexValueType.METHOD){
            return new MethodValue();
        }
        if(valueType == DexValueType.BOOLEAN){
            return new BooleanValue();
        }
        if(!valueType.isNumber()){
            return new PrimitiveValue();
        }
        return new PrimitiveValue();
    }
}
