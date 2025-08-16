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

import com.reandroid.dex.key.AnnotationGroupKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.ProtoKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;

import java.util.Iterator;

public class SmaliParamSet extends SmaliSet<SmaliMethodParameter> {

    public SmaliParamSet() {
        super();
    }

    public boolean isDuplicateRegister(SmaliMethodParameter parameter) {
        int size = size();
        int number = parameter.getRegister();
        for (int i = 0; i < size; i++) {
            SmaliMethodParameter p = get(i);
            if (p != parameter && number == p.getRegister()) {
                return true;
            }
        }
        return false;
    }
    public boolean hasParameterAnnotations() {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (get(i).hasAnnotations()) {
                return true;
            }
        }
        return false;
    }

    public AnnotationGroupKey getParameterAnnotations() {
        return getParameterAnnotations(getParentInstance(SmaliMethod.class));
    }
    public AnnotationGroupKey getParameterAnnotations(SmaliMethod smaliMethod) {
        if (isEmpty()) {
            return AnnotationGroupKey.empty();
        }
        if (smaliMethod == null) {
            return AnnotationGroupKey.empty();
        }
        ProtoKey protoKey = smaliMethod.getProtoKey();
        if (protoKey == null || protoKey.getParametersCount() == 0) {
            return AnnotationGroupKey.empty();
        }
        boolean is_static = smaliMethod.isStatic();
        AnnotationSetKey[] elements = null;
        Iterator<SmaliMethodParameter> iterator = iterator();
        while (iterator.hasNext()) {
            SmaliMethodParameter parameter = iterator.next();
            int i = parameter.getDefinitionIndex(is_static, protoKey);
            if (i < 0) {
                return AnnotationGroupKey.empty();
            }
            AnnotationSetKey setKey = parameter.getAnnotation();
            if (!setKey.isEmpty()) {
                if (elements == null) {
                    elements = new AnnotationSetKey[protoKey.getParametersCount()];
                }
                elements[i] = setKey;
            }
        }
        return AnnotationGroupKey.of(elements);
    }

    @Override
    SmaliMethodParameter createNext(SmaliReader reader) {
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive == SmaliDirective.PARAM) {
            return new SmaliMethodParameter();
        }
        return null;
    }
}
