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
package com.reandroid.dex.id;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.data.EncodedArray;
import com.reandroid.dex.key.*;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.*;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class CallSiteId extends IdItem implements Comparable<CallSiteId> {

    private static final int METHOD_HANDLE_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int TYPE_INDEX = 2;

    private final DataItemIndirectReference<EncodedArray> encodedArrayReference;

    public CallSiteId() {
        super(4);
        this.encodedArrayReference = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY,
                this, 0, UsageMarker.USAGE_CALL_SITE);
    }

    @Override
    public CallSiteKey getKey() {
        CallSiteKey last = getLastKey();
        NamedTypeKey nameAndType = getNameAndType();
        BootstrapMethodKey bootstrap = getBootstrap();
        if (last == null || !last.equals(nameAndType, bootstrap)) {
            last = checkKey(CallSiteKey.create(nameAndType, bootstrap));
        }
        return last;
    }
    @Override
    public void setKey(Key key) {
        CallSiteKey callSiteKey = (CallSiteKey) key;
        this.encodedArrayReference.setKey(callSiteKey.toArrayKey());
    }
    public NamedTypeKey getNameAndType() {
        return NamedTypeKey.create(getMethodName(), getType());
    }
    public BootstrapMethodKey getBootstrap() {
        return BootstrapMethodKey.create(getArguments(), getMethodHandle());
    }

    public String callSiteName() {
        return NAME_PREFIX + getIdx();
    }
    public MethodHandleKey getMethodHandle() {
        MethodHandleId methodHandleId = getMethodHandleId();
        if (methodHandleId != null) {
            return methodHandleId.getKey();
        }
        return null;
    }
    public MethodHandleId getMethodHandleId() {
        return getValue(SectionType.METHOD_HANDLE, METHOD_HANDLE_INDEX);
    }
    public void setMethodHandle(MethodHandleKey key) {
        if (!key.equals(getMethodHandle())) {
            setKey(getKey().changeMethodHandle(key));
        }
    }
    public StringKey getMethodName() {
        StringId stringId = getMethodNameId();
        if (stringId != null) {
            return stringId.getKey();
        }
        return null;
    }
    public void setMethodName(String methodName) {
        setMethodName(StringKey.create(methodName));
    }
    public void setMethodName(StringKey methodName) {
        if (!methodName.equals(getMethodName())) {
            setKey(getKey().changeName(methodName));
        }
    }
    public StringId getMethodNameId() {
        return getValue(SectionType.STRING_ID, NAME_INDEX);
    }
    public IdItem getTypeId() {
        return getValue(null, TYPE_INDEX);
    }
    public TypeDescriptorKey getType() {
        IdItem typeId = getTypeId();
        if (typeId != null) {
            return (TypeDescriptorKey) typeId.getKey();
        }
        return null;
    }
    public void setProto(TypeDescriptorKey protoKey) {
        if (!protoKey.equals(getType())) {
            setKey(getKey().changeType(protoKey));
        }
    }
    public ArrayValueKey getArguments() {
        int size = getArgumentsSize();
        Key[] results = new Key[size];
        for (int i = 0; i < size; i++) {
            results[i] = getArgument(i);
        }
        return ArrayValueKey.of(results);
    }
    public Iterator<DexValueBlock<?>> getArgumentValues() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            return encodedArray.iterator(3, encodedArray.size() - 3);
        }
        return EmptyIterator.of();
    }
    public Key getArgument(int i) {
        DexValueBlock<?> valueBlock = getArgumentValue(i);
        if (valueBlock != null) {
            return valueBlock.getKey();
        }
        return null;
    }
    public DexValueBlock<?> getArgumentValue(int i) {
        if (i >= 0) {
            EncodedArray encodedArray = getEncodedArray();
            if (encodedArray != null) {
                return encodedArray.get(i + 3);
            }
        }
        return null;
    }
    public int getArgumentsSize() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            int size = encodedArray.size();
            if (size > 0) {
                return size - 3;
            }
        }
        return 0;
    }
    public void setArguments(ArrayKey<?> key) {
        if (!key.equals(getArguments())) {
            setKey(getKey().changeArguments(key));
        }
    }

    private<T1 extends IdItem> T1 getValue(SectionType<T1> sectionType, int index) {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray == null) {
            return null;
        }
        DexValueBlock<?> value = encodedArray.get(index);
        if (!(value instanceof SectionValue)) {
            return null;
        }
        SectionValue<?> sectionValue = (SectionValue<?>) value;
        if (sectionType != null && sectionType != sectionValue.getSectionType()) {
            return null;
        }
        return ObjectsUtil.cast(((SectionValue<?>)value).getItem());
    }
    public EncodedArray getOrCreateEncodedArray() {
        return encodedArrayReference.getOrCreate();
    }
    public EncodedArray getEncodedArray() {
        return encodedArrayReference.getItem();
    }

    @Override
    void cacheItems() {
        encodedArrayReference.pullItem();
    }
    @Override
    public void refresh() {
        encodedArrayReference.refresh();
    }
    @Override
    public Iterator<IdItem> usedIds() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray == null) {
            return EmptyIterator.of();
        }
        return encodedArray.usedIds();
    }

    @Override
    public SectionType<CallSiteId> getSectionType() {
        return SectionType.CALL_SITE_ID;
    }
    public void merge(CallSiteId callSiteId) {
        if (callSiteId == this) {
            return;
        }
        EncodedArray encodedArray = getOrCreateEncodedArray();
        encodedArray.merge(callSiteId.getEncodedArray());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(callSiteName());
        writer.append('(');
        getMethodNameId().append(writer);
        writer.append(", ");
        getTypeId().append(writer);
        Iterator<DexValueBlock<?>> iterator = getArgumentValues();
        while (iterator.hasNext()) {
            writer.append(", ");
            iterator.next().append(writer);
        }
        writer.append(')');
        getMethodHandle().append(writer, false);
    }

    @Override
    public int compareTo(CallSiteId callSiteId) {
        if (callSiteId == this) {
            return 0;
        }
        if (callSiteId == null) {
            return -1;
        }
        int i = CompareUtil.compare(getMethodHandleId(), callSiteId.getMethodHandleId());
        if (i != 0) {
            return i;
        }
        i = CompareUtil.compare(getArguments(), callSiteId.getArguments());
        if (i != 0) {
            return i;
        }
        i = CompareUtil.compare(getMethodNameId(), callSiteId.getMethodNameId());
        if (i != 0) {
            return i;
        }
        IdItem type1 = getTypeId();
        IdItem type2 = callSiteId.getTypeId();
        if (type1 instanceof ProtoId && type2 instanceof ProtoId) {
            return CompareUtil.compare((ProtoId) type1, (ProtoId) type2);
        }
        if (type1 instanceof TypeId && type2 instanceof TypeId) {
            return CompareUtil.compare((TypeId) type1, (TypeId) type2);
        }
        return 0;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CallSiteId callSiteId = (CallSiteId) o;
        return ObjectsUtil.equals(getEncodedArray(), callSiteId.getEncodedArray());
    }
    @Override
    public int hashCode() {
        EncodedArray encodedArray = getEncodedArray();
        if (encodedArray != null) {
            return encodedArray.hashCode();
        }
        return 0;
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }

    public static final String NAME_PREFIX = ObjectsUtil.of("call_site_");
}
