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
 package com.reandroid.arsc.chunk;

 import com.reandroid.arsc.array.TypeBlockArray;
 import com.reandroid.arsc.base.Block;
 import com.reandroid.arsc.container.SpecTypePair;
 import com.reandroid.arsc.header.SpecHeader;
 import com.reandroid.arsc.item.*;
 import com.reandroid.json.JSONConvert;
 import com.reandroid.json.JSONObject;

 import java.util.List;

 public class SpecBlock extends Chunk<SpecHeader> implements JSONConvert<JSONObject> {
     private final SpecFlagsArray specFlagsArray;
     public SpecBlock() {
         super(new SpecHeader(), 1);
         SpecHeader header = getHeaderBlock();
         this.specFlagsArray = new SpecFlagsArray(header.getEntryCount());
         addChild(specFlagsArray);
     }
     public SpecFlagsArray getSpecFlagsArray(){
         return specFlagsArray;
     }
     public List<Integer> listSpecFlags(){
         return specFlagsArray.toList();
     }
     public byte getTypeId(){
         return getHeaderBlock().getId().get();
     }
     public int getId(){
         return getHeaderBlock().getId().unsignedInt();
     }
     public void setId(int id){
         setTypeId((byte) (0xff & id));
     }
     public void setTypeId(byte id){
         getHeaderBlock().getId().set(id);
         getTypeBlockArray().setTypeId(id);
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
         jsonObject.put(TypeBlock.NAME_id, getId());
         return jsonObject;
     }

     @Override
     public void fromJson(JSONObject json) {
         setId(json.getInt(TypeBlock.NAME_id));
     }
 }
