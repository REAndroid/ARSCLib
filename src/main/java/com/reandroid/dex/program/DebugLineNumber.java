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
package com.reandroid.dex.program;

import com.reandroid.dex.debug.DebugElement;

public interface DebugLineNumber extends DebugElement {

    int getLineNumber();

    static DebugLineNumber immutableOf(int lineNumber, int targetAddress) {
        return new ImmutableLineNumber(lineNumber, targetAddress);
    }
    static DebugLineNumber immutableOf(DebugLineNumber lineNumber) {
        if (lineNumber instanceof ImmutableLineNumber) {
            return lineNumber;
        }
        return new ImmutableLineNumber(lineNumber.getLineNumber(), lineNumber.getTargetAddress());
    }
    static String formatToString(DebugLineNumber lineNumber) {
        if (lineNumber == null) {
            return "# null line number";
        }
        return ".line " + lineNumber.getLineNumber();
    }
    static boolean equalsDebugLineNumber(DebugLineNumber lineNumber, Object obj) {
        if (lineNumber == obj) {
            return true;
        }
        if (!(obj instanceof DebugLineNumber) || lineNumber == null) {
            return false;
        }
        DebugLineNumber other = (DebugLineNumber) obj;
        return lineNumber.getLineNumber() == other.getLineNumber() &&
                lineNumber.getTargetAddress() == other.getTargetAddress();
    }
    static int hashCodeOfDebugLineNumber(DebugLineNumber lineNumber) {
        if (lineNumber == null) {
            return 0;
        }
        return (31 + lineNumber.getLineNumber()) * 31 + lineNumber.getTargetAddress();
    }

    class ImmutableLineNumber implements DebugLineNumber {

        private final int lineNumber;
        private final int targetAddress;

        public ImmutableLineNumber(int lineNumber, int targetAddress) {
            this.lineNumber = lineNumber;
            this.targetAddress = targetAddress;
        }
        @Override
        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public int getOwnerAddress() {
            return -1;
        }

        @Override
        public int getTargetAddress() {
            return targetAddress;
        }

        @Override
        public void setTargetAddress(int address) {

        }

        @Override
        public Instruction getTargetInstruction() {
            return null;
        }

        @Override
        public void setTargetInstruction(Instruction targetInstruction) {
        }

        @Override
        public InstructionLabelType getLabelType() {
            return InstructionLabelType.LINE;
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
}
