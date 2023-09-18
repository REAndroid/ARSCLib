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
import com.reandroid.dex.base.WarpedIntegerReference;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.index.*;
import com.reandroid.dex.item.*;
import com.reandroid.utils.HexUtil;


public class SectionType<T extends Block> {

    private static final SectionType<?>[] VALUES;

    public static final SectionType<DexHeader> HEADER;
    public static final SectionType<StringId> STRING_ID;
    public static final SectionType<TypeId> TYPE_ID;
    public static final SectionType<ProtoId> PROTO_ID;
    public static final SectionType<FieldId> FIELD_ID;
    public static final SectionType<MethodId> METHOD_ID;
    public static final SectionType<ClassId> CLASS_ID;
    public static final SectionType<Block> CALL_SITE_ID;
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
    public static final SectionType<Block> HIDDEN_API;

    static {

        VALUES = new SectionType[21];
        int index = 0;

        HEADER = new SectionType<>("HEADER", 0x0000, index, 0, new Creator<DexHeader>() {
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

        MAP_LIST = new SectionType<>("MAP_LIST", 0x1000, index, 21, new Creator<MapList>() {
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

        STRING_ID = new SectionType<>("STRING_ID", 0x0001, index, 1, new Creator<StringId>() {
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

        STRING_DATA = new SectionType<>("STRING_DATA", 0x2002, index, 7, STRING_ID, new Creator<StringData>() {
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

        TYPE_ID = new SectionType<>("TYPE_ID", 0x0002, index, 2, new Creator<TypeId>() {
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

        TYPE_LIST = new SectionType<>("TYPE_LIST", 0x1001, index, 8, new Creator<TypeList>() {
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

        PROTO_ID = new SectionType<>("PROTO_ID", 0x0003, index, 3, new Creator<ProtoId>() {
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

        FIELD_ID = new SectionType<>("FIELD_ID", 0x0004, index, 4, new Creator<FieldId>() {
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

        METHOD_ID = new SectionType<>("METHOD_ID", 0x0005, index, 5, new Creator<MethodId>() {
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

        ANNOTATION = new SectionType<>("ANNOTATION", 0x2004, index, 10, new Creator<AnnotationItem>() {
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

        ANNOTATION_SET = new SectionType<>("ANNOTATION_SET", 0x1003, index, 11, new Creator<AnnotationSet>() {
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

        ANNOTATION_GROUP = new SectionType<>("ANNOTATION_GROUP", 0x1002, index, 12, new Creator<AnnotationGroup>() {
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

        ANNOTATIONS_DIRECTORY = new SectionType<>("ANNOTATIONS_DIRECTORY", 0x2006, index, 13, new Creator<AnnotationsDirectory>() {
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

        CALL_SITE_ID = new SectionType<>("CALL_SITE_ID", 0x0007, index, 100, null);
        VALUES[index++] = CALL_SITE_ID;

        METHOD_HANDLE = new SectionType<>("METHOD_HANDLE", 0x0008, index, 100, null);
        VALUES[index++] = METHOD_HANDLE;


        DEBUG_INFO = new SectionType<>("DEBUG_INFO", 0x2003, index, 14, new Creator<DebugInfo>() {
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

        CODE = new SectionType<>("CODE", 0x2001, index, 15, new Creator<CodeItem>() {
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

        ENCODED_ARRAY = new SectionType<>("ENCODED_ARRAY", 0x2005, index, 9, new Creator<EncodedArray>() {
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

        CLASS_DATA = new SectionType<>("CLASS_DATA", 0x2000, index, 16, new Creator<ClassData>() {
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

        CLASS_ID = new SectionType<>("CLASS_ID", 0x0006, index, 6, new Creator<ClassId>() {
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

        HIDDEN_API = new SectionType<>("HIDDEN_API", 0xF000, index, 100, null);
        VALUES[index] = HIDDEN_API;

    }

    private final String name;
    private final int type;
    private final Creator<T> creator;
    private final SectionType<?> idSectionType;
    private final int readOrder;
    private final int writeOrder;
    private Boolean offsetType;

    private SectionType(String name, int type, int readOrder, int writeOrder, SectionType<?> idSectionType, Creator<T> creator){
        this.name = name;
        this.type = type;
        this.creator = creator;
        this.idSectionType = idSectionType;
        this.readOrder = readOrder;
        this.writeOrder = writeOrder;
    }
    private SectionType(String name, int type, int readOrder, int writeOrder, Creator<T> creator){
        this(name, type, readOrder, writeOrder, null, creator);
    }

    public String getName() {
        return name;
    }
    public int getType() {
        return type;
    }
    public SectionType<?> getIdSectionType() {
        return idSectionType;
    }
    public int getReadOrder() {
        return readOrder;
    }
    public int getWriteOrder() {
        return writeOrder;
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
    public boolean isIndexSection(){
        return isIndexSection(this);
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
    public int compareWriteOrder(SectionType<?> sectionType) {
        if(sectionType == null){
            return -1;
        }
        return Integer.compare(getWriteOrder(), sectionType.getWriteOrder());
    }

    @SuppressWarnings("unchecked")
    public static<T1 extends Block> SectionType<T1> get(int type){
        for(SectionType<?> itemType : VALUES){
            if(type == itemType.type){
                return (SectionType<T1>) itemType;
            }
        }
        return null;
    }
    public static boolean isIndexSection(SectionType<?> sectionType){
        return sectionType == SectionType.HEADER ||
                sectionType == SectionType.STRING_ID ||
                sectionType == SectionType.TYPE_ID ||
                sectionType == SectionType.FIELD_ID ||
                sectionType == SectionType.METHOD_ID ||
                sectionType == SectionType.PROTO_ID ||
                sectionType == SectionType.CLASS_ID ||
                sectionType == SectionType.CALL_SITE_ID;
    }
}
