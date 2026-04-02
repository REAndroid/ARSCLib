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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.data.MethodDef;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;

class InsConstCommentHelper {

    public static String getCommentForConstNumber(SizeXIns sizeXIns) {
        if (!(sizeXIns instanceof ConstNumber)) {
            return null;
        }
        long data = sizeXIns.getDataAsLong();
        if (data == 0) {
            return null;
        }
        TypeKey typeKey = findDataTypeFromNextInsLazy(sizeXIns);
        if (typeKey == null) {
            return null;
        }
        String comment = null;
        if (TypeKey.TYPE_D.equals(typeKey)) {
            comment = Double.toString(Double.longBitsToDouble(data));
        } else if (TypeKey.TYPE_F.equals(typeKey)) {
            comment = Float.intBitsToFloat((int) data) + "f";
        } else if (TypeKey.TYPE_C.equals(typeKey)) {
            comment = safeQuotedChar((char) data);
        }
        return comment;
    }
    private static String safeQuotedChar(char c) {
        if (c == '\n') {
            return "'\\n'";
        } else if (c == '\r') {
            return "'\\r'";
        } else if (c == '\t') {
            return "'\\t'";
        } else if (c == '\b') {
            return "'\\b'";
        } else if (c == '\f') {
            return "'\\f'";
        } else {
            return DexUtils.quoteChar(c);
        }
    }
    private static TypeKey findDataTypeFromNextInsLazy(SizeXIns constNumberIns) {
        InstructionList instructionList = constNumberIns.getInstructionList();
        if (instructionList == null) {
            return null;
        }
        Ins next = instructionList.get(constNumberIns.getIndex() + 1);
        if (!(next instanceof RegistersSet) || !(next instanceof SizeXIns)) {
            return null;
        }
        int register = ((RegistersSet) constNumberIns).getRegister();
        if (indexOfRegister(register, (RegistersSet) next) < 0) {
            return null;
        }
        Opcode<?> opcode = next.getOpcode();
        String opcodeName = opcode.getName();
        if (opcodeName.contains("float")) {
            return TypeKey.TYPE_F;
        }
        if (opcodeName.contains("double")) {
            return TypeKey.TYPE_D;
        }
        SizeXIns nextSizeXIns = (SizeXIns) next;
        TypeKey typeKey = findDataTypeFromFieldInsLazy(nextSizeXIns);
        if (typeKey == null) {
            typeKey = findDataTypeFromInvokeInsLazy(register, nextSizeXIns);
        }
        if (typeKey == null) {
            typeKey = findDataTypeFromReturnInsLazy(nextSizeXIns);
        }
        return typeKey;
    }
    private static TypeKey findDataTypeFromReturnInsLazy(SizeXIns sizeXIns) {
        Opcode<?> opcode = sizeXIns.getOpcode();
        if (!opcode.isReturn()) {
            return null;
        }
        MethodDef methodDef = sizeXIns.getMethodDef();
        if (methodDef == null) {
            return null;
        }
        MethodKey methodKey = methodDef.getKey();
        if (methodKey == null) {
            return null;
        }
        return methodKey.getReturnType();
    }
    private static TypeKey findDataTypeFromFieldInsLazy(SizeXIns sizeXIns) {
        Key key = sizeXIns.getKey();
        if (!(key instanceof FieldKey)) {
            return null;
        }
        if (!sizeXIns.getOpcode().isFieldPut()) {
            return null;
        }
        return ((FieldKey) key).getType();
    }
    private static TypeKey findDataTypeFromInvokeInsLazy(int register, SizeXIns sizeXIns) {
        Key key = sizeXIns.getKey();
        if (!(key instanceof MethodKey)) {
            return null;
        }
        MethodKey methodKey = (MethodKey) key;
        int registerIndex = indexOfRegister(register, (RegistersSet) sizeXIns);
        int index = methodKey.getParameterIndex(registerIndex);
        return methodKey.getParameter(index);
    }
    private static int indexOfRegister(int register, RegistersSet registersSet) {
        int count = registersSet.getRegistersCount();
        for (int i = 0; i < count; i++) {
            if (register == registersSet.getRegister(i)) {
                return i;
            }
        }
        return -1;
    }
}
