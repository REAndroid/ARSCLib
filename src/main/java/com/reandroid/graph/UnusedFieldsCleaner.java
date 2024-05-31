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
package com.reandroid.graph;

import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.model.*;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;

public class UnusedFieldsCleaner extends BaseDexClassProcessor {

    private int mCount;

    public UnusedFieldsCleaner(DexClassRepository repository) {
        super(repository);
    }
    public void apply() {
        verbose("Searching for unused methods and fields ...");
        Iterator<DexClass> iterator = getClassRepository().getDexClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            if(dexClass.usesNative()) {
                continue;
            }
            cleanUnusedFields(dexClass);
        }
        if(mCount != 0) {
            verbose("Unused fields: " + mCount);
        }
    }
    private void cleanUnusedFields(DexClass dexClass) {
        Iterator<DexField> iterator = dexClass.getDeclaredFields();
        ArrayCollection<DexField> list = null;
        while (iterator.hasNext()) {
            DexField dexField = iterator.next();
            if(isUnusedField(dexField)) {
                if(list == null) {
                    list = new ArrayCollection<>();
                }
                list.add(dexField);
            }
        }
        if(list != null) {
            boolean debugEnabled = isDebugEnabled();
            for(DexField dexField : list) {
                if(debugEnabled) {
                    debug(dexField.getKey().toString());
                }
                dexField.removeSelf();
                mCount++;
            }
        }
    }

    private boolean isUnusedField(DexField dexField) {
        return isUnusedPrivateField(dexField) || isUnusedInstanceField(dexField);
    }
    private boolean isUnusedPrivateField(DexField dexField) {
        if(!dexField.isPrivate()) {
            return false;
        }
        FieldKey fieldKey = dexField.getKey();
        String name = fieldKey.getName();
        Iterator<DexInstruction> iterator = dexField.getDexClass().getDexInstructions();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(fieldKey.equals(instruction.getFieldKey()) || name.equals(instruction.getString())) {
                return false;
            }
        }
        return true;
    }
    private boolean isUnusedInstanceField(DexField dexField) {
        // TODO:
        return false;
    }
}
