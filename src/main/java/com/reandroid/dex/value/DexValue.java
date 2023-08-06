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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.index.FieldIndex;
import com.reandroid.dex.index.StringIndex;
import com.reandroid.dex.index.TypeIndex;
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
    protected StringIndex getStringIndex(int index){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getStringPool().get(index);
        }
        return null;
    }
    protected TypeIndex getTypeIndex(int index){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getTypeSection().get(index);
        }
        return null;
    }
    protected FieldIndex getFieldId(int index){
        DexFile dexFile = getDexFile();
        if(dexFile != null){
            return dexFile.getFieldSection().get(index);
        }
        return null;
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
    public void onReadBytes(BlockReader reader) throws IOException{
        super.onReadBytes(reader);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
    }

    public static DexValue<?> createFor(DexValueType valueType){
        if(valueType == DexValueType.ENUM){
            return new EnumValue();
        }
        if(valueType == DexValueType.ARRAY){
            return new ArrayValue();
        }
        if(valueType == DexValueType.ANNOTATION){
            return new AnnotationValue();
        }
        if(valueType == DexValueType.NULL){
            return new NullValue();
        }
        if(valueType == DexValueType.METHOD){
            return new MethodValue();
        }
        if(!valueType.isPrimitive()){
            return null;
        }
        return new PrimitiveValue();
    }
}
