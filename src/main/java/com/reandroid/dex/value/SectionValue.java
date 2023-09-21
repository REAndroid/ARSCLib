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
package com.reandroid.dex.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.StringKeyItem;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class SectionValue<T extends Block> extends DexValueBlock<NumberValue> implements SmaliFormat {

    private final SectionType<T> sectionType;
    private T mData;

    public SectionValue(SectionType<T> sectionType){
        super(new NumberValue());
        this.sectionType = sectionType;
    }

    public T getData(){
        return mData;
    }
    public void setData(T data){
        if(data == mData){
            return;
        }
        this.mData = data;
        getValue().setNumberValue(data.getIndex());
        onDataUpdated(data);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        getValueTypeItem().onReadBytes(reader);
        NumberValue numberValue = getValue();
        numberValue.setSize(getValueSize() + 1);
        numberValue.readBytes(reader);
        updateData();
    }
    @Override
    protected void onPreRefresh() {
        refreshData();
    }
    private void refreshData() {
        T data = this.mData;
        if(data != null){
            NumberValue numberValue = getValue();
            numberValue.setNumberValue(data.getIndex());
            int size = numberValue.getSize();
            setValueSize(size - 1);
        }
    }
    private void updateData(){
        T data = this.mData;
        int index = (int) getValue().getNumberValue();
        if(data == null || data.getIndex() != index){
            SectionList sectionList = getParentInstance(SectionList.class);
            if(sectionList != null){
                mData = sectionList.get(sectionType, index);
                onDataUpdated(mData);
            }
        }
    }
    void onDataUpdated(T data){
    }

    @Override
    public String getAsString() {
        T data = getData();
        if(data instanceof StringKeyItem){
            return ((StringKeyItem) data).getKey();
        }
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        T data = getData();
        if(data == null){
            writer.append("value error: ");
            writer.append(sectionType.toString());
            writer.append(' ');
            writer.append(HexUtil.toHex(getValue().getNumberValue(), getValueSize()));
        }else {
            ((SmaliFormat) data).append(writer);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        T data = getData();
        if(data == null){
            builder.append("value error: ");
            builder.append(sectionType);
            builder.append(' ');
            builder.append(HexUtil.toHex(getValue().getNumberValue(), getValueSize()));
        }else {
            builder.append(data);
        }
        return builder.toString();
    }
}
