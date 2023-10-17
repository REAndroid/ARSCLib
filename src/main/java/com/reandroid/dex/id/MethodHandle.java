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

import com.reandroid.dex.reference.IdItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class MethodHandle extends IdItem implements Comparable<MethodHandle>{

    private final IdItemIndirectReference<MethodId> methodId;
    private final IdItemIndirectReference<MethodId> memberId;

    public MethodHandle() {
        super(8);
        this.methodId = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 0);
        this.memberId = new IdItemIndirectReference<>(SectionType.METHOD_ID, this, 4);
    }

    public MethodId getMethodId(){
        return methodId.getItem();
    }
    public MethodId getMemberId(){
        return memberId.getItem();
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
