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
package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BlockItem extends Block {

    private byte[] mBytes;
    public BlockItem(int bytesLength){
        super();
        mBytes=new byte[bytesLength];
    }
    public abstract void onBytesChanged();
    protected byte[] getBytesInternal() {
        return mBytes;
    }
    void setBytesInternal(byte[] bts){
        if(bts==null){
            bts=new byte[0];
        }
        if(bts==mBytes){
            return;
        }
        mBytes=bts;
        onBytesChanged();
    }
    final void ensureMinLength(int minLen){
        int len=mBytes.length;
        if(minLen<=len){
            return;
        }
        byte[] bts=new byte[minLen];
        System.arraycopy(bts, 0, mBytes, 0, mBytes.length);
        mBytes=bts;
    }
    final void setBytesLength(int length){
        setBytesLength(length, true);
    }
    final void setBytesLength(int length, boolean notify){
        if(length<0){
            length=0;
        }
        int old=mBytes.length;
        if(length==old){
            return;
        }
        byte[] bts=new byte[length];
        if(length<old){
            old=length;
        }
        System.arraycopy(mBytes, 0, bts, 0, old);
        mBytes=bts;
        if(notify){
            onBytesChanged();
        }
    }
    int getBytesLength(){
        return mBytes.length;
    }

    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return getBytesInternal().length;
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return getBytesInternal();
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
        counter.addCount(countBytes());
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        byte[] bts=getBytesInternal();
        reader.readFully(bts);
        onBytesChanged();
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        stream.write(getBytesInternal());
        return getBytesLength();
    }
}
