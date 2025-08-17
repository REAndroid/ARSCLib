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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.BooleanReference;

public class BooleanBit extends BitItem implements BooleanReference {

    private Object tag;

    public BooleanBit() {
        super(1);
    }

    @Override
    public boolean get() {
        return get(0);
    }
    @Override
    public void set(boolean value) {
        set(0, value);
    }

    public Object getTag() {
        return tag;
    }
    public void setTag(Object tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return Boolean.toString(get());
    }

    public static final Creator<BooleanBit> CREATOR = BooleanBit::new;
}
