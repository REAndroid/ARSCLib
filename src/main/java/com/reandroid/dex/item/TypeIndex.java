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

public class TypeIndex extends BaseItem {
    public TypeIndex() {
        super(4);
    }

    public StringIndex getString(){
        return getStringIndex(getStringIndex());
    }
    public int getStringIndex(){
        return getInteger(getBytesInternal(), 0);
    }
    public void setStringIndex(int index){
        putInteger(getBytesInternal(), 0, index);
    }

    @Override
    public String toString(){
        StringIndex stringIndex = getString();
        if(stringIndex != null){
            return stringIndex.getString();
        }
        return getIndex() + ":" + getStringIndex() + "{" + getString() + "}";
    }
}
