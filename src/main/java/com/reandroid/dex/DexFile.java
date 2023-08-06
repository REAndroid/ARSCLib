package com.reandroid.dex;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.ExpandableBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.index.*;
import com.reandroid.dex.header.DexHeader;
import com.reandroid.dex.sections.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DexFile extends ExpandableBlockContainer {

    private final DexHeader dexHeader;
    private final DexStringPool stringPool;
    private final DexSection<TypeIndex> typeSection;
    private final DexSection<ProtoIndex> protoSection;
    private final DexSection<FieldIndex> fieldSection;
    private final DexSection<ClassIndex> classSection;
    private final DexSection<MethodIndex> methodSection;

    private final MapIndex mapIndex;

    private final DexAnnotationPool annotationPool;
    public DexFile() {
        super(8);

        DexHeader header = new DexHeader();
        this.dexHeader =  header;
        this.stringPool = new DexStringPool(header);

        this.typeSection = new DexSection<>(header.type, CREATOR_TYPE);
        this.protoSection = new DexSection<>(header.proto, CREATOR_PROTO);
        this.fieldSection = new DexSection<>(header.field, CREATOR_FIELD);
        this.classSection = new DexSection<>(header.class_def, CREATOR_CLASS);
        this.methodSection = new DexSection<>(header.method, CREATOR_METHOD);

        this.mapIndex = new MapIndex(header.map);

        this.annotationPool = new DexAnnotationPool();
        annotationPool.setParent(this);

        addChild(dexHeader);
        addChild(stringPool);
        addChild(typeSection);
        addChild(protoSection);
        addChild(fieldSection);
        addChild(methodSection);
        addChild(classSection);
        addChild(mapIndex);
    }

    public DexHeader getHeader() {
        return dexHeader;
    }
    public DexStringPool getStringPool(){
        return stringPool;
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
    public DexSection<ClassIndex> getClassSection() {
        return classSection;
    }
    public DexSection<MethodIndex> getMethodSection(){
        return methodSection;
    }
    public MapIndex getMapItem(){
        return mapIndex;
    }

    public DexAnnotationPool getAnnotationPool(){
        return annotationPool;
    }

    public static boolean isDexFile(File file){
        if(file == null || !file.isFile()){
            return false;
        }
        DexHeader dexHeader = null;
        try {
            InputStream inputStream = new FileInputStream(file);
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    public static boolean isDexFile(InputStream inputStream){
        DexHeader dexHeader = null;
        try {
            dexHeader = DexHeader.readHeader(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return isDexFile(dexHeader);
    }
    private static boolean isDexFile(DexHeader dexHeader){
        if(dexHeader == null){
            return false;
        }
        if(dexHeader.magic.isDefault()){
            return false;
        }
        int version = dexHeader.version.getVersionAsInteger();
        return version > 0 && version < 1000;
    }
    public void read(byte[] dexBytes) throws IOException {
        BlockReader reader = new BlockReader(dexBytes);
        readBytes(reader);
        reader.close();
    }
    public void read(InputStream inputStream) throws IOException {
        BlockReader reader = new BlockReader(inputStream);
        readBytes(reader);
        reader.close();
    }
    public void read(File file) throws IOException {
        BlockReader reader = new BlockReader(file);
        readBytes(reader);
        reader.close();
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
