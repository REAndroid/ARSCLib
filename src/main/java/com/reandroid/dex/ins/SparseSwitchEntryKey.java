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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.dex.program.Instruction;

public class SparseSwitchEntryKey extends IntegerItem {

    private Instruction targetInstruction;

    public SparseSwitchEntryKey() {
        super();
    }

    public Instruction getTargetInstruction() {
        return targetInstruction;
    }
    public void setTargetInstruction(Instruction targetIns) {
        this.targetInstruction = targetIns;
    }

    public static final Creator<SparseSwitchEntryKey> CREATOR = SparseSwitchEntryKey::new;
}
