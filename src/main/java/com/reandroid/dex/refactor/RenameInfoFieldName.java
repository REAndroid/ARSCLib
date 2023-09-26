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

import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.index.FieldId;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.sections.SectionType;

public class RenameInfoFieldName extends RenameInfoName<FieldId> {

    public RenameInfoFieldName(String typeName, String search, String replace) {
        super(typeName, null, search, replace);
    }

    @Override
    SectionType<FieldId> getSectionType() {
        return SectionType.FIELD_ID;
    }

    @Override
    void apply(ItemGroup<FieldId> group) {
        String replace = getReplace();
        for(FieldId fieldId : group){
            fieldId.setName(replace);
        }
    }
    @Override
    public FieldKey getKey(){
        return new FieldKey(getTypeName(), getSearch(), null);
    }
}
