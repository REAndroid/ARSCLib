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

 import com.reandroid.lib.arsc.array.StagedAliasEntryArray;
 import com.reandroid.lib.arsc.header.StagedAliasHeader;
 import com.reandroid.lib.arsc.item.IntegerItem;
 import com.reandroid.lib.arsc.value.StagedAliasEntry;

 import java.util.Collection;

 public class StagedAlias extends BaseChunk<StagedAliasHeader>{
     private final StagedAliasEntryArray stagedAliasEntryArray;
     public StagedAlias() {
         super(new StagedAliasHeader(), 1);
         StagedAliasHeader header = getHeaderBlock();

         stagedAliasEntryArray = new StagedAliasEntryArray(header.getCount());
         addChild(stagedAliasEntryArray);
     }
     public void merge(StagedAlias stagedAlias){
         if(stagedAlias==null||stagedAlias==this){
             return;
         }
         stagedAliasEntryArray.addAll(stagedAlias
                 .getStagedAliasEntryArray().getChildes());
     }
     public StagedAliasEntryArray getStagedAliasEntryArray() {
         return stagedAliasEntryArray;
     }
     public Collection<StagedAliasEntry> listStagedAliasEntry(){
         return getStagedAliasEntryArray().listItems();
     }
     public int getStagedAliasEntryCount(){
         return getStagedAliasEntryArray().childesCount();
     }
     @Override
     public boolean isNull(){
         return getStagedAliasEntryCount()==0;
     }
     @Override
     protected void onChunkRefreshed() {
     }
     @Override
     public String toString(){
         return getClass().getSimpleName()+
                 ": count="+getStagedAliasEntryCount();
     }
     public static StagedAlias mergeAll(Collection<StagedAlias> stagedAliasList){
         if(stagedAliasList.size()==0){
             return null;
         }
         StagedAlias result=new StagedAlias();
         for(StagedAlias stagedAlias:stagedAliasList){
             if(stagedAlias.isNull()){
                 continue;
             }
             result.merge(stagedAlias);
         }
         if(!result.isNull()){
             result.refresh();
             return result;
         }
         return null;
     }
 }
