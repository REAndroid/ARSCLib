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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliClass;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;

public class ClassData extends DataItem implements SmaliFormat {

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldsCount;
    private final Ule128Item directMethodsCount;
    private final Ule128Item virtualMethodCount;

    private StaticFieldDefArray staticFields;
    private FieldDefArray instanceFields;
    private MethodDefArray directMethods;
    private MethodDefArray virtualMethods;

    private ClassId mClassId;

    public ClassData() {
        super(8);
        this.staticFieldsCount = new Ule128Item();
        this.instanceFieldsCount = new Ule128Item();
        this.directMethodsCount = new Ule128Item();
        this.virtualMethodCount = new Ule128Item();

        addChildBlock(0, staticFieldsCount);
        addChildBlock(1, instanceFieldsCount);
        addChildBlock(2, directMethodsCount);
        addChildBlock(3, virtualMethodCount);
    }

    @Override
    public SectionType<ClassData> getSectionType() {
        return SectionType.CLASS_DATA;
    }

    public void remove(Key key) {
        Def<?> def = get(key);
        if (def != null) {
            def.removeSelf();
        }
    }
    public Def<?> get(Key key) {
        if (key instanceof FieldKey) {
            return getField((FieldKey) key);
        }
        if (key instanceof MethodKey) {
            return getMethod((MethodKey) key);
        }
        if (key != null) {
            throw new RuntimeException("Unknown key type: "
                    + key.getClass() + ", '" + key + "'");
        }
        return null;
    }
    public FieldDef getField(FieldKey key) {
        FieldDef fieldDef = null;
        FieldDefArray fieldDefArray = this.staticFields;
        if (fieldDefArray != null) {
            fieldDef = fieldDefArray.get(key);
        }
        if (fieldDef == null) {
            fieldDefArray = this.instanceFields;
            if (fieldDefArray != null) {
                fieldDef = fieldDefArray.get(key);
            }
        }
        return fieldDef;
    }
    public MethodDef getMethod(MethodKey key) {
        MethodDef methodDef = null;
        MethodDefArray methodDefArray = this.directMethods;
        if (methodDefArray != null) {
            methodDef = methodDefArray.get(key);
        }
        if (methodDef == null) {
            methodDefArray = this.virtualMethods;
            if (methodDefArray != null) {
                methodDef = methodDefArray.get(key);
            }
        }
        return methodDef;
    }
    public FieldDef getOrCreateStatic(FieldKey fieldKey) {
        FieldDef fieldDef = initStaticFieldsArray().getOrCreate(fieldKey);
        fieldDef.addAccessFlag(AccessFlag.STATIC);
        return fieldDef;
    }
    public FieldDef getOrCreateInstance(FieldKey fieldKey) {
        return initInstanceFieldsArray().getOrCreate(fieldKey);
    }


    public MethodDef getOrCreateDirect(MethodKey methodKey) {
        return initDirectMethodsArray().getOrCreate(methodKey);
    }
    public MethodDef getOrCreateVirtual(MethodKey methodKey) {
        return initVirtualMethodsArray().getOrCreate(methodKey);
    }
    public Iterator<FieldDef> getFields() {
        return new CombiningIterator<>(getStaticFields(), getInstanceFields());
    }
    public Iterator<MethodDef> getMethods() {
        return new CombiningIterator<>(getDirectMethods(), getVirtualMethods());
    }
    public Iterator<MethodDef> getDirectMethods() {
        MethodDefArray methodDefArray = this.directMethods;
        if (methodDefArray == null) {
            return EmptyIterator.of();
        }
        return methodDefArray.arrayIterator();
    }
    public int getDirectMethodsCount() {
        MethodDefArray methodDefArray = this.directMethods;
        if (methodDefArray != null) {
            return methodDefArray.size();
        }
        return 0;
    }
    public int getVirtualMethodsCount() {
        MethodDefArray methodDefArray = this.virtualMethods;
        if (methodDefArray != null) {
            return methodDefArray.size();
        }
        return 0;
    }

