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
 package com.reandroid.lib.arsc.array;

 import com.reandroid.lib.arsc.base.Block;
 import com.reandroid.lib.arsc.base.BlockArray;
 import com.reandroid.lib.arsc.io.BlockLoad;
 import com.reandroid.lib.arsc.io.BlockReader;
 import com.reandroid.lib.arsc.item.IntegerItem;
 import com.reandroid.lib.arsc.value.StagedAliasEntry;
 import com.reandroid.lib.json.JSONArray;
 import com.reandroid.lib.json.JSONConvert;

 import java.io.IOException;

 public class StagedAliasEntryArray extends BlockArray<StagedAliasEntry>
         implements BlockLoad, JSONConvert<JSONArray> {
     private final IntegerItem count;
     public StagedAliasEntryArray(IntegerItem count){
         super();
         this.count=count;
         this.count.setBlockLoad(this);
     }
     @Override
     public void addAll(StagedAliasEntry[] aliasEntries){
         super.addAll(aliasEntries);
         updateCount();
     }
     @Override
     public StagedAliasEntry[] newInstance(int len) {
         return new StagedAliasEntry[len];
     }
     @Override
     protected void onRefreshed() {
         updateCount();
     }
     @Override
     public StagedAliasEntry newInstance() {
         return new StagedAliasEntry();
     }
     @Override
     public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
         if(sender==this.count){
             setChildesCount(this.count.get());
         }
     }
     private void updateCount(){
         this.count.set(childesCount());
     }

     @Override
     public JSONArray toJson() {
         StagedAliasEntry[] childes=getChildes();
         if(childes==null||childes.length==0){
             return null;
         }
         JSONArray jsonArray=new JSONArray();
         for(int i=0;i<childes.length;i++){
             jsonArray.put(i, childes[i].toJson());
         }
         return jsonArray;
     }
     @Override
     public void fromJson(JSONArray json) {
         clearChildes();
         if(json==null){
             return;
         }
         int length = json.length();
         setChildesCount(length);
         for(int i=0;i<length;i++){
             get(i).fromJson(json.getJSONObject(i));
         }
     }
 }
