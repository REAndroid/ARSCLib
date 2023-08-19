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
package com.reandroid.dex.instruction;

import com.reandroid.arsc.item.ShortItem;

public class PayloadIns extends Instruction {
    public PayloadIns(int childesCount, Opcode<?> opcode) {
        super(childesCount + 1, opcode);
        ShortItem opcodeItem = new ShortItem();
        opcodeItem.set(opcode.getValue());
        addChild(0, opcodeItem);
    }
}