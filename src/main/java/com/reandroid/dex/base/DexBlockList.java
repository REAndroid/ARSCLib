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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.container.BlockList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;

public class DexBlockList<T extends Block> extends BlockList<T> implements Collection<T> {
    private final DexPositionAlign dexPositionAlign;
    public DexBlockList(){
        super();
        this.dexPositionAlign = new DexPositionAlign();
        dexPositionAlign.setParent(this);
    }

    public DexPositionAlign getDexPositionAlign() {
        return dexPositionAlign;
    }
    @Override
    public byte[] getBytes() {
        return addBytes(super.getBytes(), dexPositionAlign.getBytes());
    }
    @Override
    public int countBytes() {
        return super.countBytes() + dexPositionAlign.countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        super.onCountUpTo(counter);
        if(counter.FOUND){
            return;
        }
        dexPositionAlign.onCountUpTo(counter);
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        return super.onWriteBytes(stream) + dexPositionAlign.writeBytes(stream);
    }

    //-------------- Collection overrides --------------//
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object obj) {
        return super.remove((T) obj);
    }
    @Override
    public void clear() {
        clearChildes();
    }
    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new IllegalArgumentException(
                "containsAll not implemented for: " + getClass().getSimpleName());
    }
    @Override
    public boolean addAll(Collection<? extends T> collection) {
        throw new IllegalArgumentException(
                "addAll not implemented for: " + getClass().getSimpleName());
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new IllegalArgumentException(
                "removeAll not implemented for: " + getClass().getSimpleName());
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException(
                "retainAll not implemented for: " + getClass().getSimpleName());
    }
}
