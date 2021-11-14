package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.base.BlockArrayCreator;
import com.reandroid.lib.arsc.item.StringItem;

import java.lang.reflect.Array;

public class StringGroup<T extends StringItem> {
    private final BlockArrayCreator<T> mBlockArrayCreator;
    private final String name;
    private T[] items;
    private final int hashCode;
    StringGroup(BlockArrayCreator<T> blockArrayCreator, String name, T[] items){
        this.mBlockArrayCreator=blockArrayCreator;
        this.name=name;
        this.items=items;
        this.hashCode=(getClass().getName()+"-"+name).hashCode();
    }
    public boolean contains(T strItem){
        if(strItem==null){
            return false;
        }
        int len=items.length;
        for(int i=0;i<len;i++){
            if(strItem==items[i]){
                return true;
            }
        }
        return false;
    }
    public void remove(T strItem){
        if(strItem==null){
            return;
        }
        boolean found=false;
        int len=items.length;
        for(int i=0;i<len;i++){
            if(strItem==items[i]){
                items[i]=null;
                found=true;
            }
        }
        if(found){
            trimToSize();
        }
    }
    public void add(T strItem){
        if(strItem==null){
            return;
        }
        int index=items.length;
        T[] update=createNew(index+1);
        System.arraycopy(items, 0, update, 0, index);
        update[index]=strItem;
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
            return name.equals(other.name);
        }
        return false;
    }
    @Override
    public String toString(){
        return items.length+"{"+name+"}";
    }
}
