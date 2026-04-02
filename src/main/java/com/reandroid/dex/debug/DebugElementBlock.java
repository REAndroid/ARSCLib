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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.base.DexException;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.program.Instruction;
import com.reandroid.dex.program.InstructionLabel;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliDebugElement;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public abstract class DebugElementBlock extends FixedDexContainerWithTool implements
        DebugElement {
    
    private final ByteItem elementType;
    private int address;
    private int lineNumber;
    private Instruction targetIns;

    DebugElementBlock(int childesCount, int flag) {
        super(childesCount + 1);

        this.elementType = new ByteItem();
        this.elementType.set((byte) flag);

        addChild(0, elementType);
    }
    DebugElementBlock(int childesCount, DebugElementType<?> elementType) {
        this(childesCount, elementType.getFlag());
    }
    DebugElementBlock(DebugElementType<?> elementType) {
        this(0, elementType.getFlag());
    }

    @Override
    public Instruction getTargetInstruction() {
        return targetIns;
    }
    @Override
    public void setTargetInstruction(Instruction targetIns) {
        if (targetIns != this.targetIns) {
            this.targetIns = targetIns;
            if (targetIns != null) {
                targetIns.addReferencingLabel(this);
            }
        }
    }

    public void removeSelf() {
        DebugSequence debugSequence = getDebugSequence();
        if (debugSequence != null) {
            debugSequence.remove(this);
        }
    }
    public boolean isValid() {
        return !isRemoved();
    }
    public boolean isVisible() {
        return isValid();
    }

    int getAddressDiff() {
        return 0;
    }
    void setAddressDiff(int diff) {
        if (diff == 0) {
            return;
        }
        DebugAdvancePc advancePc = getOrCreateDebugAdvancePc();
        if (advancePc != null) {
            advancePc.setAddressDiff(diff);
        }
    }
    int getLineDiff() {
        return 0;
    }
    void setLineDiff(int diff) {
    }

    public boolean isRemoved() {
        return getDebugSequence() == null;
    }
    DebugSequence getDebugSequence() {
        DebugSequence debugSequence = getParent(DebugSequence.class);
        if (debugSequence != null && debugSequence.isRemoved()) {
            return null;
        }
        return debugSequence;
    }

    @Override
    public int getTargetAddress() {
        return address;
    }
    @Override
    public void setTargetAddress(int address) {
        DebugElementBlock element = this;
        while (element.updateTargetAddress(address)) {
            element = element.getNext();
            if (element == null) {
                return;
            }
            address = address + element.getAddressDiff();
        }
    }
    private boolean updateTargetAddress(int address) {
        if (address == getTargetAddress()) {
            return false;
        }
        DebugElementBlock previous = getPrevious();
        int diff;
        if (previous == null) {
            diff = address;
        } else {
            diff = address - previous.getTargetAddress();
            if (diff < 0) {
                diff = 0;
            }
        }
        setAddressDiff(diff);
        this.address = address;
        return true;
    }
    private DebugElementBlock getPrevious() {
        int index = getIndex();
        if (index <= 0) {
            return null;
        }
        DebugSequence sequence = getDebugSequence();
        if (sequence != null) {
            return sequence.get(index - 1);
        }
        return null;
    }
    private DebugElementBlock getNext() {
        int index = getIndex();
        if (index < 0) {
            return null;
        }
        DebugSequence sequence = getDebugSequence();
        if (sequence != null) {
            return sequence.get(index + 1);
        }
        return null;
    }
    private DebugAdvancePc getOrCreateDebugAdvancePc() {
        DebugAdvancePc advancePc = getDebugAdvancePc();
        if (advancePc != null) {
            return advancePc;
        }
        DebugSequence debugSequence = getDebugSequence();
        if (debugSequence != null) {
            advancePc = debugSequence.createAtPosition(DebugElementType.ADVANCE_PC, getIndex());
        }
        return advancePc;
    }
    private DebugAdvancePc getDebugAdvancePc() {
        DebugSequence debugSequence = getDebugSequence();
        if (debugSequence != null) {
            DebugElementBlock element = debugSequence.get(getIndex() - 1);
            if (element instanceof DebugAdvanceLine) {
                element = debugSequence.get(element.getIndex() - 1);
            }
            if (element instanceof DebugAdvancePc) {
                return (DebugAdvancePc) element;
            }
        }
        return null;
    }
    int getLineNumber() {
        return lineNumber;
    }
    void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    int getFlag() {
        int flag = elementType.get();
        if (flag > 0x0A) {
            flag = 0x0A;
        }
        return flag;
    }
    int getFlagOffset() {
        int offset = elementType.get();
        if (offset < 0x0A) {
            return 0;
        }
        return offset - 0x0A;
    }
    void setFlagOffset(int offset) {
        int flag = getFlag();
        if (flag < 0x0A) {
            if (offset == 0) {
                return;
            }
            throw new IllegalArgumentException("Can not set offset for: " + getElementType());
        }
        if (offset < 0 || offset > 0xF5) {
            throw new DexException("Value out of range should be [0 - 245]: " + offset + ", prev = " + getFlagOffset());
        }
        int value = flag + offset;
        elementType.set((byte) value);
    }
    public abstract DebugElementType<?> getElementType();
    public SmaliDirective getSmaliDirective() {
        return getElementType().getSmaliDirective();
    }
    void cacheValues(DebugSequence debugSequence, DebugElementBlock previous) {
        int line;
        int address;
        if (previous == null) {
            address = 0;
            line = debugSequence.getLineStart();
        } else {
            address = previous.getTargetAddress();
            line = previous.getLineNumber();
        }
        address += getAddressDiff();
        line += getLineDiff();
        this.address = address;
        this.lineNumber = line;
    }
    void updateValues(DebugSequence debugSequence, DebugElementBlock previous) {
        if (previous == this) {
            return;
        }
        if (previous != null && previous.getParent() == null) {
            return;
        }
        int line;
        int address;
        if (previous == null) {
            address = 0;
            line = debugSequence.getLineStart();
        } else {
            address = previous.getTargetAddress();
            line = previous.getLineNumber();
        }
        int addressDiff = getTargetAddress() - address;
        int lineDiff = getLineNumber() - line;
        setAddressDiff(addressDiff);
        setLineDiff(lineDiff);
    }
    void onPreRemove(DebugSequence debugSequence) {
        transferLineOffset(debugSequence);
    }
    private void transferLineOffset(DebugSequence debugSequence) {
        int diff = getLineDiff();
        if (diff == 0) {
            return;
        }
        DebugElementBlock prev = debugSequence.get(getIndex() - 1);
        if (prev == null) {
            debugSequence.setLineStart(debugSequence.getLineStart() + diff);
            return;
        }
        int available = 245 - prev.getLineDiff();
        if (available > 0) {
            if (diff > available) {
                prev.setLineDiff(prev.getLineDiff() + available);
                diff = diff - available;
            } else {
                prev.setLineDiff(prev.getLineDiff() + diff);
                diff = 0;
            }
        }
        if (diff == 0) {
            return;
        }
        DebugElementBlock next = debugSequence.get(getIndex() + 1);
        if (next == null) {
            return;
        }
        available = 245 - next.getLineDiff();
        if (available > 0) {
            if (diff > available) {
                next.setLineDiff(prev.getLineDiff() + available);
            } else {
                next.setLineDiff(prev.getLineDiff() + diff);
            }
        }

    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.nonCheckRead(reader);
    }

    @Override
    public InstructionLabelType getLabelType() {
        return InstructionLabelType.DEBUG;
    }

    @Override
    public void appendLabelName(SmaliWriter writer) throws IOException {
        if (isValid()) {
            getSmaliDirective().append(writer);
        }
    }
    @Override
    public boolean equalsLabel(Object obj) {
        return obj == this;
    }

    @Override
    public void updateTarget() {
    }
    @Override
    public int getOwnerAddress() {
        return -1;
    }

    @Override
    public int compareLabel(InstructionLabel other) {
        if (other == this) {
            return 0;
        }
        if (!(other instanceof DebugElementBlock)) {
            return InstructionLabel.compareLabels(this, other);
        }
        DebugElementBlock element = (DebugElementBlock) other;
        return CompareUtil.compare(getIndex(), element.getIndex());
    }

    public Iterator<IdItem> usedIds() {
        return EmptyIterator.of();
    }
    public void merge(DebugElementBlock element) {
        this.elementType.set(element.elementType.getByte());
    }
    public void fromSmali(Smali smali) {
        setTargetAddress(((SmaliDebugElement) smali).getTargetAddress());
    }

    public int compareElement(DebugElementBlock element) {
        int i = CompareUtil.compare(getFlag(), element.getFlag());
        if (i != 0) {
            return i;
        }
        i = CompareUtil.compare(getTargetAddress(), element.getTargetAddress());
        if (i != 0) {
            return i;
        }
        return compareDetailElement(element);
    }
    int compareDetailElement(DebugElementBlock element) {
        return 0;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugElementBlock element = (DebugElementBlock) obj;
        return elementType.getByte() == element.elementType.getByte();
    }
    @Override
    public int hashCode() {
        return elementType.getByte();
    }

    @Override
    public String toString() {
        return "Type = " + getElementType();
    }
}