    public Iterator<MethodDef> getVirtualMethods() {
        MethodDefArray methodDefArray = this.virtualMethods;
        if (methodDefArray == null) {
            return EmptyIterator.of();
        }
        return methodDefArray.arrayIterator();
    }
    public Iterator<FieldDef> getStaticFields() {
        FieldDefArray fieldDefArray = this.staticFields;
        if (fieldDefArray == null) {
            return EmptyIterator.of();
        }
        return fieldDefArray.arrayIterator();
    }
    public Iterator<FieldDef> getInstanceFields() {
        FieldDefArray fieldDefArray = this.instanceFields;
        if (fieldDefArray == null) {
            return EmptyIterator.of();
        }
        return fieldDefArray.arrayIterator();
    }
    public int getStaticFieldsCount() {
        FieldDefArray fieldDefArray = this.staticFields;
        if (fieldDefArray != null) {
            return fieldDefArray.size();
        }
        return 0;
    }
    public int getInstanceFieldsCount() {
        FieldDefArray fieldDefArray = this.instanceFields;
        if (fieldDefArray != null) {
            return fieldDefArray.size();
        }
        return 0;
    }
    public StaticFieldDefArray getStaticFieldsArray() {
        return staticFields;
    }
    public FieldDefArray getInstanceFieldsArray() {
        return instanceFields;
    }
    public MethodDefArray getDirectMethodsArray() {
        return directMethods;
    }
    public MethodDefArray getVirtualMethodArray() {
        return virtualMethods;
    }

    private FieldDefArray initStaticFieldsArray() {
        StaticFieldDefArray defArray = this.staticFields;
        if (defArray == null) {
            defArray = new StaticFieldDefArray(staticFieldsCount);
            this.staticFields = defArray;
            addChildBlock(4, staticFields);
            defArray.setClassId(getClassId());
        }
        return defArray;
    }
    private FieldDefArray initInstanceFieldsArray() {
        FieldDefArray defArray = this.instanceFields;
        if (defArray == null) {
            defArray = new FieldDefArray(instanceFieldsCount);
            this.instanceFields = defArray;
            addChildBlock(5, instanceFields);
            defArray.setClassId(getClassId());
        }
        return defArray;
    }
    private MethodDefArray initDirectMethodsArray() {
        MethodDefArray defArray = this.directMethods;
        if (defArray == null) {
            defArray = new MethodDefArray(directMethodsCount);
            this.directMethods = defArray;
            addChildBlock(6, defArray);
            defArray.setClassId(getClassId());
        }
        return defArray;
    }
    private MethodDefArray initVirtualMethodsArray() {
        MethodDefArray defArray = this.virtualMethods;
        if (defArray == null) {
            defArray = new MethodDefArray(virtualMethodCount);
            this.virtualMethods = defArray;
            addChildBlock(7, defArray);
            defArray.setClassId(getClassId());
        }
        return defArray;
    }
    private Iterator<DefArray<?>> getDefArrays() {
        return CombiningIterator.four(
                SingleIterator.of(staticFields),
                SingleIterator.of(instanceFields),
                SingleIterator.of(directMethods),
                SingleIterator.of(virtualMethods)
        );
    }


    public ClassId getClassId() {
        return mClassId;
    }
    public void setClassId(ClassId classId) {
        this.mClassId = classId;
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()) {
            iterator.next().setClassId(classId);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        if (staticFieldsCount.get() != 0) {
            initStaticFieldsArray().onReadBytes(reader);
        }
        if (instanceFieldsCount.get() != 0) {
            initInstanceFieldsArray().onReadBytes(reader);
        }
        if (directMethodsCount.get() != 0) {
            initDirectMethodsArray().onReadBytes(reader);
        }
        if (virtualMethodCount.get() != 0) {
            initVirtualMethodsArray().onReadBytes(reader);
        }
    }

