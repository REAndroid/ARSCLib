package com.reandroid.lib.apk;

import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.BaseChunk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class BlockInputSource<T extends BaseChunk> extends InputSource {
    private final T mBlock;
    public BlockInputSource(String name, T block) {
        super(name);
        this.mBlock=block;
    }
    public T getBlock() {
        return mBlock;
    }
    @Override
    public InputStream openStream(){
        T block=getBlock();
        block.refresh();
        return new ByteArrayInputStream(block.getBytes());
    }
}
