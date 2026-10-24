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
package com.reandroid.arsc.base;

import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BlockContainer extends Block implements BlockRefresh {

    public BlockContainer() {
        super();
    }

    protected void onPreRefresh() {

    }
    protected abstract void onRefreshed();

    @Override
    public final void refresh() {
        if (isNull()) {
            return;
        }
        onPreRefresh();
        refreshBlocks();
        onRefreshed();
    }

    protected void refreshBlocks() {
        Object[] blocks = blocks();
        if (blocks != null) {
            int length = blocks.length;
            for (int i = 0; i < length; i++) {
                Object item = blocks[i];
                if (item instanceof BlockRefresh) {
                    ((BlockRefresh) item).refresh();
                }
            }
        }
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
        Object[] blocks = blocks();
        if (blocks != null) {
            int length = blocks.length;
            for (int i = 0; i < length; i++) {
                if (counter.FOUND) {
                    break;
                }
                Object item = blocks[i];
                if (item != null) {
                    ((Block) item).onCountUpTo(counter);
                }
            }
        }
    }
    @Override
    public int countBytes() {
        int result = 0;
        if (!isNull()) {
            Object[] blocks = blocks();
            if (blocks != null) {
                int max = blocks.length;
                for (int i = 0; i < max; i++) {
                    Object item = blocks[i];
                    if (item != null) {
                        result += ((Block) item).countBytes();
                    }
                }
            }
        }
        return result;
    }
    @Override
    public byte[] getBytes() {
        byte[] results = null;
        if (!isNull()) {
            Object[] blocks = blocks();
            if (blocks != null) {
                int length = blocks.length;
                for (int i = 0; i < length; i++) {
                    Object item = blocks[i];
                    if (item != null) {
                        results = addBytes(results, ((Block) item).getBytes());
                    }
                }
            }
        }
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        int result = 0;
        if (!isNull()) {
            Object[] blocks = blocks();
            if (blocks != null) {
                int length = blocks.length;
                for (int i = 0; i < length; i++) {
                    Object item = blocks[i];
                    if (item != null) {
                        result += ((Block) item).writeBytes(stream);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        Object[] blocks = blocks();
        if (blocks != null) {
            int length = blocks.length;
            for (int i = 0; i < length; i++) {
                Object item = blocks[i];
                if (item != null) {
                    ((Block) item).readBytes(reader);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public<T> T block(int i) {
        return (T) blocks()[i];
    }
    @SuppressWarnings("unchecked")
    public<T> T blockLast() {
        Object[] blocks = blocks();
        return (T) blocks[blocks.length - 1];
    }
    public abstract Object[] blocks();

}
