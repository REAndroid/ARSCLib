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

import com.reandroid.dex.data.InstructionList;

public interface SwitchEntry extends PayloadEntry, Label {
    @Override
    InsSwitchPayload<?> getPayload();

    default InsSwitch getInsSwitch() {
        InsSwitchPayload<?> payload = getPayload();
        if (payload != null) {
            return payload.getSwitch();
        }
        return null;
    }
    @Override
    default boolean isRemoved() {
        return getPayload().isRemoved();
    }

    default void updateTargetAddress() {
        Ins target = getTargetIns();
        setTargetAddress(target.getAddress());
    }

    default void addEquivalentIfEq(int constRegister) {
        InsSwitch insSwitch = getInsSwitch();
        InstructionList instructionList = insSwitch.getInstructionList();

        Ins targetIns = getTargetIns();

        Ins constNumberIns = (Ins) instructionList.createConstIntegerAt(
                insSwitch.getIndex() + 1,
                constRegister,
                get());


        Ins22t insIfEq = Opcode.IF_EQ.newInstance();
        insIfEq.setRegister(0, insSwitch.getRegister());
        insIfEq.setRegister(1, constRegister);
        insIfEq.setTargetIns(targetIns);
        instructionList.add(constNumberIns.getIndex() + 1, insIfEq);
        insIfEq.setTargetIns(targetIns);
    }

    default Ins findTargetIns() {
        InsBlockList insBlockList = getPayload().getInsBlockList();
        if (insBlockList != null) {
            return insBlockList.getAtAddress(getTargetAddress());
        }
        return null;
    }
}
