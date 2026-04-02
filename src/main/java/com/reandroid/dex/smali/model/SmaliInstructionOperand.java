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

import com.reandroid.dex.common.OperandType;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.key.CallSiteKey;
import com.reandroid.dex.key.DualKeyReference;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.key.MethodHandleKey;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public abstract class SmaliInstructionOperand extends Smali {

    public SmaliInstructionOperand() {
        super();
    }

    public abstract long getValueAsLong();
    public abstract OperandType getOperandType();
    @Override
    public abstract void append(SmaliWriter writer) throws IOException;

    @Override
    public abstract void parse(SmaliReader reader) throws IOException;
    public abstract void parse(Opcode<?> opcode, SmaliReader reader) throws IOException;

    public static class SmaliLabelOperand extends SmaliInstructionOperand {

        private final SmaliLabel label;

        public SmaliLabelOperand() {
            super();
            this.label = new SmaliLabel();
            this.label.setParent(this);
        }

        public SmaliLabel getLabel() {
            return label;
        }

        @Override
        public long getValueAsLong() {
            return getLabel().getIntegerData();
        }

        @Override
        public OperandType getOperandType() {
            return OperandType.LABEL;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            getLabel().append(writer);
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            getLabel().parse(reader);
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            getLabel().parse(reader);
        }
    }
    public static class SmaliHexOperand extends SmaliInstructionOperand {

        private SmaliValueNumber<?> valueNumber;

        public SmaliHexOperand() {
            super();
            valueNumber = new SmaliValueInteger();
        }

        public void setNumber(Number number) {
            setNumberValue(SmaliValueNumber.createFor(number));
        }

        public SmaliValueNumber<?> getValueNumber() {
            return valueNumber;
        }
        public void setNumberValue(SmaliValueNumber<?> valueNumber) {
            this.valueNumber = valueNumber;
            if (valueNumber != null) {
                valueNumber.setParent(this);
            }
        }

        @Override
        public long getValueAsLong() {
            return getValueNumber().getValueAsLong();
        }

        @Override
        public OperandType getOperandType() {
            return OperandType.HEX;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendOptional(getValueNumber());
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            parse(null, reader);
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            reader.skipSpaces();
            int position = reader.position();
            SmaliValueNumber<?> value = SmaliValueNumber.createNumber(reader);
            setNumberValue(value);
            value.parse(reader);
            validate(opcode, reader, position);
        }
        private void validate(Opcode<?> opcode, SmaliReader reader, int position) throws IOException {
            if (opcode == null || opcode == Opcode.CONST_WIDE) {
                return;
            }
            long value = getValueAsLong();
            if (value >= -0x8 && value <= 0x7) {
                return;
            }
            if (opcode == Opcode.CONST_4) {
                throw new IOException(reader.getOrigin(position) + " Invalid literal value: " + value + ". Must be between -8 and 7, inclusive.");
            } else if (opcode == Opcode.CONST_16 || opcode == Opcode.CONST_WIDE_16) {
                if ((value & 0xffffffffffff0000L) != 0) {
                    if (value < -0x8000 || value > 0x7fff) {
                        throw new IOException(reader.getOrigin(position) + " " + value + " cannot fit into a short");
                    }
                }
            } else if (opcode == Opcode.CONST_HIGH16) {
                if ((value & 0xffffL) != 0) {
                    throw new IOException(reader.getOrigin(position) + "Invalid literal value: "
                            + value + ". Low 16 bits must be zeroed out");
                }
                if (value < -0x80000000 || value > 0x7fffffff) {
                    throw new IOException(reader.getOrigin(position) + " " + value + " cannot fit into an int");
                }
            } else if (opcode == Opcode.CONST_WIDE_HIGH16) {
                if ((value & 0xffffffffffffL) != 0) {
                    throw new IOException(reader.getOrigin(position) + " Invalid literal value: "
                            + value + ". Low 48 bits must be zeroed out");
                }
            } else if (opcode == Opcode.CONST || opcode == Opcode.CONST_WIDE_32) {
                if (value < -0x80000000 || value > 0x7fffffff) {
                    throw new IOException(reader.getOrigin(position) + " " + value + " cannot fit into an int");
                }
            }
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SmaliHexOperand other = (SmaliHexOperand) obj;
            return ObjectsUtil.equals(getValueNumber(), other.getValueNumber());
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(getValueNumber());
        }
    }
    public static class SmaliDecimalOperand extends SmaliInstructionOperand {
        private int number;

        public SmaliDecimalOperand() {
            super();
        }

        public int getNumber() {
            return number;
        }
        public void setNumber(int number) {
            this.number = number;
        }

        @Override
        public long getValueAsLong() {
            return number;
        }
        @Override
        public OperandType getOperandType() {
            return OperandType.DECIMAL;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            writer.appendInteger(getNumber());
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            reader.skipSpaces();
            setNumber(reader.readInteger());
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            reader.skipSpaces();
            setNumber(reader.readInteger());
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SmaliDecimalOperand other = (SmaliDecimalOperand) obj;
            return getNumber() == other.getNumber();
        }
        @Override
        public int hashCode() {
            return 31 + getNumber();
        }
    }
    public static class SmaliKeyOperand extends SmaliInstructionOperand implements KeyReference {
        private final OperandType operandType;
        private Key key;

        public SmaliKeyOperand(OperandType operandType) {
            super();
            this.operandType = operandType;
        }
        @Override
        public Key getKey() {
            return key;
        }
        @Override
        public void setKey(Key key) {
            this.key = key;
        }

        @Override
        public long getValueAsLong() {
            return -1;
        }

        @Override
        public OperandType getOperandType() {
            return operandType;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            Key key = getKey();
            if (key != null) {
                key.append(writer);
            }
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            setKey(parseKey(getOperandType().getSectionType(), reader));
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            setKey(parseKey(opcode.getSectionType(), reader));
        }

        Key parseKey(SectionType<?> sectionType, SmaliReader reader) throws IOException {
            Key key;
            if (sectionType == SectionType.STRING_ID) {
                key = StringKey.read(reader);
            } else if (sectionType == SectionType.TYPE_ID) {
                key = TypeKey.read(reader);
            } else if (sectionType == SectionType.FIELD_ID) {
                key = FieldKey.read(reader);
            } else if (sectionType == SectionType.PROTO_ID) {
                key = ProtoKey.read(reader);
            } else if (sectionType == SectionType.METHOD_ID) {
                key = MethodKey.read(reader);
            } else if (sectionType == SectionType.CALL_SITE_ID) {
                key = CallSiteKey.read(reader);
            } else if (sectionType == SectionType.METHOD_HANDLE) {
                key = MethodHandleKey.read(reader);
            } else {
                throw new SmaliParseException("Undefined section type: " + sectionType, reader);
            }
            return key;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SmaliKeyOperand other = (SmaliKeyOperand) obj;
            return ObjectsUtil.equals(getKey(), other.getKey());
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(getKey());
        }
    }

    public static class SmaliDualKeyOperand extends SmaliKeyOperand implements DualKeyReference {

        private Key key2;

        public SmaliDualKeyOperand(OperandType operandType) {
            super(operandType);
        }

        @Override
        public Key getKey2() {
            return key2;
        }
        @Override
        public void setKey2(Key key) {
            this.key2 = key;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            super.append(writer);
            writer.append(", ");
            Key key = getKey2();
            if (key != null) {
                key.append(writer);
            }
        }

        @Override
        public void parse(SmaliReader reader) throws IOException {
            super.parse(reader);
            reader.skipWhitespaces();
            SmaliParseException.expect(reader, ',');
            reader.skipWhitespaces();
            setKey2(parseKey(getOperandType().getSectionType2(), reader));
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            super.parse(opcode, reader);
            reader.skipWhitespaces();
            SmaliParseException.expect(reader, ',');
            reader.skipWhitespaces();
            setKey2(parseKey(opcode.getSectionType2(), reader));
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SmaliDualKeyOperand other = (SmaliDualKeyOperand) obj;
            return ObjectsUtil.equals(getKey(), other.getKey()) &&
                    ObjectsUtil.equals(getKey2(), other.getKey2());
        }
        @Override
        public int hashCode() {
            return ObjectsUtil.hash(getKey(), getKey2());
        }
    }

    public static final SmaliInstructionOperand NO_OPERAND = new SmaliInstructionOperand() {
        @Override
        public long getValueAsLong() {
            return -1;
        }

        @Override
        public OperandType getOperandType() {
            return OperandType.NONE;
        }

        @Override
        public void append(SmaliWriter writer) {
        }

        @Override
        public void parse(SmaliReader reader) {
            reader.skipSpaces();
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) {
            reader.skipSpaces();
        }
        @Override
        void setParent(Smali parent) {
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };
}
