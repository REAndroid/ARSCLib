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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.base.ParallelReference;
import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.header.CountAndOffset;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.item.DataSectionEntry;

import java.util.Arrays;
import java.util.Iterator;

public class MapList extends DataSectionEntry
        implements Iterable<MapItem>, PositionAlignedItem {

    private final CountedArray<MapItem> itemArray;
    private final DexPositionAlign positionAlign;

    private final ParallelReference fileSize;
    private final ParallelReference dataStart;
    private final ParallelReference dataSize;

    public MapList(IntegerReference offsetReference) {
        super(3);
        IntegerItem mapItemsCount = new IntegerItem();
        this.itemArray = new CountedArray<>(mapItemsCount, CREATOR);
        this.positionAlign = new DexPositionAlign();

        addChild(0, positionAlign);
        addChild(1, mapItemsCount);
        addChild(2, itemArray);

        setOffsetReference(offsetReference);

        this.fileSize = new ParallelReference(new FileSizeReference(this));
        this.dataStart = new ParallelReference(new DataStartReference(this));
        this.dataSize = new ParallelReference(new DataSizeReference(this));
    }

    public ParallelReference getFileSize(){
        return fileSize;
    }
    public ParallelReference getDataStart() {
        return dataStart;
    }
    public ParallelReference getDataSize() {
        return dataSize;
    }

    public void sortMapItems(SectionType<?>[] order){
        itemArray.sort(SectionType.comparator(order, MapItem::getMapType));
    }

    public void linkHeader(DexHeader dexHeader){

        MapItem mapItem = get(SectionType.STRING_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.string_id);
        }
        mapItem = get(SectionType.TYPE_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.type_id);
        }
        mapItem = get(SectionType.PROTO_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.proto_id);
        }
        mapItem = get(SectionType.FIELD_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.field_id);
        }
        mapItem = get(SectionType.METHOD_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.method_id);
        }
        mapItem = get(SectionType.CLASS_ID);
        if(mapItem != null){
            mapItem.getCountAndOffset().setReference2(dexHeader.class_id);
        }
        mapItem = get(SectionType.MAP_LIST);
        if(mapItem != null){
            mapItem.getCount().set(1);
            mapItem.getOffset().setReference2(dexHeader.map);
        }
        getFileSize().setReference2(dexHeader.fileSize);
        getDataSize().setReference2(dexHeader.data.getFirst());
        getDataStart().setReference2(dexHeader.data.getSecond());
    }
    public void updateHeader(DexHeader dexHeader){
        MapItem mapItem = get(SectionType.STRING_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.string_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.TYPE_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.type_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.PROTO_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.proto_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.FIELD_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.field_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.METHOD_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.method_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.CLASS_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.class_id;
            countAndOffset.setCount(mapItem.getCountValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = getDataStartItem();
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.data;
            countAndOffset.setCount(dexHeader.fileSize.get() - mapItem.getOffsetValue());
            countAndOffset.setOffset(mapItem.getOffsetValue());
        }
        mapItem = get(SectionType.MAP_LIST);
        if(mapItem != null){
            mapItem.getCount().set(1);
            mapItem.getOffset().set(dexHeader.map.get());
        }
    }
    public MapItem getDataStartItem(){
        boolean headerFound = false;
        for(MapItem mapItem : this){
            SectionType<?> sectionType = mapItem.getMapType();
            if(!headerFound){
                headerFound = sectionType == SectionType.HEADER;
                continue;
            }
            if(sectionType.isDataSection()){
                return mapItem;
            }
        }
        return null;
    }
    public MapItem get(SectionType<?> type){
        for(MapItem mapItem:this){
            if(type == mapItem.getMapType()){
                return mapItem;
            }
        }
        return null;
    }
    public MapItem getOrCreate(SectionType<?> type){
        MapItem mapItem = get(type);
        if(mapItem != null){
            return null;
        }
        mapItem = itemArray.createNext();
        mapItem.setType(type);
        return mapItem;
    }
    @Override
    public Iterator<MapItem> iterator() {
        return itemArray.iterator();
    }

    public MapItem[] getReadSorted(){
        MapItem[] mapItemList = itemArray.getChildes().clone();
        Arrays.sort(mapItemList, SectionType.comparator(SectionType.getReadOrderList(), MapItem::getMapType));
        return mapItemList;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();

        getFileSize().refresh();
        getDataStart().refresh();
        getDataSize().refresh();

    }

    @Override
    public String toString() {
        MapItem[] mapItems = itemArray.getChildes();
        StringBuilder builder = new StringBuilder(mapItems.length * 47);
        for(int i = 0; i < mapItems.length; i++){
            if(i != 0){
                builder.append('\n');
            }
            if(i < 9){
                builder.append(' ');
            }
            builder.append((i + 1));
            builder.append(") ");
            builder.append(mapItems[i]);
        }
        return builder.toString();
    }

    @Override
    public DexPositionAlign getPositionAlign() {
        return positionAlign;
    }

    private static final Creator<MapItem> CREATOR = new Creator<MapItem>() {
        @Override
        public MapItem[] newInstance(int length) {
            return new MapItem[length];
        }
        @Override
        public MapItem newInstance() {
            return new MapItem();
        }
    };

    static class DataStartReference implements IntegerReference {

        private final MapList mapList;

        private DataStartReference(MapList mapList) {
            this.mapList = mapList;
        }

        @Override
        public int get() {
            MapItem mapItem = mapList.getDataStartItem();
            if(mapItem != null){
                return mapItem.getOffset().get();
            }
            return 0;
        }
        @Override
        public void set(int value) {
        }
    }

    static class DataSizeReference implements IntegerReference {

        private final MapList mapList;

        private DataSizeReference(MapList mapList) {
            this.mapList = mapList;
        }

        @Override
        public int get() {
            MapItem mapItem = mapList.getDataStartItem();
            if(mapItem != null){
                return mapList.getFileSize().get() - mapItem.getOffset().get();
            }
            return 0;
        }
        @Override
        public void set(int value) {
        }
    }

    static class FileSizeReference implements IntegerReference {

        private final MapList mapList;

        private FileSizeReference(MapList mapList) {
            this.mapList = mapList;
        }
        @Override
        public int get() {
            return mapList.getOffset() + mapList.countBytes();
        }
        @Override
        public void set(int value) {
        }
    }
}
