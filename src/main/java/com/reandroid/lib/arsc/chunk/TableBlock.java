package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.pool.TableStringPool;

public class TableBlock extends BaseChunk {
    private final IntegerItem mPackageCount;
    private final TableStringPool mTableStringPool;
    private final PackageArray mPackageArray;
    public TableBlock() {
        super(ChunkType.TABLE, 2);
        this.mPackageCount=new IntegerItem();
        this.mTableStringPool=new TableStringPool(true);
        this.mPackageArray=new PackageArray(mPackageCount);
        addToHeader(mPackageCount);
        addChild(mTableStringPool);
        addChild(mPackageArray);
    }
    public TableStringPool getTableStringPool(){
        return mTableStringPool;
    }
    public PackageArray getPackageArray(){
        return mPackageArray;
    }

    @Override
    protected void onChunkRefreshed() {

    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", packages=");
        int pkgCount=mPackageArray.childesCount();
        builder.append(pkgCount);
        return builder.toString();
    }
}
