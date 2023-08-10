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
 package com.reandroid.arsc.array;

 import com.reandroid.arsc.base.Block;
 import com.reandroid.arsc.base.BlockArray;
 import com.reandroid.arsc.io.BlockLoad;
 import com.reandroid.arsc.io.BlockReader;
 import com.reandroid.arsc.item.IntegerItem;
 import com.reandroid.arsc.value.StagedAliasEntry;
 import com.reandroid.json.JSONArray;
 import com.reandroid.json.JSONConvert;

 import java.io.IOException;

 public class StagedAliasEntryArray extends BlockArray<StagedAliasEntry>
         implements BlockLoad, JSONConvert<JSONArray> {
     private final IntegerItem count;
     public StagedAliasEntryArray(IntegerItem count){
         super();
         this.count=count;
         this.count.setBlockLoad(this);
     }
     public boolean contains(StagedAliasEntry aliasEntry){
         StagedAliasEntry[] children=getChildren();
         if(children==null){
             return false;
         }
         for(int i=0;i<children.length;i++){
             StagedAliasEntry entry=children[i];
             if(entry.isEqual(aliasEntry)){
                 return true;
             }
         }
         return false;
     }
     public StagedAliasEntry searchByStagedResId(int stagedResId){
         StagedAliasEntry[] children=getChildren();
         if(children==null){
             return null;
         }
         for(int i=0;i<children.length;i++){
             StagedAliasEntry entry=children[i];
             if(stagedResId==entry.getStagedResId()){
                 return entry;
             }
         }
         return null;
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
             setChildrenCount(this.count.get());
         }
     }
     private void updateCount(){
         this.count.set(getChildrenCount());
     }

     @Override
     public JSONArray toJson() {
         StagedAliasEntry[] children=getChildren();
         if(children==null||children.length==0){
             return null;
         }
         JSONArray jsonArray=new JSONArray();
         for(int i=0;i<children.length;i++){
             jsonArray.put(i, children[i].toJson());
         }
         return jsonArray;
     }
     @Override
     public void fromJson(JSONArray json) {
         clearChildren();
         if(json==null){
             return;
         }
         int length = json.length();
         setChildrenCount(length);
         for(int i=0;i<length;i++){
             get(i).fromJson(json.getJSONObject(i));
         }
     }
 }
