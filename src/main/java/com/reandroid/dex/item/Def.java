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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class Def extends FixedBlockContainer implements SmaliFormat {
    private final Ule128Item id;
    private final Ule128Item accessFlags;
    public Def(int childrenCount) {
        super(childrenCount + 2);
        this.id = new Ule128Item(true);
        this.accessFlags = new Ule128Item();
        addChild(0, id);
        addChild(1, accessFlags);
    }
    public int getIdValue() {
        return id.get();
    }
    public int getAccessFlagsValue() {
        return accessFlags.get();
    }
    public int getDefIndexId() {
        DefArray<?> parentArray = getParentInstance(DefArray.class);
        if(parentArray != null){
            Def previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return getIdValue() + previous.getDefIndexId();
            }
        }
        return id.get();
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {

    }
}
