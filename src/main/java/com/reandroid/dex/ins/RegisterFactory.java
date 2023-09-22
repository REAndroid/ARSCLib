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
package com.reandroid.dex.ins;

import com.reandroid.dex.item.CodeItem;

public class RegisterFactory {
    private final CodeItem codeItem;
    public RegisterFactory(CodeItem codeItem){
        this.codeItem = codeItem;
    }

    public int getValue(int register) {
        int local = codeItem.getLocalsCount();
        if(register >= local){
            register = register - local;
        }
        return register;
    }
    public boolean isParameter(int register) {
        return register >= codeItem.getLocalsCount();
    }
}
