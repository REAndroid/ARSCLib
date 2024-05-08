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
package com.reandroid.dex.data;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.ArraySupplier;
import com.reandroid.dex.base.DexPositionAlign;
import com.reandroid.dex.common.Register;
import com.reandroid.dex.common.RegisterType;
import com.reandroid.dex.common.RegistersTable;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.debug.DebugLineNumber;
import com.reandroid.dex.debug.DebugSequence;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliCodeSet;
import com.reandroid.dex.smali.model.SmaliInstruction;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class InstructionList extends FixedBlockContainer implements
        Iterable<Ins>, SmaliFormat {

    private final CodeItem codeItem;
    private final BlockList<Ins> insArray;
    private final DexPositionAlign blockAlign;

    private boolean mLockExtraLines;

    public InstructionList(CodeItem codeItem){
        super(2);
        this.codeItem = codeItem;

        this.insArray = new BlockList<>();
        this.blockAlign = new DexPositionAlign();

        addChild(0, insArray);
        addChild(1, blockAlign);
    }

    public RegistersEditor editRegisters(){
        return RegistersEditor.fromIns(getCodeItem(), iterator());
    }

    public MethodDef getMethodDef() {
        return getCodeItem().getMethodDef();
    }
    public DebugSequence getDebugSequence(){
        DebugInfo debugInfo = getDebugInfo();
        if(debugInfo != null){
            return debugInfo.getDebugSequence();
        }
        return null;
    }
    public DebugSequence getOrCreateDebugSequence(){
        return getOrCreateDebugInfo().getDebugSequence();
    }
    public DebugInfo getDebugInfo(){
        return getCodeItem().getDebugInfo();
    }
    public DebugInfo getOrCreateDebugInfo(){
        return getCodeItem().getOrCreateDebugInfo();
    }
    public CodeItem getCodeItem() {
        return codeItem;
    }
    public RegistersTable getRegistersTable(){
        return getCodeItem();
    }
    public void addLocalRegisters(int amount){
        addLocalRegisters(false, amount);
    }
    public void addLocalRegisters(boolean start, int amount){
        RegistersTable registersTable = getRegistersTable();
        Iterator<RegistersIterator> iterator = getRegistersIterators();
        while (iterator.hasNext()){
            RegistersIterator registersIterator = iterator.next();
            int count;
            if(registersIterator.isRange()){
                count = 1;
            }else {
                count = registersIterator.size();
            }
            for(int i = 0; i < count; i++){
                RegisterReference reference = registersIterator.get(i);
                if(start || reference.isParameter()){
                    reference.setRegisterValue(reference.getValue() + amount);
                }
            }
        }
        registersTable.setRegistersCount(registersTable.getRegistersCount() + amount);
    }
    public List<Register> getLocalFreeRegisters(int startIndex){
        RegistersTable registersTable = getRegistersTable();
        int count = registersTable.getLocalRegistersCount();
        Iterator<Register> iterator = new ArraySupplierIterator<>(new ArraySupplier<Register>() {
            @Override
            public Register get(int i) {
                return new Register(i, false, registersTable);
            }
            @Override
            public int getCount() {
                return count;
            }
        });
        iterator = FilterIterator.of(iterator, reference -> {
            int registerValue = reference.getValue();
            return registerValue < count && isFreeRegister(registerValue, startIndex);
        });
        List<Register> list = CollectionUtil.toUniqueList(iterator);
        list.sort(CompareUtil.getComparableComparator());
        return list;
    }
    public boolean isFreeRegister(int registerValue, int startIndex){
        Iterator<RegistersIterator> iterator = getRegistersIterators(startIndex);
        while (iterator.hasNext()){
            RegistersIterator registersIterator = iterator.next();
            for(RegisterReference reference : registersIterator){
                if(reference.getValue() == registerValue){
                    return reference.getRegisterType() == RegisterType.WRITE;
                }
            }
        }
        return registerValue != getRegistersTable().getLocalRegistersCount();
    }
    private Iterator<RegisterReference> getRegisters(int startIndex){
        return ExpandIterator.of(getRegistersIterators(startIndex));
    }
    private Iterator<RegistersIterator> getRegistersIterators(){
        return getRegistersIterators(0);
    }
    private Iterator<RegistersIterator> getRegistersIterators(int start){
        return ComputeIterator.of(iterator(start), ins -> {
            if(ins instanceof SizeXIns){
                return ((SizeXIns) ins).getRegistersIterator();
            }
            return null;
        });
    }

    public<T1 extends Ins> Iterator<T1> iterator(Opcode<T1> opcode){
        return iterator(opcode, null);
    }
    @SuppressWarnings("unchecked")
    public<T1 extends Ins> Iterator<T1> iterator(Opcode<T1> opcode, Predicate<? super T1> filter){
        return ComputeIterator.of(iterator(), ins -> {
            T1 result = null;
            if(ins != null && ins.getOpcode() == opcode){
                result = (T1) ins;
                if(filter != null && !filter.test(result)){
                    result = null;
                }
            }
            return result;
        });
    }
    @Override
    public Iterator<Ins> iterator() {
        return getInsArray().iterator();
    }
    public Iterator<Ins> clonedIterator() {
        return getInsArray().clonedIterator();
    }
    public Iterator<Ins> arrayIterator() {
        return getInsArray().arrayIterator();
    }
    public Iterator<Ins> iterator(int start, int size) {
        return getInsArray().iterator(start, size);
    }
    public Iterator<Ins> iterator(int start) {
        BlockList<Ins> array = getInsArray();
        return array.iterator(start, getCount() - start);
    }
    public Iterator<Ins> iteratorByAddress(int startAddress, int codeUnits) {
        Ins insStart = getAtAddress(startAddress);
        if(insStart == null){
            return EmptyIterator.of();
        }
        Ins insEnd = getAtAddress(startAddress + codeUnits);
        int count = insEnd.getIndex() - insStart.getIndex();
        return this.iterator(insStart.getIndex(), count);
    }

    private BlockList<Ins> getInsArray() {
        return insArray;
    }

    public Ins get(int i){
        return getInsArray().get(i);
    }
    public int getCount(){
        return getInsArray().getCount();
    }
    public void add(Ins ins){
        BlockList<Ins> array = getInsArray();
        array.add(ins);
        Ins previous = array.get(ins.getIndex() - 1);
        int address;
        if(previous != null){
            address = previous.getAddress() + previous.getCodeUnits();
        }else {
            address = 0;
        }
        ins.setAddress(address);
    }
    public void add(int index, Ins item) {
        reBuildExtraLines();
        getInsArray().add(index, item);
        updateAddresses();
        updateLabelAddress();
        reBuildExtraLines();
    }
    public void add(int index, Ins[] insArray) {
        reBuildExtraLines();
        getInsArray().addAll(index, insArray);
        updateAddresses();
        updateLabelAddress();
        reBuildExtraLines();
    }
    public void moveTo(Ins ins, int index){
        if(index == ins.getIndex()){
            return;
        }
        if(index < 0){
            throw new IndexOutOfBoundsException("Negative index: " + index);
        }
        if(index >= getCount()){
            throw new IndexOutOfBoundsException("Size = " + getCount() + ", " + index);
        }
        reBuildExtraLines();
        getInsArray().moveTo(ins, index);
        updateAddresses();
        updateLabelAddress();
        reBuildExtraLines();
    }
    public ConstNumber createConstIntegerAt(int index, int value) {
        return createConstIntegerAt(index, 0, value);
    }
    public ConstNumber createConstIntegerAt(int index, int register, int value) {
        ConstNumber constNumber;
        if(register <= 0x0f && value <= 0x7 && value >= -0x7){
            constNumber = createAt(index, Opcode.CONST_4);
        }else if(value <= 0x7fff && value >= -0x7fff){
            constNumber = createAt(index, Opcode.CONST_16);
        }else if((value & 0x0000ffff) == 0){
            constNumber = createAt(index, Opcode.CONST_HIGH16);
        }else {
            constNumber = createAt(index, Opcode.CONST);
        }
        constNumber.setRegister(register);
        constNumber.set(value);
        return constNumber;
    }
    public ConstNumberLong createConstLongAt(int index, long value) {
        return createConstLongAt(index, 0, value);
    }
    public ConstNumberLong createConstLongAt(int index, int register, long value) {
        ConstNumberLong constNumber;
        if((value & 0xffff00000000L) == 0){
            constNumber = createAt(index, Opcode.CONST_WIDE_HIGH16);
        }else if((value & 0xffff) == value){
            constNumber = createAt(index, Opcode.CONST_WIDE_16);
        }else if((value & 0xffffffffL) == value){
            constNumber = createAt(index, Opcode.CONST_WIDE_32);
        }else {
            constNumber = createAt(index, Opcode.CONST_WIDE);
        }
        constNumber.setRegister(register);
        constNumber.set(value);
        return constNumber;
    }
    public ConstString createStringAt(int index, StringKey value) {
        return createStringAt(index, 0, value);
    }
    public ConstString createStringAt(int index, String value) {
        return createStringAt(index, 0, value);
    }
    public ConstString createStringAt(int index, int register, String value) {
        return createStringAt(index, register, StringKey.create(value));
    }
    public ConstString createStringAt(int index, int register, StringKey value) {
        ConstString constNumber;
        StringId stringId = getCodeItem().getOrCreateSectionItem(SectionType.STRING_ID, value);
        int id = stringId.getIdx();
        if((id & 0xffff) == id){
            constNumber = createAt(index, Opcode.CONST_STRING);
        }else {
            constNumber = createAt(index, Opcode.CONST_STRING_JUMBO);
        }
        constNumber.setRegister(register);
        constNumber.setString(stringId);
        return constNumber;
    }
    public<T1 extends Ins> T1 createAt(int index, Opcode<T1> opcode) {
        if(index == getCount()) {
            return createNext(opcode);
        }
        T1 item = opcode.newInstance();
        add(index, item);
        return item;
    }
    public Ins[] createAt(int index, Opcode<?>[] opcodeArray) {
        int length = opcodeArray.length;
        Ins[] results = new Ins[length];
        for(int i = 0; i < length; i++){
            results[i] = opcodeArray[i].newInstance();
        }
        add(index, results);
        return results;
    }
    public<T1 extends Ins> T1 createNext(Opcode<T1> opcode) {
        T1 item = opcode.newInstance();
        add(item);
        return item;
    }
    public boolean isLonelyInTryCatch(Ins ins){
        buildExtraLines();
        int codeUnits = ins.getCodeUnits();
        Iterator<ExceptionHandler.TryStartLabel> iterator = ins.getExtraLines(
                ExceptionHandler.TryStartLabel.class);
        while (iterator.hasNext()){
            ExceptionHandler.TryStartLabel startLabel = iterator.next();
            int handlerCodeUnits = startLabel.getHandler().getCodeUnit();
            if(handlerCodeUnits <= codeUnits){
                return true;
            }
        }
        return false;
    }
    public boolean contains(Ins item){
        return getInsArray().contains(item);
    }
    public boolean remove(Ins item){
        return remove(item, false);
    }
    public boolean remove(Ins item, boolean force) {
        if(!contains(item)){
            return false;
        }
        if(!force && isLonelyInTryCatch(item)){
            return replaceWithNop(item) != null;
        }
        reBuildExtraLines();
        if(item.hasExtraLines()){
            Ins next = get(item.getIndex() + 1);
            if(next != null) {
                item.transferExtraLines(next);
            }
        }
        getInsArray().remove(item);
        item.setParent(null);
        item.setIndex(-1);
        updateAddresses();
        updateLabelAddress();
        return true;
    }
    public InsNop replaceWithNop(Ins ins){
        return replace(ins, Opcode.NOP);
    }
    public<T1 extends Ins> T1 replace(Ins old, Opcode<T1> opcode){
        if(!contains(old)){
            return null;
        }
        T1 item = opcode.newInstance();
        replace(old, item);
        return item;
    }
    public void replace(Ins old, Ins item) {
        if(old == item){
            return;
        }
        reBuildExtraLines();
        int index = old.getIndex();
        old.transferExtraLines(item);
        item.setAddress(old.getAddress());
        getInsArray().set(index, item);
        old.setParent(null);
        old.setIndex(-1);
        updateAddresses();
        updateLabelAddress();
    }
    private void updateLabelAddress() {
        for(Ins ins : this) {
            ins.updateLabelAddress();
        }
    }
    private void updateAddresses() {
        int outSize = 0;
        int address = 0;
        for(Ins ins : this) {
            ins.setAddress(address);
            address += ins.getCodeUnits();
            int out = ins.getOutSize();
            if(out > outSize){
                outSize = out;
            }
        }
        this.codeItem.getInstructionCodeUnitsReference().set(address);
        this.codeItem.getInstructionOutsReference().set(outSize);
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.blockAlign.align(this);
        this.codeItem.getCodeUnits().set(getCodeUnits());
        clearAndUpdateAddresses();
    }
    private void clearAndUpdateAddresses() {
        int outSize = 0;
        int address = 0;
        for(Ins ins : this) {
            ins.clearExtraLines();
            ins.setAddress(address);
            address += ins.getCodeUnits();
            int out = ins.getOutSize();
            if(out > outSize){
                outSize = out;
            }
        }
        this.codeItem.getInstructionCodeUnitsReference().set(address);
        this.codeItem.getInstructionOutsReference().set(outSize);
        this.mLockExtraLines = false;
    }
    public int getCodeUnits() {
        int result = 0;
        for (Ins ins : this) {
            result += ins.getCodeUnits();
        }
        return result;
    }
    public DexPositionAlign getBlockAlign() {
        return blockAlign;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        buildExtraLines();
        for (Ins ins : this) {
            writer.newLine();
            ins.append(writer);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {

        int insCodeUnits = codeItem.getInstructionCodeUnitsReference().get();
        int position = reader.getPosition() + insCodeUnits * 2;
        int zeroPosition = reader.getPosition();

        int count = (insCodeUnits + 1) / 2;
        BlockList<Ins> insBlockList = getInsArray();
        insBlockList.ensureCapacity(count);

        while (reader.getPosition() < position){
            Opcode<?> opcode = Opcode.read(reader);
            Ins ins = opcode.newInstance();
            ins.setAddress((reader.getPosition() - zeroPosition) / 2);
            insBlockList.add(ins);
            ins.readBytes(reader);
        }
        insBlockList.trimToSize();
        if(position != reader.getPosition()){
            // should not reach here
            reader.seek(position);
        }
        int totalRead = reader.getPosition() - zeroPosition;
        blockAlign.align(totalRead);
        reader.offset(blockAlign.size());
    }

    private void clearExtraLines() {
        for (Ins ins : this) {
            ins.clearExtraLines();
        }
        mLockExtraLines = false;
    }
    private boolean haveExtraLines() {
        if(mLockExtraLines){
            return true;
        }
        for (Ins ins : this) {
            if(ins.hasExtraLines()){
                return true;
            }
        }
        return false;
    }
    public void buildExtraLines(){
        if(haveExtraLines()){
            return;
        }
        mLockExtraLines = true;
        buildLabels();
        buildTryBlock();
        buildDebugInfo();
    }
    public void reBuildExtraLines(){
        clearExtraLines();
        buildExtraLines();
    }
    public Ins getAtAddress(int address){
        int size = getCount();
        for (int i = 0; i < size; i++) {
            Ins ins = get(i);
            if(ins.getAddress() == address){
                return ins;
            }
        }
        return null;
    }
    private void addLabel(Label label){
        Ins target = getAtAddress(label.getTargetAddress());
        if(target != null){
            target.addExtraLine(label);
        }
    }
    private void addLabels(Iterator<? extends Label> iterator){
        while (iterator.hasNext()){
            addLabel(iterator.next());
        }
    }
    private void buildLabels(){
        for(Ins ins : this){
            if(ins instanceof Label){
                addLabel((Label) ins);
            }else if(ins instanceof LabelsSet){
                addLabels(((LabelsSet) ins).getLabels());
            }
            ins.trimExtraLines();
        }
    }
    private void buildTryBlock(){
        TryBlock tryBlock = this.codeItem.getTryBlock();
        if(tryBlock == null || tryBlock.isNull()){
            return;
        }
        addLabels(tryBlock.getLabels());
    }
    private void buildDebugInfo(){
        mLockExtraLines = true;
        DebugInfo debugInfo = codeItem.getDebugInfo();
        if(debugInfo == null){
            mLockExtraLines = false;
            return;
        }
        Iterator<DebugElement> iterator = debugInfo.getExtraLines();
        while (iterator.hasNext()){
            DebugElement element = iterator.next();
            Ins target = getAtAddress(element.getTargetAddress());
            if(target != null){
                target.addExtraLine(element);
            }
        }
        mLockExtraLines = false;
    }
    public void onRemove(){
        clearExtraLines();
        getInsArray().clearChildes();
        getInsArray().destroy();
    }
    public boolean cleanInvalidDebugLineNumbers(){
        DebugInfo debugInfo = getDebugInfo();
        if(debugInfo == null){
            return false;
        }
        clearExtraLines();
        boolean result = false;
        buildDebugInfo();
        for(Ins ins : this){
            if(cleanInvalidDebugLineNumbers(ins)){
                result = true;
            }
        }
        clearExtraLines();
        return result;
    }
    private boolean cleanInvalidDebugLineNumbers(Ins ins){
        boolean result = false;
        DebugLineNumber first = null;
        int address = ins.getAddress();
        Iterator<DebugLineNumber> iterator = ins.getExtraLines(DebugLineNumber.class);
        while (iterator.hasNext()){
            DebugLineNumber lineNumber = iterator.next();
            if(lineNumber.getParent() == null){
                continue;
            }
            if(lineNumber.getTargetAddress() != address){
                lineNumber.removeSelf();
                result = true;
                continue;
            }
            if(first == null){
                first = lineNumber;
                continue;
            }
            lineNumber.removeSelf();
            result = true;
        }
        return result;
    }
    public void replaceKeys(Key search, Key replace){
        for(Ins ins : this){
            ins.replaceKeys(search, replace);
        }
    }
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<Ins, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(Ins element) {
                return element.usedIds();
            }
        };
    }
    public void merge(InstructionList instructionList){
        getInsArray().ensureCapacity(instructionList.getCount());
        for(Ins coming : instructionList){
            Ins ins = createNext(coming.getOpcode());
            ins.merge(coming);
        }
        getInsArray().trimToSize();
        updateAddresses();
    }
    public void fromSmali(SmaliCodeSet smaliCodeSet) throws IOException {
        int index = 0;
        int offset = smaliCodeSet.getAddressOffset();
        if(offset != 0) {
            Ins ins = getAtAddress(smaliCodeSet.getAddressOffset());
            if(ins != null) {
                index = ins.getIndex();
            }else if(offset >= getCodeUnits()) {
                index = getCount();
            }
        }
        fromSmali(index, smaliCodeSet);
    }
    public void fromSmali(int index, SmaliCodeSet smaliCodeSet) throws IOException {
        Iterator<SmaliInstruction> iterator = smaliCodeSet.getInstructions();
        while (iterator.hasNext()){
            SmaliInstruction smaliInstruction = iterator.next();
            Ins ins = createAt(index, smaliInstruction.getOpcode());
            ins.fromSmali(smaliInstruction);
            index ++;
        }
        getInsArray().trimToSize();
        updateAddresses();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        InstructionList list = (InstructionList) obj;
        return insArray.equals(list.insArray);
    }

    @Override
    public int hashCode() {
        return insArray.hashCode();
    }

    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        smaliWriter.indentPlus();
        try {
            append(smaliWriter);
            smaliWriter.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
