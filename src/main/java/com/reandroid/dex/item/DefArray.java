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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public abstract class DefArray<T extends Def> extends BlockArray<T>  implements SmaliFormat {
    private final IntegerReference itemCount;
    public DefArray(IntegerReference itemCount){
        super();
        this.itemCount = itemCount;
    }
    @Override
    protected void onRefreshed() {
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setChildrenCount(itemCount.get());
        super.onReadBytes(reader);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        for(Def def : getChildren()){
            def.append(writer);
            writer.newLine();
        }
    }
}
