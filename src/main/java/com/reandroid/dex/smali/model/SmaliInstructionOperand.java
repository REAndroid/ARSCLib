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
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public abstract class SmaliInstructionOperand extends Smali {

    public SmaliInstructionOperand(){
        super();
    }

    public abstract long getValueAsLong();
    public abstract OperandType getOperandType();
    @Override
    public abstract void append(SmaliWriter writer) throws IOException;

    @Override
    public final void parse(SmaliReader reader) throws IOException {
        throw new RuntimeException("Must call parse(Opcode, SmaliReader)");
    }
    public abstract void parse(Opcode<?> opcode, SmaliReader reader) throws IOException;

    public static class SmaliLabelOperand extends SmaliInstructionOperand {

        private final SmaliLabel label;

        public SmaliLabelOperand(){
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
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            getLabel().parse(reader);
        }
    }
    public static class SmaliHexOperand extends SmaliInstructionOperand {

        private SmaliValueNumber<?> valueNumber;

        public SmaliHexOperand(){
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
            if(valueNumber != null){
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
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            reader.skipSpaces();
            SmaliValueNumber<?> value = SmaliValueNumber.createNumber(reader);
            setNumberValue(value);
            value.parse(reader);
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

        public SmaliDecimalOperand(){
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
        private Key key;

        public SmaliKeyOperand(){
            super();
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
            return OperandType.KEY;
        }

        @Override
        public void append(SmaliWriter writer) throws IOException {
            Key key = getKey();
            if(key != null){
                key.append(writer);
            }
        }
        @Override
        public void parse(Opcode<?> opcode, SmaliReader reader) throws IOException {
            setKey(parseKey(opcode.getSectionType(), reader));
        }
        Key parseKey(SectionType<?> sectionType, SmaliReader reader) throws IOException {
            Key key;
            if(sectionType == SectionType.STRING_ID){
                key = StringKey.read(reader);
            }else if(sectionType == SectionType.TYPE_ID){
                key = TypeKey.read(reader);
            }else if(sectionType == SectionType.FIELD_ID){
                key = FieldKey.read(reader);
            }else if(sectionType == SectionType.PROTO_ID){
                key = ProtoKey.read(reader);
            }else if(sectionType == SectionType.METHOD_ID){
                key = MethodKey.read(reader);
            }else if(sectionType == SectionType.CALL_SITE_ID){
                key = CallSiteKey.read(reader);
            }else {
                throw new SmaliParseException("Invalid key", reader);
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

        public SmaliDualKeyOperand() {
            super();
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
        public OperandType getOperandType() {
            return OperandType.DUAL_KEY;
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
