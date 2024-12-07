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
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.MethodParameterProgram;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliMethodParameter;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.ExpandIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodParameter implements DefIndex, MethodParameterProgram, SmaliRegion {

    private final MethodDef methodDef;
    private final int index;

    public MethodParameter(MethodDef methodDef, int index) {
        this.methodDef = methodDef;
        this.index = index;
    }

    @Override
    public AnnotationSetKey getAnnotation() {
        return AnnotationSetKey.create(
                ComputeIterator.of(getAnnotationItemBlocks(), AnnotationItem::getKey));
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotationSet) {
        clearAnnotations();
        writeAnnotation(annotationSet);
    }
    @Override
    public void clearAnnotations() {
        writeAnnotation(AnnotationSetKey.empty());
    }

    public void onRemoved() {
        clearAnnotations();
        clearDebugParameter();
    }

    private boolean hasAnnotationSetBlocks() {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getAnnotationsDirectory();
        if (directory != null) {
            return directory.getParameterAnnotation(methodDef,
                    getDefinitionIndex()).hasNext();
        }
        return false;
    }

    private Iterator<AnnotationItem> getAnnotationItemBlocks() {
        MethodDef methodDef = getMethodDef();
        AnnotationsDirectory directory = methodDef.getAnnotationsDirectory();
        if (directory != null) {
            return ExpandIterator.of(directory
                    .getParameterAnnotation(methodDef, getDefinitionIndex()));
        }
        return EmptyIterator.of();
    }

    private void writeAnnotation(AnnotationSetKey key) {
        MethodDef methodDef = getMethodDef();
        int index = getDefinitionIndex();
        if (key == null || key.isEmpty()) {
            if (hasAnnotationSetBlocks()) {
                AnnotationsDirectory directory = methodDef.getOrCreateUniqueAnnotationsDirectory();
                directory.removeParameterAnnotation(methodDef, index);
            }
        } else {
            AnnotationsDirectory directory = methodDef.getOrCreateUniqueAnnotationsDirectory();
            directory.removeParameterAnnotation(methodDef, index);
            directory.setParameterAnnotation(methodDef, index, key);
        }
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

    @Override
    public String getDebugName() {
        DebugParameter debugParameter = getDebugParameter();
        if (debugParameter != null) {
            return debugParameter.getName();
        }
        return null;
    }
    @Override
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
    public TypeKey getKey() {
        TypeId typeId = getTypeId();
        if (typeId != null) {
            return typeId.getKey();
        }
        return null;
    }

    public void fromSmali(SmaliMethodParameter smaliMethodParameter) {
        if (smaliMethodParameter.hasAnnotations()) {
            setAnnotation(smaliMethodParameter.getSmaliAnnotationSet().getKey());
        }
        setDebugName(smaliMethodParameter.getDebugName());
    }

    public boolean isEmpty() {
        DebugParameter debugParameter = getDebugParameter();
        if (debugParameter != null && debugParameter.getNameId() != null) {
            return false;
        }
        return !hasAnnotations();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        DebugParameter debugParameter = getDebugParameter();
        boolean has_debug = debugParameter != null &&
                debugParameter.getNameId() != null;
        AnnotationSetKey annotation = getAnnotation();
        boolean has_annotation = !annotation.isEmpty();
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
        writer.appendAllWithDoubleNewLine(annotation.iterator());
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
