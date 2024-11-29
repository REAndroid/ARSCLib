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

import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.ProtoId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliMethodParameter;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.ExpandIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodParameter implements DefIndex, SmaliRegion {

    private final MethodDef methodDef;
    private final int index;

    public MethodParameter(MethodDef methodDef, int index) {
        this.methodDef = methodDef;
        this.index = index;
    }

    public void removeSelf() {
        clearAnnotations();
        clearDebugParameter();
    }

    public void clearAnnotations() {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getUniqueAnnotationsDirectory();
        if (directory == null || !hasAnnotationSetBlocks()) {
            return;
        }
        Iterator<DirectoryEntry<MethodDef, AnnotationGroup>> iterator =
                directory.getParameterEntries(methodDef);
        int index = getDefinitionIndex();
        while (iterator.hasNext()) {
            DirectoryEntry<MethodDef, AnnotationGroup> entry = iterator.next();
            AnnotationGroup group = entry.getValue();
            if (group == null || group.getItem(index) == null) {
                continue;
            }
            AnnotationGroup update = group.getSection(SectionType.ANNOTATION_GROUP)
                    .createItem();
            entry.setValue(update);
            update.setItemKeyAt(index, null);
            update.refresh();
        }
    }

    public boolean hasAnnotationSetBlocks() {
        return getAnnotationSetBlocks().hasNext();
    }

    public Iterator<AnnotationItem> getAnnotationItemBlocks() {
        return ExpandIterator.of(getAnnotationSetBlocks());
    }

    public Iterator<AnnotationSet> getAnnotationSetBlocks() {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getAnnotationsDirectory();
        if (directory != null) {
            return directory.getParameterAnnotation(methodDef, getDefinitionIndex());
        }
        return EmptyIterator.of();
    }

    public AnnotationItem addAnnotationItemBlock(TypeKey typeKey) {
        return getOrCreateAnnotationSet().addNewItem(typeKey);
    }

    public AnnotationItem getOrCreateAnnotationItemBlock(TypeKey typeKey) {
        return getOrCreateAnnotationSet().getOrCreate(typeKey);
    }

    public AnnotationSet getOrCreateAnnotationSet() {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getOrCreateUniqueAnnotationsDirectory();
        return directory.getOrCreateParameterAnnotation(methodDef, getDefinitionIndex());
    }

    public AnnotationSet setAnnotationSetBlock(AnnotationSetKey key) {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getOrCreateUniqueAnnotationsDirectory();
        return directory.setParameterAnnotation(methodDef, getDefinitionIndex(), key);
    }

    public TypeKey getType() {
        TypeId typeId = getTypeId();
        if (typeId != null) {
            return typeId.getKey();
        }
        return null;
    }

    public TypeId getTypeId() {
        ProtoId protoId = getMethodDef().getProtoId();
        if (protoId != null) {
            return protoId.getParameter(getDefinitionIndex());
        }
        return null;
    }

    public MethodDef getMethodDef() {
        return methodDef;
    }
    @Override
    public int getDefinitionIndex() {
        return index;
    }

    public int getRegister() {
        MethodDef methodDef = this.getMethodDef();
        int reg;
        if (methodDef.isStatic()) {
            reg = 0;
        } else {
            reg = 1;
        }
        reg += methodDef.getKey().getRegister(getDefinitionIndex());
        return reg;
    }

    public void clearDebugParameter() {
        DebugInfo debugInfo = getMethodDef().getDebugInfo();
        if (debugInfo != null) {
            debugInfo.removeDebugParameter(getDefinitionIndex());
        }
    }

    public String getDebugName() {
        DebugParameter debugParameter = getDebugParameter();
        if (debugParameter != null) {
            return debugParameter.getName();
        }
        return null;
    }

    public void setDebugName(String name) {
        if (StringsUtil.isEmpty(name)) {
            name = null;
        }
        MethodDef methodDef = getMethodDef();
        DebugInfo debugInfo = methodDef.getDebugInfo();
        if (debugInfo == null) {
            if (name == null) {
                return;
            }
            debugInfo = methodDef.getOrCreateDebugInfo();
        }
        if (name == null) {
            debugInfo.removeDebugParameter(getDefinitionIndex());
            return;
        }
        DebugParameter parameter = debugInfo.getOrCreateDebugParameter(
                getDefinitionIndex());
        parameter.setName(name);
    }

    public DebugParameter getDebugParameter() {
        DebugInfo debugInfo = getMethodDef().getDebugInfo();
        if (debugInfo != null) {
            return debugInfo.getDebugParameter(getDefinitionIndex());
        }
        return null;
    }

    @Override
    public Key getKey() {
        TypeId typeId = getTypeId();
        if (typeId != null) {
            return typeId.getKey();
        }
        return null;
    }

    public void fromSmali(SmaliMethodParameter smaliMethodParameter) {
        if (smaliMethodParameter.hasAnnotations()) {
            setAnnotationSetBlock(smaliMethodParameter.getAnnotationSet().getKey());
        }
        setDebugName(smaliMethodParameter.getName());
    }

    public boolean isEmpty() {
        DebugParameter debugParameter = getDebugParameter();
        if (debugParameter != null && debugParameter.getNameId() != null) {
            return false;
        }
        return !getAnnotationItemBlocks().hasNext();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        DebugParameter debugParameter = getDebugParameter();
        boolean has_debug = debugParameter != null &&
                debugParameter.getNameId() != null;
        Iterator<AnnotationSet> annotations = getAnnotationSetBlocks();
        boolean has_annotation = annotations.hasNext();
        if (!has_debug && !has_annotation) {
            return;
        }
        getSmaliDirective().append(writer);
        writer.append('p');
        writer.appendInteger(getRegister());
        if (has_debug) {
            debugParameter.append(writer);
        }
        writer.appendComment(getTypeId().getName());
        if (!has_annotation) {
            return;
        }
        writer.indentPlus();
        writer.appendAllWithDoubleNewLine(annotations);
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public int hashCode() {
        return methodDef.hashCode() * 31 + index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MethodParameter parameter = (MethodParameter) obj;
        return index == parameter.index && this.getMethodDef() == parameter.getMethodDef();
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PARAM;
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
