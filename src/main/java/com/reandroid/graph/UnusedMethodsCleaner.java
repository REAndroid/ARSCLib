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

import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;

public class UnusedMethodsCleaner extends BaseDexClassProcessor{
    private int mCount;

    public UnusedMethodsCleaner(DexClassRepository classRepository) {
        super(classRepository);
    }

    public void apply() {
        verbose("Searching for unused methods ...");
        Iterator<DexClass> iterator = getClassRepository().getDexClasses();
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            if(dexClass.usesNative()) {
                continue;
            }
            cleanUnusedMethods(dexClass);
        }
        if(mCount != 0) {
            verbose("Unused methods: " + mCount);
        }
    }
    private void cleanUnusedMethods(DexClass dexClass) {
        Iterator<DexMethod> iterator = dexClass.getDeclaredMethods();
        ArrayCollection<DexMethod> list = null;
        while (iterator.hasNext()) {
            DexMethod dexMethod = iterator.next();
            if(isUnusedMethod(dexMethod)) {
                if(list == null) {
                    list = new ArrayCollection<>();
                }
                list.add(dexMethod);
            }
        }
        if(list != null) {
            boolean debugEnabled = isDebugEnabled();
            for(DexMethod dexMethod : list) {
                if(debugEnabled) {
                    debug(dexMethod.getKey().toString());
                }
                dexMethod.removeSelf();
                mCount ++;
            }
        }
    }

    private boolean isUnusedMethod(DexMethod dexMethod) {
        if(dexMethod.isConstructor() && dexMethod.isStatic()) {
            return false;
        }
        return isUnusedPrivateMethod(dexMethod) || isUnusedVirtualMethod(dexMethod);
    }
    private boolean isUnusedPrivateMethod(DexMethod dexMethod) {
        if(!dexMethod.isPrivate() || dexMethod.isConstructor()) {
            return false;
        }
        MethodKey methodKey = dexMethod.getKey();
        Iterator<DexInstruction> iterator = dexMethod.getDexClass().getDexInstructions();
        while (iterator.hasNext()) {
            DexInstruction instruction = iterator.next();
            if(methodKey.equals(instruction.getMethodKey())) {
                return false;
            }
        }
        return true;
    }
    private boolean isUnusedVirtualMethod(DexMethod dexMethod) {
        // TODO:
        return false;
    }
}
