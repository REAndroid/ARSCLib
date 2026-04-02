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

import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.key.MemberKey;
import com.reandroid.dex.program.MemberProgram;


public abstract class SmaliMember extends SmaliDef implements MemberProgram {

    private int hiddenApiFlagsValue;

    public SmaliMember() {
        super();
        this.hiddenApiFlagsValue = HiddenApiFlag.NO_RESTRICTION;
    }

    @Override
    public abstract MemberKey getKey();

    @Override
    public int getHiddenApiFlagsValue() {
        return hiddenApiFlagsValue;
    }
    @Override
    public void setHiddenApiFlagsValue(int hiddenApiFlagsValue) {
        this.hiddenApiFlagsValue = hiddenApiFlagsValue;
    }
}
