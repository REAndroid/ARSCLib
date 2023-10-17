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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.base.WarpedIntegerReference;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.id.*;
import com.reandroid.dex.data.*;
import com.reandroid.utils.HexUtil;

import java.util.Comparator;
import java.util.function.Function;


public class SectionType<T extends Block> {

    public static final SectionType<?>[] VALUES;
    private static final SectionType<?>[] READ_ORDER;

    private static final SectionType<?>[] R8_ORDER;
    private static final SectionType<?>[] DEX_LIB2_ORDER;

    public static final SectionType<DexHeader> HEADER;

    public static final SectionType<StringId> STRING_ID;
    public static final SectionType<TypeId> TYPE_ID;
    public static final SectionType<ProtoId> PROTO_ID;
    public static final SectionType<FieldId> FIELD_ID;
    public static final SectionType<MethodId> METHOD_ID;
    public static final SectionType<ClassId> CLASS_ID;
    public static final SectionType<IdItem> CALL_SITE_ID;
    public static final SectionType<MethodHandle> METHOD_HANDLE;

    public static final SectionType<MapList> MAP_LIST;
    public static final SectionType<TypeList> TYPE_LIST;
    public static final SectionType<AnnotationGroup> ANNOTATION_GROUP;
    public static final SectionType<AnnotationSet> ANNOTATION_SET;
    public static final SectionType<ClassData> CLASS_DATA;
    public static final SectionType<CodeItem> CODE;
    public static final SectionType<StringData> STRING_DATA;
    public static final SectionType<DebugInfo> DEBUG_INFO;
    public static final SectionType<AnnotationItem> ANNOTATION;
    public static final SectionType<EncodedArray> ENCODED_ARRAY;
    public static final SectionType<AnnotationsDirectory> ANNOTATIONS_DIRECTORY;
    public static final SectionType<DataItem> HIDDEN_API;

