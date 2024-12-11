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
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.ins.Ins;
import com.reandroid.dex.ins.SizeXIns;
import com.reandroid.dex.key.ArrayValueKey;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.Iterator;

public class StaticFieldDefArray extends FieldDefArray {

    private Object mLockedBy;

    public StaticFieldDefArray(IntegerReference itemCount) {
        super(itemCount);
    }

    @Override
    public FieldDef createNext() {
        FieldDef fieldDef = super.createNext();
        fieldDef.addAccessFlag(AccessFlag.STATIC);
        return fieldDef;
    }

    boolean isInitializedInStaticConstructor(FieldDef fieldDef) {
        FieldKey fieldKey = fieldDef.getKey();
        if (fieldKey == null) {
            return false;
        }
        ClassId classId = getClassId();
        if (classId == null) {
            return false;
        }
        MethodKey methodKey = MethodKey.STATIC_CONSTRUCTOR.changeDeclaring(
                fieldKey.getDeclaring());
        MethodDef methodDef = (MethodDef) classId.getDef(methodKey);
        if (methodDef == null) {
            return false;
        }
        InstructionList instructionList = methodDef.getInstructionList();
        if (instructionList == null) {
            return false;
        }
        Iterator<Ins> iterator = instructionList.iterator();
        while (iterator.hasNext()) {
            Ins ins = iterator.next();
            if (ins instanceof SizeXIns) {
                SizeXIns sizeXIns = (SizeXIns) ins;
                if (sizeXIns.getOpcode().isFieldPut() &&
                        fieldKey.equals(sizeXIns.getKey())) {
                    return true;
                }
            }
        }
        return false;
    }
    Key getStaticValue(FieldDef def) {
        if (mLockedBy != null) {
            return def.cachedStaticValue();
        }
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            return encodedArray.getValueKey(def.getIndex());
        }
        return null;
    }
    void setStaticValue(FieldDef def, Key key) {
        if (mLockedBy != null ) {
            def.cachedStaticValue(key);
            return;
        }
        Object lock = cacheStaticValues();
        def.cachedStaticValue(key);
        releaseStaticValues(lock);
    }

    private Object cacheStaticValues() {
        if (this.mLockedBy != null) {
            return null;
        }
        Object lock = new Object();
        this.mLockedBy = lock;
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            int size = size();
            for (int i = 0; i < size; i++) {
                get(i).cachedStaticValue(encodedArray.getValueKey(i));
            }
        }
        return lock;
    }
    private void releaseStaticValues(Object lock) {
        if (this.mLockedBy == null || this.mLockedBy != lock) {
            return;
        }
        ClassId classId = getClassId();
        if (classId == null) {
            return;
        }
        ArrayValueKey cached = buildCachedKey();
        ArrayValueKey encoded = classId.getStaticValues();
        if (!ObjectsUtil.equals(cached, encoded)) {
            classId.setStaticValues(cached);
        }
        mLockedBy = null;
    }
    private ArrayValueKey buildCachedKey() {
        int lastIndex = lastCachedKeyIndex();
        if (lastIndex < 0) {
            return null;
        }
        int length = lastIndex + 1;
        Key[] elements =  new Key[length];
        for (int i = 0; i < length; i++) {
            FieldDef fieldDef = get(i);
            Key key = fieldDef.cachedStaticValue();
            if (key == null) {
                key = DexValueType.createDefaultValue(fieldDef.getKey().getType());
            }
            elements[i] = key;
            fieldDef.cachedStaticValue(null);
        }
        return ArrayValueKey.of(elements);
    }
    private int lastCachedKeyIndex() {
        int result = -1;
        int size = size();
        for (int i = 0; i < size; i++) {
            Key key = get(i).cachedStaticValue();
            if (key != null) {
                result = i;
            }
        }
        return result;
    }

    private EncodedArray getEncodedArray() {
        ClassId classId = getClassId();
        if (classId != null) {
            return classId.getStaticValuesEncodedArray();
        }
        return null;
    }

    @Override
    protected Object onRemoveRequestStarted() {
        return cacheStaticValues();
    }

    @Override
    protected void onRemoveRequestCompleted(Object lock) {
        releaseStaticValues(lock);
        super.onRemoveRequestCompleted(lock);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        Object forceRelease = this.mLockedBy;
        releaseStaticValues(forceRelease);
    }

    @Override
    Object onPreSort() {
        super.onPreSort();
        return cacheStaticValues();
    }
    @Override
    void onPostSort(Object lock) {
        super.onPostSort(lock);
        releaseStaticValues(lock);
    }

    @Override
    public void merge(DefArray<FieldDef> defArray) {
        Object lock = cacheStaticValues();
        super.merge(defArray);
        releaseStaticValues(lock);
    }

    @Override
    void onMerged(FieldDef def, FieldDef source) {
        Key value = source.getStaticValue();
        if (value != null) {
            setStaticValue(def, value);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.setStateWritingFields(true);
        super.append(writer);
        writer.setStateWritingFields(false);
    }

    @Override
    public void fromSmali(Iterator<? extends Smali> iterator) {
        Object lock = cacheStaticValues();
        super.fromSmali(iterator);
        releaseStaticValues(lock);
    }
}
