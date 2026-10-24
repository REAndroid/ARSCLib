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
import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public class SingleBlockContainer<T extends Block> extends BlockContainer {
    
    private T block;
    
    public SingleBlockContainer() {
        super();
    }
    
    @Override
    protected void refreshBlocks() {
        T block = this.block;
        if (block instanceof BlockRefresh) {
            ((BlockRefresh)block).refresh();
        }
    }
    @Override
    protected void onRefreshed() {
    }
    public T block() {
        return block;
    }
    public void block(T item) {
        if (item == null) {
            T oldItem = this.block;
            if (oldItem != null) {
                oldItem.setIndex(-1);
                oldItem.setParent(null);
            }
            this.block = null;
        } else {
            this.block = item;
            item.setIndex(getIndex());
            item.setParent(this);
        }
    }
    @Override
    public byte[] getBytes() {
        if (block != null) {
            return block.getBytes();
        }
        return null;
    }
    @Override
    public int countBytes() {
        Block block = this.block;
        if (block != null) {
            return block.countBytes();
        }
        return 0;
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
        Block block = this.block;
        if (block != null) {
            block.onCountUpTo(counter);
        }
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        Block block = this.block;
        if (block != null) {
            block.readBytes(reader);
        }
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        Block block = this.block;
        if (block != null) {
            return block.writeBytes(stream);
        }
        return 0;
    }

    @Override
    public final Object[] blocks() {
        throw new IllegalArgumentException("Method not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T1> T1 block(int i) {
        if (i == 0) {
            return (T1) block();
        }
        throw new IndexOutOfBoundsException();
    }
    @Override
    public <T1> T1 blockLast() {
        return block(0);
    }

    @Override
    public String toString() {
        Block block = this.block;
        if (block != null) {
            return block.toString();
        }
        return getClass().getSimpleName() + ": EMPTY";
    }
}