    static {

        VALUES = new SectionType[21];
        int index = 0;

        HEADER = new SectionType<>("HEADER", 0x0000, index, new Creator<DexHeader>() {
            @Override
            public DexHeader[] newInstance(int length) {
                return new DexHeader[length];
            }
            @Override
            public DexHeader newInstance() {
                return new DexHeader();
            }
        });
        VALUES[index++] = HEADER;

        MAP_LIST = new DataSectionType<>("MAP_LIST", 0x1000, index, new Creator<MapList>() {
            @Override
            public MapList[] newInstance(int length) {
                return new MapList[length];
            }
            @Override
            public MapList newInstance() {
                return new MapList(new WarpedIntegerReference());
            }
        });
        VALUES[index++] = MAP_LIST;

        STRING_ID = new StringIdSectionType("STRING_ID", 0x0001, index, 0, new Creator<StringId>() {
            @Override
            public StringId[] newInstance(int length) {
                return new StringId[length];
            }
            @Override
            public StringId newInstance() {
                return new StringId();
            }
        });
        VALUES[index++] = STRING_ID;

        STRING_DATA = new StringDataSectionType("STRING_DATA", 0x2002, index, new Creator<StringData>() {
            @Override
            public StringData[] newInstance(int length) {
                return new StringData[length];
            }
            @Override
            public StringData newInstance() {
                return new StringData();
            }
        });
        VALUES[index++] = STRING_DATA;

        TYPE_ID = new IdSectionType<>("TYPE_ID", 0x0002, index, 1,  new Creator<TypeId>() {
            @Override
            public TypeId[] newInstance(int length) {
                return new TypeId[length];
            }
            @Override
            public TypeId newInstance() {
                return new TypeId();
            }
        });
        VALUES[index++] = TYPE_ID;

        TYPE_LIST = new DataSectionType<>("TYPE_LIST", 0x1001, index, new Creator<TypeList>() {
            @Override
            public TypeList[] newInstance(int length) {
                return new TypeList[length];
            }
            @Override
            public TypeList newInstance() {
                return new TypeList();
            }
        });
        VALUES[index++] = TYPE_LIST;

        PROTO_ID = new IdSectionType<>("PROTO_ID", 0x0003, index, 4, new Creator<ProtoId>() {
            @Override
            public ProtoId[] newInstance(int length) {
                return new ProtoId[length];
            }
            @Override
            public ProtoId newInstance() {
                return new ProtoId();
            }
        });
        VALUES[index++] = PROTO_ID;

        FIELD_ID = new IdSectionType<>("FIELD_ID", 0x0004, index, 2, new Creator<FieldId>() {
            @Override
            public FieldId[] newInstance(int length) {
                return new FieldId[length];
            }

            @Override
            public FieldId newInstance() {
                return new FieldId();
            }
        });
        VALUES[index++] = FIELD_ID;

        METHOD_ID = new IdSectionType<>("METHOD_ID", 0x0005, index, 3, new Creator<MethodId>() {
            @Override
            public MethodId[] newInstance(int length) {
                return new MethodId[length];
            }
            @Override
            public MethodId newInstance() {
                return new MethodId();
            }
        });
        VALUES[index++] = METHOD_ID;

        ANNOTATION = new DataSectionType<>("ANNOTATION", 0x2004, index, new Creator<AnnotationItem>() {
            @Override
            public AnnotationItem[] newInstance(int length) {
                return new AnnotationItem[length];
            }
            @Override
            public AnnotationItem newInstance() {
                return new AnnotationItem();
            }
        });
        VALUES[index++] = ANNOTATION;

        ANNOTATION_SET = new DataSectionType<>("ANNOTATION_SET", 0x1003, index, new Creator<AnnotationSet>() {
            @Override
            public AnnotationSet[] newInstance(int length) {
                return new AnnotationSet[length];
            }
            @Override
            public AnnotationSet newInstance() {
                return new AnnotationSet();
            }
        });
        VALUES[index++] = ANNOTATION_SET;

        ANNOTATION_GROUP = new DataSectionType<>("ANNOTATION_GROUP", 0x1002, index, new Creator<AnnotationGroup>() {
            @Override
            public AnnotationGroup[] newInstance(int length) {
                return new AnnotationGroup[length];
            }

            @Override
            public AnnotationGroup newInstance() {
                return new AnnotationGroup();
            }
        });
        VALUES[index++] = ANNOTATION_GROUP;

        ANNOTATIONS_DIRECTORY = new DataSectionType<>("ANNOTATIONS_DIRECTORY", 0x2006, index, new Creator<AnnotationsDirectory>() {
            @Override
            public AnnotationsDirectory[] newInstance(int length) {
                return new AnnotationsDirectory[length];
            }
            @Override
            public AnnotationsDirectory newInstance() {
                return new AnnotationsDirectory();
            }
        });
        VALUES[index++] = ANNOTATIONS_DIRECTORY;

        CALL_SITE_ID = new IdSectionType<>("CALL_SITE_ID", 0x0007, index, 5, null);
        VALUES[index++] = CALL_SITE_ID;

        METHOD_HANDLE = new IdSectionType<>("METHOD_HANDLE", 0x0008, index, 6, null);
        VALUES[index++] = METHOD_HANDLE;


        DEBUG_INFO = new DataSectionType<>("DEBUG_INFO", 0x2003, index, new Creator<DebugInfo>() {
            @Override
            public DebugInfo[] newInstance(int length) {
                return new DebugInfo[length];
            }
            @Override
            public DebugInfo newInstance() {
                return new DebugInfo();
            }
        });
        VALUES[index++] = DEBUG_INFO;

        CODE = new DataSectionType<>("CODE", 0x2001, index, new Creator<CodeItem>() {
            @Override
            public CodeItem[] newInstance(int length) {
                return new CodeItem[length];
            }

            @Override
            public CodeItem newInstance() {
                return new CodeItem();
            }
        });
        VALUES[index++] = CODE;

        ENCODED_ARRAY = new DataSectionType<>("ENCODED_ARRAY", 0x2005, index,  new Creator<EncodedArray>() {
            @Override
            public EncodedArray[] newInstance(int length) {
                return new EncodedArray[length];
            }
            @Override
            public EncodedArray newInstance() {
                return new EncodedArray();
            }
        });
        VALUES[index++] = ENCODED_ARRAY;

        CLASS_DATA = new DataSectionType<>("CLASS_DATA", 0x2000, index, new Creator<ClassData>() {
            @Override
            public ClassData[] newInstance(int length) {
                return new ClassData[length];
            }
            @Override
            public ClassData newInstance() {
                return new ClassData();
            }
        });
        VALUES[index++] = CLASS_DATA;

        CLASS_ID = new IdSectionType<>("CLASS_ID", 0x0006, index, 7, new Creator<ClassId>() {
            @Override
            public ClassId[] newInstance(int length) {
                return new ClassId[length];
            }
            @Override
            public ClassId newInstance() {
                return new ClassId();
            }
        });
        VALUES[index++] = CLASS_ID;

        HIDDEN_API = new DataSectionType<>("HIDDEN_API", 0xF000, index, null);
        VALUES[index] = HIDDEN_API;

        READ_ORDER = new SectionType[]{
                HEADER,
                MAP_LIST,
                STRING_ID,
                STRING_DATA,
                TYPE_ID,
                TYPE_LIST,
                PROTO_ID,
                FIELD_ID,
                METHOD_ID,
                ANNOTATION,
                ANNOTATION_SET,
                ANNOTATION_GROUP,
                ANNOTATIONS_DIRECTORY,
                CALL_SITE_ID,
                METHOD_HANDLE,
                DEBUG_INFO,
                CODE,
                ENCODED_ARRAY,
                CLASS_DATA,
                CLASS_ID,
                HIDDEN_API
        };

        R8_ORDER = new SectionType[]{
                HEADER,
                STRING_ID,
                TYPE_ID,
                PROTO_ID,
                FIELD_ID,
                METHOD_ID,
                CLASS_ID,
                CODE,
                DEBUG_INFO,
                TYPE_LIST,
                STRING_DATA,
                ANNOTATION,
                CLASS_DATA,
                ENCODED_ARRAY,
                ANNOTATION_SET,
                ANNOTATION_GROUP,
                ANNOTATIONS_DIRECTORY,
                MAP_LIST
        };

        DEX_LIB2_ORDER = new SectionType[]{
                HEADER,
                STRING_ID,
                TYPE_ID,
                PROTO_ID,
                FIELD_ID,
                METHOD_ID,
                CLASS_ID,
                STRING_DATA,
                TYPE_LIST,
                ENCODED_ARRAY,
                ANNOTATION,
                ANNOTATION_SET,
                ANNOTATION_GROUP,
                ANNOTATIONS_DIRECTORY,
                DEBUG_INFO,
                CODE,
                CLASS_DATA,
                MAP_LIST
        };

    }

