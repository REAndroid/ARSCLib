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
package com.reandroid.arsc.header;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.value.ResConfig;

public class TypeHeader extends HeaderBlock{
    private final ByteItem id;
    private final ByteItem flags;
    private final IntegerItem count;
    private final IntegerItem entriesStart;
    private final ResConfig config;
    public TypeHeader(boolean sparse) {
        super(ChunkType.TYPE.ID);
        this.id = new ByteItem();
        this.flags = new ByteItem();
        ShortItem reserved = new ShortItem();
        this.count = new IntegerItem();
        this.entriesStart = new IntegerItem();
        this.config = new ResConfig();

        addChild(id);
        addChild(flags);
        addChild(reserved);
        addChild(count);
        addChild(entriesStart);
        addChild(config);
        setSparse(sparse);
    }
    public boolean isSparse(){
        return (getFlags().get() & FLAG_SPARSE) == FLAG_SPARSE;
    }
    public void setSparse(boolean sparse){
        byte flag = getFlags().get();
        if(sparse){
            flag = (byte) (flag | FLAG_SPARSE);
        }else {
            flag = (byte) (flag & (~FLAG_SPARSE & 0xff));
        }
        getFlags().set(flag);
    }

    @Override
    public int getMinimumSize(){
        return TYPE_MIN_SIZE;
    }
    public ByteItem getId() {
        return id;
    }
    public ByteItem getFlags() {
        return flags;
    }
    public IntegerItem getCount() {
        return count;
    }
    public IntegerItem getEntriesStart() {
        return entriesStart;
    }
    public ResConfig getConfig() {
        return config;
    }

    @Override
    public String toString(){
        if(getChunkType()!=ChunkType.TYPE){
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {id="+getId().toHex()
                +", flags=" + getFlags().toHex()
                +", count=" + getCount()
                +", entriesStart=" + getEntriesStart()
                +", config=" + getConfig() + '}';
    }

    private static final byte FLAG_SPARSE = 0x1;

    //typeHeader.countBytes() - getConfig().countBytes() + ResConfig.SIZE_16
    private static final int TYPE_MIN_SIZE = 36;
}
