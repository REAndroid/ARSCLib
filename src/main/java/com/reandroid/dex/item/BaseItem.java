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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

public class BaseItem extends FixedDexContainer
        implements PositionedItem, OffsetSupplier, OffsetReceiver {

    private IntegerReference mReference;

    public BaseItem(int childesCount) {
        super(childesCount);
    }


    public<T1 extends Block> T1 getAt(SectionType<T1> sectionType, int offset){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getAt(offset);
        }
        return null;
    }
    public<T1 extends Block> T1[] getAt(SectionType<T1> sectionType, int[] offsets){
        if(offsets == null || offsets.length == 0){
            return null;
        }
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getAt(offsets);
        }
        return null;
    }
    public<T1 extends Block> T1[] get(SectionType<T1> sectionType, int[] indexes){
        if(indexes == null || indexes.length == 0){
            return null;
        }
        Section<T1> section = getSection(sectionType);
        if(section == null){
            return null;
        }
        return section.get(indexes);
    }
    public<T1 extends Block> T1 get(SectionType<T1> sectionType, int i){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.get(i);
        }
        return null;
    }
    public<T1 extends Block> Section<T1> getSection(SectionType<T1> sectionType){
        SectionList sectionList = getParent(SectionList.class);
        if(sectionList != null){
            return sectionList.get(sectionType);
        }
        return null;
    }
    @Override
    public void setPosition(int position) {
        IntegerReference reference = getOffsetReference();
        if(reference == null){
            reference = new NumberIntegerReference(position);
            setOffsetReference(reference);
        }else {
            reference.set(position);
        }
    }
    @Override
    public IntegerReference getOffsetReference() {
        return mReference;
    }
    @Override
    public void setOffsetReference(IntegerReference reference) {
        this.mReference = reference;
    }

}
