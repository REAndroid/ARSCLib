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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public class BlockListWithFooter <T extends Block> extends BlockList<T> {

    private Block footer;

    public BlockListWithFooter(Block footer, Creator<? extends T> creator) {
        super(creator);
        if (footer != null) {
            footer.setParent(this);
            footer.setIndex(1);
            this.footer = footer;
        }
    }
    public BlockListWithFooter(Block footer) {
        this(footer, null);
    }

    @SuppressWarnings("unchecked")
    public<T1 extends Block> T1 footer() {
        return (T1) footer;
    }
    public void footer(Block footer) {
        Block block = this.footer;
        if (block == footer) {
            return;
        }
        this.footer = footer;
        if (footer != null) {
            footer.setParent(this);
            footer.setIndex(1);
        }
        if (block != null && block.getParent() == this) {
            block.setParent(null);
            block.setIndex(-1);
        }
    }

    @Override
    public int countBytes() {
        int count = super.countElementBytes();
        Block footer = footer();
        if (footer != null) {
            count += footer.countBytes();
        }
        return count;
    }

    @Override
    public void onCountUpToEntries(BlockCounter counter) {
        super.onCountUpToEntries(counter);
        if (counter.FOUND) {
            return;
        }
        Block footer = footer();
        if (footer != null) {
            footer.onCountUpTo(counter);
        }
    }
    @Override
    public byte[] getBytes() {
        byte[] bytes = super.getBytes();
        Block footer = footer();
        if (footer != null) {
            bytes = Block.addBytes(bytes, footer.getBytes());
        }
        return bytes;
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        super.readBytes(reader);
        Block footer = footer();
        if (footer != null) {
            footer.readBytes(reader);
        }
    }
    @Override
    public void readBlockElements(BlockReader reader) throws IOException {
        super.readBlockElements(reader);
        Block footer = footer();
        if (footer != null) {
            footer.readBytes(reader);
        }
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int length = super.onWriteBytes(stream);
        Block footer = footer();
        if (footer != null) {
            length += footer.writeBytes(stream);
        }
        return length;
    }

    @Override
    protected void refreshBlockElements() {
        super.refreshBlockElements();
        Block footer = footer();
        if (footer instanceof BlockRefresh) {
            ((BlockRefresh) footer).refresh();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "\nfooter=" + footer();
    }
}
