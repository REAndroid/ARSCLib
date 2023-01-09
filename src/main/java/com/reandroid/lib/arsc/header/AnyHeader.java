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
package com.reandroid.lib.arsc.header;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.item.BlockItem;
import com.reandroid.lib.arsc.item.ByteArray;

import java.io.IOException;
import java.io.InputStream;

public class AnyHeader extends HeaderBlock{
    private final ByteArray extraBytes;
    public AnyHeader() {
        super(ChunkType.NULL.ID);
        this.extraBytes = new ByteArray();
        super.addChild(extraBytes);
    }
    public ByteArray getExtraBytes() {
        return extraBytes;
    }
    @Override
    void onHeaderSizeLoaded(int size){
        int max = 0x0000ffff;
        if(size > max){
            size=max;
        }else if(size<0){
            size=0;
        }
        extraBytes.setSize(size-8);
        super.onHeaderSizeLoaded(size);
    }
    public int readBytes(InputStream inputStream) throws IOException {
        int result=0;
        Block[] childes = getChildes();
        for(Block child:childes){
            if(child instanceof BlockItem){
                BlockItem blockItem=(BlockItem) child;
                result += blockItem.readBytes(inputStream);
            }
        }
        return result;
    }
    public byte[] readChunkBytes(InputStream inputStream) throws IOException{
        int chunkSize = getChunkSize();
        int headerSize = getHeaderSize();
        if(chunkSize < 0 || chunkSize < headerSize){
            throw new IOException("Invalid chunk size: " + super.toString());
        }
        byte[] buffer = new byte[chunkSize];
        int length = chunkSize - headerSize;
        int offset = loadHeaderBytes(buffer);
        int readLength = inputStream.read(buffer, offset, length);
        if(readLength < length){
            throw new IOException("Read length is less than expected: length="
                    +chunkSize+", read="+readLength);
        }
        return buffer;
    }
    private int loadHeaderBytes(byte[] buffer){
        int index=0;
        Block[] childes = getChildes();
        for(Block child:childes){
            byte[] childBytes=child.getBytes();
            for(int i=0;i<childBytes.length;i++){
                buffer[index]=childBytes[i];
                index++;
            }
        }
        return index;
    }
}
