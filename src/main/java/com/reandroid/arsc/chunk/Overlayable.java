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

 import com.reandroid.arsc.container.BlockList;
 import com.reandroid.arsc.header.HeaderBlock;
 import com.reandroid.arsc.header.OverlayableHeader;
 import com.reandroid.arsc.io.BlockReader;
 import com.reandroid.arsc.item.ByteArray;
 import com.reandroid.json.JSONArray;
 import com.reandroid.json.JSONConvert;
 import com.reandroid.json.JSONObject;

 import java.io.IOException;
 import java.util.List;

 /**
  * Replica of struct "ResTable_overlayable_header" as on AOSP androidfw/ResourceTypes.h
  * We didn't test this class with resource table, if someone found a resource/apk please
  * create issue on https://github.com/REAndroid/ARSCLib
  * */
 public class Overlayable extends Chunk<OverlayableHeader> implements JSONConvert<JSONObject> {
     private final BlockList<OverlayablePolicy> policyList;
     private final ByteArray extraBytes;

     public Overlayable() {
         super(new OverlayableHeader(), 2);
         this.policyList = new BlockList<>();
         this.extraBytes = new ByteArray();
         addChild(this.policyList);
         addChild(this.extraBytes);
     }

     public OverlayablePolicy get(int flags){
         for(OverlayablePolicy policy:listOverlayablePolicies()){
             if(flags==policy.getFlags()){
                 return policy;
             }
         }
         return null;
     }
     public void addOverlayablePolicy(OverlayablePolicy overlayablePolicy){
         this.policyList.add(overlayablePolicy);
     }
     public List<OverlayablePolicy> listOverlayablePolicies() {
         return policyList.getChildes();
     }
     public ByteArray getExtraBytes() {
         return extraBytes;
     }

     public String getName(){
         return getHeaderBlock().getName().get();
     }
     public void setName(String str){
         getHeaderBlock().getName().set(str);
     }
     public String getActor(){
         return getHeaderBlock().getActor().get();
     }
     public void setActor(String str){
         getHeaderBlock().getActor().set(str);
     }

     @Override
     protected void onChunkRefreshed() {
     }

     @Override
     public void onReadBytes(BlockReader reader) throws IOException {
         HeaderBlock headerBlock = reader.readHeaderBlock();
         checkInvalidChunk(headerBlock);

         int size = headerBlock.getChunkSize();
         BlockReader chunkReader = reader.create(size);
         headerBlock = getHeaderBlock();
         headerBlock.readBytes(chunkReader);

         readOverlayablePlolicies(chunkReader);
         readExtraBytes(chunkReader);

         reader.offset(size);
         chunkReader.close();
         onChunkLoaded();
     }
     private void readOverlayablePlolicies(BlockReader reader) throws IOException {
         HeaderBlock headerBlock = reader.readHeaderBlock();
         BlockList<OverlayablePolicy> policyList = this.policyList;
         while (headerBlock!=null && headerBlock.getChunkType()==ChunkType.OVERLAYABLE_POLICY){
             OverlayablePolicy policy = new OverlayablePolicy();
             policyList.add(policy);
             policy.readBytes(reader);
             headerBlock = reader.readHeaderBlock();
         }
     }
     private void readExtraBytes(BlockReader reader) throws IOException {
         int remaining = reader.available();
         this.extraBytes.setSize(remaining);
         this.extraBytes.readBytes(reader);
     }

     @Override
     public JSONObject toJson() {
         JSONObject jsonObject = new JSONObject();
         jsonObject.put(NAME_name, getName());
         jsonObject.put(NAME_actor, getActor());
         JSONArray jsonArray = new JSONArray();
         for(OverlayablePolicy policy:listOverlayablePolicies()){
             jsonArray.put(policy.toJson());
         }
         jsonObject.put(NAME_policies, jsonArray);
         return jsonObject;
     }
     @Override
     public void fromJson(JSONObject json) {
         setName(json.optString(NAME_name));
         setActor(json.optString(NAME_actor));
         JSONArray jsonArray = json.getJSONArray(NAME_policies);
         int length = jsonArray.length();
         BlockList<OverlayablePolicy> policyList = this.policyList;
         for(int i=0;i<length;i++){
             OverlayablePolicy policy = new OverlayablePolicy();
             policyList.add(policy);
             policy.fromJson(jsonArray.getJSONObject(i));
         }
     }
     public void merge(Overlayable overlayable){
         if(overlayable==null||overlayable==this){
             return;
         }
         setName(overlayable.getName());
         setActor(overlayable.getActor());
         for(OverlayablePolicy policy:overlayable.listOverlayablePolicies()){
             OverlayablePolicy exist = get(policy.getFlags());
             if(exist==null){
                 exist = new OverlayablePolicy();
                 addOverlayablePolicy(exist);
             }
             exist.merge(policy);
         }
     }

     @Override
     public String toString(){
         return getClass().getSimpleName()+
                 ": name='" + getName()
                 +"', actor='" + getActor()
                 +"', policies=" + policyList.size()
                 +"', extra=" + getExtraBytes().size();
     }

     public static final String NAME_name = "name";
     public static final String NAME_actor = "actor";
     public static final String NAME_policies = "policies";

 }
