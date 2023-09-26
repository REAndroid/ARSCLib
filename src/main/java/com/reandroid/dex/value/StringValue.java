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
package com.reandroid.dex.value;

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.AnnotationElement;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.sections.SectionType;

public class StringValue extends SectionIdValue<StringId> {

    public StringValue() {
        super(SectionType.STRING_ID, DexValueType.STRING);
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.STRING;
    }
    public StringData getStringData(){
        StringId stringId = get();
        if(stringId != null) {
            return stringId.getStringData();
        }
        return null;
    }

    @Override
    void onDataUpdated(StringId data) {
        super.onDataUpdated(data);
        if(data != null){
            StringData stringData = data.getStringData();
            int usage;
            if(getParent(AnnotationElement.class) != null){
                usage = StringData.USAGE_ANNOTATION;
            }else {
                usage = StringData.USAGE_INITIAL;
            }
            stringData.addStringUsage(usage);
        }
    }
    public String getString() {
        StringData stringData = getStringData();
        if(stringData != null) {
            return stringData.getString();
        }
        return null;
    }
    @Override
    public String getAsString() {
        StringData stringData = getStringData();
        if(stringData != null) {
            return DexUtils.quoteString(stringData.getString());
        }
        return null;
    }
}
