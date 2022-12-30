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
package com.reandroid.lib.arsc.chunk;

 import com.reandroid.lib.arsc.header.HeaderBlock;
 import com.reandroid.lib.arsc.item.ByteArray;

 import java.io.IOException;

 /**
 * This class can load any valid chunk, aimed to
 * handle any future android changes
 * */
public class UnknownChunk extends BaseChunk implements HeaderBlock.HeaderLoaded {
     private final ByteArray headerExtra;
     private final ByteArray body;
     public UnknownChunk() {
         super(INITIAL_CHUNK_TYPE, 1);
         this.headerExtra = new ByteArray();
         this.body = new ByteArray();

         addToHeader(this.headerExtra);
         addChild(body);

         setHeaderLoaded(this);
     }
     @Override
     public void onChunkTypeLoaded(short type) {
     }
     @Override
     public void onHeaderSizeLoaded(int headerSize) {
         int extraSize = headerSize - 8;
         this.headerExtra.setSize(extraSize);
     }
     @Override
     public void onChunkSizeLoaded(int headerSize, int chunkSize) {
         int bodySize = chunkSize - headerSize;
         this.body.setSize(bodySize);
     }

     @Override
     void checkInvalidChunk(HeaderBlock headerBlock) throws IOException {
     }
     @Override
     protected void onChunkRefreshed() {
     }
     @Override
     public String toString(){
         HeaderBlock headerBlock = getHeaderBlock();
         return getClass().getSimpleName()
                 +"{ type="+String.format("0x%04x", headerBlock.getType())
                 +", chunkSize="+headerBlock.getChunkSize()
                 +", headerExtra="+headerExtra.size()
                 +", body="+body.size()+"}";
     }

     // This value must not exist is ChunkType enum list
     private static final short INITIAL_CHUNK_TYPE = 0x0207;

 }
