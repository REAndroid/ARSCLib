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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public class WrappedBlock extends Block implements BlockRefresh {

    private final Block baseBlock;

    public WrappedBlock(Block baseBlock) {
        if (baseBlock == null) {
            throw new NullPointerException();
        }
        this.baseBlock = baseBlock;
        baseBlock.setParent(this);
        baseBlock.setIndex(0);
    }

    public final Block getBaseBlock() {
        return baseBlock;
    }

    @Override
    public boolean isNull() {
        return getBaseBlock().isNull();
    }

    @Override
    public void setNull(boolean is_null) {
        getBaseBlock().setNull(is_null);
    }

    @Override
    public byte[] getBytes() {
        return getBaseBlock().getBytes();
    }
    @Override
    public int countBytes() {
        return getBaseBlock().countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        getBaseBlock().onCountUpTo(counter);
    }
    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        getBaseBlock().readBytes(reader);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        return getBaseBlock().writeBytes(stream);
    }

    @Override
    public final void refresh() {
        Block base = getBaseBlock();
        if (base instanceof BlockRefresh) {
            onPreRefresh();
            ((BlockRefresh) base).refresh();
            onRefreshed();
        }
    }
    protected void onPreRefresh() {
    }
    protected void onRefreshed() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        Block base = getBaseBlock();
        if (obj instanceof WrappedBlock) {
            WrappedBlock wrappedBlock = (WrappedBlock) obj;
            return base.equals(wrappedBlock.getBaseBlock());
        }
        return base.equals(obj);
    }
    @Override
    public int hashCode() {
        return getBaseBlock().hashCode();
    }
    @Override
    public String toString() {
        return getBaseBlock().toString();
    }
}
