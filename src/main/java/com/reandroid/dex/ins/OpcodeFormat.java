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

import com.reandroid.dex.common.OperandType;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.sections.SectionType;

abstract class OpcodeFormat<T extends Ins> {

    private final RegisterFormat registerFormat;
    private final OperandType operandType;

    OpcodeFormat(RegisterFormat registerFormat, OperandType operandType) {
        this.registerFormat = registerFormat;
        this.operandType = operandType;
    }

    public RegisterFormat getRegisterFormat() {
        return registerFormat;
    }

    public OperandType getOperandType() {
        return operandType;
    }

    public SectionType<? extends IdItem> getSectionType() {
        return operandType.getSectionType();
    }

    public SectionType<? extends IdItem> getSectionType2() {
        return operandType.getSectionType2();
    }

    public abstract T newInstance(Opcode<?> opcode);

    static class Format10x extends OpcodeFormat<Ins10x> {
        Format10x() {
            super(RegisterFormat.NONE, OperandType.NONE);
        }

        @Override
        public Ins10x newInstance(Opcode<?> opcode) {
            return new Ins10x(opcode);
        }
    }

    static class Format11x extends OpcodeFormat<Ins11x> {
        Format11x() {
            super(RegisterFormat.READ8, OperandType.NONE);
        }

        Format11x(RegisterFormat registerFormat) {
            super(registerFormat, OperandType.NONE);
        }

        @Override
        public Ins11x newInstance(Opcode<?> opcode) {
            return new Ins11x(opcode);
        }
    }

    static class Format12x extends OpcodeFormat<Ins12x> {
        Format12x() {
            super(RegisterFormat.WRITE4_READ4, OperandType.NONE);
        }

        Format12x(RegisterFormat registerFormat) {
            super(registerFormat, OperandType.NONE);
        }

        @Override
        public Ins12x newInstance(Opcode<?> opcode) {
            return new Ins12x(opcode);
        }
    }

    static class Format20bc extends OpcodeFormat<Ins20bc> {
        Format20bc() {
            super(RegisterFormat.NONE, OperandType.NONE);
        }

        @Override
        public Ins20bc newInstance(Opcode<?> opcode) {
            return new Ins20bc(opcode);
        }
    }

    static class Format21c extends OpcodeFormat<Ins21c> {
        Format21c(OperandType operandType) {
            super(RegisterFormat.WRITE8, operandType);
        }

        Format21c(RegisterFormat registerFormat, OperandType operandType) {
            super(registerFormat, operandType);
        }

        @Override
        public Ins21c newInstance(Opcode<?> opcode) {
            return new Ins21c(opcode);
        }
    }

    static class FormatConstWideHigh16 extends OpcodeFormat<InsConstWideHigh16> {
        FormatConstWideHigh16() {
            super(RegisterFormat.WRITE8W, OperandType.HEX);
        }
        @Override
        public InsConstWideHigh16 newInstance(Opcode<?> opcode) {
            return new InsConstWideHigh16();
        }
    }

    static class FormatConstWide16 extends OpcodeFormat<InsConstWide16> {
        FormatConstWide16() {
            super(RegisterFormat.WRITE8W, OperandType.HEX);
        }

        @Override
        public InsConstWide16 newInstance(Opcode<?> opcode) {
            return new InsConstWide16();
        }
    }

    static class FormatConst16 extends OpcodeFormat<InsConst16> {

        FormatConst16() {
            super(RegisterFormat.WRITE8, OperandType.HEX);
        }

        @Override
        public InsConst16 newInstance(Opcode<?> opcode) {
            return new InsConst16();
        }
    }

    static class Format21t extends OpcodeFormat<Ins21t> {
        Format21t() {
            super(RegisterFormat.READ8, OperandType.LABEL);
        }

        @Override
        public Ins21t newInstance(Opcode<?> opcode) {
            return new Ins21t(opcode);
        }
    }

    static class Format22b extends OpcodeFormat<Ins22b> {
        Format22b() {
            super(RegisterFormat.WRITE8_READ8, OperandType.HEX);
        }

        @Override
        public Ins22b newInstance(Opcode<?> opcode) {
            return new Ins22b(opcode);
        }
    }

    static class Format22c extends OpcodeFormat<Ins22c> {
        Format22c(OperandType operandType) {
            super(RegisterFormat.WRITE4_READ4, operandType);
        }

        Format22c(RegisterFormat registerFormat, OperandType operandType) {
            super(registerFormat, operandType);
        }

        @Override
        public Ins22c newInstance(Opcode<?> opcode) {
            return new Ins22c(opcode);
        }
    }

    static class Format22cs extends OpcodeFormat<Ins22cs> {
        Format22cs(RegisterFormat registerFormat, OperandType operandType) {
            super(registerFormat, operandType);
        }

