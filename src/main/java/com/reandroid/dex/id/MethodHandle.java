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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodHandleKey;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.CombiningIterator;

import java.io.IOException;
import java.util.Iterator;

public class MethodHandle extends IdItem implements Comparable<MethodHandle> {

    private final IdItemIndirectReference<MethodId> id;
    private final IdItemIndirectReference<MethodId> member;

    public MethodHandle() {
        super(8);
        this.id = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 0);
        this.member = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 4);
    }

    @Override
    public MethodHandleKey getKey() {
        return checkKey(new MethodHandleKey(getIdKey(),
                getMemberKey()));
    }
    @Override
    public void setKey(Key key) {
        MethodHandleKey methodHandle = (MethodHandleKey) key;
        setId(methodHandle.getId());
        setMember(methodHandle.getMember());
    }

    public MethodId getId(){
        return id.getItem();
    }
    public MethodKey getIdKey(){
        return (MethodKey) id.getKey();
    }
    public void setId(MethodKey methodKey){
        id.setItem(methodKey);
    }
    public MethodId getMember(){
        return member.getItem();
    }
    public MethodKey getMemberKey(){
        return (MethodKey) member.getKey();
    }
    public void setMember(MethodKey methodKey){
        member.setItem(methodKey);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.two(
                getId().usedIds(),
                getMember().usedIds()
        );
    }
    @Override
    public SectionType<MethodHandle> getSectionType(){
        return SectionType.METHOD_HANDLE;
    }
    @Override
    public void refresh() {
        id.refresh();
        member.refresh();
    }
    @Override
    void cacheItems() {
        id.pullItem();
        member.pullItem();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        MethodId id = getId();
        if(id == null){
            writer.append("error id = ");
            writer.append(this.id.get());
        }else {
            id.append(writer);
        }
        writer.append(", ");
        MethodId member = getMember();
        if(member == null){
            writer.append("error member = ");
            writer.append(this.member.get());
        }else {
            member.append(writer);
        }
    }

    @Override
    public int compareTo(MethodHandle methodHandle) {
        if(methodHandle == null){
            return -1;
        }
        int i = CompareUtil.compare(getId(), methodHandle.getId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getMember(), methodHandle.getMember());
    }

    @Override
    public String toString() {
        return SmaliWriter.toStringSafe(this);
    }
}