    @Override
    public boolean isBlank() {
        return isNullOrEmpty(staticFields) &&
                isNullOrEmpty(instanceFields) &&
                isNullOrEmpty(directMethods) &&
                isNullOrEmpty(virtualMethods);
    }

    @Override
    public void removeSelf() {
        super.removeSelf();
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()) {
            iterator.next().clearChildes();
        }
        setClassId(null);
    }
    public void replaceKeys(Key search, Key replace) {
        Iterator<DefArray<?>> iterator = getDefArrays();
        while (iterator.hasNext()) {
            iterator.next().replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        if (staticFields != null) {
            staticFields.editInternal(user);
        }
        if (instanceFields != null) {
            instanceFields.editInternal(user);
        }
        if (directMethods != null) {
            directMethods.editInternal(user);
        }
        if (virtualMethods != null) {
            virtualMethods.editInternal(user);
        }
    }

    @Override
    public Iterator<IdItem> usedIds() {
        return new IterableIterator<DefArray<?>, IdItem>(getDefArrays()) {
            @Override
            public Iterator<IdItem> iterator(DefArray<?> element) {
                return element.usedIds();
            }
        };
    }
    public void merge(ClassData classData) {
        ClassId classId = this.getClassId();
        if (!isNullOrEmpty(classData.staticFields)) {
            FieldDefArray defArray = initStaticFieldsArray();
            defArray.setClassId(classId);
            defArray.merge(classData.staticFields);
        }
        if (!isNullOrEmpty(classData.instanceFields)) {
            FieldDefArray defArray = initInstanceFieldsArray();
            defArray.setClassId(classId);
            defArray.merge(classData.instanceFields);
        }
        if (!isNullOrEmpty(classData.directMethods)) {
            MethodDefArray methodsArray = initDirectMethodsArray();
            methodsArray.setClassId(classId);
            methodsArray.merge(classData.directMethods);
        }
        if (!isNullOrEmpty(classData.virtualMethods)) {
            MethodDefArray methodsArray = initVirtualMethodsArray();
            methodsArray.setClassId(classId);
            methodsArray.merge(classData.virtualMethods);
        }
    }
    public void fromSmali(SmaliClass smaliClass) throws IOException {
        Iterator<? extends Smali> iterator;

        iterator = smaliClass.getStaticFields();
        if (iterator.hasNext()) {
            initStaticFieldsArray().fromSmali(iterator);
        }

        iterator = smaliClass.getInstanceFields();
        if (iterator.hasNext()) {
            initInstanceFieldsArray().fromSmali(iterator);
        }

        iterator = smaliClass.getDirectMethods();
        if (iterator.hasNext()) {
            initDirectMethodsArray().fromSmali(iterator);
        }

        iterator = smaliClass.getVirtualMethods();
        if (iterator.hasNext()) {
            initVirtualMethodsArray().fromSmali(iterator);
        }
    }
    public void toSmali(SmaliClass smaliClass) {
        if (staticFields != null) {
            smaliClass.addFields(ObjectsUtil.cast(staticFields.toSmali()));
        }
        if (instanceFields != null) {
            smaliClass.addFields(ObjectsUtil.cast(instanceFields.toSmali()));
        }
        if (directMethods != null) {
            smaliClass.addMethods(ObjectsUtil.cast(directMethods.toSmali()));
        }
        if (virtualMethods != null) {
            smaliClass.addMethods(ObjectsUtil.cast(virtualMethods.toSmali()));
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendOptional(staticFields, "static fields");
        writer.appendOptional(instanceFields, "instance fields");
        writer.appendOptional(directMethods, "direct methods");
        writer.appendOptional(virtualMethods, "virtual methods");
    }
    @Override
    public String toString() {
        return "staticFieldsCount=" + staticFieldsCount +
                ", instanceFieldCount=" + instanceFieldsCount +
                ", directMethodCount=" + directMethodsCount +
                ", virtualMethodCount=" + virtualMethodCount;
    }

    private static boolean isNullOrEmpty(DefArray<?> defArray) {
        return defArray == null || defArray.size() == 0;
    }
}
