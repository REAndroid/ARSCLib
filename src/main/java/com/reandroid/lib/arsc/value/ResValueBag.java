package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.array.ResValueBagItemArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ResValueBag extends BaseResValue {

    private final IntegerItem mParentId;
    private final IntegerItem mCount;
    private final ResValueBagItemArray mResValueBagItemArray;
    public ResValueBag(){
        super(0);
        mParentId =new IntegerItem();
        mCount=new IntegerItem();
        mResValueBagItemArray=new ResValueBagItemArray();

        mParentId.setParent(this);
        mParentId.setIndex(0);
        mCount.setParent(this);
        mCount.setIndex(1);
        mResValueBagItemArray.setParent(this);
        mResValueBagItemArray.setIndex(2);
    }
    public ResValueBagItemArray getResValueBagItemArray(){
        return mResValueBagItemArray;
    }

    public int getParentId(){
        return mParentId.get();
    }
    public void setParentId(int id){
        mParentId.set(id);
    }
    public int getCount(){
        return mCount.get();
    }
    public void setCount(int count){
        if(count<0){
            count=0;
        }
        mCount.set(count);
    }

    @Override
    public byte[] getBytes() {
        byte[] results = mParentId.getBytes();
        results=addBytes(results, mCount.getBytes());
        results=addBytes(results, mResValueBagItemArray.getBytes());
        return results;
    }
    @Override
    public int countBytes() {
        int result;
        result = mParentId.countBytes();
        result+=mCount.countBytes();
        result+=mResValueBagItemArray.countBytes();
        return result;
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(countSubChildes(counter, mParentId)){
            return;
        }
        if(countSubChildes(counter, mCount)){
            return;
        }
        countSubChildes(counter, mResValueBagItemArray);
    }
    private boolean countSubChildes(BlockCounter counter, Block child){
        if(counter.FOUND){
            return true;
        }
        if(counter.END==child){
            counter.FOUND=true;
            return true;
        }
        int c=child.countBytes();
        counter.addCount(c);
        return false;
    }

    private void refreshCount(){
        setCount(getResValueBagItemArray().childesCount());
    }
    @Override
    protected int onWriteBytes(OutputStream writer) throws IOException {
        if(isNull()){
            return 0;
        }
        refreshCount();
        int result;
        result=mParentId.writeBytes(writer);
        result+=mCount.writeBytes(writer);
        result+=mResValueBagItemArray.writeBytes(writer);
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        mResValueBagItemArray.clearChildes();
        mParentId.readBytes(reader);
        mCount.readBytes(reader);
        mResValueBagItemArray.setChildesCount(mCount.get());
        mResValueBagItemArray.readBytes(reader);
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": parent=");
        builder.append(String.format("0x%08x", getParentId()));
        builder.append(", count=");
        builder.append(getCount());
        return builder.toString();
    }


    // NOT Applicable
    @Override
    void setHeaderSize(short size) {
    }
    // NOT Applicable
    @Override
    short getHeaderSize() {
        return HEADER_COMPLEX;
    }
    // NOT Applicable
    @Override
    void setReserved(byte reserved) {
    }
    // NOT Applicable
    @Override
    byte getReserved() {
        return 0;
    }
    // NOT Applicable
    @Override
    public void setType(byte type) {
    }

    // NOT Applicable
    @Override
    public byte getType() {
        return 0;
    }

    // NOT Applicable
    @Override
    public int getData() {
        return 0;
    }

    // NOT Applicable
    @Override
    public void setData(int data) { }

    private final static short HEADER_COMPLEX=0x0010;
}
