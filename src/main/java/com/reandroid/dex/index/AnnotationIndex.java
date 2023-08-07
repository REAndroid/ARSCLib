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


import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexItem;
import com.reandroid.dex.base.IndirectInteger;

import java.io.IOException;

public class AnnotationIndex extends DexItem {

    private final IndirectInteger classOffset;
    private final IndirectInteger fieldCount;
    private final IndirectInteger methodCount;
    private final IndirectInteger parameterCount;
    private final IndirectInteger startIndex;

    public AnnotationIndex() {
        super(SIZE);
        int offset = -4;

        this.classOffset = new IndirectInteger(this, offset += 4);
        this.fieldCount = new IndirectInteger(this, offset += 4);
        this.methodCount = new IndirectInteger(this, offset += 4);
        this.parameterCount = new IndirectInteger(this, offset += 4);

        this.startIndex = new IndirectInteger(this, offset += 4);
    }

    public IndirectInteger getClassOffset() {
        return classOffset;
    }

    public IndirectInteger getFieldCount() {
        return fieldCount;
    }
    public IndirectInteger getMethodCount() {
        return methodCount;
    }
    public IndirectInteger getParameterCount() {
        return parameterCount;
    }

    private void adjustSize(){
        int size = SIZE;
        if(hasCount()){
            size += 4;
        }
        setBytesLength(size, false);
    }
    private boolean hasCount(){
        return getFieldCount().get() > 0
                || getMethodCount().get() > 0
                || getParameterCount().get() > 0;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        int size = countBytes();
        super.onReadBytes(reader);
        adjustSize();
        int sizeUpdated = countBytes();
        if(sizeUpdated > size){
            byte[] bytes = getBytesInternal();
            reader.read(bytes, size, sizeUpdated - size);
        }
    }

    @Override
    public String toString() {
        return  "classOffset=" + classOffset +
                ", fieldCount=" + fieldCount +
                ", methodCount=" + methodCount +
                ", parameterCount=" + parameterCount;
    }

    private static final int SIZE = 16;

}
