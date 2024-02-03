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
package com.reandroid.arsc.array;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.AlignItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;

import java.io.IOException;

public class StyleArray extends OffsetBlockArray<StyleItem> implements JSONConvert<JSONArray> {
    public StyleArray(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart) {
        super(offsets, itemCount, itemStart);
    }
    protected void onStringShifted(int index){
        StyleItem styleItem = get(index);
        if(styleItem == null || styleItem.getIndex() == index){
            return;
        }
        styleItem = newInstance();
        setItem(index, styleItem);
        styleItem.setNull(true);
        styleItem.linkStringsInternal();
    }
    @Override
    public void clearChildes(){
        for(StyleItem styleItem:listItems()){
            styleItem.onRemoved();
        }
        super.clearChildes();
    }
    @Override
    void refreshAlignment(BlockReader reader, AlignItem alignItem) throws IOException {
        alignItem.clear();
        alignItem.setFill(END_BYTE);
        if(reader.available() < 4){
            return;
        }
        IntegerItem integerItem = new IntegerItem();
        while (reader.available() >= 4){
            int position = reader.getPosition();
            integerItem.readBytes(reader);
            if(integerItem.get() != 0xFFFFFFFF){
                reader.seek(position);
                break;
            }
            alignItem.setSize(alignItem.size() + 4);
        }
    }
    @Override
    void refreshAlignment(AlignItem alignItem) {
        if(getChildesCount() == 0){
            alignItem.clear();
            return;
        }
        alignItem.setFill(END_BYTE);
        alignItem.ensureSize(8);
    }
    @Override
    protected void refreshChildes(){
        // Not required
    }
    @Override
    protected boolean remove(StyleItem block, boolean trim){
        if(block == null){
            return false;
        }
        boolean removed = super.remove(block, trim);
        if(!removed && trim){
            trimNullBlocks();
        }
        return removed;
    }
    @Override
    public StyleItem newInstance() {
        return new StyleItem();
    }
    @Override
    public StyleItem[] newArrayInstance(int len) {
        return new StyleItem[len];
    }

    @Override
    public JSONArray toJson() {
        if(getChildesCount()==0){
            return null;
        }
        return null;
    }
    @Override
    public void fromJson(JSONArray json) {

    }
    public void merge(StyleArray styleArray){
        if(styleArray==null||styleArray==this){
            return;
        }
        if(getChildesCount()!=0){
            return;
        }
        int count=styleArray.getChildesCount();
        ensureSize(count);
        for(int i=0;i<count;i++){
            StyleItem exist=get(i);
            StyleItem coming=styleArray.get(i);
            exist.merge(coming);
        }
    }
    private static final byte END_BYTE= (byte) 0xFF;
}
