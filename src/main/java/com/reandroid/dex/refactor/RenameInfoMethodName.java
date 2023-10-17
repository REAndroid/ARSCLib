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
package com.reandroid.dex.refactor;

import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.key.KeyUtil;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.dex.sections.SectionType;

public class RenameInfoMethodName extends RenameInfoName<MethodId> {

    public RenameInfoMethodName(String typeName, String[] parameters, String search, String replace) {
        super(typeName, parameters, search, replace);
    }
    public RenameInfoMethodName(MethodKey methodKey, String replace) {
        this(methodKey.getDefining(), methodKey.getParameters(), methodKey.getName(), replace);
    }

    @Override
    public void apply(DexDirectory directory){
        MethodKey methodKey = getKey();

        ArrayCollection<MethodId> check = ArrayCollection.of(
                directory.getMethods(methodKey.changeName(getReplace())));

        ArrayCollection<MethodId> collection = ArrayCollection.of(directory.getMethods(methodKey));

        if(!check.isEmpty() && collection.isEmpty()){
            log("Already renamed: " + methodKey + " to " + getReplace());
            return;
        }
        if(!check.isEmpty()){
            // TODO: should throw ?
            log("Conflicting methods found: " + check.size());
            for(MethodId methodId : check){
                log(methodId.getKey());
            }
            return;
        }
        if(collection.isEmpty()){
            // Method not found
            log("Method not found: " + methodKey);
            return;
        }
        for(MethodId methodId : collection){
            renameMethod(methodId);
        }
        log("Renamed = " + collection.size() + ", '" + methodKey + "', to '" + getReplace() + "'");
    }
    private void renameMethod(MethodId methodId){
        methodId.setName(getReplace());
        log("    " + methodId.getKey());
    }

    @Override
    SectionType<MethodId> getSectionType() {
        return SectionType.METHOD_ID;
    }
    @Override
    void apply(Iterable<MethodId> group){
        // Nah check super, implement ... first
    }
    @Override
    public MethodKey getKey(){
        return new MethodKey(getTypeName(), getSearch(), getParameters(), KeyUtil.ANY_NAME);
    }
}
