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

 import com.reandroid.lib.arsc.array.TypeBlockArray;
 import com.reandroid.lib.arsc.base.Block;
 import com.reandroid.lib.arsc.container.SpecTypePair;
 import com.reandroid.lib.arsc.item.*;
 import com.reandroid.lib.json.JSONConvert;
 import com.reandroid.lib.json.JSONObject;

 import java.util.List;

 public class SpecBlock extends BaseChunk implements JSONConvert<JSONObject> {
     private final SpecFlagsArray specFlagsArray;
     private final ByteItem mTypeId;
     public SpecBlock() {
         super(ChunkType.SPEC, 1);
         this.mTypeId=new ByteItem();
         ByteItem res0 = new ByteItem();
         ShortItem res1 = new ShortItem();
         IntegerItem entryCount = new IntegerItem();
         this.specFlagsArray = new SpecFlagsArray(entryCount);
         addToHeader(mTypeId);
         addToHeader(res0);
         addToHeader(res1);
         addToHeader(entryCount);

         addChild(specFlagsArray);
     }
     public SpecFlagsArray getSpecFlagsArray(){
         return specFlagsArray;
     }
     public List<Integer> listSpecFlags(){
         return specFlagsArray.toList();
     }
     public byte getTypeId(){
         return mTypeId.get();
     }
     public int getTypeIdInt(){
         return (0xff & mTypeId.get());
     }
     public void setTypeId(int id){
         setTypeId((byte) (0xff & id));
     }
     public void setTypeId(byte id){
         mTypeId.set(id);
     }
     public TypeBlockArray getTypeBlockArray(){
         SpecTypePair specTypePair=getSpecTypePair();
         if(specTypePair!=null){
             return specTypePair.getTypeBlockArray();
         }
         return null;
     }
     SpecTypePair getSpecTypePair(){
         Block parent=getParent();
         while (parent!=null){
             if(parent instanceof SpecTypePair){
                 return (SpecTypePair)parent;
             }
             parent=parent.getParent();
         }
         return null;
     }
     public int getEntryCount() {
         return specFlagsArray.size();
     }
     public void setEntryCount(int count){
         specFlagsArray.setSize(count);
         specFlagsArray.refresh();
     }
     @Override
     protected void onChunkRefreshed() {
         specFlagsArray.refresh();
     }
     @Override
     public String toString(){
         StringBuilder builder=new StringBuilder();
         builder.append(super.toString());
         TypeBlockArray typeBlockArray=getTypeBlockArray();
         if(typeBlockArray!=null){
             builder.append(", typesCount=");
             builder.append(typeBlockArray.childesCount());
         }
         return builder.toString();
     }

     @Override
     public JSONObject toJson() {
         JSONObject jsonObject=new JSONObject();
         jsonObject.put(TypeBlock.NAME_id, getTypeIdInt());
         return jsonObject;
     }

     @Override
     public void fromJson(JSONObject json) {
         setTypeId(json.getInt(TypeBlock.NAME_id));
     }
 }
