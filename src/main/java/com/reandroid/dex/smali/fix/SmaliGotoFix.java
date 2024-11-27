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
package com.reandroid.dex.smali.fix;

import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliMethod;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.util.List;

public class SmaliGotoFix {

    private final SmaliMethod smaliMethod;

    public SmaliGotoFix(SmaliMethod smaliMethod) {
        this.smaliMethod = smaliMethod;
    }

    public void apply() {
        List<SmaliInstruction> gotoList = CollectionUtil.toList(
                FilterIterator.of(smaliMethod.getInstructions(), instruction -> {
            Opcode<?> opcode = instruction.getOpcode();
            return opcode == Opcode.GOTO || opcode == Opcode.GOTO_16;
        }));
        if (gotoList.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (SmaliInstruction instruction : gotoList) {
            if (fix(instruction)) {
                changed = true;
            }
        }
        if (changed) {
            smaliMethod.getCodeSet().updateAddresses();
        }
    }
    private boolean fix(SmaliInstruction instruction) {
        Opcode<?> opcode = getReplacement(instruction);
        if (opcode != null) {
            instruction.replaceOpcode(opcode);
            return true;
        }
        return false;
    }
    private Opcode<?> getReplacement(SmaliInstruction instruction) {
        long data = instruction.getDataAsLong();
        Opcode<?> replacement = null;
        if (data < Byte.MIN_VALUE || data > Byte.MAX_VALUE) {
            replacement = Opcode.GOTO_16;
            if (data < Short.MIN_VALUE || data > Short.MAX_VALUE) {
                replacement = Opcode.GOTO_32;
            }
        }
        return replacement;
    }
}
