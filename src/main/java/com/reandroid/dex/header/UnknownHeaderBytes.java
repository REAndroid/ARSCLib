package com.reandroid.dex.header;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.InputStream;

public class UnknownHeaderBytes extends ByteArray implements DirectStreamReader, BlockRefresh {

    public UnknownHeaderBytes() {
        super();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        updateSize();
        super.onReadBytes(reader);
    }

    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        updateSize();
        return super.readBytes(inputStream);
    }

    @Override
    public void refresh() {
        updateSize();
    }

    private void updateSize() {
        setSize(computeUnknownSize());
    }
    private int computeUnknownSize() {
        DexHeader header = getParentInstance(DexHeader.class);
        if (header == null) {
            return 0;
        }
        int unknownSize = header.headerSize.get() - header.countBytes() - size();
        if (unknownSize < 0 || unknownSize > 0xff) {
            unknownSize = 0;
        }
        return unknownSize;
    }

    @Override
    public String toString() {
        int size = size();
        if (size > 0 && size <= 32) {
            return "size = " + size + " [" + HexUtil.toHexString(getBytesInternal()) + "]";
        }
        return super.toString();
    }
}
