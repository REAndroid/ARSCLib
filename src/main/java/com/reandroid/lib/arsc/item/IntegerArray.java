package com.reandroid.lib.arsc.item;


import java.util.AbstractList;
import java.util.List;

public class IntegerArray extends BlockItem {
    public IntegerArray() {
        super(0);
    }
    public final void clear(){
        setSize(0);
    }
    public final void add(int[] values){
        if(values==null || values.length==0){
            return;
        }
        int old=size();
        int s=old+values.length;
        setSize(s);
        for(int i=0;i<s;i++){
            put(old+i, values[i]);
        }
    }
    public final void set(int[] values){
        if(values==null || values.length==0){
            setSize(0);
            return;
        }
        int s=values.length;
        setSize(s);
        for(int i=0;i<s;i++){
            put(i, values[i]);
        }
    }
    public final List<Integer> toList(){
        List<Integer> results=new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return IntegerArray.this.get(i);
            }

            @Override
            public int size() {
                return IntegerArray.this.size();
            }
        };
        return results;
    }
    public final int[] toArray(){
        int s=size();
        int[] result=new int[s];
        for(int i=0;i<s;i++){
            result[i]=get(i);
        }
        return result;
    }
    public final void fill(int value){
        int max=size();
        for(int i=0;i<max;i++){
            put(i, value);
        }
    }
    public final void ensureArraySize(int s){
        int sz=size();
        if(sz>=s){
            return;
        }
        setSize(s);
    }
    public final void setSize(int s){
        if(s<0){
            s=0;
        }
        int len=s*4;
        setBytesLength(len);
    }
    public Integer get(int index){
        if(index<0 || index>=size()){
            return null;
        }
        int i=index*4;
        byte[] bts = getBytesInternal();
        return bts[i] & 0xff |
                (bts[i+1] & 0xff) << 8 |
                (bts[i+2] & 0xff) << 16 |
                (bts[i+3] & 0xff) << 24;
    }
    public final int size(){
        return getBytesLength()/4;
    }
    final void add(int value){
        int len=getBytesLength();
        len=len + 4;
        setBytesLength(len, false);
        int pos=size()-1;
        put(pos, value);
    }
    public final void put(int index, int value){
        int i=index*4;
        byte[] bts = getBytesInternal();
        bts[i+3]= (byte) (value >>> 24 & 0xff);
        bts[i+2]= (byte) (value >>> 16 & 0xff);
        bts[i+1]= (byte) (value >>> 8 & 0xff);
        bts[i]= (byte) (value & 0xff);
    }

    @Override
    public void onBytesChanged() {

    }
}
