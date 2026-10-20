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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.ParentChunk;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.graphics.AndroidColor;

public interface Value {
    void setData(EncodeResult encodeResult);
    DataType getDataType();
    void setDataType(DataType dataType);
    int getData();
    void setData(int data);
    String getDataAsString();
    void setDataAsString(String value);
    PackageBlock getPackageBlock();
    ParentChunk getParentChunk();
    default void setTypeAndData(DataType dataType, int data) {
        setData(data);
        setDataType(dataType);
    }
    default AndroidColor getDataAsColor() {
        DataType dataType = getDataType();
        if (dataType == null || !dataType.isColor()) {
            return null;
        }
        return AndroidColor.decode(ValueCoder.decode(dataType, getData()));
    }
    default void setData(AndroidColor color) {
        setData(ValueCoder.encode(color.toHexString()));
    }
    default ResourceEntry getDataAsReference() {
        DataType dataType = getDataType();
        if (dataType == null || !dataType.isReference()) {
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        int data = getData();
        ResourceEntry resourceEntry = packageBlock.getResource(data);
        if (resourceEntry == null) {
            TableBlock tableBlock = packageBlock.getTableBlock();
            if (tableBlock != null) {
                resourceEntry = tableBlock.getResource(packageBlock, data);
            }
        }
        return resourceEntry;
    }
    default Float getDataAsFloat() {
        DataType dataType = getDataType();
        if (dataType != DataType.FLOAT) {
            return null;
        }
        return Float.intBitsToFloat(getData());
    }
    default void setDataAsFloat(float f) {
        setTypeAndData(DataType.FLOAT, Float.floatToIntBits(f));
    }
    default Integer getDataAsInteger() {
        DataType dataType = getDataType();
        if (dataType == DataType.DEC || dataType == DataType.HEX) {
            return getData();
        }
        return null;
    }
    default void setDataAsDecimal(int i) {
        setTypeAndData(DataType.DEC, i);
    }
    default void setDataAsHex(int i) {
        setTypeAndData(DataType.HEX, i);
    }
    default int getDataAsResourceId() {
        DataType dataType = getDataType();
        if (dataType != null && dataType.isReference()) {
            return getData();
        }
        return 0;
    }
    default void setDataAsResourceId(int id) {
        setTypeAndData(DataType.REFERENCE, id);
    }
}
