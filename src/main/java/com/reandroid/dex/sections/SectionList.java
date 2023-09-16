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

import com.reandroid.common.ArraySupplier;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.dex.header.DexHeader;

import java.io.IOException;
import java.util.*;

public class SectionList extends FixedBlockContainer
        implements OffsetSupplier, Iterable<Section<?>> , ArraySupplier<Section<?>> {
    private final IntegerReference baseOffset;
    private final DexHeader dexHeader;
    private final BlockList<Section<?>> dexSectionList;
    private final Map<SectionType<?>, Section<?>> typeMap;
    private final MapList mapList;

    public SectionList() {
        super(1);

        this.baseOffset = new NumberIntegerReference();

        this.dexHeader = new DexHeader(baseOffset);
        this.dexSectionList = new BlockList<>();
        this.mapList = new MapList(dexHeader);

        dexHeader.setParent(this);
        mapList.setParent(this);

        this.typeMap = new HashMap<>();

        addChild(0, dexSectionList);

    }

    public void updateHeader(){
        dexHeader.updateHeaderInternal(this);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        updateIdCounts();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        mapList.refresh();
        mapList.updateHeader(dexHeader);
    }
    private void updateIdCounts(){
        for(Section<?> section : this){
            Section<?> idSection = get(
                    section.getSectionType().getIdSectionType());
            if(idSection == null){
                continue;
            }
            idSection.getItemArray()
                    .setChildesCount(section.getCount());
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        dexHeader.readBytes(reader);
        mapList.readBytes(reader);
        add(mapList.get(SectionType.HEADER).createNewSection())
                .add(dexHeader);
        add(mapList.get(SectionType.MAP_LIST).createNewSection())
                .add(mapList);

        MapItem[] mapItemList = mapList.getReadSorted();
        int length = mapItemList.length;
        for(int i = 0; i < length; i++){
            MapItem mapItem = mapItemList[i];
            if(mapItem == null){
                continue;
            }
            loadSection(mapItem, reader);
        }
        dexSectionList.sort(getOffsetComparator());
    }
    private void loadSection(MapItem mapItem, BlockReader reader) throws IOException {
        if(mapItem == null){
            return;
        }
        SectionType<?> sectionType = mapItem.getMapType();
        if(typeMap.containsKey(sectionType)){
            return;
        }
        Section<?> section = mapItem.createNewSection();
        if(section == null){
            return;
        }
        add(section);
        section.readBytes(reader);
        section.buildOffsetMap();
    }
    public<T1 extends Block> Section<T1> add(Section<T1> section){
        dexSectionList.add(section);
        typeMap.put(section.getSectionType(), section);
        return section;
    }
    public DexHeader getHeader() {
        return dexHeader;
    }
    public MapList getMapList() {
        return mapList;
    }

    public<T1 extends Block> T1 getAt(SectionType<T1> sectionType, int i){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.getAt(i);
        }
        return null;
    }
    public<T1 extends Block> T1 get(SectionType<T1> sectionType, int i){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.get(i);
        }
        return null;
    }
    public<T1 extends Block> T1[] get(SectionType<T1> sectionType, int[] indexes){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section.get(indexes);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public<T1 extends Block> Section<T1> get(SectionType<T1> sectionType){
        return (Section<T1>) typeMap.get(sectionType);
    }

    @Override
    public Section<?> get(int i) {
        return dexSectionList.get(i);
    }
    @Override
    public int getCount(){
        return dexSectionList.size();
    }
    @Override
    public Iterator<Section<?>> iterator() {
        return dexSectionList.iterator();
    }
    @Override
    public IntegerReference getOffsetReference() {
        return baseOffset;
    }

    private static  Comparator<Section<?>> getOffsetComparator() {
        return new Comparator<Section<?>>() {
            @Override
            public int compare(Section<?> section1, Section<?> section2) {
                return Integer.compare(section1.getOffset(), section2.getOffset());
            }
        };
    }
}
