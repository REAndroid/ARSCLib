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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.index.*;

public class IndexSections extends FixedBlockContainer {

    private final DexSection<TypeIndex> typeSection;
    private final DexSection<ProtoIndex> protoSection;
    private final DexSection<FieldIndex> fieldSection;
    private final DexSection<MethodIndex> methodSection;
    private final DexSection<ClassIndex> classSection;

    public IndexSections(DexHeader header) {
        super(5);

        this.typeSection = new DexSection<>(header.type, CREATOR_TYPE);
        this.protoSection = new DexSection<>(header.proto, CREATOR_PROTO);
        this.fieldSection = new DexSection<>(header.field, CREATOR_FIELD);
        this.methodSection = new DexSection<>(header.method, CREATOR_METHOD);
        this.classSection = new DexSection<>(header.class_def, CREATOR_CLASS);

        addChild(0, typeSection);
        addChild(1, protoSection);
        addChild(2, fieldSection);
        addChild(3, methodSection);
        addChild(4, classSection);
    }

    public TypeIndex getTypeIndex(int i){
        return getTypeSection().get(i);
    }
    public ProtoIndex getProtoIndex(int i){
        return getProtoSection().get(i);
    }
    public FieldIndex getFieldIndex(int i){
        return getFieldSection().get(i);
    }
    public MethodIndex getMethodIndex(int i){
        return getMethodSection().get(i);
    }
    public ClassIndex getClassIndex(int i){
        return getClassSection().get(i);
    }

    public DexSection<TypeIndex> getTypeSection() {
        return typeSection;
    }
    public DexSection<ProtoIndex> getProtoSection() {
        return protoSection;
    }
    public DexSection<FieldIndex> getFieldSection() {
        return fieldSection;
    }
    public DexSection<MethodIndex> getMethodSection(){
        return methodSection;
    }
    public DexSection<ClassIndex> getClassSection() {
        return classSection;
    }


    private static final Creator<TypeIndex> CREATOR_TYPE = new Creator<TypeIndex>() {
        @Override
        public TypeIndex[] newInstance(int length) {
            return new TypeIndex[length];
        }
        @Override
        public TypeIndex newInstance() {
            return new TypeIndex();
        }
    };
    private static final Creator<ProtoIndex> CREATOR_PROTO = new Creator<ProtoIndex>() {
        @Override
        public ProtoIndex[] newInstance(int length) {
            return new ProtoIndex[length];
        }
        @Override
        public ProtoIndex newInstance() {
            return new ProtoIndex();
        }
    };
    private static final Creator<FieldIndex> CREATOR_FIELD = new Creator<FieldIndex>() {
        @Override
        public FieldIndex[] newInstance(int length) {
            return new FieldIndex[length];
        }
        @Override
        public FieldIndex newInstance() {
            return new FieldIndex();
        }
    };
    private static final Creator<ClassIndex> CREATOR_CLASS = new Creator<ClassIndex>() {
        @Override
        public ClassIndex[] newInstance(int length) {
            return new ClassIndex[length];
        }
        @Override
        public ClassIndex newInstance() {
            return new ClassIndex();
        }
    };
    private static final Creator<MethodIndex> CREATOR_METHOD = new Creator<MethodIndex>() {
        @Override
        public MethodIndex[] newInstance(int length) {
            return new MethodIndex[length];
        }
        @Override
        public MethodIndex newInstance() {
            return new MethodIndex();
        }
    };
}
