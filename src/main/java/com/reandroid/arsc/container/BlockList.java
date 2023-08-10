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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class BlockList<T extends Block> extends Block {
    private final List<T> mItems;
    public BlockList(){
        super();
        mItems=new ArrayList<>();
    }
    public Iterator<T> iterator(){
        if(size() == 0){
            return EmptyIterator.of();
        }
        return mItems.iterator();
    }
    public Iterator<T> iterator(Predicate<? super T> filter){
        return FilterIterator.of(this.iterator(), filter);
    }
    public void clearChildren(){
        ArrayList<T> childList = new ArrayList<>(getChildren());
        for(T child:childList){
            remove(child);
        }
    }
    public void sort(Comparator<T> comparator){
        mItems.sort(comparator);
        updateIndex();
    }
    public boolean remove(T item){
        if(item!=null){
            item.setParent(null);
            item.setIndex(-1);
        }
        return mItems.remove(item);
    }
    public void add(int index, T item){
        if(item == null){
            return;
        }
        item.setIndex(index);
        item.setParent(this);
        mItems.add(index, item);
        updateIndex();
    }
    private void updateIndex(){
        int index = 0;
        for(T item : mItems){
            item.setIndex(index);
            index++;
        }
    }
    public void add(T item){
        if(item==null){
            return;
        }
        item.setIndex(mItems.size());
        item.setParent(this);
        mItems.add(item);
    }
    public T get(int i){
        if(i>=mItems.size() || i<0){
            return null;
        }
        return mItems.get(i);
    }
    public int size(){
        return mItems.size();
    }
    public List<T> getChildren(){
        return mItems;
    }
    public final void refresh(){
        if(isNull()){
            return;
        }
        refreshChildren();
    }
    private void refreshChildren(){
        for(T item:getChildren()){
            if(item instanceof BlockContainer){
                BlockContainer<?> container=(BlockContainer<?>)item;
                container.refresh();
            }else if(item instanceof BlockList){
                BlockList<?> blockList=(BlockList<?>)item;
                blockList.refresh();
            }
        }
    }
    @Override
    public byte[] getBytes() {
        byte[] results=null;
        for(T item:mItems){
            if(item!=null){
                results=addBytes(results, item.getBytes());
            }
        }
        return results;
    }
    @Override
    public int countBytes() {
        int result=0;
        for(T item:mItems){
            result+=item.countBytes();
        }
        return result;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        for(T item:mItems){
            if(counter.FOUND){
                break;
            }
            item.onCountUpTo(counter);
        }
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result=0;
        for(T item:mItems){
            result+=item.writeBytes(stream);
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        for(T item:mItems){
            item.readBytes(reader);
        }
    }
}
