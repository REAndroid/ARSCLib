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

import com.reandroid.arsc.array.LibraryInfoArray;
import com.reandroid.arsc.header.LibraryHeader;
import com.reandroid.arsc.value.LibraryInfo;

import java.util.Collection;

 public class LibraryBlock extends Chunk<LibraryHeader> {
    private final LibraryInfoArray mLibraryInfoArray;
    public LibraryBlock() {
        super(new LibraryHeader(),1);
        LibraryHeader header = getHeaderBlock();
        this.mLibraryInfoArray = new LibraryInfoArray(header.getCount());

        addChild(mLibraryInfoArray);
    }
    public LibraryInfoArray getLibraryInfoArray(){
        return mLibraryInfoArray;
    }
    public void addLibraryInfo(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        for(LibraryInfo info:libraryBlock.getLibraryInfoArray().listItems()){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        if(info==null){
            return;
        }
        getLibraryInfoArray().add(info);
        getHeaderBlock().getCount().set(mLibraryInfoArray.childesCount());
    }
    public Collection<LibraryInfo> listLibraryInfo(){
        return getLibraryInfoArray().listItems();
    }
    @Override
    public boolean isNull(){
        return mLibraryInfoArray.childesCount()==0;
    }
    public int getLibraryCount(){
        return mLibraryInfoArray.childesCount();
    }
    public void setLibraryCount(int count){
        getHeaderBlock().getCount().set(count);
        mLibraryInfoArray.setChildesCount(count);
    }
    @Override
    protected void onChunkRefreshed() {
        getHeaderBlock().getCount().set(mLibraryInfoArray.childesCount());
    }

    public void merge(LibraryBlock libraryBlock){
        if(libraryBlock==null||libraryBlock==this){
            return;
        }
        getLibraryInfoArray().merge(libraryBlock.getLibraryInfoArray());
    }
}
