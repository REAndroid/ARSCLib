package com.reandroid.lib.apk;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.chunk.TableBlock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BlockInputSource<T extends BaseChunk> extends ByteInputSource{
    private final T mBlock;
    public BlockInputSource(String name, T block) {
        super(new byte[0], name);
        this.mBlock=block;
    }
    public T getBlock() {
        mBlock.refresh();
        return mBlock;
    }
    @Override
    public long getLength() throws IOException{
        Block block = getBlock();
        return block.countBytes();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getBlock().writeBytes(outputStream);
    }
    @Override
    public byte[] getBytes() {
        return getBlock().getBytes();
    }
}
