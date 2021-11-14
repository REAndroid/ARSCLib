package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.SpecTypePairArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.PackageLastBlocks;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.PackageName;
import com.reandroid.lib.arsc.pool.SpecStringPool;
import com.reandroid.lib.arsc.pool.TypeStringPool;
import com.reandroid.lib.arsc.value.LibraryInfo;


public class PackageBlock extends BaseChunk {
    private final IntegerItem mPackageId;
    private final PackageName mPackageName;

    private final IntegerItem mTypeStrings;
    private final IntegerItem mLastPublicType;
    private final IntegerItem mKeyStrings;
    private final IntegerItem mLastPublicKey;
    private final IntegerItem mTypeIdOffset;

    private final TypeStringPool mTypeStringPool;
    private final SpecStringPool mSpecStringPool;

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlock mLibraryBlock;

    private final PackageLastBlocks mPackageLastBlocks;

    public PackageBlock() {
        super(ChunkType.PACKAGE, 3);
        this.mPackageId=new IntegerItem();
        this.mPackageName=new PackageName();
        this.mTypeStrings=new IntegerItem();
        this.mLastPublicType=new IntegerItem();
        this.mKeyStrings=new IntegerItem();
        this.mLastPublicKey=new IntegerItem();
        this.mTypeIdOffset=new IntegerItem();

        this.mTypeStringPool=new TypeStringPool(false);
        this.mSpecStringPool=new SpecStringPool(true);

        this.mSpecTypePairArray=new SpecTypePairArray();
        this.mLibraryBlock=new LibraryBlock();
        this.mPackageLastBlocks=new PackageLastBlocks(mSpecTypePairArray, mLibraryBlock);

        addToHeader(mPackageId);
        addToHeader(mPackageName);
        addToHeader(mTypeStrings);
        addToHeader(mLastPublicType);
        addToHeader(mKeyStrings);
        addToHeader(mLastPublicKey);
        addToHeader(mTypeIdOffset);

        addChild(mTypeStringPool);
        addChild(mSpecStringPool);

        addChild(mPackageLastBlocks);

    }
    public int getId(){
        return mPackageId.get();
    }
    public void setId(int id){
        mPackageId.set(id);
    }
    public String getName(){
        return mPackageName.get();
    }
    public void setName(String name){
        mPackageName.set(name);
    }


    public TableBlock getTableBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof TableBlock){
                return (TableBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public TypeStringPool getTypeStringPool(){
        return mTypeStringPool;
    }
    public SpecStringPool getSpecStringPool(){
        return mSpecStringPool;
    }

    public void addLibrary(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        LibraryInfo[] allInfo=libraryBlock.getAllInfo();
        if (allInfo==null){
            return;
        }
        for(LibraryInfo info:allInfo){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        mLibraryBlock.addLibraryInfo(info);
    }

    private void refreshKeyStrings(){
        int pos=countUpTo(mSpecStringPool);
        mKeyStrings.set(pos);
    }
    @Override
    public void onChunkLoaded() {
    }

    @Override
    protected void onChunkRefreshed() {
        refreshKeyStrings();
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(String.format("0x%02x", getId()));
        builder.append(", name=");
        builder.append(getName());
        int libCount=mLibraryBlock.getLibraryCount();
        if(libCount>0){
            builder.append(", libraries=");
            builder.append(libCount);
        }
        return builder.toString();
    }
}
