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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.base.StringKeyItem;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.MergingIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class RenameInfo<T extends Block> implements StringKeyItem {
    private final String search;
    private final String replace;
    private List<RenameInfo<?>> childRenames;

    public RenameInfo(String search, String replace){
        this.search = search;
        this.replace = replace;
    }

    public void apply(SectionList sectionList){
        apply(sectionList.get(getSectionType()));
        Iterator<RenameInfo<?>> iterator = getChildRenames();
        while (iterator.hasNext()){
            RenameInfo<?> renameInfo = iterator.next();
            renameInfo.apply(sectionList);
        }
    }
    private void apply(Section<T> section){
        if(section == null){
            return;
        }
        DexIdPool<T> pool = section.getPool();
        String key = getKey();
        ItemGroup<T> group = pool.getGroup(getKey());
        if(group == null){
            return;
        }
        apply(group);
        pool.keyChanged(key);
    }

    public Iterator<RenameInfo<?>> iterator(){
        return new MergingIterator<>(ComputeIterator.of(getChildRenames(), RenameInfo::iterator));
    }

    public String getSearch() {
        return search;
    }
    public String getReplace() {
        return replace;
    }
    Iterator<RenameInfo<?>> getChildRenames(){
        return listChildRenames().iterator();
    }
    public void add(RenameInfo<?> renameInfo){
        if(renameInfo == null || renameInfo == this){
            return;
        }
        List<RenameInfo<?>> renameInfoList = listChildRenames();
        if(renameInfoList == null || renameInfoList.isEmpty()){
            renameInfoList = new ArrayList<>();
        }
        renameInfoList.add(renameInfo);
    }
    public List<RenameInfo<?>> listChildRenames() {
        List<RenameInfo<?>> childRenames = this.childRenames;
        if(childRenames == null){
            childRenames = createChildRenames();
            this.childRenames = childRenames;
        }
        return childRenames;
    }
    public RenameInfo<?> getParent(){
        return null;
    }
    abstract SectionType<T> getSectionType();
    abstract void apply(ItemGroup<T> group);
    abstract List<RenameInfo<?>> createChildRenames();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RenameInfo renameInfo = (RenameInfo) obj;
        return Objects.equals(getKey(), renameInfo.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    @Override
    public String toString() {
        return getKey() + "=" + getReplace();
    }
}
