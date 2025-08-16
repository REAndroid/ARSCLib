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
package com.reandroid.arsc.list;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.util.Arrays;

public class StyleItemListEnd extends BlockItem implements BlockRefresh {

    private final IntegerReference stylesCount;

    public StyleItemListEnd(IntegerReference stylesCount) {
        super(0);
        this.stylesCount = stylesCount;
    }

    private boolean updateSize() {
        if(stylesCount.get() == 0){
            setBytesLength(0, false);
            return false;
        }

        final int size = 8;
        setBytesLength(size, false);
        Arrays.fill(getBytesInternal(), 0, size, (byte) 0xff);
        return true;
    }
    @Override
    public void refresh() {
        updateSize();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if (updateSize()) {
            super.onReadBytes(reader);
        }
    }

    @Override
    public String toString() {
        return HexUtil.toHexString(getBytesInternal());
    }
}
