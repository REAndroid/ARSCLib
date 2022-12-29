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

 import com.reandroid.lib.arsc.base.Block;
 import com.reandroid.lib.arsc.header.HeaderBlock;
 import com.reandroid.lib.arsc.io.BlockLoad;
 import com.reandroid.lib.arsc.io.BlockReader;
 import com.reandroid.lib.arsc.item.ByteArray;
 import com.reandroid.lib.arsc.item.FixedLengthString;

 import java.io.IOException;

 /**
  * Replica of struct "ResTable_overlayable_header" as on AOSP androidfw/ResourceTypes.h
  * We didn't test this class with resource table, if someone found a resource/apk please
  * create issue on https://github.com/REAndroid/ARSCLib
  * */
 public class Overlayable extends BaseChunk implements BlockLoad {
     private final FixedLengthString name;
     private final FixedLengthString actor;
     /**
      * @link body
      * As on AOSP there is only a description of header struct but no mention about
      * chunk-content/body, thus we will use empty body byte array to avoid parse error
      * */
     private final ByteArray body;
     public Overlayable() {
         super(ChunkType.OVERLAYABLE, 1);
         this.name = new FixedLengthString(512);
         this.actor = new FixedLengthString(512);
         this.body = new ByteArray();
         addToHeader(this.name);
         addToHeader(this.actor);
         addChild(this.body);
         this.actor.setBlockLoad(this);
     }
     public ByteArray getBody() {
         return body;
     }
     public String getName(){
         return this.name.get();
     }
     public void setName(String str){
         this.name.set(str);
     }
     public String getActor(){
         return this.actor.get();
     }
     public void setActor(String str){
         this.actor.set(str);
     }
     @Override
     public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
         if(sender==this.actor){
             HeaderBlock header = getHeaderBlock();
             int bodySize=header.getChunkSize()-header.getHeaderSize();
             this.body.setSize(bodySize);
         }
     }
     @Override
     protected void onChunkRefreshed() {
     }
     @Override
     public String toString(){
         return getClass().getSimpleName()+
                 ": name='"+getName()
                 +"', actor='"+getActor()
                 +"', body-size="+getBody().size();
     }
 }
