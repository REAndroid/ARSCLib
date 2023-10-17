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
import com.reandroid.dex.value.DexValueBlock;

public class StaticFieldDefArray extends FieldDefArray {

    public StaticFieldDefArray(IntegerReference itemCount) {
        super(itemCount);
    }

    @Override
    public boolean remove(FieldDef fieldDef){
        EncodedArray encodedArray = getStaticValues();
        DexValueBlock<?> value = fieldDef.getStaticInitialValue();
        boolean removed = super.remove(fieldDef);
        if(value != null && encodedArray != null){
            encodedArray.remove(value);
        }
        return removed;
    }
    @Override
    void onPreSort(){
        super.onPreSort();
        holdStaticValues();
    }
    @Override
    void onPostSort(){
        super.onPostSort();
        sortStaticValues();
    }
    private void holdStaticValues(){
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray == null){
            return;
        }
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = encodedArray.get(i);
            assert def != null;
            def.setTmpValue(valueBlock);
        }
    }
    private void sortStaticValues(){
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray == null){
            return;
        }
        FieldDef[] childes = getChildes();
        int count = childes.length;
        for(int i = 0; i < count; i++){
            FieldDef def = childes[i];
            DexValueBlock<?> valueBlock = def.getTmpValue();
            if(valueBlock != null && valueBlock.getIndex() != i){
                encodedArray.set(i, valueBlock);
            }
            def.setTmpValue(null);
        }
        encodedArray.trimNull();
    }
    private EncodedArray getStaticValues(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getUniqueStaticValues();
        }
        return null;
    }
}
