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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public abstract class SmaliValue extends Smali implements KeyReference {

    public SmaliValue(){
        super();
    }

    public abstract DexValueType<?> getValueType();
    @Override
    public abstract Key getKey();
    @Override
    public abstract void setKey(Key key);
    @Override
    public abstract void parse(SmaliReader reader) throws IOException;
}
