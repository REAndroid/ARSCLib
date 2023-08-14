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
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.debug.DebugElementList;
import com.reandroid.dex.debug.DebugParameterIndex;

public class DebugInfo extends DexItem {

    private final Ule128Item lineStart;
    private final Ule128Item parameterCount;
    private final CountedArray<DebugParameterIndex> parameterNames;
    private final DebugElementList elementList;

    public DebugInfo() {
        super(4);
        this.lineStart = new Ule128Item(true);
        this.parameterCount = new Ule128Item();
        this.parameterNames = new CountedArray<>(parameterCount, CREATOR);
        this.elementList = new DebugElementList(lineStart);

        addChild(0, lineStart);
        addChild(1, parameterCount);
        addChild(2, parameterNames);
        addChild(3, elementList);
    }
    @Override
    public String toString() {
        return "DebugInfo{" +
                "lineStart=" + lineStart.get() +
                ", parameterCount=" + parameterCount.get() +
                ", elements=" + parameterNames.getChildesCount() +
                '}';
    }

    private static final Creator<DebugParameterIndex> CREATOR = new Creator<DebugParameterIndex>() {
        @Override
        public DebugParameterIndex[] newInstance(int length) {
            return new DebugParameterIndex[length];
        }
        @Override
        public DebugParameterIndex newInstance() {
            return new DebugParameterIndex();
        }
    };

}
