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
package com.reandroid.dex.reference;

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.ins.SizeXIns;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;

public class InsIdSectionReference extends IdSectionReference<IdItem> {

    public InsIdSectionReference(SizeXIns sizeXIns) {
        super(sizeXIns, UsageMarker.USAGE_INSTRUCTION);
    }

    @Override
    protected SizeXIns getSectionTool() {
        return (SizeXIns) super.getSectionTool();
    }

    @Override
    public int get() {
        return getSectionTool().getData();
    }
    @Override
    public void set(int value) {
        getSectionTool().setData(value);
    }
    @SuppressWarnings("unchecked")
    @Override
    public SectionType<IdItem> getSectionType() {
        return (SectionType<IdItem>) getSectionTool().getSectionType();
    }

    @Override
    public IdItem validateReplace(IdItem idItem){
        idItem = super.validateReplace(idItem);
        return validateType(idItem);
    }
    private IdItem validateType(IdItem idItem){
        Key key = idItem.getKey();
        if(key instanceof TypeKey){
            TypeKey typeKey = (TypeKey) key;
            if(this.getItem() != null && !typeKey.isTypeObject() &&
                    !getSectionTool().is(Opcode.CONST_CLASS)) {
                throw new DexException("Unexpected type '" + key + "', " + buildTrace(idItem));
            }
        }
        return idItem;
    }
    @Override
    protected String buildTrace(IdItem item) {
        return SizeXIns.buildTrace(getSectionTool(), item, get());
    }
}
