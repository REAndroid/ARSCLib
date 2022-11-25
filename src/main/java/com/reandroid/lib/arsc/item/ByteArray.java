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
    public final void setInt(short val){
        byte[] bts = getBytesInternal();
        bts[3]= (byte) (val >>> 24 & 0xff);
        bts[2]= (byte) (val >>> 16 & 0xff);
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    public final void setShort(short val){
        byte[] bts = getBytesInternal();
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    public final short getShort(){
        byte[] bts = getBytesInternal();
        return (short) (bts[0] & 0xff | (bts[1] & 0xff) << 8);
    }
    public final int getInt(){
        byte[] bts = getBytesInternal();
        return bts[0] & 0xff |
                (bts[1] & 0xff) << 8 |
                (bts[2] & 0xff) << 16 |
                (bts[3] & 0xff) << 24;
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
    @Override
    public String toString(){
        return "size="+size();
    }
}
