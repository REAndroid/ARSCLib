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
package com.reandroid.dex.header;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.NumberIntegerReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.sections.SpecialItem;

import java.io.IOException;
import java.io.InputStream;

public class DexHeader extends SpecialItem implements OffsetSupplier, DirectStreamReader {

    private final IntegerReference offsetReference;

    public final Magic magic;
    public final Version version;
    public final Checksum checksum;
    public final Signature signature;

    public final IntegerReference fileSize;
    public final IntegerReference headerSize;
    public final Endian endian;
    public final IntegerReference map;

    public final CountAndOffset string_id;
    public final CountAndOffset type_id;
    public final CountAndOffset proto_id;
    public final CountAndOffset field_id;
    public final CountAndOffset method_id;
    public final CountAndOffset class_id;
    public final CountAndOffset data;
    public final DexContainerInfo containerInfo;

    /**
     * A placeholder for unknown bytes. Normally the size should be zero, but a tampered dex or
     * future versions may have extra bytes than declared on headerSize.
     * */
    public final UnknownHeaderBytes unknown;

    public DexHeader(IntegerReference offsetReference) {
        super(17);
        this.offsetReference = offsetReference;

        this.magic = new Magic();
        this.version = new Version();
        this.checksum = new Checksum();
        this.signature = new Signature();

        this.fileSize = new IntegerItem();
        this.headerSize = new IntegerItem();

        this.endian = new Endian();

        this.map = new IntegerItem();

        this.string_id = new CountAndOffset();
        this.type_id = new CountAndOffset();
        this.proto_id = new CountAndOffset();
        this.field_id = new CountAndOffset();
        this.method_id = new CountAndOffset();
        this.class_id = new CountAndOffset();
        this.data = new CountAndOffset();
        this.containerInfo = new DexContainerInfo();

        this.unknown = new UnknownHeaderBytes();

        addChild(0, magic);
        addChild(1, version);
        addChild(2, checksum);
        addChild(3, signature);
        addChild(4, (Block) fileSize);
        addChild(5, (Block) headerSize);
        addChild(6, endian);
        addChild(7, (Block) map);

        addChild(8, string_id);
        addChild(9, type_id);
        addChild(10, proto_id);
        addChild(11, field_id);
        addChild(12, method_id);
        addChild(13, class_id);
        addChild(14, data);
        addChild(15, containerInfo);

        addChild(16, unknown);

        setOffsetReference(offsetReference);
    }
    public DexHeader(){
        this(new NumberIntegerReference(0));
    }

    @Override
    public SectionType<DexHeader> getSectionType() {
        return SectionType.HEADER;
    }

    public int getVersion(){
        return version.getVersionAsInteger();
    }
    public void setVersion(int version){
        this.version.setVersionAsInteger(version);
    }
    public CountAndOffset get(SectionType<?> sectionType){
        if(sectionType == SectionType.STRING_ID){
            return string_id;
        }
        if(sectionType == SectionType.TYPE_ID){
            return type_id;
        }
        if(sectionType == SectionType.PROTO_ID){
            return proto_id;
        }
        if(sectionType == SectionType.FIELD_ID){
            return field_id;
        }
        if(sectionType == SectionType.METHOD_ID){
            return method_id;
        }
        if(sectionType == SectionType.CLASS_ID){
            return class_id;
        }
        return null;
    }
    public void updateHeaderInternal(Block parent){
        byte[] bytes = parent.getBytes();
        headerSize.set(countBytes());
        fileSize.set(bytes.length);
        signature.update(parent, bytes);
        checksum.update(parent, bytes);
    }
    @Override
    public IntegerReference getOffsetReference() {
        return offsetReference;
    }
    @Override
    protected boolean isValidOffset(int offset){
        return offset >= 0;
    }

    public boolean isClassDefinitionOrderEnforced(){
        return version.isClassDefinitionOrderEnforced();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        int result = 0;
        Block[] childes = getChildes();
        for (Block block : childes) {
            result += ((DirectStreamReader) block).readBytes(inputStream);
        }
        return result;
    }


    @Override
    public String toString() {
        return "Header {" +
                "magic=" + magic +
                ", version=" + version +
                ", checksum=" + checksum +
                ", signature=" + signature +
                ", fileSize=" + fileSize +
                ", headerSize=" + headerSize +
                ", endian=" + endian +
                ", map=" + map +
                ", strings=" + string_id +
                ", type=" + type_id +
                ", proto=" + proto_id +
                ", field=" + field_id +
                ", method=" + method_id +
                ", clazz=" + class_id +
                ", data=" + data +
                ", container-v41" + containerInfo +
                ", unknown=" + unknown +
                '}';
    }

    public static DexHeader readHeader(InputStream inputStream) throws IOException {
        DexHeader dexHeader = new DexHeader();
        int read = dexHeader.readBytes(inputStream);
        if(read < dexHeader.countBytes()) {
            throw new IOException("Few bytes to read header: " + read);
        }
        return dexHeader;
    }
}
