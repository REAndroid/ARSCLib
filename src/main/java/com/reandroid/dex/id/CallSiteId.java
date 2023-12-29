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
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class CallSiteId extends IdItem implements Comparable<CallSiteId> {

    private final DataItemIndirectReference<EncodedArray> encodedArrayReference;

    private final IdKey<CallSiteId> mKey;

    public CallSiteId() {
        super(4);
        this.encodedArrayReference = new DataItemIndirectReference<>(SectionType.ENCODED_ARRAY,
                this, 0, UsageMarker.USAGE_CALL_SITE);
        this.mKey = new IdKey<>(this);
    }

    @Override
    public IdKey<CallSiteId> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key) {
        IdKey<CallSiteId> idKey = (IdKey<CallSiteId>) key;
        merge(idKey.getItem());
    }

    public MethodHandle getMethodHandle(){
        return getValue(SectionType.METHOD_HANDLE, 0);
    }
    public void setMethodHandle(MethodHandleKey key){
        getOrCreateValue(SectionType.METHOD_HANDLE, 0, key);
    }
    public String getMethodName(){
        StringId stringId = getMethodNameId();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public void setMethodName(String methodName){
        setMethodName(StringKey.create(methodName));
    }
    public void setMethodName(StringKey methodName){
        getOrCreateValue(SectionType.STRING_ID, 1, methodName);
    }
    public StringId getMethodNameId(){
        return getValue(SectionType.STRING_ID, 1);
    }
    public MethodId getMethodId(){
        return getValue(SectionType.METHOD_ID, 2);
    }
    public void setMethodId(MethodKey methodKey){
        getOrCreateValue(SectionType.METHOD_ID, 2, methodKey);
    }
    private<T1 extends IdItem> T1 getOrCreateValue(SectionType<T1> sectionType, int index, Key key){
        EncodedArray encodedArray = getOrCreateEncodedArray();
        SectionValue<T1> sectionValue = encodedArray.getOrCreate(sectionType, index);
        sectionValue.setItem(key);
        return sectionValue.getItem();
    }
    @SuppressWarnings("unchecked")
    private<T1 extends IdItem> T1 getValue(SectionType<T1> sectionType, int index){
        EncodedArray encodedArray = getEncodedArray();
        if(encodedArray == null){
            return null;
        }
        DexValueBlock<?> value = encodedArray.get(index);
        if(!(value instanceof SectionValue)){
            return null;
        }
        SectionValue<?> sectionValue = (SectionValue<?>) value;
        if(sectionValue.getSectionType() != sectionType){
            return null;
        }
        return ((SectionValue<T1>)value).getItem();
    }
    public EncodedArray getOrCreateEncodedArray(){
        return encodedArrayReference.getOrCreate();
    }
    public EncodedArray getEncodedArray(){
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
        if(encodedArray == null){
            return EmptyIterator.of();
        }
        return encodedArray.usedIds();
    }

    @Override
    public SectionType<CallSiteId> getSectionType() {
        return SectionType.CALL_SITE_ID;
    }
    public void merge(CallSiteId callSiteId){
        if(callSiteId == this){
            return;
        }
        EncodedArray encodedArray = getOrCreateEncodedArray();
        encodedArray.merge(callSiteId.getEncodedArray());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

    @Override
    public int compareTo(CallSiteId callSiteId) {
        if(callSiteId == this){
            return 0;
        }
        if(callSiteId == null){
            return -1;
        }
        int i = CompareUtil.compare(getMethodHandle(), callSiteId.getMethodHandle());
        if(i != 0){
            return i;
        }
        i = CompareUtil.compare(getMethodNameId(), callSiteId.getMethodNameId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getMethodId(), callSiteId.getMethodId());
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
        return Objects.equals(getEncodedArray(), callSiteId.getEncodedArray());
    }
    @Override
    public int hashCode() {
        EncodedArray encodedArray = getEncodedArray();
        if(encodedArray != null){
            return encodedArray.hashCode();
        }
        return 0;
    }

}
