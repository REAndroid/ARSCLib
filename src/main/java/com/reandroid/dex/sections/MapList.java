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
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.*;
import com.reandroid.dex.header.CountAndOffset;
import com.reandroid.dex.header.DexHeader;

import java.io.IOException;
import java.util.*;

public class MapList extends FixedDexContainer
        implements Iterable<MapItem>, OffsetSupplier {

    private final IntegerReference offsetReference;

    private final IntegerItem mapItemsCount;

    private final DexItemArray<MapItem> itemArray;

    public MapList(IntegerReference offsetReference) {
        super(2);
        this.offsetReference = offsetReference;
        this.mapItemsCount = new IntegerItem();

        IntegerPair countAndOffset = IntegerPair.of(mapItemsCount,
                new AddingIntegerReference(offsetReference, mapItemsCount));
        this.itemArray = new DexItemArray<>(countAndOffset, CREATOR);

        addChild(0, mapItemsCount);
        addChild(1, itemArray);
    }
    public MapList(DexHeader header){
        this(header.map);
    }


    public void updateHeader(DexHeader dexHeader){
        MapItem mapItem = get(SectionType.STRING_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.strings;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = get(SectionType.TYPE_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.type;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = get(SectionType.PROTO_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.proto;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = get(SectionType.FIELD_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.field;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = get(SectionType.METHOD_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.method;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = get(SectionType.CLASS_ID);
        if(mapItem != null){
            CountAndOffset countAndOffset = dexHeader.class_def;
            countAndOffset.setCount(mapItem.getCount().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
        mapItem = getDataStart();
        if(mapItem != null){
            //TODO: this is not right way
            CountAndOffset countAndOffset = dexHeader.data;
            countAndOffset.setCount(dexHeader.fileSize.get() - mapItem.getOffset().get());
            countAndOffset.setOffset(mapItem.getOffset().get());
        }
    }
    public MapItem getDataStart(){
        for(MapItem mapItem : this){
            if(!mapItem.getMapType().isIndexSection()){
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
    @Override
    public IntegerReference getOffsetReference() {
        return offsetReference;
    }
    @Override
    public Iterator<MapItem> iterator() {
        return itemArray.iterator();
    }

    public MapItem[] getReadSorted(){
        MapItem[] mapItemList = itemArray.getChildes().clone();
        Arrays.sort(mapItemList, MapItem.READ_COMPARATOR);
        return mapItemList;
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
}
