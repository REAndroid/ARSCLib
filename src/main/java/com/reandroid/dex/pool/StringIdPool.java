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
package com.reandroid.dex.pool;

import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;

public class StringIdPool extends DexIdPool<StringId> {

    private final SectionList sectionList;
    private Section<StringData> dataSection;

    public StringIdPool(SectionList sectionList) {
        super();
        this.sectionList = sectionList;
    }

    @Override
    public KeyItemGroup<StringId> getGroup(Key key) {
        DexIdPool<StringData> dataPool = getDataPool();
        if(dataPool == null){
            return null;
        }
        KeyItemGroup<StringData> dataGroup = dataPool.getGroup(key);
        if(dataGroup == null){
            return null;
        }
        KeyItemGroup<StringId> group = new KeyItemGroup<>(getSectionType().getCreator(), null);
        for(StringData data : dataGroup){
            //group.add(data.getStringId());
        }
        return group;
    }

    @Override
    public void keyChanged(Key old) {
        DexIdPool<StringData> dataPool = getDataPool();
        if(dataPool != null){
            dataPool.keyChanged(old);
        }
    }
    @Override
    public int size() {
        DexIdPool<StringData> dataPool = getDataPool();
        if(dataPool != null){
            return dataPool.size();
        }
        return 0;
    }
    @Override
    public void load() {
    }
    @Override
    SectionType<StringId> getSectionType(){
        return SectionType.STRING_ID;
    }
    private DexIdPool<StringData> getDataPool(){
        Section<StringData> section = getDataSection();
        if(section != null){
            return section.getPool();
        }
        return null;
    }

    private Section<StringData> getDataSection() {
        if(this.dataSection == null){
            Section<StringData> section = sectionList.get(SectionType.STRING_DATA);
            if(section == null || !section.isPoolLoaded()){
                return null;
            }
            this.dataSection = section;
        }
        return this.dataSection;
    }
}