        Format22cs(OperandType operandType) {
            super(RegisterFormat.READ4_READ4, operandType);
        }

        @Override
        public Ins22cs newInstance(Opcode<?> opcode) {
            return new Ins22cs(opcode);
        }
    }

    static class Format22s extends OpcodeFormat<Ins22s> {
        Format22s() {
            super(RegisterFormat.WRITE4_READ4, OperandType.HEX);
        }

        @Override
        public Ins22s newInstance(Opcode<?> opcode) {
            return new Ins22s(opcode);
        }
    }

    static class Format22t extends OpcodeFormat<Ins22t> {
        Format22t() {
            super(RegisterFormat.READ4_READ4, OperandType.LABEL);
        }

        @Override
        public Ins22t newInstance(Opcode<?> opcode) {
            return new Ins22t(opcode);
        }
    }

    static class Format22x extends OpcodeFormat<Ins22x> {
        Format22x() {
            super(RegisterFormat.WRITE8_READ16, OperandType.NONE);
        }

        Format22x(RegisterFormat registerFormat) {
            super(registerFormat, OperandType.NONE);
        }

        @Override
        public Ins22x newInstance(Opcode<?> opcode) {
            return new Ins22x(opcode);
        }
    }

    static class Format23x extends OpcodeFormat<Ins23x> {
        Format23x() {
            super(RegisterFormat.WRITE8_READ8_READ8, OperandType.NONE);
        }

        Format23x(RegisterFormat registerFormat) {
            super(registerFormat, OperandType.NONE);
        }

        @Override
        public Ins23x newInstance(Opcode<?> opcode) {
            return new Ins23x(opcode);
        }
    }

    static class Format23xAput extends Format23x {
        Format23xAput() {
            super(RegisterFormat.READ8_READ8_READ8);
        }

        Format23xAput(RegisterFormat registerFormat) {
            super(registerFormat);
        }
    }

    static class FormatConstWide32 extends OpcodeFormat<InsConstWide32> {

        FormatConstWide32() {
            super(RegisterFormat.WRITE8W, OperandType.HEX);
        }

        @Override
        public InsConstWide32 newInstance(Opcode<?> opcode) {
            return new InsConstWide32();
        }
    }

    static class Format32x extends OpcodeFormat<Ins32x> {
        Format32x() {
            super(RegisterFormat.WRITE16_READ16, OperandType.NONE);
        }

        Format32x(RegisterFormat registerFormat) {
            super(registerFormat, OperandType.NONE);
        }

        @Override
        public Ins32x newInstance(Opcode<?> opcode) {
            return new Ins32x(opcode);
        }
    }

    static class Format35c extends OpcodeFormat<Ins35c> {
        Format35c(OperandType operandType) {
            super(RegisterFormat.OUT, operandType);
        }

        @Override
        public Ins35c newInstance(Opcode<?> opcode) {
            return new Ins35c(opcode);
        }
    }

    static class Format35mi extends OpcodeFormat<Ins35mi> {
        Format35mi(OperandType operandType) {
            super(RegisterFormat.WRITE4_READ4, operandType);
        }

        @Override
        public Ins35mi newInstance(Opcode<?> opcode) {
            return new Ins35mi(opcode);
        }
    }

    static class Format35ms extends OpcodeFormat<Ins35ms> {
        Format35ms(OperandType operandType) {
            super(RegisterFormat.OUT, operandType);
        }

        @Override
        public Ins35ms newInstance(Opcode<?> opcode) {
            return new Ins35ms(opcode);
        }
    }

    static class Format3rc extends OpcodeFormat<Ins3rc> {
        Format3rc(OperandType operandType) {
            super(RegisterFormat.OUT_RANGE, operandType);
        }

        @Override
        public Ins3rc newInstance(Opcode<?> opcode) {
            return new Ins3rc(opcode);
        }
    }

    static class Format3rmi extends OpcodeFormat<Ins3rmi> {
        Format3rmi(OperandType operandType) {
            super(RegisterFormat.OUT, operandType);
        }

        @Override
        public Ins3rmi newInstance(Opcode<?> opcode) {
            return new Ins3rmi(opcode);
        }
    }

    static class Format3rms extends OpcodeFormat<Ins3rms> {
        Format3rms(OperandType operandType) {
            super(RegisterFormat.OUT_RANGE, operandType);
        }

        @Override
        public Ins3rms newInstance(Opcode<?> opcode) {
            return new Ins3rms(opcode);
        }
    }

    static class Format45cc extends OpcodeFormat<Ins45cc> {
        Format45cc(OperandType operandType) {
            super(RegisterFormat.OUT, operandType);
        }

        @Override
        public Ins45cc newInstance(Opcode<?> opcode) {
            return new Ins45cc(opcode);
        }
    }

