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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.ins.Ins35c;
import com.reandroid.dex.ins.Opcode;
import com.reandroid.dex.item.*;
import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.value.ArrayValue;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.value.TypeValue;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.io.IOUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.*;

public class RClassParent extends DexClass implements IntegerVisitor {

    private final Map<String, RClass> mMembers;
    private final Map<Integer, RField> mFieldsMap;

    public RClassParent(DexFile dexFile, ClassId classId) {
        super(dexFile, classId);
        this.mMembers = new HashMap<>();
        this.mFieldsMap = new HashMap<>();
    }

    @Override
    public void visit(Object sender, IntegerReference reference) {
        DexFile.replaceRFields(getDexFile(), mFieldsMap, reference);
    }
    public void replaceConstIds(){
        updateFields();
        getDexFile().visitIntegers(this);
    }
    private void updateFields(){
        Map<Integer, RField> map = this.mFieldsMap;
        map.clear();
        for(RClass rClass : mMembers.values()){
            Iterator<RField> iterator = rClass.getStaticFields();
            while (iterator.hasNext()){
                RField rField = iterator.next();
                map.put(rField.getResourceId(), rField);
            }
        }
    }

    public void load(PackageBlock packageBlock){
        Iterator<ResourceEntry> iterator = packageBlock.getResources();
        while (iterator.hasNext()){
            load(iterator.next());
        }
    }
    private void load(ResourceEntry entry){
        RClass rClass = getOrCreateMember(entry.getType());
        rClass.load(entry);
    }
    public RClass getOrCreateMember(String simpleName){
        String type = toMemberName(simpleName);
        RClass rClass = mMembers.get(type);
        if(rClass != null){
            return rClass;
        }
        addMemberAnnotation(simpleName);
        ClassId classId = getDexFile().getOrCreateClassId(new TypeKey(type));
        rClass = new RClass(getDexFile(), classId);
        mMembers.put(type, rClass);
        rClass.initialize();
        return rClass;
    }
    public void addMemberAnnotation(String simpleName){
        ArrayValue arrayValue = getOrCreateMembersArray();
        Iterator<String> iterator = FilterIterator.of(getMemberSimpleNames(), simpleName::equals);
        if(iterator.hasNext()){
            return;
        }
        String type = toMemberName(simpleName);
        TypeValue typeValue = arrayValue.createNext(DexValueType.TYPE);
        typeValue.set(new TypeKey(type));
    }
    public Iterator<String> getMemberSimpleNames(){
        return ComputeIterator.of(getMemberNames(), DexUtils::getInnerSimpleName);
    }
    private String toMemberName(String simpleName){
        String type = getClassName();
        type = type.substring(0, type.length() - 1);
        return type + "$" + simpleName + ";";
    }
    public Iterator<String> getMemberNames(){
        ArrayValue arrayValue = getOrCreateMembersArray();
        return ComputeIterator.of(arrayValue.iterator(TypeValue.class), TypeValue::getType);
    }
    private ArrayValue getOrCreateMembersArray(){
        AnnotationItem item = getOrCreateMemberAnnotation();
        AnnotationElement element = item.getElement("value");
        DexValueBlock<?> value = element.getValue();
        if(value == null){
            ArrayValue array = DexValueType.ARRAY.newInstance();
            element.setValue(array);
            value = array;
        }
        return (ArrayValue) value;
    }
    private AnnotationItem getOrCreateMemberAnnotation(){
        AnnotationSet annotationSet = getOrCreateClassAnnotations();
        AnnotationKey key = new AnnotationKey(DexUtils.DALVIK_MEMBER, "value");
        AnnotationItem item = annotationSet.get(key);
        if(item != null){
            return item;
        }
        item = annotationSet.getOrCreate(key);
        item.setVisibility(AnnotationVisibility.SYSTEM);
        AnnotationElement element = item.getElement(key.getName());
        ArrayValue array = DexValueType.ARRAY.newInstance();
        element.setValue(array);
        return item;
    }
    private AnnotationSet getOrCreateClassAnnotations() {
        AnnotationSet annotationSet = getClassAnnotations();
        if(annotationSet != null){
            return annotationSet;
        }
        annotationSet = getClassId().getOrCreateClassAnnotations();
        return annotationSet;
    }
    private AnnotationSet getClassAnnotations() {
        return getClassId().getClassAnnotations();
    }
    public void initialize(){
        ClassId classId = getClassId();
        classId.addAccessFlag(AccessFlag.PUBLIC);
        ClassData classData = classId.getOrCreateClassData();
        MethodKey methodKey = new MethodKey(classId.getName(), "<init>", null, "V");
        if(classData.getDirectMethods().get(methodKey) != null){
            return;
        }
        MethodDef methodDef = classData.getDirectMethods().getOrCreate(methodKey);
        methodDef.addAccessFlags(AccessFlag.PUBLIC, AccessFlag.CONSTRUCTOR);
        InstructionList insList = methodDef.getOrCreateInstructionList();
        Ins35c ins = insList.createNext(Opcode.INVOKE_DIRECT);
        ins.setSectionItem(MethodKey.parse("Ljava/lang/Object;-><init>()V"));
        ins.setRegistersCount(1);
        ins.setRegister(0, 0);
        insList.createNext(Opcode.RETURN_VOID);
    }
    static boolean isRParentClassName(ClassId classId) {
        if(classId != null){
            return isRParentClassName(classId.getName());
        }
        return false;
    }
    static boolean isRParentClassName(String name) {
        if(name == null){
            return false;
        }
        return SIMPLE_NAME_PREFIX.equals(DexUtils.getSimpleName(name));
    }
    public static void serializePublicXml(Collection<RField> rFields, XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.text("\n");
        serializer.startTag(null, PackageBlock.TAG_resources);

        List<RField> fieldList = new ArrayList<>(rFields);
        fieldList.sort(CompareUtil.getComparableComparator());
        for(RField rField : fieldList) {
            rField.serializePublicXml(serializer);
        }

        serializer.text("\n");
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
        serializer.flush();
        IOUtil.close(serializer);
    }

    private static final String SIMPLE_NAME_PREFIX = "R";

}
