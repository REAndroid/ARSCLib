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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.debug.DebugElementType;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.InstanceIterator;

import java.util.Iterator;

public class SmaliDebug extends SmaliCode{
    public SmaliDebug(){
        super();
    }

    public int getAddress(){
        return searchAddress();
    }
    private int searchAddress(){
        SmaliCodeSet codeSet = getCodeSet();
        if(codeSet == null){
            return -1;
        }
        Iterator<SmaliCode> iterator = codeSet.iterator(codeSet.indexOf(this) + 1);
        SmaliInstruction next = CollectionUtil.getFirst(
                InstanceIterator.of(iterator, SmaliInstruction.class));
        if(next != null){
            return next.getAddress();
        }
        return -1;
    }
    public DebugElementType<?> getDebugElementType(){
        return null;
    }
}
