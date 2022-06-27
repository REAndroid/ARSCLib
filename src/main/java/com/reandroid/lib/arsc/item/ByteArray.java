package com.reandroid.lib.arsc.item;

import java.util.AbstractList;
import java.util.List;

public class ByteArray extends BlockItem {
    public ByteArray(int bytesLength) {
        super(bytesLength);
    }
    public ByteArray() {
        this(0);
    }
    public final void clear(){
        setSize(0);
    }
    public final void add(byte[] values){
        if(values==null || values.length==0){
            return;
        }
        int old=size();
        int len=values.length;
        setBytesLength(old+len, false);
        byte[] bts = getBytesInternal();
        for(int i=0;i<len;i++){
            bts[old+i]=values[i];
        }
    }
    public final void set(byte[] values){
        super.setBytesInternal(values);
    }
    public final List<Byte> toList(){
        List<Byte> results=new AbstractList<Byte>() {
            @Override
            public Byte get(int i) {
                return ByteArray.this.get(i);
            }

            @Override
            public int size() {
                return ByteArray.this.size();
            }
        };
        return results;
    }
    public final byte[] toArray(){
        return getBytes();
    }
    public final void fill(byte value){
        byte[] bts=getBytesInternal();
        int max=bts.length;
        for(int i=0;i<max;i++){
            bts[i]=value;
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
        setBytesLength(s);
    }
    public Byte get(int index){
        if(index<0 || index>=size()){
            return null;
        }
        return getBytesInternal()[index];
    }
    public final int size(){
        return getBytesLength();
    }
    final void add(byte value){
        int len=getBytesLength();
        len=len + 1;
        setBytesLength(len, false);
        put(len, value);
    }
    public final void put(int index, byte value){
        byte[] bts = getBytesInternal();
        bts[index]=value;
    }
    @Override
    public void onBytesChanged() {

    }
}