    static class Format4rcc extends OpcodeFormat<Ins4rcc> {
        Format4rcc(OperandType operandType) {
            super(RegisterFormat.OUT_RANGE, operandType);
        }

        @Override
        public Ins4rcc newInstance(Opcode<?> opcode) {
            return new Ins4rcc(opcode);
        }
    }

    static class FormatArrayData extends OpcodeFormat<InsArrayData> {
        FormatArrayData() {
            super(RegisterFormat.NONE, OperandType.DECIMAL);
        }

        @Override
        public InsArrayData newInstance(Opcode<?> opcode) {
            return new InsArrayData();
        }
    }

    static class FormatConst16High extends OpcodeFormat<InsConst16High> {
        FormatConst16High() {
            super(RegisterFormat.WRITE8, OperandType.HEX);
        }

        @Override
        public InsConst16High newInstance(Opcode<?> opcode) {
            return new InsConst16High();
        }
    }

    static class FormatConst4 extends OpcodeFormat<InsConst4> {
        FormatConst4() {
            super(RegisterFormat.WRITE4, OperandType.HEX);
        }

        @Override
        public InsConst4 newInstance(Opcode<?> opcode) {
            return new InsConst4();
        }
    }

    static class FormatConst extends OpcodeFormat<InsConst> {
        FormatConst() {
            super(RegisterFormat.WRITE8, OperandType.HEX);
        }

        @Override
        public InsConst newInstance(Opcode<?> opcode) {
            return new InsConst();
        }
    }

    static class FormatConstString extends OpcodeFormat<InsConstString> {
        FormatConstString() {
            super(RegisterFormat.WRITE8, OperandType.STRING);
        }

        @Override
        public InsConstString newInstance(Opcode<?> opcode) {
            return new InsConstString();
        }
    }

    static class FormatConstStringJumbo extends OpcodeFormat<InsConstStringJumbo> {
        FormatConstStringJumbo() {
            super(RegisterFormat.WRITE8, OperandType.STRING);
        }

        @Override
        public InsConstStringJumbo newInstance(Opcode<?> opcode) {
            return new InsConstStringJumbo();
        }
    }

    static class FormatConstWide extends OpcodeFormat<InsConstWide> {
        FormatConstWide() {
            super(RegisterFormat.WRITE8W, OperandType.HEX);
        }

        @Override
        public InsConstWide newInstance(Opcode<?> opcode) {
            return new InsConstWide();
        }
    }

    static class FormatFillArrayData extends OpcodeFormat<InsFillArrayData> {
        FormatFillArrayData() {
            super(RegisterFormat.READ8, OperandType.LABEL);
        }

        @Override
        public InsFillArrayData newInstance(Opcode<?> opcode) {
            return new InsFillArrayData();
        }
    }

    static class FormatGoto extends OpcodeFormat<InsGoto> {
        FormatGoto() {
            super(RegisterFormat.NONE, OperandType.LABEL);
        }
        @Override
        public InsGoto newInstance(Opcode<?> opcode) {
            return new InsGoto(opcode);
        }
    }
    static class FormatNop extends OpcodeFormat<InsNop> {
        FormatNop() {
            super(RegisterFormat.NONE, OperandType.NONE);
        }

        @Override
        public InsNop newInstance(Opcode<?> opcode) {
            return new InsNop();
        }
    }

    static class FormatPackedSwitch extends OpcodeFormat<InsPackedSwitch> {
        FormatPackedSwitch() {
            super(RegisterFormat.READ8, OperandType.LABEL);
        }

        @Override
        public InsPackedSwitch newInstance(Opcode<?> opcode) {
            return new InsPackedSwitch();
        }
    }

    static class FormatPackedSwitchData extends OpcodeFormat<InsPackedSwitchData> {
        FormatPackedSwitchData() {
            super(RegisterFormat.NONE, OperandType.HEX);
        }

        @Override
        public InsPackedSwitchData newInstance(Opcode<?> opcode) {
            return new InsPackedSwitchData();
        }
    }

    static class FormatSparseSwitch extends OpcodeFormat<InsSparseSwitch> {
        FormatSparseSwitch() {
            super(RegisterFormat.READ8, OperandType.LABEL);
        }

        @Override
        public InsSparseSwitch newInstance(Opcode<?> opcode) {
            return new InsSparseSwitch();
        }
    }

    static class FormatSparseSwitchData extends OpcodeFormat<InsSparseSwitchData> {
        FormatSparseSwitchData() {
            super(RegisterFormat.NONE, OperandType.NONE);
        }

        @Override
        public InsSparseSwitchData newInstance(Opcode<?> opcode) {
            return new InsSparseSwitchData();
        }
    }
}
