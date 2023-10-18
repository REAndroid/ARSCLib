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
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.dex.base.ParallelReference;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.key.*;
import com.reandroid.utils.collection.ArraySupplierIterator;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.*;

public class SectionList extends FixedBlockContainer
        implements OffsetSupplier, Iterable<Section<?>> , ArraySupplier<Section<?>> {
    private final IntegerReference baseOffset;
    private final DexHeader dexHeader;
    private final Section<DexHeader> dexHeaderSection;
    private final BlockList<IdSection<?>> idSectionList;
    private final BlockList<DataSection<?>> dataSectionList;
    private final Section<MapList> mapListSection;
    private final Map<SectionType<?>, Section<?>> typeMap;
    private final MapList mapList;

    public SectionList() {
        super(4);

        this.baseOffset = new NumberIntegerReference();
        this.idSectionList = new BlockList<>();
        this.dataSectionList = new BlockList<>();

        IntegerPair headerCountAndOffset = IntegerPair.of(
                new NumberIntegerReference(),
                baseOffset);

        headerCountAndOffset.getFirst().set(1);

        Section<DexHeader> dexHeaderSection = new Section<>(headerCountAndOffset, SectionType.HEADER);
        DexHeader dexHeader = new DexHeader(baseOffset);
        dexHeaderSection.add(dexHeader);
        this.dexHeaderSection = dexHeaderSection;

        IntegerPair mapListCountAndOffset = IntegerPair.of(
                new NumberIntegerReference(),
                new ParallelReference(dexHeader.map));
        mapListCountAndOffset.getFirst().set(1);

        Section<MapList> mapListSection = new Section<>(mapListCountAndOffset, SectionType.MAP_LIST);
        MapList mapList = new MapList(dexHeader.map);
        mapListSection.add(mapList);
        this.mapListSection = mapListSection;

        this.typeMap = new HashMap<>();

        addChild(0, dexHeaderSection);
        addChild(1, idSectionList);
        addChild(2, dataSectionList);
        addChild(3, mapListSection);

        this.dexHeader = dexHeader;
        this.mapList = mapList;

        typeMap.put(SectionType.HEADER, dexHeaderSection);
        typeMap.put(SectionType.MAP_LIST, mapListSection);
    }

    public void clearUsageTypes(){
        Iterator<Section<?>> iterator = getSections();
        while (iterator.hasNext()) {
            iterator.next().clearUsageTypes();
        }
    }
    public void updateHeader() {
        Block parent = getParentInstance(DexFileBlock.class);
        if(parent == null){
            parent = this;
        }
        dexHeader.updateHeaderInternal(parent);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        mapList.refresh();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        get(SectionType.HEADER).readBytes(reader);
        get(SectionType.MAP_LIST).readBytes(reader);

        MapItem[] mapItemList = mapList.getReadSorted();
        int length = mapItemList.length;
        for(int i = 0; i < length; i++){
            MapItem mapItem = mapItemList[i];
            if(mapItem == null){
                continue;
            }
            loadSection(mapItem, reader);
        }
        idSectionList.sort(getOffsetComparator());
        dataSectionList.sort(getOffsetComparator());
        mapList.linkHeader(dexHeader);
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
    }
    public<T1 extends Block> Section<T1> add(Section<T1> section){
        if(section instanceof IdSection){
            idSectionList.add((IdSection<?>) section);
        }else {
            dataSectionList.add((DataSection<?>) section);
        }
        typeMap.put(section.getSectionType(), section);
        return section;
    }
    public DexHeader getHeader() {
        return dexHeader;
    }
    public MapList getMapList() {
        return mapList;
    }

    public void clearPools(){
        for(Section<?> section : this){
            section.clearPool();
        }
    }
    public void sortSection(SectionType<?>[] order){
        idSectionList.sort(SectionType.comparator(order, Section::getSectionType));
        dataSectionList.sort(SectionType.comparator(order, Section::getSectionType));
        mapList.sortMapItems(order);
    }
    public void sortStrings(){
        if(!sortItems(SectionType.STRING_DATA)){
            return;
        }
        if(!sortItems(SectionType.STRING_ID)){
            return;
        }
        if(!sortItems(SectionType.TYPE_ID)){
            return;
        }
        sortItems(SectionType.PROTO_ID);
        sortItems(SectionType.FIELD_ID);
        sortItems(SectionType.METHOD_ID);
        sortItems(SectionType.CLASS_ID);
    }
    private boolean sortItems(SectionType<?> sectionType){
        Section<?> section = get(sectionType);
        if(section != null){
            section.sort();
            return true;
        }
        return false;
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
    public<T1 extends Block> Section<T1> getOrCreate(SectionType<T1> sectionType){
        Section<T1> section = get(sectionType);
        if(section != null){
            return section;
        }
        if(sectionType == SectionType.MAP_LIST){
            return null;
        }
        MapList mapList = getMapList();
        MapItem mapItem = mapList.getOrCreate(sectionType);
        section = mapItem.createNewSection();
        add(section);
        sortSection(SectionType.getR8Order());

        return section;
    }

    public int indexOf(Section<?> section){
        if(section == dexHeaderSection){
            return 0;
        }
        if(section == mapListSection){
            return getCount() - 1;
        }
        if (section == idSectionList.get(section.getIndex())){
            return 1 + section.getIndex();
        }
        if (section == dataSectionList.get(section.getIndex())){
            return 1 + idSectionList.size() + section.getIndex();
        }
        return -1;
    }
    @Override
    public Section<?> get(int i) {
        if(i == 0){
            return dexHeaderSection;
        }
        if(i == getCount() - 1){
            return mapListSection;
        }
        if(i <= idSectionList.size()){
            return idSectionList.get(i - 1);
        }
        return dataSectionList.get(i - 1 - idSectionList.size());
    }

    public boolean contains(Key key){
        if(key == null){
            return false;
        }
        if(key instanceof StringKey){
            return contains(SectionType.STRING_ID, key);
        }
        if(key instanceof TypeKey){
            return contains(SectionType.TYPE_ID, key);
        }
        if(key instanceof FieldKey){
            return contains(SectionType.FIELD_ID, key);
        }
        if(key instanceof ProtoKey){
            return contains(SectionType.PROTO_ID, key);
        }
        if(key instanceof MethodKey){
            return contains(SectionType.METHOD_ID, key);
        }
        if(key instanceof TypeListKey){
            return contains(SectionType.TYPE_LIST, key);
        }
        throw new IllegalArgumentException("Unknown key type: " + key.getClass() + ", '" + key + "'");
    }
    private boolean contains(SectionType<?> sectionType, Key key){
        Section<?> section = get(sectionType);
        if(section != null){
            return section.contains(key);
        }
        return false;
    }
    public void keyChanged(SectionType<?> sectionType, Key oldKey){
        Section<?> section = get(sectionType);
        if(section == null){
            return;
        }
        boolean updated = section.keyChanged(oldKey);
        if(!updated || (!(oldKey instanceof TypeKey))){
            return;
        }
        TypeKey typeKey = (TypeKey) oldKey;
        //TODO: notify to all uses TypeKey
    }
    public Iterator<Section<?>> getSections() {
        return new CombiningIterator<>(getIdSections(), getDataSections());
    }
    public Iterator<IdSection<?>> getIdSections() {
        return idSectionList.iterator();
    }
    public Iterator<DataSection<?>> getDataSections() {
        return dataSectionList.iterator();
    }
    @Override
    public int getCount(){
        return 2 + idSectionList.size() + dataSectionList.size();
    }
    @Override
    public Iterator<Section<?>> iterator() {
        return ArraySupplierIterator.of(this);
    }
    @Override
    public IntegerReference getOffsetReference() {
        return baseOffset;
    }

    private static<T1 extends Section<?>> Comparator<T1> getOffsetComparator() {
        return (section1, section2) -> {
            if(section1 == section2){
                return 0;
            }
            if(section1 == null){
                return 1;
            }
            return section1.compareOffset(section2);
        };
    }
}
