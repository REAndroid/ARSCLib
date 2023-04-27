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
package com.reandroid.arsc.item;


import java.util.AbstractList;
import java.util.List;

public class IntegerArray extends BlockItem {
    public IntegerArray() {
        super(0);
    }
    public final boolean contains(int value){
        int s=size();
        for(int i=0;i<s;i++){
            if(value==get(i)){
                return true;
            }
        }
        return false;
    }
    public final void clear(){
        setSize(0);
    }
    public final void add(int value){
        int old=size();
        setSize(old+1);
        put(old, value);
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
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return IntegerArray.this.get(i);
            }
            @Override
            public int size() {
                return IntegerArray.this.size();
            }
        };
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
    public int getAt(int index){
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
    public final void put(int index, int value){
        int i=index*4;
        byte[] bts = getBytesInternal();
        bts[i+3]= (byte) (value >>> 24 & 0xff);
        bts[i+2]= (byte) (value >>> 16 & 0xff);
        bts[i+1]= (byte) (value >>> 8 & 0xff);
        bts[i]= (byte) (value & 0xff);
    }
    @Override
    public String toString(){
        return "size="+size();
    }
}
