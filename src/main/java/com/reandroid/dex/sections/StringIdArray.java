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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.data.StringData;

public class StringIdArray extends IdSectionArray<StringId> {

    private StringDataArray mStringDataArray;

    public StringIdArray(IntegerPair countAndOffset, Creator<StringId> creator) {
        super(countAndOffset, creator);
    }

    @Override
    public void setSize(int size) {
        StringDataArray dataArray = getStringDataArray();
        dataArray.setSize(size);
        super.setSize(size);
        for (int i = 0; i < size; i++) {
            StringId stringId = get(i);
            StringData stringData = dataArray.get(i);
            stringId.linkStringData(stringData);
        }
    }
    @Override
    public StringId createNext() {
        StringDataArray stringDataArray = getStringDataArray();
        StringData stringData = stringDataArray.createNext();
        StringId stringId = super.createNext();
        stringId.linkStringData(stringData);
        return stringId;
    }

    @Override
    public void clear() {
        getStringDataArray().clear();
        super.clear();
    }
    private StringDataArray getStringDataArray() {
        StringDataArray stringDataArray = this.mStringDataArray;
        if (stringDataArray == null) {
            SectionList sectionList = getParent(SectionList.class);
            if (sectionList != null) {
                stringDataArray = ((StringDataSection) sectionList
                        .getOrCreateSection(SectionType.STRING_DATA))
                        .getItemArray();
                this.mStringDataArray = stringDataArray;
            }
        }
        return stringDataArray;
    }
}
