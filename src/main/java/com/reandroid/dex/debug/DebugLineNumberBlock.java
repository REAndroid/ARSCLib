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
package com.reandroid.dex.debug;

import com.reandroid.dex.program.DebugLineNumber;
import com.reandroid.dex.program.InstructionLabel;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliLineNumber;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class DebugLineNumberBlock extends DebugElementBlock implements DebugLineNumber {

    public DebugLineNumberBlock() {
        super(DebugElementType.LINE_NUMBER);
    }

    @Override
    int getAddressDiff(){
        return getFlagOffset() / 15;
    }
    void setAddressDiff(int diff){
        int lineDiff = getLineDiff();
        int offset = (diff * 15) + lineDiff + 4;
        if(offset > 245){
            super.setAddressDiff(diff);
            offset = lineDiff + 4;
        }
        if(offset < 0){
            offset = 0;
        }
        setFlagOffset(offset);
    }
    @Override
    int getLineDiff(){
        return (getFlagOffset() % 15) - 4;
    }
    void setLineDiff(int diff){
        int addressDiff = getAddressDiff();
        int offset = (addressDiff * 15) + diff + 4;
        if(offset > 245){
            DebugAdvanceLine advanceLine = getOrCreateAdvanceLine();
            int advance = diff - 4;
            advanceLine.setLineDiff(advance);
            offset = addressDiff * 15;
        }
        setFlagOffset(offset);
    }
    private int getMaxLineDiff(){
        if(getAddressDiff() == 16){
            return 1;
        }
        return 10;
    }
    @Override
    public int getLineNumber(){
        return super.getLineNumber();
    }

    @Override
    public InstructionLabelType getLabelType() {
        return InstructionLabelType.LINE;
    }

    @Override
    public void setLineNumber(int lineNumber) {
        setUpLineNumber(lineNumber);
        super.setLineNumber(lineNumber);
    }
    private int getPreviousLineNumber(){
        DebugSequence debugSequence = getDebugSequence();
        int index = getIndex();
        if(debugSequence == null || index == 0){
            return 0;
        }
        for(int i = index - 1; i >=0; i--){
            DebugElementBlock element = debugSequence.get(i);
            if(!(element instanceof DebugLineNumberBlock)){
                continue;
            }
            DebugLineNumberBlock lineNumber = (DebugLineNumberBlock) element;
            return lineNumber.getLineNumber();
        }
        return 0;
    }
    private void setUpLineNumber(int lineNumber) {
        int diff = lineNumber - getPreviousLineNumber();
        int max = getMaxLineDiff();
        if(diff >= -4 && diff <= max){
            setLineDiff(diff);
            return;
        }
        DebugSequence debugSequence = getDebugSequence();
        if(debugSequence == null){
            return;
        }
        if(getIndex() == 0){
            diff = 0;
            debugSequence.setLineStartInternal(lineNumber);
            setLineDiff(diff);
            return;
        }
        DebugAdvanceLine advanceLine = getOrCreateAdvanceLine();
        if(advanceLine == null){
            return;
        }
        setLineDiff(0);
        advanceLine.setLineDiff(diff);
    }
    private DebugAdvanceLine getOrCreateAdvanceLine(){
        DebugAdvanceLine advanceLine = getAdvanceLine();
        if(advanceLine != null){
            return advanceLine;
        }
        DebugSequence debugSequence = getDebugSequence();
        if(debugSequence != null){
            advanceLine = debugSequence.createAtPosition(DebugElementType.ADVANCE_LINE, getIndex());
        }
        return advanceLine;
    }
    private DebugAdvanceLine getAdvanceLine(){
        DebugSequence debugSequence = getDebugSequence();
        if(debugSequence != null){
            DebugElementBlock element = debugSequence.get(getIndex() - 1);
            if(element instanceof DebugAdvancePc){
                element = debugSequence.get(element.getIndex() - 1);
            }
            if(element instanceof DebugAdvanceLine){
                return (DebugAdvanceLine) element;
            }
        }
        return null;
    }
    @Override
    public boolean equalsLabel(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof DebugLineNumberBlock)){
            return false;
        }
        DebugLineNumberBlock debugLineNumberBlock = (DebugLineNumberBlock) obj;
        return getTargetAddress() == debugLineNumberBlock.getTargetAddress();
    }

    @Override
    public void fromSmali(Smali smali) {
        super.fromSmali(smali);
        SmaliLineNumber lineNumber = (SmaliLineNumber) smali;
        setLineNumber(lineNumber.getLineNumber());
    }

    @Override
    public void appendLabelName(SmaliWriter writer) throws IOException {
        if(isValid()) {
            int lineNum = getLineNumber();
            if(lineNum == -1){
                return;
            }
            getSmaliDirective().append(writer);
            writer.appendInteger(lineNum);
        }
    }
    @Override
    public DebugElementType<DebugLineNumberBlock> getElementType() {
        return DebugElementType.LINE_NUMBER;
    }

    @Override
    int compareDetailElement(DebugElementBlock element) {
        return CompareUtil.compare(getLineNumber(), element.getLineNumber());
    }

    @Override
    public int compareLabel(InstructionLabel label) {
        if (label == this) {
            return 0;
        }
        int i = super.compareLabel(label);
        if (i == 0) {
            DebugLineNumber lineNumber = (DebugLineNumber) label;
            i = CompareUtil.compare(getLineNumber(), lineNumber.getLineNumber());
        }
        return i;
    }

    @Override
    public int hashCode() {
        return DebugLineNumber.hashCodeOfDebugLineNumber(this);
    }

    @Override
    public boolean equals(Object obj) {
        return DebugLineNumber.equalsDebugLineNumber(this, obj);
    }

    @Override
    public String toString() {
        return DebugLineNumber.formatToString(this);
    }
}
