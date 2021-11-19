package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.decoder.ResourceNameProvider;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.arsc.value.ResValueBag;

import java.util.Collection;

public class TableBlock extends BaseChunk implements ResourceNameProvider {
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
    public Collection<PackageBlock> listPackages(){
        return getPackageArray().listItems();
    }
    public TableStringPool getTableStringPool(){
        return mTableStringPool;
    }
    public PackageBlock getPackageBlockById(byte pkgId){
        return getPackageArray().getPackageBlockById(pkgId);
    }
    public PackageArray getPackageArray(){
        return mPackageArray;
    }

    private void refreshPackageCount(){
        int count = getPackageArray().childesCount();
        mPackageCount.set(count);
    }
    @Override
    protected void onChunkRefreshed() {
        refreshPackageCount();
    }
    @Override
    public String getResourceFullName(int resId, boolean includePackageName) {
        byte pkgId= (byte) ((resId>>24)&0xFF);
        if(pkgId==0){
            return null;
        }
        PackageBlock packageBlock=getPackageBlockById(pkgId);
        if(packageBlock!=null){
            return packageBlock.getResourceFullName(resId, includePackageName);
        }
        return null;
    }

    @Override
    public String getResourceName(int resId, boolean includePackageName) {
        byte pkgId= (byte) ((resId>>24)&0xFF);
        if(pkgId==0){
            return null;
        }
        PackageBlock packageBlock=getPackageBlockById(pkgId);
        if(packageBlock!=null){
            return packageBlock.getResourceName(resId, includePackageName);
        }
        return null;
    }
    @Override
    public ResValueBag getAttributeBag(int resId){
        byte pkgId= (byte) ((resId>>24)&0xFF);
        if(pkgId==0){
            return null;
        }
        PackageBlock packageBlock=getPackageBlockById(pkgId);
        if(packageBlock!=null){
            return packageBlock.getAttributeBag(resId);
        }
        return null;
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
