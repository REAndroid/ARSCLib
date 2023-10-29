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

import com.reandroid.dex.key.IdKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodHandle extends IdItem implements Comparable<MethodHandle>{

    private final IdItemIndirectReference<MethodId> methodId;
    private final IdItemIndirectReference<MethodId> memberId;

    private final IdKey<MethodHandle> mKey;

    public MethodHandle() {
        super(8);
        this.methodId = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 0);
        this.memberId = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 4);
        this.mKey = new IdKey<>(this);
    }

    @Override
    public IdKey<MethodHandle> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key) {
        MethodHandle methodHandle = ((IdKey<MethodHandle>) key).getItem();
        if(methodHandle == this){
            return;
        }
        setMethodId(methodHandle.getMethodIdKey());
        setMemberId(methodHandle.getMemberIdKey());
    }

    public MethodId getMethodId(){
        return methodId.getItem();
    }
    public MethodKey getMethodIdKey(){
        return (MethodKey) methodId.getKey();
    }
    public void setMethodId(MethodKey methodKey){
        methodId.setItem(methodKey);
    }
    public MethodId getMemberId(){
        return memberId.getItem();
    }
    public MethodKey getMemberIdKey(){
        return (MethodKey) memberId.getKey();
    }
    public void setMemberId(MethodKey methodKey){
        memberId.setItem(methodKey);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.two(
                getMethodId().usedIds(),
                getMemberId().usedIds()
        );
    }
    @Override
    public SectionType<MethodHandle> getSectionType(){
        return SectionType.METHOD_HANDLE;
    }
    @Override
    public void refresh() {
        methodId.refresh();
        memberId.refresh();
    }
    @Override
    void cacheItems() {
        methodId.updateItem();
        memberId.updateItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

    @Override
    public int compareTo(MethodHandle methodHandle) {
        if(methodHandle == null){
            return -1;
        }
        int i = CompareUtil.compare(getMethodId(), methodHandle.getMethodId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getMemberId(), methodHandle.getMemberId());
    }

    @Override
    public String toString() {
        return memberId + "->" + getMethodId();
    }
}
