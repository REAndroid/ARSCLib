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
package com.reandroid.dex.smali;

import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.model.DexMethod;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;

public interface MethodComment {

    void writeMethodComment(SmaliWriter writer, MethodKey methodKey) throws IOException;

    class MethodHierarchyComment implements MethodComment{
        private final DexClassRepository classRepository;
        public MethodHierarchyComment(DexClassRepository classRepository){
            this.classRepository = classRepository;
        }
        @Override
        public void writeMethodComment(SmaliWriter writer, MethodKey methodKey) throws IOException {
            DexMethod dexMethod = classRepository.getDeclaredMethod(methodKey);
            if(dexMethod.isConstructor()){
                return;
            }
            DexMethod superMethod = CollectionUtil.getFirst(dexMethod.getSuperMethods());
            if(superMethod == null){
                return;
            }
            writer.newLine();
            writer.appendComment("@Override: ");
            writer.appendComment(superMethod.getKey().getDeclaring().getTypeName());
        }
    }
}
