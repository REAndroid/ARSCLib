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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BlockItem extends Block {

    private byte[] mBytes;
    public BlockItem(int bytesLength){
        super();
        mBytes=new byte[bytesLength];
    }
    protected void onBytesChanged(){
    }
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
    final void setBytesLength(int length){
        setBytesLength(length, true);
    }
    protected final void setBytesLength(int length, boolean notify){
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
        reader.readFully(getBytesInternal());
        onBytesChanged();
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        stream.write(getBytesInternal());
        return getBytesLength();
    }
    public int readBytes(InputStream inputStream) throws IOException {
        byte[] bts=getBytesInternal();
        if(bts==null || bts.length==0){
            return 0;
        }
        int length=bts.length;
        int offset=0;
        int read=length;
        while (length>0 && read>0){
            read = inputStream.read(bts, offset, length);
            length-=read;
            offset+=read;
        }
        onBytesChanged();
        super.notifyBlockLoad();
        return bts.length;
    }

    protected static int getInteger(byte[] bts, int offset){
        if((offset+4)>bts.length){
            return 0;
        }
        return bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 |
                (bts[offset+2] & 0xff) << 16 |
                (bts[offset+3] & 0xff) << 24;
    }
    protected static short getShort(byte[] bts, int offset){
        return (short) (bts[offset] & 0xff | (bts[offset+1] & 0xff) << 8);
    }
    protected static void putInteger(byte[] bts, int offset, int val){
        if((offset+4)>bts.length){
            return;
        }
        bts[offset+3]= (byte) (val >>> 24 & 0xff);
        bts[offset+2]= (byte) (val >>> 16 & 0xff);
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
    }
    protected static void putShort(byte[] bts, int offset, short val){
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
    }
    protected static boolean getBit(byte[] bts, int byteOffset, int bitIndex){
        return (((bts[byteOffset] & 0xff) >>bitIndex) & 0x1) == 1;
    }
    protected static void putBit(byte[] bytes, int byteOffset, int bitIndex, boolean bit){
        int mask = 1 << bitIndex;
        int add = bit ? mask : 0;
        mask = (~mask) & 0xff;
        int value = (bytes[byteOffset] & mask) | add;
        bytes[byteOffset] = (byte) value;
    }
    protected static long getLong(byte[] bytes, int offset){
        if((offset + 8)>bytes.length){
            return 0;
        }
        long result = 0;
        int index = offset + 7;
        while (index>=offset){
            result = result << 8;
            result |= (bytes[index] & 0xff);
            index --;
        }
        return result;
    }
    protected static void putLong(byte[] bytes, int offset, long value){
        if((offset + 8) > bytes.length){
            return;
        }
        int index = offset;
        offset = index + 8;
        while (index<offset){
            bytes[index] = (byte) (value & 0xff);
            value = value >>> 8;
            index++;
        }
    }
}
