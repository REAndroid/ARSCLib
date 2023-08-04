package com.reandroid.dex.base;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.io.StreamUtil;

import java.io.IOException;

public class Ule128Item extends DexItem{
    private final boolean large;
    private int value;
    public Ule128Item(boolean large) {
        super(1);
        this.large = large;
    }
    public Ule128Item() {
        this(false);
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value) {
        if(this.value == value){
            return;
        }
        this.value = value;
        setBytesLength((large ? 5 : 4), false);
        int length = writeUleb128(getBytesInternal(), 0, value);
        setBytesLength(length, false);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        int value = readUleb128(StreamUtil.createByteReader(reader), (large ? 5 : 4));
        int length = reader.getPosition() - position;
        reader.seek(position);
        setBytesLength(length, false);
        reader.readFully(getBytesInternal());
        this.value = value;
    }

    @Override
    public String toString() {
        return "bytes = " + countBytes() + ", value = " + getValue();
    }
}
