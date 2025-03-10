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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.StringData;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.Swappable;

import java.io.IOException;
import java.util.Comparator;

public class StringDataArray extends DataSectionArray<StringData> {

    public StringDataArray(IntegerPair countAndOffset, Creator<StringData> creator) {
        super(countAndOffset, creator);
    }

    @Override
    public boolean sort(Comparator<? super StringData> comparator) {
        return this.sort(comparator, null);
    }

    @Override
    public boolean sort(Comparator<? super StringData> comparator, Swappable swappable) {
        StringIdArray stringIdArray = getStringIdArray();
        if (stringIdArray != null) {
            return super.sort(comparator, stringIdArray);
        }
        return false;
    }

    @Override
    public void moveTo(StringData item, int index) {
        super.moveTo(item, index);
        getStringIdArray().moveTo(item.getOffsetReference(), index);
    }

    @Override
    public boolean swap(StringData item1, StringData item2) {
        boolean swapped = super.swap(item1, item2);
        getStringIdArray().swap(item1.getOffsetReference(), item2.getOffsetReference());
        return swapped;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
    }
    @Override
    public void readChildes(BlockReader reader) throws IOException {
    }


    private StringIdArray getStringIdArray(){
        SectionList sectionList = getParent(SectionList.class);
        if(sectionList != null){
            StringIdSection section = (StringIdSection) sectionList.getSection(SectionType.STRING_ID);
            if(section != null){
                return section.getItemArray();
            }
        }
        return ObjectsUtil.getNull();
    }
}
