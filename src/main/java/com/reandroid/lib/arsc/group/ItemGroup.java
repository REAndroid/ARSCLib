package com.reandroid.lib.arsc.group;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArrayCreator;

import java.util.AbstractList;
import java.util.List;

public class ItemGroup<T extends Block> {
    private final BlockArrayCreator<T> mBlockArrayCreator;
    private final String name;
    private T[] items;
    private final int hashCode;
    public ItemGroup(BlockArrayCreator<T> blockArrayCreator, String name){
        this.mBlockArrayCreator=blockArrayCreator;
        this.name=name;
        this.items=blockArrayCreator.newInstance(0);
        this.hashCode=(getClass().getName()+"-"+name).hashCode();
    }
    public List<T> listItems(){
        return new AbstractList<T>() {
            @Override
            public T get(int i) {
                return ItemGroup.this.get(i);
            }

            @Override
            public int size() {
                return ItemGroup.this.size();
            }
        };
    }
    public T get(int i){
        if(i<0||i>= size()){
            return null;
        }
        return items[i];
    }
    public int size(){
        if(items==null){
            return 0;
        }
        return items.length;
    }
    public boolean contains(T block){
        if(block==null){
            return false;
        }
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                return true;
            }
        }
        return false;
    }
    public void remove(T block){
        if(block==null){
            return;
        }
        boolean found=false;
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                items[i]=null;
                found=true;
            }
        }
        if(found){
            trimToSize();
        }
    }
    public void add(T block){
        if(block==null){
            return;
        }
        int index=items.length;
        T[] update=createNew(index+1);
        System.arraycopy(items, 0, update, 0, index);
        update[index]=block;
        items=update;
    }
    public T[] getItems(){
        return items;
    }
    private void trimToSize(){
        int count=countNonNull();
        int len=items.length;
        if(count==len){
            return;
        }
        T[] update=createNew(count);
        int index=0;
        for(int i=0;i<len;i++){
            T block=items[i];
            if(block!=null){
                update[index]=block;
                index++;
            }
        }
        items=update;
    }
    private int countNonNull(){
        int result=0;
        for(T t:items){
            if(t!=null){
                result++;
            }
        }
        return result;
    }
    private T[] createNew(int len){
        return mBlockArrayCreator.newInstance(len);
    }
    @Override
    public int hashCode(){
        return hashCode;
    }
    @Override
    public boolean equals(Object obj){
        if(obj instanceof StringGroup){
            StringGroup other=(StringGroup)obj;
            return hashCode==other.hashCode();
        }
        return false;
    }
    @Override
    public String toString(){
        return items.length+"{"+name+"}";
    }
}
