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
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.EmptyList;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class BlockList<T extends Block> extends Block implements BlockRefresh {
    private List<T> mItems;
    public BlockList(){
        super();
        mItems = EmptyList.of();
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
    public void clearChildes(){
        if(mItems.isEmpty()){
            return;
        }
        ArrayList<T> childList = new ArrayList<>(getChildes());
        for(T child:childList){
            remove(child);
        }
        mItems = EmptyList.of();
    }
    public void sort(Comparator<T> comparator){
        mItems.sort(comparator);
        updateIndex();
    }
    public boolean remove(T item){
        if(item != null){
            item.setParent(null);
            item.setIndex(-1);
        }
        return mItems.remove(item);
    }
    public void add(int index, T item){
        if(item == null){
            return;
        }
        unlockList();
        item.setIndex(index);
        item.setParent(this);
        mItems.add(index, item);
        updateIndex();
    }
    private void updateIndex(){
        int index = 0;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            iterator.next().setIndex(index);
            index++;
        }
    }
    public boolean add(T item){
        if(item == null){
            return false;
        }
        unlockList();
        item.setIndex(mItems.size());
        item.setParent(this);
        return mItems.add(item);
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
    public boolean contains(Object obj){
        return mItems.contains(obj);
    }
    public Object[] toArray(){
        return mItems.toArray();
    }
    public <T1> T1[] toArray(T1[] ts) {
        return mItems.toArray(ts);
    }

    public List<T> getChildes(){
        return mItems;
    }
    private void unlockList(){
        if(mItems.isEmpty()){
            mItems = new ArrayList<>();
        }
    }
    @Override
    public final void refresh(){
        if(isNull()){
            return;
        }
        onPreRefresh();
        refreshChildes();
        onRefreshed();
    }
    protected void onPreRefresh(){
    }
    protected void onRefreshed(){
    }
    private void refreshChildes(){
        Iterator<?> iterator = iterator();
        while (iterator.hasNext()){
            Object item = iterator.next();
            if(item instanceof BlockRefresh){
                ((BlockRefresh) item).refresh();
            }
        }
    }
    @Override
    public byte[] getBytes() {
        byte[] results = null;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            results = addBytes(results, iterator.next().getBytes());
        }
        return results;
    }
    @Override
    public int countBytes() {
        int result = 0;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            result += iterator.next().countBytes();
        }
        return result;
    }

    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        counter.setCurrent(this);
        if(counter.END == this){
            counter.FOUND = true;
            return;
        }
        Iterator<T> iterator = iterator();
        while (!counter.FOUND && iterator.hasNext()){
            iterator.next().onCountUpTo(counter);
        }
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result = 0;
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            result += iterator.next().writeBytes(stream);
        }
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        Iterator<T> iterator = iterator();
        while (iterator.hasNext()){
            iterator.next().readBytes(reader);
        }
    }

    @Override
    public String toString() {
        return "size=" + size();
    }
}
