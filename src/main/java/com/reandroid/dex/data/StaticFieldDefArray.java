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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.value.*;


public class StaticFieldDefArray extends FieldDefArray {

    private boolean mValuesLinked;

    public StaticFieldDefArray(IntegerReference itemCount) {
        super(itemCount);
    }

    @Override
    public void onPreRemove(FieldDef fieldDef) {
        EncodedArray encodedArray = getUniqueStaticValues();
        DexValueBlock<?> value = fieldDef.getStaticInitialValue();
        if(value == null || value.getParentInstance(EncodedArray.class) != encodedArray) {
            holdStaticValues(encodedArray);
            value = fieldDef.getStaticInitialValue();
        }
        if(value != null && encodedArray != null){
            encodedArray.remove(value);
        }
        super.onPreRemove(fieldDef);
    }

    @Override
    void onPreSort(){
        super.onPreSort();
        holdStaticValues(getUniqueStaticValues());
    }
    @Override
    void onPostSort(){
        super.onPostSort();
        sortStaticValues();
    }
    private void sortStaticValues(){
        EncodedArray encodedArray = getUniqueStaticValues();
        if(encodedArray == null){
            return;
        }
        encodedArray.removeAll();
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = def.getStaticInitialValue();
            if(valueBlock != null){
                ensureArraySize(encodedArray, i + 1);
                encodedArray.set(i, valueBlock);
            }
        }
        encodedArray.trimNull();
    }
    private void ensureArraySize(EncodedArray encodedArray, int size){
        int arraySize = encodedArray.size();
        if(size <= arraySize){
            return;
        }
        for(int i = arraySize; i < size; i++){
            FieldDef def = get(i);
            TypeKey typeKey = def.getKey().getType();
            encodedArray.add(createFor(typeKey));
        }
    }
    private EncodedArray getUniqueStaticValues(){
        ClassId classId = getClassId();
        if(classId != null){
            EncodedArray previous = classId.getStaticValues();
            EncodedArray current = classId.getUniqueStaticValues();
            if(previous != null && previous != current) {
                updateStaticValues(current);
            }
            return current;
        }
        return null;
    }
    private EncodedArray getStaticValues(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getStaticValues();
        }
        return null;
    }

    @Override
    public void setClassId(ClassId classId) {
        if(getClassId() != classId){
            mValuesLinked = false;
        }
        super.setClassId(classId);
        if(!mValuesLinked){
            holdStaticValues(getStaticValues());
        }
    }

    private void holdStaticValues(){
        holdStaticValues(getUniqueStaticValues());
    }
    private void holdStaticValues(EncodedArray encodedArray){
        if(encodedArray == null){
            return;
        }
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = encodedArray.get(i);
            def.holdStaticInitialValue(valueBlock);
        }
        if(count != 0){
            mValuesLinked = true;
        }
    }
    private void updateStaticValues(EncodedArray encodedArray){
        if(encodedArray == null){
            return;
        }
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> value = def.getStaticInitialValue();
            if(value != null) {
                def.holdStaticInitialValue(encodedArray.get(value.getIndex()));
            }
        }
        if(count != 0){
            mValuesLinked = true;
        }
    }

    @Override
    public void merge(DefArray<?> defArray) {
        super.merge(defArray);
        holdStaticValues();
    }

    private static DexValueBlock<?> createFor(TypeKey typeKey){
        DexValueBlock<?> valueBlock;
        if(typeKey.isTypeArray() || !typeKey.isPrimitive()){
            valueBlock = NullValue.PLACE_HOLDER;
        }else if(TypeKey.TYPE_I.equals(typeKey)){
            valueBlock = new IntValue();
        } else if(TypeKey.TYPE_J.equals(typeKey)){
            valueBlock = new LongValue();
        } else if(TypeKey.TYPE_D.equals(typeKey)){
            valueBlock = new DoubleValue();
        } else if(TypeKey.TYPE_F.equals(typeKey)){
            valueBlock = new FloatValue();
        } else if(TypeKey.TYPE_S.equals(typeKey)){
            valueBlock = new ShortValue();
        } else if(TypeKey.TYPE_B.equals(typeKey)){
            valueBlock = new ByteValue();
        } else if(TypeKey.TYPE_C.equals(typeKey)){
            valueBlock = new CharValue();
        } else if(TypeKey.TYPE_Z.equals(typeKey)){
            valueBlock = new BooleanValue();
        }else {
            throw new IllegalArgumentException("Undefined: " + typeKey);
        }
        valueBlock.setTemporary(true);
        return valueBlock;
    }
}
