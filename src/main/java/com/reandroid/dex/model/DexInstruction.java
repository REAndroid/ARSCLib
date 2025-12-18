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
package com.reandroid.dex.model;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegisterFormat;
import com.reandroid.dex.common.RegisterType;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.dex.smali.model.SmaliMethod;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.LinkedIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class DexInstruction extends DexCode {

    private final DexMethod dexMethod;
    private Ins mIns;
    private boolean mEdit;

    public DexInstruction(DexMethod dexMethod, Ins ins) {
        this.dexMethod = dexMethod;
        this.mIns = ins;
    }

    public boolean usesRegister(int register) {
        RegistersSet registersSet = getRegistersSet();
        int count = registersSet.getRegistersCount();
        RegisterFormat format = getRegisterFormat();
        for (int i = 0; i < count; i++) {
            int r = registersSet.getRegister(i);
            if (register == r || (register == (r + 1) && format.isWide(i))) {
                return true;
            }
        }
        return false;
    }
    public boolean usesRegister(int register, RegisterType type) {
        RegisterFormat format = getOpcode().getRegisterFormat();
        RegistersSet registersSet = getRegistersSet();
        int count = registersSet.getRegistersCount();
        for (int i = 0; i < count; i++) {
            if (!type.is(format.get(i))) {
                continue;
            }
            int r = registersSet.getRegister(i);
            if (register == r || (register == (r + 1) && format.isWide(i))) {
                return true;
            }
        }
        return false;
    }
    public int getAddress() {
        return getIns().getAddress();
    }
    public int getCodeUnits() {
        return getIns().getCodeUnits();
    }
    public List<Register> getLocalFreeRegisters() {
        return getDexMethod().getLocalFreeRegisters(getIndex());
    }
    public String getString() {
        IdItem idItem = getIdSectionEntry();
        if (idItem instanceof StringId) {
            return ((StringId) idItem).getString();
        }
        return null;
    }
    public void setString(String text) {
        setKey(StringKey.create(text));
    }
    public DexInstruction setStringWithJumbo(String text) {
        SizeXIns sizeXIns = (SizeXIns) edit();
        StringId stringId = sizeXIns.getOrCreateSectionItem(
                SectionType.STRING_ID, StringKey.create(text));
        if ((stringId.getIdx() & 0xffff0000) == 0 || !sizeXIns.is(Opcode.CONST_STRING)) {
            sizeXIns.setSectionId(stringId);
            return this;
        }
        int register = ((RegistersSet)sizeXIns).getRegister();
        InsConstStringJumbo jumbo = sizeXIns.replace(Opcode.CONST_STRING_JUMBO);
        jumbo.setRegister(register);
        jumbo.setSectionId(stringId);
        return DexInstruction.create(getDexMethod(), jumbo);
    }
    public FieldKey getKeyAsField() {
        IdItem idItem = getIdSectionEntry();
        if (idItem instanceof FieldId) {
            return ((FieldId) idItem).getKey();
        }
        return null;
    }
    public MethodKey getKeyAsMethod() {
        IdItem idItem = getIdSectionEntry();
        if (idItem instanceof MethodId) {
            return ((MethodId) idItem).getKey();
        }
        return null;
    }
    public Key getKey() {
        IdItem entry = getIdSectionEntry();
        if (entry != null) {
            return entry.getKey();
        }
        return null;
    }
    public void setKey(Key key) {
        Ins ins = getIns();
        if (ins instanceof SizeXIns) {
            ((SizeXIns) edit()).setKey(key);
        }
    }
    public IdItem getIdSectionEntry() {
        Ins ins = getIns();
        if (ins instanceof SizeXIns) {
            return  ((SizeXIns) ins).getSectionId();
        }
        return null;
    }
    public RegisterFormat getRegisterFormat() {
        return getOpcode().getRegisterFormat();
    }
    public RegistersSet getRegistersSet() {
        Ins ins = getIns();
        if (ins instanceof RegistersSet) {
            return (RegistersSet) ins;
        }
        return RegistersSet.NO_REGISTERS;
    }
    public int getRegister(int i) {
        if (i < 0) {
            return -1;
        }
        RegistersSet registersSet = getRegistersSet();
        if (i < registersSet.getRegistersCount()) {
            return registersSet.getRegister(i);
        }
        return -1;
    }
    public int getRegister() {
        return getRegister(0);
    }
    public int getRegistersCount() {
        Ins ins = getIns();
        if (ins instanceof RegistersSet) {
            return ((RegistersSet) ins).getRegistersCount();
        }
        return 0;
    }
    public void setRegister(int register) {
        setRegister(0, register);
    }
    public void setRegister(int i, int register) {
        Ins ins = getIns();
        if (ins instanceof RegistersSet) {
            ensureRegistersCount(i + 1);
            ((RegistersSet) ins).setRegister(i, register);
        }
    }
    public boolean removeRegisterAt(int index) {
        Ins ins = edit();
        if (ins instanceof RegistersSet) {
            return ((RegistersSet) ins).removeRegisterAt(index);
        }
        return false;
    }
    private void ensureRegistersCount(int count) {
        if (count > getRegistersCount()) {
            if (getOpcode().getRegisterFormat().isOut()) {
                setRegistersCount(count);
            }
        }
    }
    public void setRegistersCount(int count) {
        if (getIns() instanceof RegistersSet) {
            ((RegistersSet) edit()).setRegistersCount(count);
        }
    }
    public boolean is(Opcode<?> opcode) {
        return opcode == getOpcode();
    }
    public boolean isConstString() {
        return getOpcode().isConstString();
    }
    public boolean isConstNumber() {
        return getOpcode().isConstNumber();
    }
    public boolean isConstInteger() {
        return getOpcode().isConstInteger();
    }
    public boolean isConstWide() {
        return getOpcode().isConstWide();
    }
    public boolean isGoto() {
        return getOpcode().isGoto();
    }
    public boolean isIfTest() {
        return getOpcode().isIfTest();
    }
    public boolean isSwitch() {
        return getOpcode().isSwitch();
    }
    public boolean isPayload() {
        return getOpcode().isPayload();
    }
    public boolean isReturn() {
        return getOpcode().isReturn();
    }
    public boolean isThrow() {
        return getOpcode() == Opcode.THROW;
    }
    public boolean isMethodExit() {
        return getOpcode().isMethodExit();
    }
    public boolean isInsBranching() {
        return getOpcode().isInsBranching();
    }
    public boolean isArrayOp() {
        return getOpcode().isArrayOp();
    }
    public boolean isArrayGet() {
        return getOpcode().isArrayGet();
    }
    public boolean isArrayPut() {
        return getOpcode().isArrayPut();
    }
    public boolean isFieldInstanceGet() {
        return getOpcode().isFieldInstanceGet();
    }
    public boolean isFieldInstancePut() {
        return getOpcode().isFieldInstancePut();
    }
    public boolean isFieldInstanceOp() {
        return getOpcode().isFieldInstanceOp();
    }
    public boolean isFieldStaticGet() {
        return getOpcode().isFieldStaticGet();
    }
    public boolean isFieldStaticPut() {
        return getOpcode().isFieldStaticPut();
    }
    public boolean isFieldGet() {
        return getOpcode().isFieldGet();
    }
    public boolean isFieldPut() {
        return getOpcode().isFieldPut();
    }
    public boolean isFieldStaticOp() {
        return getOpcode().isFieldStaticOp();
    }
    public boolean isFieldOp() {
        return getOpcode().isFieldOp();
    }
    public boolean isMethodInvokeVirtual() {
        return getOpcode().isMethodInvokeVirtual();
    }
    public boolean isMethodInvokeSuper() {
        return getOpcode().isMethodInvokeSuper();
    }
    public boolean isMethodInvokeDirect() {
        return getOpcode().isMethodInvokeDirect();
    }
    public boolean isMethodInvokeStatic() {
        return getOpcode().isMethodInvokeStatic();
    }
    public boolean isMethodInvokeInterface() {
        return getOpcode().isMethodInvokeInterface();
    }
    public boolean isMethodInvoke() {
        return getOpcode().isMethodInvoke();
    }
    public boolean isMove() {
        return getOpcode().isMove();
    }
    public boolean isMoveResult() {
        return getOpcode().isMoveResult();
    }
    public boolean hasOutRegisters() {
        return getOpcode().hasOutRegisters();
    }
    public int getTargetAddress() {
        Ins ins = getIns();
        if (ins instanceof Label) {
            return ((Label) ins).getTargetAddress();
        }
        return -1;
    }
    public void setTargetAddress(int address) {
        if (getIns() instanceof Label) {
            ((Label) edit()).setTargetAddress(address);
        }
    }
    public IntegerReference getAsIntegerReference() {
        Ins ins = getIns();
        if (ins instanceof ConstNumber) {
            return ((ConstNumber) ins);
        }
        return null;
    }
    public Integer getAsInteger() {
        Ins ins = getIns();
        if (ins instanceof ConstNumber) {
            return ((ConstNumber) ins).get();
        }
        return null;
    }
    public Long getAsLong() {
        Ins ins = getIns();
        if (ins instanceof ConstNumberLong) {
            return ((ConstNumberLong) ins).getLong();
        }
        return null;
    }
    public void setAsInteger(int value) {
        Ins ins = edit();
        if (ins instanceof ConstNumber) {
            ((ConstNumber) ins).set(value);
        }
    }
    public void setAsLong(long value) {
        Ins ins = edit();
        if (ins instanceof ConstNumberLong) {
            ((ConstNumberLong) ins).set(value);
        }
    }
    public boolean trapsCatchAll() {
        return traps(null);
    }
    public boolean traps(TypeKey typeKey) {
        return getCatches(typeKey).hasNext();
    }
    public Iterator<DexCatch> getCatches(TypeKey typeKey) {
        return FilterIterator.of(getCatches(), dexCatch -> dexCatch.traps(typeKey));
    }
    public Iterator<DexCatch> getCatches() {
        final int address = getAddress();
        return new IterableIterator<DexTry, DexCatch>(getTries()) {
            @Override
            public Iterator<DexCatch> iterator(DexTry element) {
                return element.getCatches(address);
            }
        };
    }
    public Iterator<DexTry> getTries() {
        return getDexMethod().getDexTry(getAddress());
    }

    public DexInstruction replace(Opcode<?> opcode) {
        return DexInstruction.create(getDexMethod(), edit().replace(opcode));
    }
    public DexInstruction replace(SmaliInstruction smaliInstruction) {
        DexInstruction dexInstruction = replace(smaliInstruction.getOpcode());
        dexInstruction.getIns().fromSmali(smaliInstruction);
        return dexInstruction;
    }
    public DexInstruction createNext(Opcode<?> opcode) {
        return DexInstruction.create(getDexMethod(), edit().createNext(opcode));
    }
    public DexInstruction createNext(SmaliInstruction smaliInstruction) {
        DexInstruction dexInstruction = createNext(smaliInstruction.getOpcode());
        dexInstruction.getIns().fromSmali(smaliInstruction);
        return dexInstruction;
    }
    public DexInstruction replaceWithSmali(String smaliString) throws IOException {
        return replaceWithSmali(SmaliReader.of(smaliString));
    }
    public DexInstruction replaceWithSmali(SmaliReader reader) throws IOException {
        SmaliInstruction smaliInstruction = new SmaliInstruction();
        smaliInstruction.parse(reader);
        return replace(smaliInstruction);
    }
    public DexInstruction replaceFromSmaliAll(String smaliString) throws IOException {
        return replaceFromSmaliAll(SmaliReader.of(smaliString));
    }
    public DexInstruction replaceFromSmaliAll(SmaliReader reader) throws IOException {
        SmaliMethod smaliMethod = SmaliMethod.create(getDexMethod());
        smaliMethod.getCodeSet().parse(reader);
        DexInstruction lastInstruction = this;
        Iterator<SmaliInstruction> iterator = smaliMethod.getInstructions();
        if (iterator.hasNext()) {
            lastInstruction = lastInstruction.replace(iterator.next());
        }
        while (iterator.hasNext()) {
            lastInstruction = lastInstruction.createNext(iterator.next());
        }
        return lastInstruction;
    }
    public DexInstruction createNextFromSmali(String smaliString) throws IOException {
        return createNextFromSmali(SmaliReader.of(smaliString));
    }
    public DexInstruction createNextFromSmali(SmaliReader reader) throws IOException {
        SmaliInstruction smaliInstruction = new SmaliInstruction();
        smaliInstruction.parse(reader);
        Ins ins = edit().createNext(smaliInstruction.getOpcode());
        ins.fromSmali(smaliInstruction);
        return DexInstruction.create(getDexMethod(), ins);
    }
    public DexInstruction createNextFromSmaliAll(String smaliString) throws IOException {
        return createNextFromSmaliAll(SmaliReader.of(smaliString));
    }
    public DexInstruction createNextFromSmaliAll(SmaliReader reader) throws IOException {
        SmaliMethod smaliMethod = SmaliMethod.create(getDexMethod());
        smaliMethod.getCodeSet().parse(reader);
        DexInstruction lastInstruction = this;
        Iterator<SmaliInstruction> iterator = smaliMethod.getInstructions();
        while (iterator.hasNext()) {
            lastInstruction = lastInstruction.createNext(iterator.next());
        }
        return lastInstruction;
    }
    @Override
    public void removeSelf() {
        Ins ins = edit();
        InstructionList instructionList = ins.getInstructionList();
        if (instructionList != null) {
            instructionList.remove(ins);
        }
    }
    @Override
    public boolean isRemoved() {
        if (getDexMethod().isRemoved()) {
            return true;
        }
        Ins ins = getIns();
        return ins == null || ins.isRemoved();
    }

    public Opcode<?> getOpcode() {
        return getIns().getOpcode();
    }
    public Ins getIns() {
        Ins ins = this.mIns;
        if (mEdit) {
            return ins;
        }
        DexMethod dexMethod = getDexMethod();
        int editIndex = dexMethod.getEditIndex();
        int index = ins.getIndex();
        if (editIndex < index) {
            ins = dexMethod.getDefinition()
                    .getInstruction(index);
            this.mIns = ins;
            mEdit = true;
        }
        return ins;
    }
    public Ins edit() {
        Ins ins = getIns();
        if (mEdit) {
            return ins;
        }
        ins = mIns.edit();
        if (ins != mIns) {
            getDexMethod().setEditIndex(ins.getIndex());
            this.mIns = ins;
            this.mEdit = true;
        }
        return ins;
    }

    @Override
    public boolean uses(Key key) {
        Key insKey = getKey();
        if (insKey != null) {
            return insKey.uses(key);
        }
        return false;
    }
    @Override
    public DexMethod getDexMethod() {
        return dexMethod;
    }

    public DexDeclaration findDeclaration() {
        Key key = getKey();
        if (key != null) {
            DexClassRepository dexClassRepository = getClassRepository();
            if (dexClassRepository != null) {
                return dexClassRepository.getDexDeclaration(key);
            }
        }
        return null;
    }
    public DexInstruction getTargetInstruction() {
        return DexInstruction.create(getDexMethod(), getIns().getTargetIns());
    }
    public boolean hasTargetingInstructions() {
        return getIns().getForcedExtraLines(Ins.class).hasNext();
    }
    public boolean hasTargetingInstructionsIfOpcode(Predicate<Opcode<?>> predicate) {
        return FilterIterator.of(getIns().getForcedExtraLines(Ins.class),
                ins -> predicate.test(ins.getOpcode())).hasNext();
    }
    public Iterator<DexInstruction> getTargetingInstructions() {
        return DexInstruction.createAll(getDexMethod(),
                CollectionUtil.copyOf(getIns().getForcedExtraLines(Ins.class)));
    }
    public Iterator<DexInstruction> getTargetingInstructionsIfOpcode(Predicate<Opcode<?>> predicate) {
        Iterator<Ins> iterator = CollectionUtil.copyOf(getIns().getForcedExtraLines(Ins.class));
        if (!iterator.hasNext()) {
            return EmptyIterator.of();
        }
        iterator = FilterIterator.of(iterator, ins -> predicate.test(ins.getOpcode()));
        return DexInstruction.createAll(getDexMethod(), iterator);
    }
    // for switch payload instruction
    public Iterator<DexInstruction> getTargetSwitchCases() {
        Ins ins = getIns();
        if (!(ins instanceof InsSwitchPayload)) {
            return EmptyIterator.of();
        }
        InsSwitchPayload<? extends SwitchEntry> payload = (InsSwitchPayload<? extends SwitchEntry>) ins;
        Iterator<? extends SwitchEntry> switchEntryIterator = payload.iterator();
        if (!switchEntryIterator.hasNext()) {
            return EmptyIterator.of();
        }
        Iterator<Ins> iterator = ComputeIterator.of(switchEntryIterator, SwitchEntry::getTargetIns);
        iterator = CollectionUtil.copyOfUniqueOf(iterator);
        return DexInstruction.createAll(getDexMethod(), iterator);
    }
    public DexInstruction getTargetingSwitch() {
        return CollectionUtil.getFirst(getTargetingSwitches(null));
    }
    public Iterator<DexInstruction> getTargetingSwitches(Opcode<? extends InsSwitch> switchOpcode) {
        Iterator<SwitchEntry> switchEntryIterator = getIns().getForcedExtraLines(SwitchEntry.class);
        if (!switchEntryIterator.hasNext()) {
            return EmptyIterator.of();
        }
        Iterator<InsSwitch> iterator = ComputeIterator.of(switchEntryIterator, SwitchEntry::getInsSwitch);
        if (switchOpcode != null) {
            iterator = FilterIterator.of(iterator, ins -> ins.getOpcode() == switchOpcode);
        }
        iterator = CollectionUtil.copyOfUniqueOf(iterator);
        return DexInstruction.createAll(getDexMethod(), iterator);
    }
    public DexInstruction getNext() {
        return getDexMethod().getInstruction(getIndex() + 1);
    }
    public Iterator<DexInstruction> getNextInstructions() {
        return LinkedIterator.of(this, DexInstruction::getNext);
    }
    public DexInstruction getPrevious() {
        return getDexMethod().getInstruction(getIndex() - 1);
    }
    public Iterator<DexInstruction> getPreviousInstructions() {
        return LinkedIterator.of(this, DexInstruction::getPrevious);
    }
    public DexInstruction getMoveResult() {
        if (!hasOutRegisters()) {
            return null;
        }
        DexInstruction next = getNext();
        if (next != null && next.isMoveResult()) {
            return next;
        }
        return null;
    }

    public DexInstruction getPreviousReader(int register) {
        return getPreviousReader(register, CollectionUtil.getAcceptAll());
    }
    public DexInstruction getPreviousReader(int register, Opcode<?> opcode) {
        return getPreviousReader(register, instruction -> instruction.is(opcode));
    }
    public DexInstruction getPreviousReader(int register, Predicate<DexInstruction> predicate) {
        DexInstruction previous = getPrevious();
        while (previous != null) {
            Opcode<?> opcode = previous.getOpcode();
            if (opcode.isMove() && previous.getRegister(0) == register) {
                register = previous.getRegister(1);
            } else {
                RegisterFormat registerFormat = opcode.getRegisterFormat();
                int size = previous.getRegistersCount();
                for (int i = 0; i < size; i++) {
                    if (register == previous.getRegister(i) &&
                            RegisterType.READ.is(registerFormat.get(i))) {
                        if (predicate.test(previous)) {
                            return previous;
                        }
                        return null;
                    }
                }
            }
            previous = previous.getPrevious();
        }
        return null;
    }
    public DexInstruction getPreviousSetter(int register) {
        return getPreviousSetter(register, CollectionUtil.getAcceptAll());
    }
    public DexInstruction getPreviousSetter(int register, Opcode<?> opcode) {
        return getPreviousSetter(register, instruction -> instruction.is(opcode));
    }
    public DexInstruction getPreviousSetter(int register, Predicate<DexInstruction> predicate) {
        DexInstruction previous = getPrevious();
        while (previous != null) {
            Opcode<?> opcode = previous.getOpcode();
            if (opcode.isMove() && previous.getRegister(1) == register) {
                register = previous.getRegister(0);
            } else {
                RegisterFormat format = opcode.getRegisterFormat();
                int size = previous.getRegistersCount();
                for (int i = 0; i < size; i++) {
                    if (register == previous.getRegister(i) &&
                            RegisterType.WRITE.is(format.get(i))) {
                        if (predicate.test(previous)) {
                            return previous;
                        }
                        return null;
                    }
                }
            }
            previous = previous.getPrevious();
        }
        return null;
    }
    public int getIndex() {
        return getIns().getIndex();
    }
    public void moveBackward() {
        int index = getIndex();
        if (index != 0) {
            edit().moveTo(index - 1);
        }
    }
    public void moveForward() {
        int index = getIndex() + 1;
        if (index < getDexMethod().getInstructionsCount()) {
            edit().moveTo(index);
        }
    }
    public void moveTo(int index) {
        edit().moveTo(index);
    }
    public void merge(DexInstruction other) {
        getIns().merge(other.getIns());
    }

    @Override
    public DexClassRepository getClassRepository() {
        return getDexMethod().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getIns().append(writer);
    }
    @Override
    public String toString() {
        return getIns().toString();
    }

    public static Iterator<DexInstruction> createAll(DexMethod dexMethod, Iterator<? extends Ins> iterator) {
        if (dexMethod == null || !iterator.hasNext()) {
            return EmptyIterator.of();
        }
        return ComputeIterator.of(iterator, ins -> create(dexMethod, ins));
    }
    public static DexInstruction create(DexMethod dexMethod, Ins ins) {
        if (dexMethod == null || ins == null) {
            return null;
        }
        return new DexInstruction(dexMethod, ins);
    }
}
