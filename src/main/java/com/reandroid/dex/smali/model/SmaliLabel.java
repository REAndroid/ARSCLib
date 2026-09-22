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

import com.reandroid.dex.base.DexException;
import com.reandroid.dex.program.InstructionLabel;
import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class SmaliLabel extends SmaliCode implements InstructionLabel {

    private String labelName;
    private InstructionLabelType labelType;

    public SmaliLabel() {
        super();
        this.labelType = InstructionLabelType.DEBUG;
    }

    @Override
    public String getLabelName() {
        return labelName;
    }
    public void setLabelName(String labelName) {
        if (labelName.charAt(0) != ':') {
            labelName = ":" + labelName;
        }
        setLabelNameInternal(labelName);
    }
    private void setLabelNameInternal(String labelName) {
        this.labelName = labelName;
    }

    public SmaliLabel getDestinationLabel() {
        SmaliCodeSet codeSet = getCodeSet();
        if (codeSet == null) {
            return null;
        }
        if (codeSet == this.getParent()) {
            return this;
        }
        int i = codeSet.indexOf(this);
        if (i < 0) {
            return null;
        }
        return (SmaliLabel) codeSet.get(i);
    }
    public int getIntegerData() {
        int address = getTargetAddress();
        if (address == -1) {
            throw new DexException("Missing target label '" + getLabelName() + "'" + buildOrigin());
        }
        return address;
    }
    public int getTargetAddress() {
        SmaliInstruction instruction = getTargetInstruction();
        if (instruction != null) {
            return instruction.getAddress();
        }
        return -1;
    }
    @Override
    public void setTargetAddress(int address) {
        // TODO
        throw new RuntimeException("Method not implemented");
    }
    @Override
    public SmaliInstruction getTargetInstruction() {
        SmaliCodeSet codeSet = getCodeSet();
        if (codeSet != null) {
            return codeSet.getNextInstruction(codeSet.indexOf(this));
        }
        return null;
    }

    @Override
    public int getOwnerAddress() {
        SmaliInstruction owner = getOwnerInstruction();
        if (owner != null) {
            return owner.getAddress();
        }
        return -1;
    }
    @Override
    public SmaliInstruction getOwnerInstruction() {
        Smali parent = getParent();
        if (parent == null || (parent instanceof SmaliCodeSet)) {
            return null;
        }
        if (parent instanceof SmaliInstruction) {
            return (SmaliInstruction) parent;
        }
        return parent.getParentInstance(SmaliInstruction.class);
    }

    public boolean isDestinationLabel() {
        Smali parent = getParent();
        return getParent() instanceof SmaliCodeSet;
    }
    public boolean isSourceLabel() {
        Smali parent = getParent();
        return parent != null && !(parent instanceof SmaliCodeSet);
    }
    @Override
    public InstructionLabelType getLabelType() {
        // TODO: implement properly
        return labelType;
    }
    public void setLabelType(InstructionLabelType labelType) {
        this.labelType = labelType;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendLabelName(getLabelName());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        setOrigin(reader.getCurrentOrigin());
        SmaliParseException.expect(reader, ':');
        reader.skip(-1);
        int i1 = reader.indexOfWhiteSpaceOrComment();
        int i2 = reader.indexOfBeforeLineEnd('}');
        int i;
        if (i2 >= 0 && i2 < i1) {
            i = i2;
        } else {
            i = i1;
        }
        int length = i - reader.position();
        setLabelNameInternal(reader.readString(length));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SmaliLabel)) {
            return false;
        }
        SmaliLabel other = (SmaliLabel) obj;
        return ObjectsUtil.equals(getLabelName(), other.getLabelName());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getLabelName());
    }
}
