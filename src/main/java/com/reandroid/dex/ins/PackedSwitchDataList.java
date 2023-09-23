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
package com.reandroid.dex.ins;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.common.ArraySupplier;
import com.reandroid.common.ArraySupplierIterator;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.item.IntegerList;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class PackedSwitchDataList extends IntegerList
        implements SmaliFormat, LabelList, VisitableInteger {

    private final IntegerReference firstKey;
    private final InsPackedSwitchData switchData;

    public PackedSwitchDataList(InsPackedSwitchData switchData, IntegerReference itemCount, IntegerReference firstKey){
        super(itemCount);
        this.firstKey = firstKey;
        this.switchData = switchData;
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        int size = size();
        for(int i = 0; i < size; i++){
            visitor.visit(this, getData(i));
        }
    }
    public int getFirstKey(){
        return firstKey.get();
    }
    public int getBaseAddress(){
        InsPackedSwitch packedSwitch = switchData.getParentPackedSwitch();
        if(packedSwitch == null){
            return 0;
        }
        return packedSwitch.getAddress();
    }
    public int size(){
        return super.size();
    }
    public Data getData(int index){
        return new Data(this, index);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        for(int i = 0; i < size; i++){
            getData(i).append(writer);
        }
    }

    @Override
    public Iterator<Data> getLabels() {
        return new ArraySupplierIterator<>(new ArraySupplier<Data>() {
            @Override
            public Data get(int i) {
                return PackedSwitchDataList.this.getData(i);
            }
            @Override
            public int getCount() {
                return PackedSwitchDataList.this.size();
            }
        });
    }

    public static class Data implements IntegerReference, SmaliFormat, Label {
        private final PackedSwitchDataList dataList;
        private final int index;
        Data(PackedSwitchDataList dataList, int index){
            this.dataList = dataList;
            this.index = index;
        }
        @Override
        public void set(int value) {
            dataList.put(index, value);
        }

        @Override
        public int get() {
            return dataList.get(index);
        }
        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.newLine();
            writer.append(getLabelName());
        }
        @Override
        public void appendExtra(SmaliWriter writer) throws IOException {
            writer.append(getLabelName());
        }

        @Override
        public int getAddress() {
            return get();
        }
        @Override
        public int getTargetAddress() {
            return dataList.getBaseAddress() + get();
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":pswitch_", getTargetAddress(), 1);
        }
        @Override
        public int hashCode() {
            return Objects.hash(dataList, index);
        }
        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_INSTRUCTION_LABEL;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Data data = (Data) obj;
            return index == data.index && dataList == data.dataList;
        }

        @Override
        public String toString() {
            return getLabelName();
        }
    }
}
