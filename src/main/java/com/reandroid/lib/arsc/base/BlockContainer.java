package com.reandroid.lib.arsc.base;

import com.reandroid.lib.arsc.container.BlockList;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BlockContainer<T extends Block> extends Block{
    public BlockContainer(){
        super();
    }

    protected final BlockContainer getTopContainer(){
        Block parent=this;
        BlockContainer result=this;
        while (parent!=null){
            if(parent instanceof BlockContainer){
                result=(BlockContainer)parent;
            }
            parent=parent.getParent();
        }
        return result;
    }
    protected void onPreRefreshRefresh(){

    }
    protected abstract void onRefreshed();
    public final void refresh(){
        if(isNull()){
            return;
        }
        onPreRefreshRefresh();
        refreshChildes();
        onRefreshed();
    }
    protected void refreshChildes(){
        T[] childes=getChildes();
        if(childes!=null){
            int max=childes.length;
            for(int i=0;i<max;i++){
                T item=childes[i];
                if(item instanceof BlockContainer){
                    BlockContainer<?> container=(BlockContainer<?>)item;
                    container.refresh();
                }else if(item instanceof BlockList){
                    BlockList<?> blockList=(BlockList<?>)item;
                    blockList.refresh();
                }
            }
        }
    }
    @Override
    public void onCountUpTo(BlockCounter counter){
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        T[] childes=getChildes();
        if(childes==null){
            return;
        }
        int max=childes.length;
        for(int i=0;i<max;i++){
            if(counter.FOUND){
                return;
            }
            T item=childes[i];
            if(item!=null){
                item.onCountUpTo(counter);
            }
        }
    }
    @Override
    public int countBytes(){
        if(isNull()){
            return 0;
        }
        T[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                result += item.countBytes();
            }
        }
        return result;
    }
    @Override
    public byte[] getBytes(){
        if(isNull()){
            return null;
        }
        T[] childes=getChildes();
        if(childes==null){
            return null;
        }
        byte[] results=null;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                results = addBytes(results, item.getBytes());
            }
        }
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        T[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                result+=item.writeBytes(stream);
            }
        }
        return result;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        T[] childes=getChildes();
        if(childes==null){
            return;
        }
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                item.readBytes(reader);
            }
        }
    }

    public abstract int childesCount();
    public abstract T[] getChildes();
}
