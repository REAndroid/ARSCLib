 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.arsc.base;

import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class Block {
    private int mIndex=-1;
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
    public void notifyBlockLoad() throws IOException {
        notifyBlockLoad(null);
    }
    private void notifyBlockLoad(BlockReader reader) throws IOException{
        BlockLoad blockLoad=mBlockLoad;
        if(blockLoad!=null){
            blockLoad.onBlockLoaded(reader, this);
        }
    }
    protected void onReadBytes(BlockReader reader) throws IOException{
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
        int old=mIndex;
        if(index==old){
            return;
        }
        mIndex=index;
        if(old!=-1 && index!=-1){
            onIndexChanged(old, index);
        }
    }
    public void onIndexChanged(int oldIndex, int newIndex){

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
    public final <T> T getParent(Class<T> parentClass){
        Block parent = getParent();
        while (parent!=null){
            if(parent.getClass() == parentClass){
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
    public final <T> T getParentInstance(Class<T> parentClass){
        Block parent = getParent();
        while (parent!=null){
            if(parentClass.isInstance(parent)){
                return (T) parent;
            }
            parent = parent.getParent();
        }
        return null;
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
