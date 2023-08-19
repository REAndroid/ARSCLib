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
package com.reandroid.dex.instruction;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.base.NumberArray;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class InsArray extends PayloadIns {

    private final ShortItem elementWidth;
    private final IntegerItem elementCount;
    private final NumberArray numberArray;
    private final DexBlockAlign blockAlign;

    public InsArray() {
        super(4, Opcode.ARRAY_PAYLOAD);
        this.elementWidth = new ShortItem();
        this.elementCount = new IntegerItem();
        this.numberArray = new NumberArray(elementWidth, elementCount);
        this.blockAlign = new DexBlockAlign(numberArray);
        this.blockAlign.setAlignment(2);

        addChild(1, elementWidth);
        addChild(2, elementCount);
        addChild(3, numberArray);
        addChild(4, blockAlign);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getOpcode());
        String separator = "\n    ";
        int width = numberArray.getWidth() * 2;
        if(width == 2){
            buildHex(builder, separator, width, numberArray.getByteUnsignedArray());
        }else if(width == 4){
            buildHex(builder, separator, width, numberArray.getShortUnsignedArray());
        }else if(width == 8){
            buildHex(builder, separator, width, numberArray.getIntArray());
        }else if(width == 16){
            buildHex(builder, separator, width, numberArray.getLongArray());
        }else {
            builder.append(" # Invalid number width: ").append(numberArray.getWidth());
        }
        return builder.toString();
    }
    private static void buildHex(StringBuilder builder, String separator, int width, int[] numbers){
        for(int num : numbers){
            builder.append(separator);
            builder.append(HexUtil.toHex(num, width));
        }
    }
    private static void buildHex(StringBuilder builder, String separator, int width, long[] numbers){
        for(long num : numbers){
            builder.append(separator);
            builder.append(HexUtil.toHex(num, width));
        }
    }
}