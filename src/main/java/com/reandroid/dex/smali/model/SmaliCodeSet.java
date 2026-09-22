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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class SmaliCodeSet extends SmaliSet<SmaliCode> {

    private int addressOffset;
    private SmaliNullInstruction nullInstruction;

    public SmaliCodeSet() {
        super();
    }

    public int getAddressOffset() {
        return addressOffset;
    }
    public void setAddressOffset(int addressOffset) {
        this.addressOffset = addressOffset;
    }

    public void updateAddresses() {
        int address = getAddressOffset();
        int size = size();
        for (int i = 0; i < size; i++) {
            SmaliCode code = getSuper(i);
            code.setIndex(i);
            if (code instanceof SmaliInstruction) {
                SmaliInstruction ins = (SmaliInstruction) code;
                ins.setAddress(address);
                address += ins.getCodeUnits();
            }
        }
        SmaliInstruction instruction = getNullInstruction();
        if (instruction != null) {
            instruction.setAddress(address);
        }
    }
    public Iterator<SmaliInstruction> getInstructions(SmaliLabel label) {
        if (label != null) {
            return FilterIterator.of(getInstructions(),
                    smaliInstruction -> smaliInstruction.hasLabelOperand(label));
        }
        return EmptyIterator.of();
    }
    public Iterator<SmaliInstruction> getInstructions() {
        return iterator(SmaliInstruction.class);
    }
    public Iterator<SmaliCodeTryItem> getTryItems() {
        return iterator(SmaliCodeTryItem.class);
    }
    public Iterator<SmaliDebug> getDebugs() {
        return iterator(SmaliDebug.class);
    }
    public Iterator<SmaliDebugElement> getDebugElements() {
        return iterator(SmaliDebugElement.class);
    }
    public Iterator<SmaliMethodParameter> getMethodParameters() {
        return iterator(SmaliMethodParameter.class);
    }
    public void clearInstructions() {
        removeInstances(SmaliDebug.class);
    }
    public void clearDebugs() {
        removeInstances(SmaliDebug.class);
    }

    public SmaliInstruction getNullInstruction() {
        SmaliNullInstruction nullInstruction = this.nullInstruction;
        if (needsNullInstruction()) {
            if (nullInstruction == null) {
                nullInstruction = new SmaliNullInstruction();
                nullInstruction.setParent(this);
                this.nullInstruction = nullInstruction;
            }
        } else {
            if (nullInstruction != null) {
                nullInstruction.setParent(null);
                nullInstruction = null;
                this.nullInstruction = null;
            }
        }
        return nullInstruction;
    }
    private boolean needsNullInstruction() {
        int size = size();
        if (size != 0) {
            return !(getSuper(size - 1) instanceof SmaliInstruction);
        }
        return false;
    }

    public SmaliInstruction newInstruction(Opcode<?> opcode) {
        SmaliInstruction instruction = createInstruction(opcode);
        add(instruction);
        return instruction;
    }
    public SmaliInstruction getNextInstruction(int index) {
        if (index < 0) {
            return null;
        }
        int size = size();
        for (int i = index; i < size; i++) {
            SmaliCode code = get(i);
            if (code instanceof SmaliInstruction) {
                return (SmaliInstruction) code;
            }
        }
        if (index == size) {
            return getNullInstruction();
        }
        return null;
    }
    public SmaliLabel newLabelAtIndex(int index, InstructionLabelType type) {
        return newLabelAtIndex(index, type.prefix());
    }
    public SmaliLabel newLabelAtIndex(int index, String prefix) {
        SmaliLabel label = new SmaliLabel();
        label.setLabelName(generateUniqueLabelName(prefix));
        add(index, label);
        return label;
    }
    public String generateUniqueLabelName(String prefix) {
        if (prefix.charAt(0) != ':') {
            prefix = ":" + prefix;
        }
        Set<String> uniqueSet = CollectionUtil.toHashSet(
                ComputeIterator.of(iterator(SmaliLabel.class), SmaliLabel::getLabelName));
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            String name = HexUtil.toHex(prefix, i, 1);
            if (!uniqueSet.contains(name)) {
                return name;
            }
            i ++;
        }
        throw new IllegalStateException("Can not generate unique name, tried: " + i);
    }
    @Override
    public SmaliCode get(int i) {
        if (i == size()) {
            return this.getNullInstruction();
        }
        return getSuper(i);
    }
    private SmaliCode getSuper(int i) {
        return super.get(i);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if (isEmpty()) {
            return;
        }
        writer.newLine();
        writer.appendAll(iterator());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        super.parse(reader);
        updateAddresses();
    }
    @Override
    SmaliCode createNext(SmaliReader reader) {
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive != null) {
            return createFor(directive);
        }
        reader.skipWhitespaces();
        if (reader.get() == ':') {
            return new SmaliLabel();
        }
        Opcode<?> opcode = Opcode.parseSmali(reader, false);
        if (opcode != null) {
            return new SmaliInstruction();
        }
        return null;
    }
    private static SmaliCode createFor(SmaliDirective directive) {
        if (directive == SmaliDirective.LINE) {
            return new SmaliLineNumber();
        }
        if (directive == SmaliDirective.CATCH || directive == SmaliDirective.CATCH_ALL) {
            return new SmaliCodeTryItem();
        }
        if (directive == SmaliDirective.PARAM) {
            return new SmaliMethodParameter();
        }
        if (directive == SmaliDirective.END_LOCAL) {
            return new SmaliDebugEndLocal();
        }
        if (directive == SmaliDirective.LOCAL) {
            return new SmaliDebugLocal();
        }
        if (directive == SmaliDirective.RESTART_LOCAL) {
            return new SmaliDebugRestartLocal();
        }
        if (directive == SmaliDirective.ARRAY_DATA) {
            return new SmaliPayloadArray();
        }
        if (directive == SmaliDirective.PACKED_SWITCH) {
            return new SmaliPayloadPackedSwitch();
        }
        if (directive == SmaliDirective.SPARSE_SWITCH) {
            return new SmaliPayloadSparseSwitch();
        }
        if (directive == SmaliDirective.PROLOGUE) {
            return new SmaliDebugPrologue();
        }
        if (directive == SmaliDirective.EPILOGUE) {
            return new SmaliDebugEpilogue();
        }
        return null;
    }

    public static SmaliInstruction createInstruction(Opcode<?> opcode) {
        SmaliInstruction instruction;
        if (opcode == Opcode.ARRAY_PAYLOAD) {
            instruction = new SmaliPayloadArray();
        } else if (opcode == Opcode.PACKED_SWITCH_PAYLOAD) {
            instruction = new SmaliPayloadPackedSwitch();
        } else if (opcode == Opcode.SPARSE_SWITCH_PAYLOAD) {
            instruction = new SmaliPayloadSparseSwitch();
        } else {
            instruction = new SmaliInstruction(opcode);
        }
        return instruction;
    }
}
