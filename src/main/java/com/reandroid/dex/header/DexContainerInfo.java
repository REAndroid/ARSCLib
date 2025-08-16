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
package com.reandroid.dex.header;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.sections.DexContainerBlock;

public class DexContainerInfo extends CountAndOffsetV41 implements BlockRefresh {

    public DexContainerInfo() {
        super();
    }

    public int getFileSize() {
        return getCount();
    }
    public void setFileSize(int count) {
        super.setCount(count);
    }

    public IntegerReference getOffsetReference() {
        return getSecond();
    }

    @Override
    public void refresh() {
        if (isNull()) {
            return;
        }
        DexContainerBlock containerBlock = getParentInstance(DexContainerBlock.class);
        if (containerBlock == null) {
            return;
        }
        setFileSize(containerBlock.getFileSize());

        setOffset(getParentInstance(DexHeader.class).getOffset());
    }
    @Override
    public String toString() {
        if (isNull()) {
            return "(--, --)";
        }
        return super.toString();
    }
}
