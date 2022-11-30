package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.item.ByteItem;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.pool.TypeStringPool;

abstract class BaseTypeBlock extends BaseChunk {
    private final ByteItem mTypeId;
    private final ByteItem mTypeFlags;
    private final ByteItem mReserved1;
    private final ByteItem mReserved2;
    private final IntegerItem mEntryCount;
    private TypeString mTypeString;
    BaseTypeBlock(ChunkType chunkType, int initialChildesCount) {
        super(chunkType, initialChildesCount);
        this.mTypeId=new ByteItem();
        this.mTypeFlags=new ByteItem();
        this.mReserved1=new ByteItem();
        this.mReserved2=new ByteItem();
        this.mEntryCount=new IntegerItem();
        addToHeader(mTypeId);
        addToHeader(mTypeFlags);
        addToHeader(mReserved1);
        addToHeader(mReserved2);
        addToHeader(mEntryCount);
    }
    public byte getTypeId(){
        return mTypeId.get();
    }
    public void setTypeId(byte id){
        mTypeId.set(id);
    }
    public void setTypeName(String name){
        TypeStringPool typeStringPool=getTypeStringPool();
        byte id=getTypeId();
        TypeString typeString=typeStringPool.getById(id);
        if(typeString==null){
            typeString=typeStringPool.getOrCreate(id, name);
        }
        typeString.set(name);
    }
    public void setEntryCount(int count){
        if(count == mEntryCount.get()){
            return;
        }
        mEntryCount.set(count);
        onSetEntryCount(count);
    }
    public int getEntryCount(){
        return mEntryCount.get();
    }
    public PackageBlock getPackageBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof SpecTypePair){
                return ((SpecTypePair)parent).getPackageBlock();
            }
            parent=parent.getParent();
        }
        return null;
    }
    public TypeString getTypeString(){
        if(mTypeString!=null){
            if(mTypeString.getId()==getTypeId()){
                return mTypeString;
            }
            mTypeString=null;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TypeStringPool typeStringPool=packageBlock.getTypeStringPool();
        mTypeString=typeStringPool.getById(getTypeId());
        return mTypeString;
    }
    SpecTypePair getSpecTypePair(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof SpecTypePair){
                return (SpecTypePair)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    abstract void onSetEntryCount(int count);
    IntegerItem getEntryCountBlock(){
        return mEntryCount;
    }
    private TypeStringPool getTypeStringPool(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock!=null){
            return packageBlock.getTypeStringPool();
        }
        return null;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock!=null){
            builder.append("PKG=");
            builder.append(String.format("0x%02x", packageBlock.getId()));
            builder.append(" ");
        }
        builder.append(getHeaderBlock().toString());
        builder.append(" entries=");
        builder.append(getEntryCount());
        builder.append(", id=");
        builder.append(String.format("0x%02x", getTypeId()));
        TypeString typeString=getTypeString();
        if(typeString!=null){
            builder.append('(');
            builder.append(typeString.get());
            builder.append(')');
        }
        return builder.toString();
    }
}
