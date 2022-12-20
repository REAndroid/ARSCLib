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
package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.item.BlockItem;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

public abstract class BaseResValue extends BlockItem implements JSONConvert<JSONObject> {
    BaseResValue(int bytesLength){
        super(bytesLength);
    }

    protected void onRemoved(){
    }

    public EntryBlock getEntryBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof EntryBlock){
                return (EntryBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    boolean removeSpecReference(ReferenceItem ref){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        return entryBlock.removeSpecReference(ref);
    }
    boolean removeTableReference(ReferenceItem ref){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        return entryBlock.removeTableReference(ref);
    }
    void addSpecReference(ReferenceItem ref){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return;
        }
        entryBlock.addSpecReference(ref);
    }
    void addTableReference(ReferenceItem ref){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return;
        }
        entryBlock.addTableReference(ref);
    }
    @Override
    public void onBytesChanged() {

    }
    void onDataLoaded() {

    }
    int getInt(int offset){
        byte[] bts = getBytesInternal();
        return bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 |
                (bts[offset+2] & 0xff) << 16 |
                (bts[offset+3] & 0xff) << 24;
    }
    void setInt(int offset, int val){
        if(val==getInt(offset)){
            return;
        }
        byte[] bts = getBytesInternal();
        bts[offset+3]= (byte) (val >>> 24 & 0xff);
        bts[offset+2]= (byte) (val >>> 16 & 0xff);
        bts[offset+1]= (byte) (val >>> 8 & 0xff);
        bts[offset]= (byte) (val & 0xff);
        onBytesChanged();
    }
    void setShort(int offset, short val){
        if(val==getShort(offset)){
            return;
        }
        byte[] bts = getBytesInternal();
        bts[offset+1]= (byte) (val >>> 8 & 255);
        bts[offset]= (byte) (val & 255);
        onBytesChanged();
    }
    short getShort(int offset){
        byte[] bts=getBytesInternal();
        int i= bts[offset] & 0xff |
                (bts[offset+1] & 0xff) << 8 ;
        return (short)i;
    }
    void setByte(int offset, byte b){
        byte[] bts=getBytesInternal();
        if(b==bts[offset]){
            return;
        }
        bts[offset]=b;
        onBytesChanged();
    }
    byte getByte(int offset){
        return getBytesInternal()[offset];
    }

    static final String NAME_data = "data";
    static final String NAME_value_type="value_type";
    static final String NAME_id="id";
    static final String NAME_parent="parent";
    static final String NAME_items="items";
}
