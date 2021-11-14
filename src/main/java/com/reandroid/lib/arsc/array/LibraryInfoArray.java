package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.LibraryInfo;

import java.io.IOException;

public class LibraryInfoArray extends BlockArray<LibraryInfo> {
    private final IntegerItem mInfoCount;
    public LibraryInfoArray(IntegerItem infoCount){
        this.mInfoCount=infoCount;
    }
    @Override
    public LibraryInfo newInstance() {
        return new LibraryInfo();
    }
    @Override
    public LibraryInfo[] newInstance(int len) {
        return new LibraryInfo[len];
    }
    @Override
    protected void onRefreshed() {
        mInfoCount.set(childesCount());
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setChildesCount(mInfoCount.get());
        super.onReadBytes(reader);
    }
}
