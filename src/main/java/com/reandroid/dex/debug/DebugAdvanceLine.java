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
package com.reandroid.dex.debug;

import com.reandroid.dex.base.Sle128Item;

public class DebugAdvanceLine extends DebugElement{
    private final Sle128Item lineDiff;
    public DebugAdvanceLine() {
        super(1, DebugElementType.ADVANCE_LINE);
        this.lineDiff = new Sle128Item();
        addChild(1, lineDiff);
    }

    public int getLineDiff() {
        return lineDiff.get();
    }
    public void setLineDiff(int lineDiff) {
        this.lineDiff.set(lineDiff);
    }

    @Override
    public String toString() {
        return "lineDiff=" + lineDiff;
    }
}
