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
package com.reandroid.arsc.value;

import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.MainChunk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.ParentChunk;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.CoderUnknownStringRef;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.SpanSet;
import com.reandroid.xml.StyleDocument;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Objects;

public abstract class ValueItem extends BlockItem implements Value,
        JSONConvert<JSONObject> {

    private ReferenceItem mStringReference;
    private final int sizeOffset;

    public ValueItem(int bytesLength, int sizeOffset) {
        super(bytesLength);
        this.sizeOffset = sizeOffset;
        writeSize();
    }

    public boolean isUndefined() {
        return getDataType() == DataType.NULL && getData() == 0;
    }
    public ResourceEntry resolve(int resourceId) {
        PackageBlock context = getPackageBlock();
        if (context == null) {
            return null;
        }
        TableBlock tableBlock = context.getTableBlock();
        if (tableBlock == null) {
            return null;
        }
        return tableBlock.getResource(context, resourceId);
    }
    public PackageBlock getPackageBlock() {
        ParentChunk parentChunk = getParentChunk();
        if (parentChunk != null) {
            return parentChunk.getPackageBlock();
        }
        return null;
    }

    void linkTableStrings(TableStringPool tableStringPool) {
        if (getDataType() == DataType.STRING) {
            linkStringReference(tableStringPool);
        }
    }
    public void onRemoved() {
        unLinkStringReference();
    }
    protected void onDataChanged() {
    }
    public void refresh() {
        updateSize();
    }

    @SuppressWarnings("unused")
    byte getRes0() {
        return getBytesInternal()[this.sizeOffset + OFFSET_RES0];
    }
    void setRes0(byte b) {
        getBytesInternal()[this.sizeOffset + OFFSET_RES0] = b;
    }
    public int dataType() {
        return getBytesInternal()[this.sizeOffset + OFFSET_TYPE] & 0xff;
    }
    public void setDataType(int type) {
        if (type == dataType()) {
            return;
        }
        byte[] bytes = getBytesInternal();
        int offset = this.sizeOffset + OFFSET_TYPE;
        int old = bytes[offset] & 0xff;
        bytes[offset] = (byte) type;
        onTypeChanged(old, type);
        onDataChanged();
    }
    public int getSize() {
        return 0xffff & getShort(getBytesInternal(), this.sizeOffset + OFFSET_SIZE);
    }
    public void setSize(int size) {
        size = this.sizeOffset + size;
        setBytesLength(size, false);
        writeSize();
    }
    void updateSize() {
        writeSize();
    }
    private void writeSize() {
        int offset = this.sizeOffset;
        int size = countBytes() - offset;
        putShort(getBytesInternal(), offset + OFFSET_SIZE, (short) size);
    }
    protected void onDataLoaded() {
        if (getDataType() == DataType.STRING) {
            linkStringReference();
        } else {
            unLinkStringReference();
        }
    }
    @Override
    public DataType getDataType() {
        return DataType.valueOf(dataType());
    }
    @Override
    public void setDataType(DataType dataType) {
        setDataType(dataType != null ? dataType.flag() : 0);
    }
    @Override
    public int getData() {
        return getInteger(getBytesInternal(), this.sizeOffset + OFFSET_DATA);
    }
    @Override
    public void setData(int data) {
        int old = getData();
        if (old == data) {
            return;
        }
        unLinkStringReference();
        writeData(data);
        if (DataType.STRING== getDataType()) {
            linkStringReference();
        }
        onDataChanged();
    }
    void writeData(int data) {
        putInteger(getBytesInternal(), this.sizeOffset + OFFSET_DATA, data);
    }

    public StringItem getDataAsPoolString() {
        if (getDataType() != DataType.STRING) {
            return null;
        }
        StringPool<?> stringPool = getStringPool();
        if (stringPool == null) {
            return null;
        }
        return stringPool.get(getData());
    }
    private void onTypeChanged(int old, int type) {
        int typeString = DataType.STRING.flag();
        if (old == typeString) {
            unLinkStringReference();
        } else if (type == typeString) {
            linkStringReference();
        }
    }
    private void linkStringReference() {
        StringPool<?> stringPool = getStringPool();
        if (stringPool == null || stringPool.isStringLinkLocked()) {
            return;
        }
        linkStringReference(stringPool);
    }
    private void linkStringReference(StringPool<?> stringPool) {
        StringItem tableString = stringPool.get(getData());
        if (tableString == null) {
            unLinkStringReference();
            return;
        }
        ReferenceItem stringReference = mStringReference;
        if (stringReference != null) {
            unLinkStringReference();
        }
        stringReference = new ValueStringReference(this);
        mStringReference = stringReference;
        tableString.addReference(stringReference);
    }
    private void unLinkStringReference() {
        ReferenceItem stringReference = mStringReference;
        if (stringReference == null) {
            return;
        }
        mStringReference = null;
        onUnlinkDataString(stringReference);
    }
    protected void onUnlinkDataString(ReferenceItem referenceItem) {
        StringPool<?> stringPool = getStringPool();
        if (stringPool == null) {
            return;
        }
        stringPool.removeReference(referenceItem);
    }
    public StringPool<?> getStringPool() {
        Block parent = getParent();
        while (parent != null) {
            if (parent instanceof MainChunk) {
                return ((MainChunk) parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int readSize = initializeBytes(reader);
        super.onReadBytes(reader);
        if (readSize < 8) {
            setBytesLength(this.sizeOffset + 8, false);
            writeSize();
        }
    }
    private int initializeBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        int offset = this.sizeOffset;
        reader.offset(offset);
        int readSize = reader.readUnsignedShort();
        int size = readSize;
        if (size < 8) {
            if (reader.available() >= 8) {
                size = 8;
            }
        }
        reader.seek(position);
        setBytesLength(offset + size, false);
        return readSize;
    }
    @Override
    public String getDataAsString() {
        StringItem stringItem = getDataAsPoolString();
        if (stringItem != null) {
            String value = stringItem.getXml();
            if (value == null) {
                value = "";
            }
            return value;
        }
        return null;
    }
    public Object getDataAsEncoded() {
        DataType dataType = getDataType();
        if (dataType == DataType.STRING) {
            return getDataAsString();
        }
        if (dataType == DataType.BOOLEAN) {
            return getDataAsBoolean();
        }
        if (dataType == DataType.DEC || dataType == DataType.HEX) {
            return getData();
        }
        if (dataType == DataType.FLOAT) {
            return getDataAsFloat();
        }
        return null;
    }
    public StyleDocument getDataAsStyleDocument() {
        StringItem stringItem = getDataAsPoolString();
        if (stringItem != null) {
            return stringItem.getStyleDocument();
        }
        return null;
    }
    public void setDataAsString(SpanSet<?> spanSet) {
        if (spanSet == null) {
            setDataAsString("");
            return;
        }
        StringItem stringItem = getStringPool().getOrCreate(spanSet);
        setData(stringItem.getIndex());
        setDataType(DataType.STRING);
    }
    public void setDataAsString(StringItem stringItem) {
        if (stringItem == null) {
            return;
        }
        StringItem item = getStringPool().getOrCreate(stringItem);
        setData(item.getIndex());
        setDataType(DataType.STRING);
    }
    @Override
    public void setDataAsString(String str) {
        if (getDataType() == DataType.STRING
                && Objects.equals(str, getDataAsString())) {
            return;
        }
        if (str == null) {
            str = "";
        }
        if (getStringPool() == null) {
            throw new NullPointerException(str);
        }
        StringItem stringItem = getStringPool().getOrCreate(str);
        setData(stringItem.getIndex());
        setDataType(DataType.STRING);
    }
    public void serializeText(XmlSerializer serializer) throws IOException {
        serializeText(serializer, false);
    }
    public void serializeText(XmlSerializer serializer, boolean escapeValues) throws IOException {
        if (getDataType() == DataType.STRING) {
            StringItem stringItem = getDataAsPoolString();
            if (stringItem != null) {
                stringItem.serializeText(serializer, escapeValues);
            } else {
                serializer.text(CoderUnknownStringRef.INS.decode(getData()));
            }
            return;
        }
        String value = decodeValue();
        if (value == null) {
            // TODO: could not happen ?
            value = "";
        }
        serializer.text(value);
    }
    public void serializeAttribute(XmlSerializer serializer, String name, boolean ignore_empty) throws IOException {
        serializeAttribute(serializer, null, name, ignore_empty);
    }
    public void serializeAttribute(XmlSerializer serializer, String namespace, String name, boolean ignore_empty) throws IOException {
        if (getDataType() == DataType.STRING) {
            StringItem stringItem = getDataAsPoolString();
            if (stringItem != null) {
                stringItem.serializeAttribute(serializer, namespace, name);
            } else {
                // TODO: should throw ?
                serializer.attribute(namespace, name, CoderUnknownStringRef.INS.decode(getData()));
            }
            return;
        }
        String value = decodeValue();
        if (ignore_empty && StringsUtil.isEmpty(value)) {
            return;
        }
        if (value == null) {
            value = "";
        }
        serializer.attribute(namespace, name, value);
    }
    public boolean getDataAsBoolean() {
        return getData() != 0;
    }
    public void setValueAsBoolean(boolean value) {
        setDataType(DataType.BOOLEAN);
        setData(value ? 0xffffffff : 0);
    }
    @Override
    public void setData(EncodeResult encodeResult) {
        if (encodeResult == null) {
            throw new NullPointerException();
        }
        if (encodeResult.isError()) {
            throw new IllegalArgumentException("Can not set error value: "
                    + encodeResult.getError());
        }
        setTypeAndData(encodeResult.dataType, encodeResult.value);
    }
    public void merge(ValueItem valueItem) {
        if (valueItem == null || valueItem == this) {
            return;
        }
        int size = valueItem.getSize();
        if (size != 0) {
            setSize(valueItem.getSize());
        }
        DataType coming = valueItem.getDataType();
        if (coming == DataType.STRING) {
            setDataAsString(valueItem.getDataAsPoolString());
        } else {
            setTypeAndData(coming, valueItem.getData());
        }
    }
    public void mergeWithName(ResourceMergeOption mergeOption, ValueItem valueItem) {
        if (valueItem == null || valueItem == this) {
            return;
        }
        int size = valueItem.getSize();
        if (size != 0) {
            setSize(valueItem.getSize());
        }
        DataType coming = valueItem.getDataType();
        if (coming == DataType.STRING) {
            StyleDocument styleDocument = valueItem.getDataAsStyleDocument();
            if (styleDocument != null) {
                setDataAsString(styleDocument);
            } else {
                ApkFile apk1 = getPackageBlock().getTableBlock().getApkFile();
                ApkFile apk2 = valueItem.getPackageBlock().getTableBlock().getApkFile();
                String value = valueItem.getDataAsString();
                setDataAsString(value);
                if (apk1 != null && apk2 != null) {
                    apk1.mergeWithName(mergeOption, apk2, value);
                }
            }
        } else if (coming.isReference()) {
            int id = 0;
            ResourceEntry comingResourceEntry = valueItem.getDataAsReference();
            if (comingResourceEntry == null) {
                id = valueItem.getData();
            } else if (comingResourceEntry.isContext(valueItem.getPackageBlock())) {
                ResourceEntry mergedReference;
                if (comingResourceEntry.isDeclared()) {
                    mergedReference = getPackageBlock().mergeWithName(mergeOption, comingResourceEntry);
                } else {
                    mergedReference = mergeOption.resolveUndeclared(getPackageBlock(), comingResourceEntry);
                }
                if (mergedReference != null) {
                    id = mergedReference.getResourceId();
                }
            } else {
                id = valueItem.getData();
            }
            setTypeAndData(coming, id);
        } else {
            setTypeAndData(coming, valueItem.getData());
        }
    }
    public String decodeValue() {
        return decodeValue(true);
    }
    public String decodeValue(boolean validatePackage) {
        DataType dataType = getDataType();
        if (dataType == null) {
            return null;
        }
        if (dataType.isReference()) {
            return decodeAsReferenceString(dataType, validatePackage);
        }
        if (dataType == DataType.STRING) {
            return getDataAsString();
        }
        return ValueCoder.decode(dataType, getData());
    }
    private String decodeAsReferenceString(DataType dataType, boolean validatePackage) {
        int data = getData();
        if (data == 0) {
            return ValueCoder.decodeReference(null, dataType, data);
        }
        ResourceEntry resourceEntry = getDataAsReference();
        if (validatePackage && resourceEntry == null && getPackageBlock() == null) {
            throw new NullPointerException("Parent package block is null");
        }
        if (resourceEntry == null || !resourceEntry.isDeclared()) {
            return ValueCoder.decodeUnknownResourceId(dataType, data);
        }
        return resourceEntry.buildReference(getPackageBlock(), dataType);
    }
    @Override
    public JSONObject toJson() {
        if (isNull()) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        toJson(jsonObject);
        return jsonObject;
    }

    public void toJson(JSONObject jsonObject) {
        DataType dataType = getDataType();
        jsonObject.put(NAME_data_type, dataType.name());
        if (dataType == DataType.STRING) {
            StringItem stringItem = getDataAsPoolString();
            if (stringItem != null) {
                jsonObject.put(NAME_data, stringItem.toJson());
            } else {
                // unlikely, in case of wrong string index
                jsonObject.put(NAME_data, getData());
            }
        } else if (dataType == DataType.BOOLEAN) {
            jsonObject.put(NAME_data, getDataAsBoolean());
        } else {
            jsonObject.put(NAME_data, getData());
        }
    }
    @Override
    public void fromJson(JSONObject json) {
        if (json != null) {
            DataType dataType = DataType.fromName(json.getString(NAME_data_type));
            if (dataType == DataType.STRING) {
                Object obj = json.get(NAME_data);
                if (obj instanceof Integer) {
                    // unlikely, in case of wrong string index
                    setTypeAndData(dataType, (Integer) obj);
                } else {
                    StringItem stringItem = getStringPool()
                            .getOrCreateFromJson(obj);
                    setTypeAndData(dataType, stringItem.getIndex());
                }
            } else if (dataType == DataType.BOOLEAN) {
                setValueAsBoolean(json.getBoolean(NAME_data));
            } else {
                setTypeAndData(dataType, json.getInt(NAME_data));
            }
        }
    }

    @Override
    public String toString() {
        if (getPackageBlock() != null) {
            return getDataType() + ":" + HexUtil.toHex8(getData()) + " " + decodeValue();
        }
        StringBuilder builder = new StringBuilder();
        int size = getSize();
        if (size!=8) {
            builder.append("size=").append(getSize());
            builder.append(", ");
        }
        builder.append("type=");
        DataType dataType = getDataType();
        if (dataType != null) {
            builder.append(dataType);
        } else {
            builder.append(HexUtil.toHex2(dataType()));
        }
        builder.append(", data=");
        int data = getData();
        if (dataType == DataType.STRING) {
            StringItem tableString = getDataAsPoolString();
            if (tableString != null) {
                builder.append(tableString.getHtml());
            } else {
                builder.append(HexUtil.toHex8(data));
            }
        } else {
            builder.append(HexUtil.toHex8(data));
        }
        return builder.toString();
    }

    static class ValueStringReference implements ReferenceItem {

        private final ValueItem valueItem;

        ValueStringReference(ValueItem valueItem) {
            this.valueItem = valueItem;
        }
        @Override
        public int get() {
            return valueItem.getData();
        }
        @Override
        public void set(int value) {
            valueItem.writeData(value);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
            ValueItem block = this.valueItem;
            if (parentClass.isInstance(block)) {
                return (T1) block;
            }
            return block.getParentInstance(parentClass);
        }
    }
    private static final int OFFSET_SIZE = 0;
    private static final int OFFSET_RES0 = 2;
    private static final int OFFSET_TYPE = 3;
    private static final int OFFSET_DATA = 4;


    public static final String NAME_data = ObjectsUtil.of("data");
    public static final String NAME_data_type = ObjectsUtil.of("data_type");
}
