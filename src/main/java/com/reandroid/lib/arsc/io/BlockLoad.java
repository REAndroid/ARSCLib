package com.reandroid.lib.arsc.io;

import com.reandroid.lib.arsc.base.Block;

import java.io.IOException;

public interface BlockLoad {
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException;
}
