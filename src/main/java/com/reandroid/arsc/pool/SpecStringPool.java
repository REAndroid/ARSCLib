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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.SpecStringArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.SpecString;

public class SpecStringPool extends StringPool<SpecString> {
    public SpecStringPool(boolean is_utf8){
        super(is_utf8);
    }

    @Override
    StringArray<SpecString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new SpecStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public PackageBlock getPackageBlock(){
        return getParent(PackageBlock.class);
    }

    @Override
    void linkStrings(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            packageBlock.linkSpecStringsInternal(this);
        }
    }
}
