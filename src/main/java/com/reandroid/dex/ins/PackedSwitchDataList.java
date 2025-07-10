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

import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliPackedSwitchEntry;
import com.reandroid.dex.smali.model.SmaliPayloadPackedSwitch;
import com.reandroid.dex.smali.model.SmaliSet;

import java.io.IOException;
import java.util.Iterator;

public class PackedSwitchDataList extends CountedBlockList<PackedSwitchEntry>
        implements SmaliFormat, LabelsSet {

    private final InsPackedSwitchData switchData;

    public PackedSwitchDataList(InsPackedSwitchData switchData, IntegerReference itemCount){
        super(InsPackedSwitchData.CREATOR, itemCount);
        this.switchData = switchData;
    }

    public int getFirstKey(){
        return switchData.getFirstKey();
    }
    public int getBaseAddress(){
        InsPackedSwitch packedSwitch = switchData.getSwitch();
        if(packedSwitch == null){
            return 0;
        }
        return packedSwitch.getAddress();
    }
    public InsPackedSwitchData getSwitchData() {
        return switchData;
    }
    void onDataChange(int index, int value) {
        this.switchData.onDataChange(index, value);
    }

    public void merge(PackedSwitchDataList dataList){
        int size = dataList.size();
        setSize(size);
        for(int i = 0; i < size; i++){
            get(i).merge(dataList.get(i));
        }
    }
    public void fromSmali(SmaliPayloadPackedSwitch smali) {
        SmaliSet<SmaliPackedSwitchEntry> entries = smali.getEntries();
        int size = entries.size();
        setSize(size);
        for (int i = 0; i < size; i++) {
            get(i).fromSmali(entries.get(i));
        }
    }
    public void toSmali(SmaliPayloadPackedSwitch smali) {
        int size = this.size();
        for (int i = 0; i < size; i++) {
            smali.addEntry(get(i).toSmali());
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        for(int i = 0; i < size; i++){
            get(i).append(writer);
        }
    }

    @Override
    public Iterator<PackedSwitchEntry> getLabels() {
        return iterator();
    }

}
