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

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.item.ClassData;
import com.reandroid.dex.item.FieldDef;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.Iterator;

public class RClass extends DexClass {

    public RClass(DexFile dexFile, ClassId classId) {
        super(dexFile, classId);
    }

    public String toJavaDeclare() {
        return toJavaDeclare(true);
    }
    public String toJavaDeclare(boolean makeFieldsFinal) {
        StringBuilder builder = new StringBuilder();
        builder.append("    public static class ");
        builder.append(getResourceType());
        String indent = "\n        ";
        builder.append(" {");
        Iterator<RField> iterator = getStaticFields();
        while (iterator.hasNext()){
            builder.append(indent);
            builder.append(iterator.next().toJavaDeclare(makeFieldsFinal));
        }
        builder.append('\n');
        builder.append("    }");
        return builder.toString();
    }
    public String getResourceType(){
        return toResourceTypeName(getClassName());
    }

    @Override
    public Iterator<RField> getStaticFields() {
        ClassData classData = getClassData();
        if(classData != null){
            return ComputeIterator.of(classData
                    .getStaticFields().iterator(), this::createRField);
        }
        return EmptyIterator.of();
    }
    private RField createRField(FieldDef fieldDef){
        if(fieldDef.isStatic()){
            fieldDef.setClassId(getClassId());
            if(RField.isResourceIdValue(fieldDef.getStaticInitialValue())){
                return new RField(this, fieldDef);
            }
        }
        return null;
    }
    @Override
    public Iterator<DexField> getFields() {
        return super.getFields();
    }

    @Override
    public String toString() {
        return toJavaDeclare();
    }

    static boolean isRClassName(ClassId classId) {
        if(classId != null){
            return isRClassName(classId.getName());
        }
        return false;
    }
    static boolean isRClassName(String name) {
        if(name == null){
            return false;
        }
        return toResourceTypeName(name) != null;
    }
    private static String toResourceTypeName(String className) {
        String simpleName = DexUtils.getSimpleName(className);
        String prefix = SIMPLE_NAME_PREFIX;
        if(simpleName.length() < 4 || !simpleName.startsWith(prefix)){
            return null;
        }
        simpleName = simpleName.substring(prefix.length());
        if(simpleName.indexOf('$') >= 0){
            return null;
        }
        return simpleName;
    }

    private static final String SIMPLE_NAME_PREFIX = "R$";

    static final TableBlock EMPTY_TABLE = new TableBlock();
}
