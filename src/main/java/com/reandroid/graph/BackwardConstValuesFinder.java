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
package com.reandroid.graph;

import com.reandroid.dex.common.RegisterType;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexInstruction;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BackwardConstValuesFinder {

    private List<DexInstruction> mResults;
    private final Set<Integer> mDiscoveredAddresses;

    public BackwardConstValuesFinder() {
        this.mDiscoveredAddresses = new HashSet<>();
    }

    public Object[] findConstValues(int parameterIndex, DexInstruction instruction) {
        TypeKey valueType;
        if (instruction.isMethodInvoke()) {
            valueType = instruction.getKeyAsMethod().getParameter(parameterIndex);
        } else if (instruction.isFieldOp()) {
            valueType = instruction.getKeyAsField().getType();;
        } else if (instruction.is(Opcode.FILLED_NEW_ARRAY) || instruction.is(Opcode.FILLED_NEW_ARRAY_RANGE)) {
            valueType = ((TypeKey) instruction.getKey()).setArrayDimension(0);
        } else if (instruction.isArrayOp()) {
            if (parameterIndex == 2) {
                valueType = TypeKey.TYPE_I;
            } else {
                valueType = Opcode.typeOfArrayOperation(instruction.getOpcode());
                if (parameterIndex == 1 && valueType != null) {
                    valueType = valueType.setArrayDimension(1);
                }
            }
        } else {
            // TODO: consider payloads
            valueType = null;
        }
        List<DexInstruction> results = findForParameter(instruction, parameterIndex);
        int size = results.size();
        Object[] values = new Object[size];
        for (int i = 0; i < size; i++) {
            values[i] = results.get(i).getAsConstValue(valueType);
        }
        return values;
    }
    public List<DexInstruction> findForParameter(DexInstruction instruction, int parameterIndex) {
        if (parameterIndex < 0 || parameterIndex > instruction.getRegistersCount()) {
            throw new IndexOutOfBoundsException("Invalid parameter index: " + parameterIndex +
                    ", expecting: 0 - " + instruction.getRegistersCount());
        }
        int registerIndex = parameterIndex;
        if (instruction.isMethodInvoke()) {
            MethodKey methodKey = instruction.getKeyAsMethod();
            registerIndex = methodKey.getRegister(parameterIndex);
            if (!instruction.isMethodInvokeStatic()) {
                registerIndex = registerIndex + 1;
            }
        }
        int register = instruction.getRegister(registerIndex);
        if (!instruction.usesRegister(register, RegisterType.READ)) {
            throw new IllegalArgumentException("Instruction doesn't read register at register index: "
                    + parameterIndex);
        }
        return findForRegister(instruction, register);
    }
    public List<DexInstruction> findForRegister(DexInstruction instruction, int register) {
        reset();
        scanPrevious(instruction, register, false);
        List<DexInstruction> results = getResults();
        reset();
        return results;
    }

    private void scanPrevious(DexInstruction instruction, int register, boolean ignoreGoto) {
        if (!addDiscovered(instruction)) {
            return;
        }
        if (instruction.isReturn() || instruction.is(Opcode.THROW)) {
            return;
        }
        if (!ignoreGoto && instruction.isGoto()) {
            return;
        }
        if (instruction.usesRegister(register, RegisterType.WRITE)) {
            if (instruction.isMove()) {
                register = instruction.getRegister(1);
            } else {
                addResult(instruction);
                return;
            }
        }

        Iterator<DexInstruction> iterator = getTryForCatchPoint(instruction);
        while (iterator.hasNext()) {
            scanPrevious(iterator.next(), register, false);
        }
        iterator = instruction.getTargetingInstructions();
        while (iterator.hasNext()) {
            scanPrevious(iterator.next(), register, true);
        }
        DexInstruction sw = instruction.getTargetingSwitch();
        if (sw != null) {
            scanPrevious(sw, register, false);
            return;
        }
        scanPrevious(instruction.getPrevious(), register, false);
    }
    private Iterator<DexInstruction> getTryForCatchPoint(DexInstruction instruction) {
        return ComputeIterator.of(instruction.getTargetingCatches(),
                dexCatch -> dexCatch.getDexTry().getFirst());
    }

    private void reset() {
        mResults = null;
        mDiscoveredAddresses.clear();
    }
    private List<DexInstruction> getResults() {
        List<DexInstruction> results = mResults;
        if (results == null) {
            results = EmptyList.of();
        }
        return results;
    }

    private void addResult(DexInstruction instruction) {
        if (instruction != null) {
            List<DexInstruction> results = mResults;
            if (results == null) {
                results = new ArrayCollection<>(2);
                this.mResults = results;
            }
            results.add(instruction);
        }
    }
    private boolean addDiscovered(DexInstruction instruction) {
        if (instruction != null) {
            return mDiscoveredAddresses.add(instruction.getAddress());
        }
        return false;
    }
}
