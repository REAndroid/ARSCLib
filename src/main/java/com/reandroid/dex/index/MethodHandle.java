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
package com.reandroid.dex.index;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.IndirectInteger;
import com.reandroid.dex.base.IndirectShort;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class MethodHandle extends ItemId {
    private final IntegerReference methodId;
    private final IntegerReference memberId;
    public MethodHandle() {
        super(8);
        int offset = -4;
        this.methodId = new IndirectInteger(this, offset += 4);
        this.memberId = new IndirectShort(this, offset += 4);
    }

    public TypeId getTypeId(){
        return getTypeId(memberId);
    }
    public MethodId getMethodId(){
        return get(SectionType.METHOD_ID, methodId.get());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

    @Override
    public String toString() {
        return getTypeId() + "->" + getMethodId();
    }
}
