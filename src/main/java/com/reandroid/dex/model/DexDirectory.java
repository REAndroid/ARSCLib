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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.debug.DebugSequence;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.ins.*;
import com.reandroid.dex.data.*;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.IntValue;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DexDirectory implements Iterable<DexFile>, VisitableInteger, IntegerVisitor {
    private final List<DexFile> dexFileList;
    private Object mTag;

    private final Set<RClassParent> mRParents;

    public DexDirectory() {
        this.dexFileList = new ArrayList<>();
        this.mRParents = new HashSet<>();
    }

    public Object getTag() {
        return mTag;
    }
    public void setTag(Object tag) {
        this.mTag = tag;
    }


    public void cleanDuplicateDebugLines(String className){
        DexClass dexClass = getDexClass(TypeKey.create(className));
        if(dexClass == null){
            return;
        }
        int i = 0;
        Iterator<DexMethod> iterator = dexClass.getMethods();
        while (iterator.hasNext()){
            i += 2;
            DexMethod dexMethod = iterator.next();
            CodeItem codeItem = dexMethod.getMethodDef().getCodeItem();
            if(codeItem == null){
                continue;
            }
            DebugInfo debugInfo = codeItem.getDebugInfo();
            if(debugInfo == null){
                continue;
            }
            DebugSequence debugSequence = debugInfo.getDebugSequence();
            if(debugSequence != null){
                debugSequence.removeInvalid(codeItem.getInstructionList());
                debugSequence.setLineStart(i);
                i += debugSequence.getLast() - debugSequence.getLineStart();
            }
        }
        for(DexFile dexFile : this){
            dexFile.cleanDuplicateDebugLines();
        }
    }
    public void cleanDuplicateDebugLines(){
        for(DexFile dexFile : this){
            dexFile.cleanDuplicateDebugLines();
        }
    }
    public Iterator<FieldKey> findEquivalentFields(FieldKey fieldKey){
        DexClass defining = getDexClass(fieldKey.getDefiningKey());
        if(defining == null){
            return EmptyIterator.of();
        }
        DexField dexField = defining.getField(fieldKey);
        if(dexField == null){
            return EmptyIterator.of();
        }
        defining = dexField.getDexClass();

        FieldKey definingKey = dexField.getKey();

        Iterator<FieldKey> subKeys = ComputeIterator.of(getSubTypes(defining.getKey()),
                dexClass -> {
                    FieldKey key = definingKey.changeDefining(dexClass.getKey());
                    DexField field = dexClass.getField(key);
                    if(definingKey.equals(field.getKey())){
                        return key;
                    }
                    return null;
                }
        );
        return CombiningIterator.two(SingleIterator.of(definingKey), subKeys);
    }
    public Iterator<MethodKey> findEquivalentMethods(MethodKey methodKey){
        DexClass defining = getDexClass(methodKey.getDefiningKey());
        if(defining == null){
            return EmptyIterator.of();
        }
        Iterator<DexMethod> iterator = defining.getMethods(methodKey);

        return new IterableIterator<DexMethod, MethodKey>(iterator) {
            @Override
            public Iterator<MethodKey> iterator(DexMethod element) {
                element = element.getDeclared();
                MethodKey definingKey = element.getKey();
                return CombiningIterator.two(SingleIterator.of(definingKey), element.getOverridingKeys());
            }
        };
    }
    public Iterator<DexClass> getSubTypes(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getSubTypeClasses(typeKey);
            }
        };
    }
    public Iterator<DexClass> getImplementClasses(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getImplementClasses(typeKey);
            }
        };
    }
    public void loadSuperTypesMap(){
        for(DexFile dexFile : this){
            dexFile.loadSuperTypesMap();
        }
    }
    @Override
    public void visit(Object sender, IntegerReference reference) {
        if((reference instanceof InsConst) || (reference instanceof InsConst16High)){
            replaceR((SizeXIns) reference);
        }else if(sender instanceof InsArrayData){
            replaceR((InsArrayData) sender);
        }
    }
    private void replaceRIns(Ins ins) {
        if(ins instanceof SizeXIns){
            replaceR((SizeXIns) ins);
        }else if(ins instanceof InsArrayData){
            replaceR((InsArrayData) ins);
        }
    }
    int mRCount;
    private void replaceR(SizeXIns insConst) {
        RField rField = getRField(insConst.getData());
        if(rField == null){
            return;
        }
        boolean replaced = DexFile.replaceRFields(rField, insConst);
        if(!replaced){
            return;
        }
        mRCount ++;
        log(mRCount + ") " + HexUtil.toHex8(rField.getResourceId()) + " --> " + rField.getFieldKey());
    }

    int mRArrayCount;
    private void replaceR(InsArrayData insArrayData) {
        if(!isAllResourceIds(insArrayData)){
            return;
        }
        replaceFillArray(insArrayData);
    }
    private InstructionList mPrevInsList;
    private int mPrevReg;
    private void replaceFillArray(InsArrayData insArrayData){
        InstructionList instructionList = insArrayData.getInstructionList();
        int reg1 = 0;
        if(mPrevInsList != instructionList){
            RegistersEditor editor = instructionList.editRegisters();
            reg1 = editor.getLocalRegistersCount();
            if((reg1 & 0xffff00) != 0){
                mPrevInsList = null;
                //return;1
            }
            editor.addLocalRegistersCount(2);
            editor.apply();
        }else {
            reg1 = mPrevReg;
            System.err.println("Reuse*************************  " + reg1);
        }
        int reg2 = reg1 + 1;
        mPrevReg = reg1;
        mPrevInsList = instructionList;
        int size = insArrayData.size();
        Iterator<InsFillArrayData> iterator = insArrayData.getInsFillArrayData();
        ArrayCollection<InsFillArrayData> fillList = ArrayCollection.of(iterator);
        iterator = fillList.iterator();
        while (iterator.hasNext()){
            InsFillArrayData fillArrayData = iterator.next();
            int fillArrayReg = fillArrayData.getRegister(0);
            Ins last = fillArrayData;
            for(int i = 0; i < size; i++){
                int resourceId = insArrayData.getAsInteger(i);
                RField rField = getRField(resourceId);
                if(rField == null){
                    continue;
                }
                Ins[] insArray = last.createNext(new Opcode[]{Opcode.SGET, Opcode.CONST, Opcode.APUT});

                Ins21c insSget = (Ins21c) insArray[0];
                insSget.setRegister(0, reg1);
                insSget.setSectionItem(rField.getFieldKey());

                InsConst insConst = (InsConst) insArray[1];
                insConst.setRegister(0, reg2);
                insConst.setData(i);

                Ins23x aput = (Ins23x) insArray[2];
                aput.setRegister(0, insSget.getRegister(0));
                aput.setRegister(1, fillArrayReg);
                aput.setRegister(2, insConst.getRegister(0));

                last = insArray[insArray.length - 1];

                insArrayData.put(i, 0xfafbfcfe);

                mRArrayCount ++;

                System.err.println(mRArrayCount + ") "
                        + ": [" + i + "] " + HexUtil.toHex8(resourceId) + " --> " + rField.getFieldKey());

            }
            instructionList.remove(fillArrayData);
        }
        instructionList.remove(insArrayData);

    }
    private boolean isAllResourceIds(InsArrayData insArrayData){
        if(insArrayData.getWidth() != 4){
            return false;
        }
        int size = insArrayData.size();
        if(size == 0){
            return false;
        }
        boolean found = false;
        for(int i = 0; i < size; i++){
            int id = insArrayData.getAsInteger(i);
            if(id == 0){
                continue;
            }
            if(!hasRField(id)){
                return false;
            }
            found = true;
        }
        return found;
    }

    public void replaceRIds(){
        mRCount = 0;
        mRArrayCount = 0;
        log("\n\n --------------------- R ---------------------- \n");
        replaceObfRFields();
        log("Searching ins ...");
        ArrayCollection<Ins> insList = ArrayCollection.of(searchRIns());
        log("Found ins: " + insList.size());
        for(Ins ins : insList){
            replaceRIns(ins);
        }
        log("Total R const replaced = " + mRCount);
        log("Total R array replaced = " + mRArrayCount);
        log("\n\n --------------------- DONE ---------------------- \n");
    }
    private static void log(Object message){
        System.err.println(message);
    }
    private final Map<FieldKey, FieldDef> mObfFields = new HashMap<>();
    private void replaceObfRFields(){
        mapObfRFields();
        ArrayCollection<Ins> invokeList = ArrayCollection.of(FilterIterator.of(getInstructions(), this::isObfRInvoke));

        log("OBF R Invokes = " + invokeList.size());
        for(Ins ins : invokeList){
            replaceObfRInvoke(ins);
        }
        log("DONE OBF R Invokes = " + invokeList.size());
    }
    private void replaceObfRInvoke(Ins ins){
        Ins21c ins21c = (Ins21c) ins;
        FieldKey fieldKey = (FieldKey) ins21c.getSectionItemKey();
        FieldDef fieldDef = mObfFields.get(fieldKey);
        IntValue intValue = (IntValue) fieldDef.getStaticInitialValue();
        RField rField = getRField(intValue.get());
        ins21c.setSectionItem(rField.getFieldKey());
    }
    boolean isObfRInvoke(Ins ins) {
        if(ins.getOpcode() != Opcode.SGET){
            return false;
        }
        Ins21c ins21c = (Ins21c) ins;
        FieldKey key = (FieldKey) ins21c.getSectionItemKey();
        return mObfFields.containsKey(key);
    }
    private void mapObfRFields(){
        log("Searching OBF R fields ...");
        Map<FieldKey, FieldDef> obfFields = this.mObfFields;
        obfFields.clear();
        Iterator<FieldDef> iterator = new IterableIterator<ClassId, FieldDef>(searchObfRClasses()) {
            @Override
            public Iterator<FieldDef> iterator(ClassId element) {
                return element.getClassData().getStaticFields();
            }
        };
        while (iterator.hasNext()){
            FieldDef fieldDef = iterator.next();
            obfFields.put(fieldDef.getKey(), fieldDef);
        }
        log("OBF R fields = " + obfFields.size());
    }
    private Iterator<ClassId> searchObfRClasses() {
        return FilterIterator.of(getClassIds(), this::isObfRClass);
    }
    boolean isObfRClass(ClassId classId){
        String name = DexUtils.getSimpleName(classId.getName());
        if(name.indexOf('$') >= 0 || name.equals("R") ){
            return false;
        }
        ClassData classData = classId.getClassData();
        if(classData == null){
            return false;
        }
        Iterator<FieldDef> iterator = classData.getFields();
        boolean found = false;
        while (iterator.hasNext()){
            FieldDef fieldDef = iterator.next();
            if(!isObfRField(fieldDef)){
                return false;
            }
            found = true;
        }
        return found;
    }
    private boolean isObfRField(FieldDef fieldDef){
        if(!fieldDef.isStatic() || !fieldDef.isFinal()){
            return false;
        }
        if(!"I".equals(fieldDef.getKey().getType())){
            return false;
        }
        DexValueBlock<?> valueBlock = fieldDef.getStaticInitialValue();
        if(valueBlock == null || valueBlock.getValueType() != DexValueType.INT){
            return false;
        }
        IntValue intValue = (IntValue) valueBlock;
        return hasRField(intValue.get());
    }
    private Iterator<Ins> searchRIns() {
        return FilterIterator.of(getInstructions(), this::hasRField);
    }
    boolean hasRField(Ins ins){
        if(ins instanceof InsArrayData){
            return isAllResourceIds((InsArrayData) ins);
        }
        if(!(ins instanceof SizeXIns)){
            return false;
        }
        Opcode<?> opcode = ins.getOpcode();
        if(opcode != Opcode.CONST && opcode != Opcode.CONST_HIGH16){
            return false;
        }
        SizeXIns sizeXIns = (SizeXIns) ins;
        return hasRField(sizeXIns.getData());
    }
    private boolean hasRField(int resourceId){
        if(!PackageBlock.isResourceId(resourceId)){
            return false;
        }
        for(RClassParent parent : mRParents){
            if(parent.hasRField(resourceId)){
                return true;
            }
        }
        return false;
    }
    private RField getRField(int resourceId){
        if(!PackageBlock.isResourceId(resourceId)){
            return null;
        }
        for(RClassParent parent : mRParents){
            RField rField = parent.getRField(resourceId);
            if(rField != null){
                return rField;
            }
        }
        return null;
    }
    public void loadRClass(TableBlock tableBlock){
        DexFile dexFile = getMain();
        if(dexFile == null){
            return;
        }
        System.err.println("Creating R on: " + dexFile.getSimpleName());
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            RClassParent rClassParent = dexFile.loadRClass(packageBlock);
            mRParents.add(rClassParent);
        }
    }
    private DexFile getMain(){
        if(dexFileList.size() != 0){
            return dexFileList.get(0);
        }
        return null;
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        for(DexFile dexFile : this){
            dexFile.visitIntegers(visitor);
            save(dexFile);
        }
    }
    public void save(){
        for(DexFile dexFile : this){
            save(dexFile);
        }
    }
    private void save(DexFile dexFile){
        Object tag = dexFile.getTag();
        if(!(tag instanceof File)){
            return;
        }
        File file = (File) tag;
        String name = file.getName();
        File dir = new File(file.getParentFile(), "dex");
        File modFile = new File(dir, name);
        System.err.println("Saving: " + name);
        try {
            dexFile.sortStrings();
            dexFile.refreshFull();
            dexFile.write(modFile);
            System.err.println("Saved: " + modFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    public List<RField> listRFields() {
        List<RField> fieldList = CollectionUtil.toUniqueList(getRFields());
        fieldList.sort(CompareUtil.getComparableComparator());
        return fieldList;
    }
    public Iterator<RField> getRFields() {
        return new MergingIterator<>(ComputeIterator.of(getRClasses(),
                RClass::getStaticFields));
    }
    public Iterator<ClassId> getClassIds() {
        return getItems(SectionType.CLASS_ID);
    }
    public <T1 extends Block> T1 get(SectionType<T1> sectionType, Key key){
        for (DexFile dexFile : this) {
            T1 item = dexFile.get(sectionType, key);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    public <T1 extends Block> Iterator<T1> getAll(SectionType<T1> sectionType, Key key){
        return new IterableIterator<DexFile, T1>(iterator()) {
            @Override
            public Iterator<T1> iterator(DexFile element) {
                return element.getAll(sectionType, key);
            }
        };
    }
    public<T1 extends Block> Iterator<T1> getItems(SectionType<T1> sectionType) {
        return new IterableIterator<DexFile, T1>(iterator()) {
            @Override
            public Iterator<T1> iterator(DexFile element) {
                return element.getItems(sectionType);
            }
        };
    }
    public Iterator<RClass> getRClasses() {
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                DexFile::getRClasses));
    }
    public DexClass get(String name) {
        for(DexFile dexFile : this) {
            DexClass dexClass = dexFile.get(name);
            if(dexClass != null){
                return dexClass;
            }
        }
        return null;
    }
    public Iterator<DexClass> getDexClasses() {
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                DexFile::getDexClasses));
    }
    public Iterator<Ins> getInstructions() {
        return new IterableIterator<DexFile, Ins>(iterator()) {
            @Override
            public Iterator<Ins> iterator(DexFile element) {
                return element.getInstructions();
            }
        };
    }
    @Override
    public Iterator<DexFile> iterator() {
        return getDexFileList().iterator();
    }

    public void clearPools(){
        for(DexFile dexFile : this){
            dexFile.clearPools();
        }
    }
    public void sortStrings(){
        for(DexFile dexFile : this){
            dexFile.sortStrings();
        }
    }
    public void refreshFull(){
        for(DexFile dexFile : this){
            dexFile.refreshFull();
        }
    }
    public void refresh(){
        for(DexFile dexFile : this){
            dexFile.refresh();
        }
    }
    public void addDirectory(File dir) throws IOException {
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File file : files){
            String name = file.getName();
            if(file.isFile() && name.endsWith(".dex") && !name.contains("_mod")){
                add(file);
            }
        }
    }
    public void add(File file) throws IOException {
        DexFile dexFile = DexFile.read(file);
        dexFile.setTag(file);
        dexFile.setSimpleName(file.getName());
        add(dexFile);
    }
    public void add(InputStream inputStream) throws IOException {
        DexFile dexFile = DexFile.read(inputStream);
        add(dexFile);
    }
    public void add(DexFile dexFile){
        if(dexFile == null || dexFileList.contains(dexFile)){
            return;
        }
        dexFileList.add(dexFile);
        dexFile.setDexDirectory(this);
        dexFileList.sort(DexUtils.getDexPathComparator(DexFile::getTag));
    }
    public void rename(TypeKey search, TypeKey replace){
        int count = 0;
        Iterator<StringId> iterator = renameTypes(search, replace, true, true);
        while (iterator.hasNext()){
            StringId stringId = iterator.next();
            count++;
            System.err.println(count + ") Renamed: " + stringId.getString());
        }
        System.err.println(count + ") Renamed: " + search + " to " + replace);
        if(count == 0){
            return;
        }
        clearPools();
        sortStrings();
        refreshFull();
    }

    public Iterator<StringId> renameTypes(TypeKey search, TypeKey replace){
        return renameTypes(search, replace, true, true);
    }
    public Iterator<StringId> renameTypes(TypeKey search, TypeKey replace, boolean renameInner, boolean renameJava){
        return renameTypes(new KeyPair<>(search, replace), renameInner, renameJava);
    }
    public Iterator<StringId> renameTypes(KeyPair<TypeKey, TypeKey> pair, boolean renameInner, boolean renameJava){
        return FilterIterator.of(getItems(SectionType.STRING_ID),
                stringId -> renameTypes(stringId, pair, renameInner, renameJava));
    }
    public Iterator<StringId> renameTypes(Iterable<KeyPair<TypeKey, TypeKey>> iterable, boolean renameInner, boolean renameJava){
        return FilterIterator.of(getItems(SectionType.STRING_ID),
                stringId -> renameTypes(stringId, iterable, renameInner, renameJava));
    }
    boolean renameTypes(StringId stringId, Iterable<KeyPair<TypeKey, TypeKey>> iterable, boolean renameInner, boolean renameJava){
        for(KeyPair<TypeKey, TypeKey> pair : iterable){
            boolean renamed = renameTypes(stringId, pair, renameInner, renameJava);
            if(renamed){
                return true;
            }
        }
        return false;
    }
    boolean renameTypes(StringId stringId, KeyPair<TypeKey, TypeKey> pair, boolean renameInner, boolean renameJava){

        String text = stringId.getString();

        TypeKey search = pair.getFirst();
        TypeKey replace = pair.getSecond();
        String type = search.getType();
        String type2 = replace.getType();

        if(type.equals(text)){
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getType();
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getSignatureTypeName();
        if(type.equals(text)){
            type2 = replace.getSignatureTypeName();
            stringId.setString(type2);
            return true;
        }
        type = search.getArrayType(1);
        if(type.equals(text)){
            type2 = replace.getArrayType(1);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(1);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getArrayType(2);
        if(type.equals(text)){
            type2 = replace.getArrayType(2);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(2);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        type = search.getArrayType(3);
        if(type.equals(text)){
            type2 = replace.getArrayType(3);
            stringId.setString(type2);
            return true;
        }
        if(renameInner){
            type = type.replace(';', '$');
            if(text.startsWith(type)){
                type2 = replace.getArrayType(3);
                type2 = type2.replace(';', '$');
                text = text.substring(type.length());
                stringId.setString(type2 + text);
                return true;
            }
        }
        if(renameJava){
            type = search.toJavaType();
            if(type.equals(text)){
                type2 = replace.toJavaType();
                stringId.setString(type2);
                return true;
            }
            if(renameInner){
                type = type + "$";
                if(text.startsWith(type)){
                    type2 = replace.toJavaType();
                    type2 = type2 + "$";
                    text = text.substring(type.length());
                    stringId.setString(type2 + text);
                    return true;
                }
                type = type + ".";
                if(text.startsWith(type)){
                    type2 = replace.toJavaType();
                    type2 = type2 + ".";
                    text = text.substring(type.length());
                    stringId.setString(type2 + text);
                    return true;
                }
            }
        }
        return false;
    }
    public List<MethodKey> rename(MethodKey methodKey, String name){
        if(containsDeepSearch(methodKey.changeName(name))){
            return EmptyList.of();
        }
        ArrayCollection<MethodId> methodIdList = new ArrayCollection<>();
        methodIdList.add(getMethods(methodKey));
        if(methodIdList.size() == 0){
            return EmptyList.of();
        }
        MethodKey renamed = methodKey.changeName(name);
        for(MethodId methodId : methodIdList){
            if(renamed.equals(methodId.getKey())){
                throw new IllegalArgumentException("Duplicate: " + renamed);
            }
        }
        List<MethodKey> results = new ArrayList<>(methodIdList.size());
        for(MethodId methodId : methodIdList){
            System.err.println(methodIdList.size()+"  RENAMED: " + methodId.getKey());
            methodId.setName(name);
            results.add(methodId.getKey());
        }
        return results;
    }
    public Collection<FieldKey> rename(FieldKey fieldKey, String name){
        ArrayCollection<FieldKey> existingFields = ArrayCollection.of(findEquivalentFields(fieldKey.changeName(name)));
        ArrayCollection<FieldId> fieldIdList = ArrayCollection.of(getFields(fieldKey));
        if(fieldIdList.isEmpty()){
            return EmptyList.of();
        }
        if(!existingFields.isEmpty()){
            throw new IllegalArgumentException("Conflicting fields: " + existingFields.get(0));
        }
        FieldKey renamed = fieldKey.changeName(name);
        for(FieldId fieldId : fieldIdList){
            if(renamed.equals(fieldId.getKey())){
                throw new IllegalArgumentException("Duplicate: " + renamed);
            }
        }
        ArrayCollection<FieldKey> results = new ArrayCollection<>(fieldIdList.size());
        for(FieldId fieldId : fieldIdList){
            fieldId.setName(name);
            results.add(fieldId.getKey());
        }
        return results;
    }
    public boolean containsDeepSearch(MethodKey methodKey){
        DexClass startClass = getDexClass(methodKey.getDefiningKey());
        if(startClass == null){
            return false;
        }
        Iterator<DexClass> iterator = startClass.getOverridingAndSuperTypes();
        while (iterator.hasNext()){
            DexClass dexClass = iterator.next();
            MethodKey key = methodKey.changeDefining(dexClass.getClassName());
            if(methodKey.equals(key, true, false)){
                return true;
            }
        }
        return false;
    }
    public boolean containsDeepSearch(FieldKey fieldKey){
        DexClass startClass = getDexClass(fieldKey.getDefiningKey());
        if(startClass == null){
            return false;
        }
        Iterator<DexClass> iterator = startClass.getOverridingAndSuperTypes();
        while (iterator.hasNext()){
            DexClass dexClass = iterator.next();
            FieldKey key = fieldKey.changeDefining(dexClass.getClassName());
            if(fieldKey.equals(key)){
                return true;
            }
        }
        return false;
    }
    public Iterator<FieldId> getFields(FieldKey fieldKey){
        return new IterableIterator<FieldKey, FieldId>(findEquivalentFields(fieldKey)) {
            @Override
            public Iterator<FieldId> iterator(FieldKey element) {
                return getAll(SectionType.FIELD_ID, element);
            }
        };
    }
    public Iterator<MethodId> getMethods(MethodKey methodKey){
        return new IterableIterator<MethodKey, MethodId>(findEquivalentMethods(methodKey)) {
            @Override
            public Iterator<MethodId> iterator(MethodKey element) {
                return getAll(SectionType.METHOD_ID, element);
            }
        };
    }
    public DexField getDexField(FieldKey fieldKey){
        DexClass dexClass = getDexClass(fieldKey.getDefiningKey());
        if(dexClass != null){
            return dexClass.getField(fieldKey);
        }
        return null;
    }
    public Iterator<DexClass> searchExtending(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getExtendingClasses(typeKey);
            }
        };
    }
    public Iterator<DexClass> searchImplementations(TypeKey typeKey){
        return new IterableIterator<DexFile, DexClass>(iterator()) {
            @Override
            public Iterator<DexClass> iterator(DexFile element) {
                return element.getImplementClasses(typeKey);
            }
        };
    }
    public boolean contains(Key key){
        for(DexFile dexFile : this){
            if(dexFile.contains(key)){
                return true;
            }
        }
        return false;
    }
    public DexClass getDexClass(TypeKey key){
        for(DexFile dexFile : this){
            DexClass dexClass = dexFile.get(key);
            if(dexClass != null){
                return dexClass;
            }
        }
        return null;
    }
    public DexFile get(int i){
        return dexFileList.get(i);
    }
    public int size() {
        return dexFileList.size();
    }
    public void clear() {
        dexFileList.clear();
    }
    public List<DexFile> getDexFileList() {
        return dexFileList;
    }
    public void serializePublicXml(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, PackageBlock.TAG_resources);

        List<RField> fieldList = listRFields();
        for(RField rField : fieldList) {
            rField.serializePublicXml(serializer);
        }

        serializer.text("\n");
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
    }
}
