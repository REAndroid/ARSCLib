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
package com.reandroid.dex.item;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.debug.DebugElementList;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.dex.ins.LabelList;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class DebugInfo extends DexItem {

    private final Ule128Item lineStart;
    private final Ule128Item parameterCount;
    private CountedArray<DebugParameter> parameterNames;
    private final DebugElementList elementList;

    public DebugInfo() {
        super(4);
        this.lineStart = new Ule128Item(true);
        this.parameterCount = new Ule128Item();

        this.elementList = new DebugElementList(lineStart);

        addChild(0, lineStart);
        addChild(1, parameterCount);
        // index = 2, parameter names
        addChild(3, elementList);
    }
    public Iterator<DebugParameter> getParameters(){
        if(parameterNames != null){
            return parameterNames.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<DebugElement> getExtraLines(){
        return elementList.getExtraLines();
    }
    public DebugElementList getElementList(){
        return elementList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        lineStart.onReadBytes(reader);
        parameterCount.onReadBytes(reader);
        if(parameterCount.get() > 0 && parameterNames == null){
            this.parameterNames = new CountedArray<>(parameterCount, CREATOR);
            addChild(2, parameterNames);
            parameterNames.onReadBytes(reader);
        }
        elementList.onReadBytes(reader);
    }

    @Override
    public String toString() {
        return "DebugInfo{" +
                "lineStart=" + lineStart.get() +
                ", parameterCount=" + parameterCount.get() +
                ", elements=" + parameterNames +
                '}';
    }

    private static final Creator<DebugParameter> CREATOR = new Creator<DebugParameter>() {
        @Override
        public DebugParameter[] newInstance(int length) {
            if(length == 0){
                return EMPTY;
            }
            return new DebugParameter[length];
        }
        @Override
        public DebugParameter newInstance() {
            return new DebugParameter();
        }
    };
    static final DebugParameter[] EMPTY = new DebugParameter[0];

}
