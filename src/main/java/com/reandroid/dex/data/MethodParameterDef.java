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

import com.reandroid.dex.common.DefIndex;
import com.reandroid.dex.dalvik.DalvikSignature;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.id.ProtoId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.DalvikSignatureKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.ParameterisedTypeKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.program.MethodParameter;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliMethodParameter;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;

public class MethodParameterDef implements DefIndex, MethodParameter, SmaliRegion {

    private final MethodDef methodDef;
    private final int index;

    public MethodParameterDef(MethodDef methodDef, int index) {
        this.methodDef = methodDef;
        this.index = index;
    }

    @Override
    public TypeKey getKey() {
        TypeId typeId = getTypeId();
        if (typeId != null) {
            return typeId.getKey();
        }
        return null;
    }

    @Override
    public AnnotationSetKey getAnnotation() {
        AnnotationsDirectory directory = getMethodDef()
                .getAnnotationsDirectory();
        if (directory != null) {
            return directory.getParameter(this);
        }
        return AnnotationSetKey.empty();
    }
    @Override
    public void setAnnotation(AnnotationSetKey annotationSet) {
        MethodDef methodDef = getMethodDef();
        if (annotationSet == null || annotationSet.isEmpty()) {
            AnnotationsDirectory directory = methodDef.getAnnotationsDirectory();
            if (directory != null && directory.contains(this)) {
                directory = methodDef.getOrCreateUniqueAnnotationsDirectory();
                directory.put(this, null);
            }
        } else {
            methodDef.getOrCreateUniqueAnnotationsDirectory()
                    .put(this, annotationSet);
        }
    }
    @Override
    public void clearAnnotations() {
        setAnnotation(AnnotationSetKey.empty());
    }
    @Override
    public boolean hasAnnotations() {
        AnnotationsDirectory directory = getMethodDef()
                .getAnnotationsDirectory();
        return directory != null && directory.contains(this);
    }
    @Override
    public AnnotationItemKey getAnnotation(TypeKey typeKey) {
        AnnotationsDirectory directory = getMethodDef()
                .getAnnotationsDirectory();
        if (directory != null) {
            return directory.getAnnotation(this, typeKey);
        }
        return null;
    }
    @Override
    public Key getAnnotationValue(TypeKey typeKey, String name) {
        AnnotationsDirectory directory = getMethodDef()
                .getAnnotationsDirectory();
        if (directory != null) {
            return directory.getAnnotationValue(this, typeKey, name);
        }
        return null;
    }

    public void onRemoved() {
        clearAnnotations();
        clearDebugParameter();
    }

    private TypeId getTypeId() {
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
    @Override
    public int getRegister() {
        MethodDef methodDef = this.getMethodDef();
        int reg;
        if (methodDef.isStatic()) {
            reg = 0;
        } else {
            reg = 1;
        }
        reg += methodDef.getProtoKey().getRegister(getDefinitionIndex());
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

    public void fromSmali(SmaliMethodParameter smaliMethodParameter) {
        fromSmali(smaliMethodParameter, true);
    }
    public void fromSmali(SmaliMethodParameter smaliMethodParameter, boolean withAnnotations) {
        if (withAnnotations && smaliMethodParameter.hasAnnotations()) {
            setAnnotation(smaliMethodParameter.getAnnotation());
        }
        setDebugName(smaliMethodParameter.getDebugName());
    }

    public ParameterisedTypeKey getParameterisedTypeKey() {
        DalvikSignature dalvikSignature = DalvikSignature.of(getMethodDef());
        if (dalvikSignature != null) {
            DalvikSignatureKey signatureKey = dalvikSignature.getSignature();
            if (signatureKey != null) {
                ParameterisedTypeKey typeKey = signatureKey.getProtoParameter(
                        getDefinitionIndex());
                if (typeKey != null && typeKey.isParametrisedType()) {
                    return typeKey;
                }
            }
        }
        return null;
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
        writer.newLine();
        getSmaliDirective().append(writer);
        writer.append('p');
        writer.appendInteger(getRegister());
        if (has_debug) {
            debugParameter.append(writer);
        }
        ParameterisedTypeKey typeKey = getParameterisedTypeKey();
        if (typeKey != null) {
            writer.appendComment(typeKey.getComment());
        } else {
            writer.appendComment(getKey().getTypeName());
        }
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
        MethodParameterDef parameter = (MethodParameterDef) obj;
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
