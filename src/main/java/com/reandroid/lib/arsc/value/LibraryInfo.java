package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.PackageName;

import java.io.IOException;
import java.io.OutputStream;

public class LibraryInfo extends Block {
    private final IntegerItem mPackageId;
    private final PackageName mPackageName;

    public LibraryInfo(){
        super();
        this.mPackageId=new IntegerItem();
        this.mPackageName=new PackageName();
        mPackageId.setIndex(0);
        mPackageId.setParent(this);
        mPackageName.setIndex(1);
        mPackageName.setParent(this);
    }

    public int getPackageId(){
        return mPackageId.get();
    }
    public void setPackageId(int id){
        mPackageId.set(id);
    }
    public String getPackageName(){
        return mPackageName.get();
    }
    public void setPackageName(String packageName){
        mPackageName.set(packageName);
    }

    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return addBytes(mPackageId.getBytes(), mPackageName.getBytes());
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return mPackageId.countBytes()+mPackageName.countBytes();
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
        mPackageId.onCountUpTo(counter);
        mPackageName.onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result=mPackageId.writeBytes(stream);
        result+=mPackageName.writeBytes(stream);
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        mPackageId.readBytes(reader);
        mPackageName.readBytes(reader);
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("LIBRARY{");
        builder.append(String.format("0x%02x", getPackageId()));
        builder.append(':');
        String name=getPackageName();
        if(name==null){
            name="NULL";
        }
        builder.append(name);
        builder.append('}');
        return builder.toString();
    }
}
