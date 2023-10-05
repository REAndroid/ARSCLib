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

import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Iterator;

public class InsPackedSwitchData extends PayloadData implements LabelsSet {
    private final ShortItem elementCount;
    private final IntegerItem firstKey;
    private final PackedSwitchDataList elements;
    private InsPackedSwitch insPackedSwitch;

    public InsPackedSwitchData() {
        super(3, Opcode.PACKED_SWITCH_PAYLOAD);
        this.elementCount = new ShortItem();
        this.firstKey = new IntegerItem();
        this.elements = new PackedSwitchDataList(this, elementCount, firstKey);

        addChild(1, elementCount);
        addChild(2, firstKey);
        addChild(3, elements);
    }
    public InsPackedSwitch getParentPackedSwitch() {
        if(insPackedSwitch == null){
            Iterator<ExtraLine> iterator = getExtraLines();
            while (iterator.hasNext()){
                ExtraLine extraLine = iterator.next();
                if(extraLine instanceof InsPackedSwitch){
                    insPackedSwitch = (InsPackedSwitch) extraLine;
                    break;
                }
            }
        }
        return insPackedSwitch;
    }

    @Override
    public Iterator<PackedSwitchDataList.Data> getLabels() {
        return elements.getLabels();
    }

    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append('.');
        writer.append(getOpcode().getName());
        writer.append(' ');
        writer.append(HexUtil.toHex(firstKey.get(), 1));
        writer.indentPlus();
        elements.append(writer);
        writer.indentMinus();
        writer.newLine();
        writer.append(".end ");
        writer.append(getOpcode().getName());
    }

    @Override
    public String toString() {
        return "InsPackedSwitchData{" +
                "elementCount=" + elementCount +
                ", firstKey=" + firstKey +
                ", elements=" + elements +
                '}';
    }
}