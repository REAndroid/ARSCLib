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

import java.io.IOException;

public class ShortList extends DexItem {
    public ShortList() {
        super(4);
    }

    public int size(){
        return getInteger(getBytesInternal(), 0);
    }
    public void setSize(int size){
        setBytesLength(4 + size * getFactor(), false);
        putInteger(getBytesInternal(), 0, size);
    }
    private int realSize(){
        return (countBytes() - 4) / getFactor();
    }
    int getFactor(){
        return 2;
    }
    public void addAll(int[] values){
        if(values == null || values.length == 0){
            return;
        }
        int length = values.length;
        int start = size();
        setSize(start + length);
        for(int i = 0; i < length; i++){
            set(start + i, values[i]);
        }
    }
    public void add(int value){
        int index = size();
        setSize(index + 1);
        set(index, value);
    }
    public void set(int index, int value){
        int i = 4 + index * 2;
        putShort(getBytesInternal(), i, (short) value);
    }
    public int get(int index){
        int i = 4 + index * 2;
        return getShort(getBytesInternal(), i) & 0xffff;
    }
    public int[] toArray(){
        int size = size();
        int[] result = new int[size];
        for(int i = 0; i < size; i++){
            result[i] = get(i);
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setSize(0);
        int position = reader.getPosition();
        reader.readFully(getBytesInternal());
        reader.seek(position);
        int size = size();
        if(size > 1000){
            String junk="";
        }
        if(size <= 0){
            setSize(0);
            return;
        }
        setSize(size);
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        int size = size();
        int real = realSize();
        if(size == real){
            return "size=" + size;
        }
        return "size=" + size + ", real=" + real;
    }
}
