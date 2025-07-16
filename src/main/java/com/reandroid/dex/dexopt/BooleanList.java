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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;

import java.io.IOException;
import java.io.OutputStream;

public class BooleanList extends CountedBlockList<BooleanBit> {

    public BooleanList(IntegerReference countReference) {
        super(BooleanBit.CREATOR, countReference);
    }

    @Override
    public int countBytes() {
        return BitItem.bitsToBytes(size());
    }

    @Override
    public byte[] getBytes() {
        int size = size();
        if (size == 0) {
            return new byte[0];
        }
        BitItem pool = new BitItem(size);
        for (int i = 0; i < size; i++) {
            pool.set(i, get(i).get());
        }
        return pool.getBytes();
    }

    public int readFrom(BitItem pool, int poolOffset) {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).set(pool.get(poolOffset));
            poolOffset ++;
        }
        return poolOffset;
    }
    public int writeTo(BitItem pool, int poolOffset) {
        int size = size();
        pool.ensureBitsLength(poolOffset + size);
        for (int i = 0; i < size; i++) {
            pool.set(poolOffset, get(i).get());
            poolOffset ++;
        }
        return poolOffset;
    }
    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        int itemsCount = getCountReference().get();
        setSize(itemsCount);
        BitItem pool = new BitItem(itemsCount);
        pool.onReadBytes(reader);
        for (int i = 0; i < itemsCount; i++) {
            get(i).set(pool.get(i));
        }
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        byte[] bytes = getBytes();
        int length = bytes.length;
        stream.write(bytes, 0, length);
        return length;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if (counter.FOUND) {
            return;
        }
        counter.setCurrent(this);
        if (counter.END == this) {
            counter.FOUND = true;
            return;
        }
        Block end = counter.END;
        if (end != null && get(end.getIndex()) == end) {
            counter.addCount(BitItem.bitsToBytes(end.getIndex()));
            counter.setCurrent(end);
            counter.FOUND = true;
            return;
        }
        counter.addCount(countBytes());
    }
}
