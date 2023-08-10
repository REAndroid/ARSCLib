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
 package com.reandroid.arsc.chunk.xml;

 import com.reandroid.arsc.chunk.ChunkType;
 import com.reandroid.arsc.array.ResXmlIDArray;
 import com.reandroid.arsc.chunk.Chunk;
 import com.reandroid.arsc.header.HeaderBlock;
 import com.reandroid.arsc.item.ResXmlID;
 import com.reandroid.arsc.item.ResXmlString;
 import com.reandroid.arsc.pool.ResXmlStringPool;

 import java.util.Collection;

 public class ResXmlIDMap extends Chunk<HeaderBlock> {
     private final ResXmlIDArray mResXmlIDArray;
     public ResXmlIDMap() {
         super(new HeaderBlock(ChunkType.XML_RESOURCE_MAP), 1);
         this.mResXmlIDArray=new ResXmlIDArray(getHeaderBlock());
         addChild(mResXmlIDArray);
     }
     void removeSafely(ResXmlID resXmlID){
         if(resXmlID==null
                 || resXmlID.getParent()==null
                 || resXmlID.getIndex()<0
                 || resXmlID.hasReference()){
             return;
         }
         ResXmlString xmlString = resXmlID.getResXmlString();
         if(xmlString == null
                 || xmlString.getParent()==null
                 || xmlString.getIndex()<0
                 || xmlString.hasReference()){
             return;
         }
         ResXmlStringPool stringPool = getXmlStringPool();
         if(stringPool == null){
             return;
         }
         resXmlID.set(0);
         ResXmlIDArray idArray = getResXmlIDArray();
         idArray.remove(resXmlID);
         stringPool.removeString(xmlString);
     }
     public int countId(){
         return getResXmlIDArray().getChildrenCount();
     }
     public void destroy(){
         getResXmlIDArray().clearChildren();
     }
     public ResXmlIDArray getResXmlIDArray(){
         return mResXmlIDArray;
     }

     public Collection<ResXmlID> listResXmlID(){
         return getResXmlIDArray().listItems();
     }
     public void addResourceId(int index, int resId){
         getResXmlIDArray().addResourceId(index, resId);
     }
     public ResXmlID getResXmlID(int ref){
         return getResXmlIDArray().get(ref);
     }
     public ResXmlID getOrCreate(int resId){
         return getResXmlIDArray().getOrCreate(resId);
     }
     public ResXmlID getByResId(int resId){
         return getResXmlIDArray().getByResId(resId);
     }
     @Override
     protected void onChunkRefreshed() {

     }
     ResXmlStringPool getXmlStringPool(){
         ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
         if(resXmlDocument!=null){
             return resXmlDocument.getStringPool();
         }
         return null;
     }
 }
