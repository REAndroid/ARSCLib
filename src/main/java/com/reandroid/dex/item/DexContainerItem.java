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
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

public class DexContainerItem extends FixedDexContainer {

    public DexContainerItem(int childesCount) {
        super(childesCount);
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
    public<T1 extends Block> DexIdPool<T1> getPool(SectionType<T1> sectionType){
        Section<T1> section = getSection(sectionType);
        if(section != null){
            return section.getPool();
        }
        return null;
    }
}
