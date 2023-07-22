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
package com.reandroid.archive.io;

import java.io.*;

public class BytesOutputStream extends OutputStream {
    private final Object mLock = new Object();
    private byte[] array = new byte[0];
    private int position = 0;
    private int size = 0;
    public int arrayGrowCount;
    public BytesOutputStream(){
    }

    public int position() {
        return position;
    }
    public void position(int pos) {
        synchronized (mLock){
            if(pos < 0){
                pos = 0;
            }
            int length = pos - size;
            ensureCapacity(length);
            this.position = pos;
            if(this.position > size){
                size = position;
            }
        }
    }

    public void write(InputStream inputStream) throws IOException{
        if(inputStream instanceof BytesInputStream){
            write((BytesInputStream) inputStream);
            return;
        }
        int bufferStep = 500;
        int maxBuffer = 4096 * 20;
        int length;
        byte[] buffer = new byte[2048];
        while ((length = inputStream.read(buffer, 0, buffer.length)) >= 0){
            write(buffer, 0, length);
            if(buffer.length < maxBuffer){
                buffer = new byte[buffer.length + bufferStep];
            }
        }
        inputStream.close();
    }
    public void write(BytesInputStream bis) throws IOException {
        synchronized (mLock){
            if(position == 0 && size == 0){
                this.array = bis.toByteArray();
                this.position = array.length;
                this.size = position;
                return;
            }
            byte[] bytes = bis.toByteArray();
            write(bytes, 0, bytes.length);
        }
    }
    @Override
    public void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }
    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        synchronized (mLock){
            if(length == 0){
                return;
            }
            ensureCapacity(length);
            int pos = this.position;
            position += length;
            if(position > size){
                size = position;
            }
            System.arraycopy(bytes, offset, array, pos, length);
        }
    }
    @Override
    public void write(int i) throws IOException {
        byte b = (byte) (i & 0xff);
        write(new byte[]{b});
    }
    @Override
    public void flush() throws IOException {
    }
    @Override
    public void close() throws IOException {
        trim();
    }
    public byte[] getArray(){
        trim();
        return array;
    }
    private int capacity(){
        return array.length - position;
    }
    private void ensureCapacity(int length){
        if(length <= capacity()){
            return;
        }
        grow(getGrowAmount(length));
    }
    private int getGrowAmount(int length){
        int add = length / 3 + array.length / 20;
        add = add + add * arrayGrowCount;
        if(add < 5000){
            add = 5000;
        }else if(add > MAX_GROW){
            add = MAX_GROW;
        }
        return length + add;
    }
    private void grow(int amount){
        if(amount <= 0){
            return;
        }
        int length = array.length + amount;
        byte[] bytes = new byte[length];
        System.arraycopy(array, 0, bytes, 0, array.length);
        array = bytes;
        arrayGrowCount++;
    }
    private void trim(){
        if(size == array.length){
            return;
        }
        byte[] bytes = new byte[size];
        System.arraycopy(array, 0, bytes, 0, bytes.length);
        array = bytes;
    }
    @Override
    public String toString(){
        return "pos = " + position
                + ", size = " + size
                + ", array = " + array.length;
    }
    private static final int MAX_GROW = 4 * 1024 * 1000;
}
