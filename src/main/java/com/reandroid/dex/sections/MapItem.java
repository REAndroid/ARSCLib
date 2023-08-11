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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.*;

import java.util.Comparator;

public class MapItem extends DexItem{
    private final IndirectInteger type;
    private final IntegerPair countAndOffset;

    public MapItem() {
        super(SIZE);
        int offset = -4;
        this.type = new IndirectInteger(this, offset += 4);
        this.countAndOffset = new IndirectIntegerPair(this, offset += 4);
    }

    public IntegerPair getCountAndOffset() {
        return countAndOffset;
    }
    public<T1 extends Block> SectionType<T1> getMapType(){
        return SectionType.get(getType().get());
    }

    public<T1 extends Block> Section<T1> createNewSection(){
        SectionType<T1> sectionType = getMapType();
        if(sectionType == null || sectionType.getCreator() == null){
            return null;
        }
        Block parent = getParentInstance(MapList.class);
        if(parent != null){
            parent = parent.getParent();
        }
        if(parent == null){
            parent = getParentInstance(DexFile.class);
        }
        if(parent == null){
            parent = getParent();
        }
        Section<T1> section = new Section<>(getCountAndOffset(), sectionType);
        section.setParent(parent);
        return section;
    }

    public void setType(SectionType<?> type){
        getType().set(type.getType());
    }
    public IntegerReference getType(){
        return type;
    }
    public IntegerReference getCount(){
        return countAndOffset.getFirst();
    }
    public IntegerReference getOffset(){
        return countAndOffset.getSecond();
    }

    @Override
    public String toString() {
        return  "type=" + getMapType() +
                ", count=" + countAndOffset;
    }

    public static final Comparator<MapItem> READ_COMPARATOR = new Comparator<MapItem>() {
        @Override
        public int compare(MapItem mapItem1, MapItem mapItem2) {
            if(mapItem1 == mapItem2){
                return 0;
            }
            if(mapItem1 == null){
                return 1;
            }
            if(mapItem2 == null){
                return -1;
            }
            SectionType<?> sectionType1 = mapItem1.getMapType();
            SectionType<?> sectionType2 = mapItem2.getMapType();
            if(sectionType1 == sectionType2){
                return 0;
            }
            if(sectionType1 == null){
                return 1;
            }
            return sectionType1.compareReadOrder(sectionType2);
        }
    };

    public static final Comparator<MapItem> WRITE_COMPARATOR = new Comparator<MapItem>() {
        @Override
        public int compare(MapItem mapItem1, MapItem mapItem2) {
            if(mapItem1 == mapItem2){
                return 0;
            }
            if(mapItem1 == null){
                return 1;
            }
            if(mapItem2 == null){
                return -1;
            }
            SectionType<?> sectionType1 = mapItem1.getMapType();
            SectionType<?> sectionType2 = mapItem2.getMapType();
            if(sectionType1 == sectionType2){
                return 0;
            }
            if(sectionType1 == null){
                return 1;
            }
            return sectionType1.compareWriteOrder(sectionType2);
        }
    };

    private static final int SIZE = 12;
}
