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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.CountedArray;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.debug.DebugElement;
import com.reandroid.dex.debug.DebugSequence;
import com.reandroid.dex.debug.DebugParameter;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class DebugInfo extends DataItem {

    private final Ule128Item lineStart;
    private final Ule128Item debugParameterCount;
    private CountedArray<DebugParameter> debugParametersArray;
    private final DebugSequence debugSequence;

    public DebugInfo() {
        super(4);
        this.lineStart = new Ule128Item(true);
        this.debugParameterCount = new Ule128Item();

        this.debugSequence = new DebugSequence(lineStart);

        addChild(0, lineStart);
        addChild(1, debugParameterCount);
        // index = 2, parameter names
        addChild(3, debugSequence);
    }
    public int getParameterCount(){
        if(debugParametersArray != null){
            return debugParametersArray.getCount();
        }
        return 0;
    }
    public DebugParameter getDebugParameter(int index){
        CountedArray<DebugParameter> debugParametersArray = this.debugParametersArray;
        if(debugParametersArray != null){
            return debugParametersArray.get(index);
        }
        return null;
    }
    public DebugParameter getOrCreateDebugParameter(int index){
        CountedArray<DebugParameter> debugParametersArray = initParametersArray();
        debugParametersArray.ensureSize(index + 1);
        return debugParametersArray.get(index);
    }
    public void removeDebugParameter(int index){
        CountedArray<DebugParameter> parameterNames = this.debugParametersArray;
        if(parameterNames == null){
            return;
        }
        DebugParameter parameter = parameterNames.get(index);
        if(parameter == null){
            return;
        }
        if(index == parameterNames.getCount() - 1){
            parameterNames.remove(parameter);
        }else {
            parameter.set(0);
        }
        parameterNames.refresh();
    }
    public Iterator<DebugParameter> getParameters(){
        if(debugParametersArray != null){
            return debugParametersArray.iterator();
        }
        return EmptyIterator.of();
    }
    public Iterator<DebugElement> getExtraLines(){
        return getDebugSequence().getExtraLines();
    }
    public DebugSequence getDebugSequence(){
        return debugSequence;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        lineStart.onReadBytes(reader);
        debugParameterCount.onReadBytes(reader);
        if(debugParameterCount.get() > 0){
            initParametersArray().onReadBytes(reader);
        }
        debugSequence.onReadBytes(reader);
    }
    private CountedArray<DebugParameter> initParametersArray(){
        CountedArray<DebugParameter> debugParametersArray = this.debugParametersArray;
        if(debugParametersArray == null){
            debugParametersArray = new CountedArray<>(this.debugParameterCount, CREATOR);
            this.debugParametersArray = debugParametersArray;
            addChild(2, debugParametersArray);
        }
        return debugParametersArray;
    }

    @Override
    public String toString() {
        return "DebugInfo{" +
                "lineStart=" + lineStart.get() +
                ", parameterCount=" + debugParameterCount.get() +
                ", elements=" + debugSequence +
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
