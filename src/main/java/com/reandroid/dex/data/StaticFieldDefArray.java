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
import com.reandroid.dex.key.ArrayKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class StaticFieldDefArray extends FieldDefArray {

    public StaticFieldDefArray(IntegerReference itemCount) {
        super(itemCount);
    }

    @Override
    public void setClassId(ClassId classId) {
        if(getClassId() == classId){
            return;
        }
        super.setClassId(classId);
        pullStaticValues();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateStaticValues();
    }

    private void pullStaticValues() {
        ClassId classId = getClassId();
        if (classId == null) {
            return;
        }
        EncodedArray encodedArray = classId.getStaticValuesEncodedArray();
        if (encodedArray == null) {
            return;
        }
        Iterator<DexValueBlock<?>> values = encodedArray.iterator();
        Iterator<FieldDef> iterator = iterator();
        while (iterator.hasNext() && values.hasNext()) {
            iterator.next().setStaticValue(values.next().getKey());
        }
    }
    private void updateStaticValues() {
        ClassId classId = getClassId();
        if (classId == null) {
            return;
        }
        ArrayKey current = getValuesKey();
        ArrayKey encodedKey = classId.getStaticValues();
        if (ObjectsUtil.equals(current, encodedKey)) {
            return;
        }
        classId.setStaticValues(current);
    }
    private ArrayKey getValuesKey() {
        int lastIndex = lastKeyIndex();
        if (lastIndex < 0) {
            return null;
        }
        int size = lastIndex + 1;
        Key[] elements =  new Key[size];
        for (int i = 0; i < size; i++) {
            FieldDef fieldDef = get(i);
            Key key = fieldDef.getStaticValue();
            if (key == null) {
                key = DexValueType.createNull(fieldDef.getKey().getType());
            }
            elements[i] = key;
        }
        return new ArrayKey(elements);
    }
    private int lastKeyIndex() {
        int result = -1;
        int size = size();
        for (int i = 0; i < size; i++) {
            FieldDef fieldDef = get(i);
            Key key = fieldDef.getStaticValue();
            if (key != null) {
                result = i;
            }
        }
        return result;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.setStateWritingFields(true);
        super.append(writer);
        writer.setStateWritingFields(false);
    }
}
