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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockList<T extends Block> extends Block {
    private final List<T> mItems;
    public BlockList(){
        super();
        mItems=new ArrayList<>();
    }
    public void clearChildes(){
        ArrayList<T> childList = new ArrayList<>(getChildes());
        for(T child:childList){
            remove(child);
        }
    }
    public void sort(Comparator<T> comparator){
        mItems.sort(comparator);
    }
    public boolean remove(T item){
        if(item!=null){
            item.setParent(null);
            item.setIndex(-1);
        }
        return mItems.remove(item);
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
    public List<T> getChildes(){
        return mItems;
    }
    public final void refresh(){
        if(isNull()){
            return;
        }
        refreshChildes();
    }
    private void refreshChildes(){
        for(T item:getChildes()){
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
