package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockContainer;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class BlockList<T extends Block> extends Block {
    private final List<T> mItems;
    public BlockList(){
        super();
        mItems=new ArrayList<>();
    }
    public void remove(T item){
        mItems.remove(item);
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
            }
            if(item instanceof BlockList){
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
