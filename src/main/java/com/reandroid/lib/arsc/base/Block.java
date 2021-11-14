package com.reandroid.lib.arsc.base;

import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Block {
    private int mIndex;
    private Block mParent;
    private boolean mNull;
    private BlockLoad mBlockLoad;
    public abstract byte[] getBytes();
    public abstract int countBytes();
    public final int countUpTo(Block block){
        BlockCounter counter=new BlockCounter(block);
        onCountUpTo(counter);
        return counter.COUNT;
    }
    public abstract void onCountUpTo(BlockCounter counter);
    public final void readBytes(BlockReader reader) throws IOException{
        onReadBytes(reader);
        notifyBlockLoad(reader);
    }
    public final void setBlockLoad(BlockLoad blockLoad){
        mBlockLoad=blockLoad;
    }
    private void notifyBlockLoad(BlockReader reader) throws IOException{
        BlockLoad blockLoad=mBlockLoad;
        if(blockLoad!=null){
            blockLoad.onBlockLoaded(reader, this);
        }
    }
    public void onReadBytes(BlockReader reader) throws IOException{

    }
    public final int writeBytes(OutputStream stream) throws IOException{
        if(isNull()){
            return 0;
        }
        return onWriteBytes(stream);
    }
    protected abstract int onWriteBytes(OutputStream stream) throws IOException;
    public boolean isNull(){
        return mNull;
    }
    public void setNull(boolean is_null){
        mNull=is_null;
    }
    public final int getIndex(){
        return mIndex;
    }
    public final void setIndex(int index){
        mIndex=index;
    }
    public final void setParent(Block parent){
        if(parent==this){
            return;
        }
        mParent=parent;
    }
    public final Block getParent(){
        return mParent;
    }


    protected static byte[] addBytes(byte[] bts1, byte[] bts2){
        boolean empty1=(bts1==null || bts1.length==0);
        boolean empty2=(bts2==null || bts2.length==0);
        if(empty1 && empty2){
            return null;
        }
        if(empty1){
            return bts2;
        }
        if(empty2){
            return bts1;
        }
        int len=bts1.length+bts2.length;
        byte[] result=new byte[len];
        int start=bts1.length;
        System.arraycopy(bts1, 0, result, 0, start);
        System.arraycopy(bts2, 0, result, start, bts2.length);
        return result;
    }
}
