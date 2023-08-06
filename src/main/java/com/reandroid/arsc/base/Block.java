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
        return counter.getCount();
    }
    public final Block locateBlock(int bytePosition){
        BlockLocator locator = new BlockLocator(bytePosition);
        onCountUpTo(locator);
        return locator.getResult();
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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


    public static int getInteger(byte[] bytes, int offset){
        if((offset + 4) > bytes.length){
            return 0;
        }
        return bytes[offset] & 0xff |
                (bytes[offset + 1] & 0xff) << 8 |
                (bytes[offset + 2] & 0xff) << 16 |
                (bytes[offset + 3] & 0xff) << 24;
    }
    public static short getShort(byte[] bytes, int offset){
        if((offset + 2) > bytes.length){
            return 0;
        }
        return (short) (bytes[offset] & 0xff
                | (bytes[offset + 1] & 0xff) << 8);
    }
    public static void putInteger(byte[] bytes, int offset, int val){
        if((offset + 4) > bytes.length){
            return;
        }
        bytes[offset + 3]= (byte) (val >>> 24 & 0xff);
        bytes[offset + 2]= (byte) (val >>> 16 & 0xff);
        bytes[offset + 1]= (byte) (val >>> 8 & 0xff);
        bytes[offset]= (byte) (val & 0xff);
    }
    public static void putShort(byte[] bytes, int offset, short val){
        bytes[offset + 1]= (byte) (val >>> 8 & 0xff);
        bytes[offset]= (byte) (val & 0xff);
    }
    public static boolean getBit(byte[] bytes, int byteOffset, int bitIndex){
        return (((bytes[byteOffset] & 0xff) >>bitIndex) & 0x1) == 1;
    }
    public static void putBit(byte[] bytes, int byteOffset, int bitIndex, boolean bit){
        int mask = 1 << bitIndex;
        int add = bit ? mask : 0;
        mask = (~mask) & 0xff;
        int value = (bytes[byteOffset] & mask) | add;
        bytes[byteOffset] = (byte) value;
    }
    public static long getLong(byte[] bytes, int offset){
        if((offset + 8) > bytes.length){
            return 0;
        }
        long result = 0;
        int index = offset + 7;
        while (index >= offset){
            result = result << 8;
            result |= (bytes[index] & 0xff);
            index --;
        }
        return result;
    }
    public static void putLong(byte[] bytes, int offset, long value){
        if((offset + 8) > bytes.length){
            return;
        }
        int index = offset;
        offset = index + 8;
        while (index < offset){
            bytes[index] = (byte) (value & 0xff);
            value = value >>> 8;
            index++;
        }
    }
    public static byte[] getBytes(byte[] bytes, int offset, int length){
        if(bytes.length == 0){
            return new byte[0];
        }
        int available = bytes.length - offset;
        if(available < 0){
            available = 0;
        }
        if(length > available){
            length = available;
        }
        byte[] result = new byte[length];
        System.arraycopy(bytes, offset, result, 0, length);
        return result;
    }
    protected static byte[] addBytes(byte[] bytes1, byte[] bytes2){
        boolean empty1 = (bytes1 == null || bytes1.length == 0);
        boolean empty2 = (bytes2 == null || bytes2.length == 0);
        if(empty1 && empty2){
            return null;
        }
        if(empty1){
            return bytes2;
        }
        if(empty2){
            return bytes1;
        }
        int length = bytes1.length + bytes2.length;
        byte[] result = new byte[length];
        int start = bytes1.length;
        System.arraycopy(bytes1, 0, result, 0, start);
        System.arraycopy(bytes2, 0, result, start, bytes2.length);
        return result;
    }
}
