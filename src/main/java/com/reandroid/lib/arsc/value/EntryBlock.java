package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ShortItem;
import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.pool.SpecStringPool;


import java.io.IOException;
import java.io.OutputStream;

public class EntryBlock extends Block {
    private ShortItem mHeaderSize;
    private ShortItem mFlags;
    private IntegerItem mSpecReference;
    private BaseResValue mResValue;
    private boolean mUnLocked;
    public EntryBlock() {
        super();
    }

    public short getFlags(){
        return mFlags.get();
    }
    public void setFlags(short sh){
        mFlags.set(sh);
    }
    public int getSpecReference(){
        return mSpecReference.get();
    }
    public void setSpecReference(int ref){
        mSpecReference.set(ref);
    }
    public BaseResValue getResValue(){
        return mResValue;
    }
    public void setResValue(BaseResValue resValue){
        if(resValue==mResValue){
            return;
        }
        if(resValue!=null){
            resValue.setIndex(3);
            resValue.setParent(this);
        }
        if(mResValue!=null){
            mResValue.setIndex(-1);
            mResValue.setParent(null);
        }
        mResValue=resValue;
    }

    public void setFlagComplex(boolean is_complex){
        if(is_complex){
            setFlags(FLAG_COMPLEX);
        }else {
            setFlags(FLAG_INT);
        }
        refreshHeaderSize();
        refreshResValue();
    }

    public ResConfig getResConfig(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getResConfig();
        }
        return null;
    }
    public SpecString getSpecString(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock==null){
            return null;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        return specStringPool.get(getSpecReference());
    }
    public TypeString getTypeString(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeString();
        }
        return null;
    }
    public String getResourceName(){
        return getResourceName("@", null);
    }
    public String getResourceName(String prefix){
        return getResourceName(prefix, null);
    }
    public String getResourceName(String prefix, String appName){
        if(isNull()){
            return null;
        }
        TypeString type=getTypeString();
        if(type==null){
            return null;
        }
        SpecString spec=getSpecString();
        if(spec==null){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        if(prefix!=null){
            builder.append(prefix);
        }
        if(appName!=null){
            builder.append(appName);
        }
        builder.append(type.get());
        builder.append('/');
        builder.append(spec.get());
        return builder.toString();
    }
    public int getResourceId(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock==null){
            return 0;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return 0;
        }
        int pkgId=packageBlock.getId();
        int typeId=typeBlock.getTypeId();
        int entryId=getIndex();
        return ((pkgId << 24) | (typeId << 16) | entryId);
    }
    public TypeBlock getTypeBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof TypeBlock){
                return (TypeBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }


    private void unlockEntry(){
        if(mUnLocked){
            return;
        }
        mUnLocked =true;
        this.mHeaderSize =new ShortItem();
        this.mFlags =new ShortItem();
        this.mSpecReference = new IntegerItem();

        mHeaderSize.setIndex(0);
        mFlags.setIndex(1);
        mSpecReference.setIndex(2);

        mHeaderSize.setParent(this);
        mFlags.setParent(this);
        mSpecReference.setParent(this);

    }
    private void lockEntry(){
        if(!mUnLocked){
            return;
        }
        mUnLocked =false;
        mHeaderSize.setParent(null);
        mFlags.setParent(null);
        mSpecReference.setParent(null);

        mHeaderSize.setIndex(-1);
        mFlags.setIndex(-1);
        mSpecReference.setIndex(-1);
        removeResValue();

        this.mHeaderSize =null;
        this.mFlags =null;
        this.mSpecReference =null;
    }
    private void removeResValue(){
        if(mResValue!=null){
            mResValue.setParent(null);
            mResValue.setIndex(-1);
            mResValue=null;
        }
    }
    private void refreshResValue(){
        if(mResValue==null){
            return;
        }
        if(isFlagsComplex()==(mResValue instanceof ResValueBag)){
            return;
        }
        removeResValue();
        createResValue();
    }
    private void refreshHeaderSize(){
        if(isFlagsComplex()){
            mHeaderSize.set(HEADER_COMPLEX);
        }else {
            mHeaderSize.set(HEADER_INT);
        }
    }
    private void createResValue(){
        if(getResValue()!=null){
            return;
        }
        BaseResValue resValue;
        if(isFlagsComplex()){
            resValue=new ResValueBag();
        }else {
            resValue=new ResValueInt();
        }
        setResValue(resValue);
    }

    private boolean isFlagsComplex(){
        return ((mFlags.get() & FLAG_COMPLEX_MASK) != 0);
    }

    @Override
    public void setNull(boolean is_null){
        if(is_null){
            lockEntry();
        }else {
            unlockEntry();
        }
        super.setNull(is_null);
    }
    @Override
    public boolean isNull(){
        if(super.isNull() || !mUnLocked){
            return true;
        }
        return mResValue==null;
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        byte[] results=mHeaderSize.getBytes();
        results=addBytes(results, mFlags.getBytes());
        results=addBytes(results, mSpecReference.getBytes());
        results=addBytes(results, mResValue.getBytes());
        return results;
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return 8+mResValue.countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        if(isNull()){
            return;
        }
        counter.addCount(countBytes());
        mHeaderSize.onCountUpTo(counter);
        mFlags.onCountUpTo(counter);
        mSpecReference.onCountUpTo(counter);
        mResValue.onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        int result=mHeaderSize.writeBytes(stream);
        result+=mFlags.writeBytes(stream);
        result+= mSpecReference.writeBytes(stream);
        result+=mResValue.writeBytes(stream);
        return result;
    }

    private void updatePackage(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock==null){
            return;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return;
        }
        packageBlock.onEntryAdded(this);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        setNull(false);
        removeResValue();
        mHeaderSize.readBytes(reader);
        mFlags.readBytes(reader);
        mSpecReference.readBytes(reader);
        createResValue();
        mResValue.readBytes(reader);
        updatePackage();
    }
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        ResConfig resConfig=getResConfig();
        if(resConfig!=null){
            builder.append(resConfig.toString());
            builder.append(", ");
        }
        builder.append(" resId=");
        builder.append(String.format("0x%08x", getResourceId()));
        if(isNull()){
            builder.append(", null entry");
            return builder.toString();
        }
        String name=getResourceName();
        if(name!=null){
            builder.append('(');
            builder.append(name);
            builder.append(')');
        }
        return builder.toString();
    }

    private final static short FLAG_COMPLEX_MASK = 0x0001;

    private final static short FLAG_COMPLEX = 0x0003;
    private final static short FLAG_INT = 0x0002;

    private final static short HEADER_COMPLEX=0x0010;
    private final static short HEADER_INT=0x0008;
}
