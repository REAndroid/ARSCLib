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
package com.reandroid.dex.index;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.DexBlockItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.item.AnnotationSet;
import com.reandroid.dex.sections.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ItemId extends DexBlockItem implements SmaliFormat {
    ItemId(int bytesLength) {
        super(bytesLength);
    }
    TypeId getTypeId(IntegerReference reference){
        return getTypeId(reference.get());
    }
    TypeId getTypeId(int index){
        return getSectionList().get(SectionType.TYPE_ID, index);
    }
    StringData getStringData(int index){
        return getSectionList().get(SectionType.STRING_DATA, index);
    }
    SectionList getSectionList(){
        return getParentInstance(SectionList.class);
    }
    public<T1 extends Block> T1 getAt(SectionType<T1> sectionType, int offset){
        if(offset == 0){
            return null;
        }
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
}
