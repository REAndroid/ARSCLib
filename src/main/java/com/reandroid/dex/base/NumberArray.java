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
package com.reandroid.dex.base;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.StringsUtil;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

public class NumberArray extends DexBlockItem {

    private final IntegerReference widthReference;
    private final IntegerReference itemCount;

    private List<Number> numberList;

    public NumberArray(IntegerReference widthReference, IntegerReference itemCount) {
        super(0);
        this.widthReference = widthReference;
        this.itemCount = itemCount;
    }
    public IntegerReference getReference(int index) {
        return new Data(this, index);
    }
    public short[] getShortArray(){
        int width = getWidth();
        short[] results = new short[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getShort(bytes, i * width);
        }
        return results;
    }
    public int[] getByteUnsignedArray(){
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = bytes[i] & 0xff;
        }
        return results;
    }
    public int[] getShortUnsignedArray(){
        int width = getWidth();
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getShortUnsigned(bytes, i * width);
        }
        return results;
    }
    public int[] getIntArray(){
        int width = getWidth();
        int[] results = new int[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getInteger(bytes, i * width);
        }
        return results;
    }
    public long[] getLongArray(){
        int width = getWidth();
        long[] results = new long[size()];
        byte[] bytes = getBytesInternal();
        for(int i = 0; i < results.length; i++){
            results[i] = getLong(bytes, i * width);
        }
        return results;
    }

    public int getByteUnsigned(int index){
        return getBytesInternal()[index * getWidth()] & 0xff;
    }
    public int getShortUnsigned(int index){
        return getShortUnsigned(getBytesInternal(), index * getWidth());
    }
    public int getInteger(int index){
        return getInteger(getBytesInternal(), index * getWidth());
    }
    public void setInteger(int index, int value){
        putInteger(getBytesInternal(), index * getWidth(), value);
    }
    public long getLong(int index){
        return getLong(getBytesInternal(), index * getWidth());
    }

    public List<Number> toList(){
        if(numberList == null){
            numberList = new AbstractList<Number>() {
                @Override
                public Number get(int i) {
                    return NumberArray.this.getNumber(i);
                }
                @Override
                public int size() {
                    return NumberArray.this.size();
                }
            };
        }
        return numberList;
    }
    public Number getNumber(int index){
        int width = getWidth();
        if(width == 1){
            return getBytesInternal()[index];
        }
        int offset = index * width;
        if(width == 2){
            return getShortUnsigned(getBytesInternal(), offset);
        }
        if(width == 4){
            return getInteger(getBytesInternal(), offset);
        }
        return getLong(getBytesInternal(), offset);
    }
    public int size(){
        return countBytes() / getWidth();
    }
    public void setSize(int size){
        setBytesLength(size * getWidth(), false);
        itemCount.set(size);
    }
    public int getWidth(){
        int width = widthReference.get();
        if(width == 0){
            width = 1;
        }
        return width;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int count = itemCount.get();
        int width = widthReference.get();
        if(width == 0){
            width = 1;
            count = 0;
            itemCount.set(0);
        }
        setBytesLength(count * width, false);
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        return "width=" + getWidth() + ", " + StringsUtil.toString(toList());
    }

    static class Data implements IntegerReference {
        private final NumberArray numberArray;
        private final int index;

        public Data(NumberArray numberArray, int index){
            this.numberArray = numberArray;
            this.index = index;
        }
        @Override
        public int get() {
            return numberArray.getInteger(index);
        }
        @Override
        public void set(int value) {
            numberArray.setInteger(index, value);
        }
    }
}