    private final String name;
    private final int type;
    private final Creator<T> creator;
    private final int readOrder;
    private Boolean offsetType;

    private SectionType(String name, int type, int readOrder, Creator<T> creator){
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.readOrder = readOrder;
    }

    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }
    public int getReadOrder() {
        return readOrder;
    }
    public boolean isOffsetType(){
        if(offsetType != null){
            return offsetType;
        }
        synchronized (this){
            if(creator != null){
                offsetType = (creator.newInstance() instanceof OffsetSupplier);
            }else {
                offsetType = false;
            }
            return offsetType;
        }
    }
    public boolean isIdSection(){
        return false;
    }
    public boolean isDataSection(){
        return false;
    }
    public Section<T> createSection(IntegerPair countAndOffset){
        return null;
    }
    public int getReferenceType(){
        return 7;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        return type;
    }
    @Override
    public String toString() {
        return name + "(" + HexUtil.toHex4((short) type) + ")";
    }

    public Creator<T> getCreator() {
        return creator;
    }

    public int compareReadOrder(SectionType<?> sectionType) {
        if(sectionType == null){
            return -1;
        }
        return Integer.compare(getReadOrder(), sectionType.getReadOrder());
    }

    @SuppressWarnings("unchecked")
    public static<T1 extends Block> SectionType<T1> get(int type){
        for(SectionType<?> sectionType : VALUES){
            if(type == sectionType.type){
                return (SectionType<T1>) sectionType;
            }
        }
        return null;
    }
    public static SectionType<? extends IdItem> getReferenceType(int reference){
        switch (reference){
            case 0:
                return STRING_ID;
            case 1:
                return TYPE_ID;
            case 2:
                return FIELD_ID;
            case 3:
                return METHOD_ID;
            case 4:
                return PROTO_ID;
            case 5:
                return CALL_SITE_ID;
            case 6:
                return METHOD_HANDLE;
            default:
                return null;
        }
    }

    public static<T1> Comparator<T1> comparator(SectionType<?>[] sortOrder, Function<? super T1, SectionType<?>> function){
        return new OrderBasedComparator<>(sortOrder, function);
    }
    public static SectionType<?>[] getR8Order() {
        return R8_ORDER.clone();
    }
    public static SectionType<?>[] getDexLib2Order() {
        return DEX_LIB2_ORDER.clone();
    }
    public static SectionType<?>[] getReadOrderList() {
        return READ_ORDER.clone();
    }

    private static class IdSectionType<T1 extends IdItem> extends SectionType<T1> {

        private final int referenceType;

        private IdSectionType(String name, int type, int readOrder, int referenceType, Creator<T1> creator) {
            super(name, type, readOrder, creator);
            this.referenceType = referenceType;
        }
        @Override
        public boolean isIdSection() {
            return true;
        }
        @Override
        public Section<T1> createSection(IntegerPair countAndOffset){
            return new IdSection<>(countAndOffset, this);
        }
        @Override
        public int getReferenceType() {
            return referenceType;
        }
    }
    private static class StringIdSectionType extends IdSectionType<StringId> {
        StringIdSectionType(String name, int type, int readOrder, int referenceType, Creator<StringId> creator) {
            super(name, type, readOrder, referenceType, creator);
        }
        @Override
        public StringIdSection createSection(IntegerPair countAndOffset){
            return new StringIdSection(countAndOffset, this);
        }
    }
    private static class DataSectionType<T1 extends DataItem> extends SectionType<T1> {
        DataSectionType(String name, int type, int readOrder, Creator<T1> creator) {
            super(name, type, readOrder, creator);
        }

        @Override
        public boolean isDataSection() {
            return true;
        }
        @Override
        public Section<T1> createSection(IntegerPair countAndOffset){
            return new DataSection<>(countAndOffset, this);
        }
    }
    private static class StringDataSectionType extends DataSectionType<StringData> {
        StringDataSectionType(String name, int type, int readOrder, Creator<StringData> creator) {
            super(name, type, readOrder, creator);
        }
        @Override
        public StringDataSection createSection(IntegerPair countAndOffset){
            return new StringDataSection(countAndOffset, this);
        }
    }

    static class OrderBasedComparator<T1> implements Comparator<T1> {
        private final Function<? super T1, SectionType<?>> function;
        private final SectionType<?>[] sortOrder;

        public OrderBasedComparator(SectionType<?>[] sortOrder, Function<? super T1, SectionType<?>> function){
            this.sortOrder = sortOrder;
            this.function = function;
        }
        private int getOrder(SectionType<?> sectionType){
            SectionType<?>[] sortOrder = this.sortOrder;
            int length = sortOrder.length;
            for(int i = 0; i < length; i++){
                if(sortOrder[i] == sectionType){
                    return i;
                }
            }
            return length - 2;
        }
        private int getOrder(T1 item){
            if(item == null){
                return this.sortOrder.length - 1;
            }
            return getOrder(this.function.apply(item));
        }
        @Override
        public int compare(T1 item1, T1 item2) {
            return Integer.compare(getOrder(item1), getOrder(item2));
        }
    }
}
