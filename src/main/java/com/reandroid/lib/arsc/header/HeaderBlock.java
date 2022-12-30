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
package com.reandroid.lib.arsc.header;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.ExpandableBlockContainer;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ShortItem;

import java.io.IOException;

 public class HeaderBlock extends ExpandableBlockContainer implements BlockLoad {
    private final ShortItem mType;
    private final ShortItem mHeaderSize;
    private final IntegerItem mChunkSize;
    private HeaderLoaded mHeaderLoaded;
    public HeaderBlock(short type){
        super(3);
        this.mType=new ShortItem(type);
        this.mHeaderSize=new ShortItem();
        this.mChunkSize=new IntegerItem();
        addChild(mType);
        addChild(mHeaderSize);
        addChild(mChunkSize);
        this.mType.setBlockLoad(this);
        this.mHeaderSize.setBlockLoad(this);
        this.mChunkSize.setBlockLoad(this);
    }
    public void setHeaderLoaded(HeaderLoaded headerLoaded){
        this.mHeaderLoaded=headerLoaded;
    }
    public ChunkType getChunkType(){
        return ChunkType.get(mType.get());
    }
    public short getType(){
        return mType.get();
    }
    public void setType(ChunkType chunkType){
        short type;
        if(chunkType==null){
            type=0;
        }else {
            type=chunkType.ID;
        }
        setType(type);
    }
    public void setType(short type){
        mType.set(type);
    }

    public int getHeaderSize(){
        return mHeaderSize.unsignedInt();
    }
    public void setHeaderSize(short headerSize){
        mHeaderSize.set(headerSize);
    }
    public int getChunkSize(){
        return mChunkSize.get();
    }
    public void setChunkSize(int chunkSize){
        mChunkSize.set(chunkSize);
    }

    public final void refreshHeader(){
        refreshHeaderSize();
        refreshChunkSize();
    }
    private void refreshHeaderSize(){
        int count=countBytes();
        setHeaderSize((short)count);
    }
    private void refreshChunkSize(){
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int count=parent.countBytes();
        setChunkSize(count);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        HeaderLoaded headerLoaded = mHeaderLoaded;
        if(headerLoaded==null){
            return;
        }
        if(sender==this.mType){
            headerLoaded.onChunkTypeLoaded(mType.get());
        }else if(sender==this.mHeaderSize){
            headerLoaded.onHeaderSizeLoaded(mHeaderSize.unsignedInt());
        }else if(sender==this.mChunkSize){
            headerLoaded.onChunkSizeLoaded(mHeaderSize.unsignedInt(),
                    mChunkSize.get());
        }
    }

    @Override
    protected void onRefreshed() {
        // Not required, the parent should call refreshHeader()
    }
    @Override
    protected void refreshChildes(){
        // Not required
    }

    @Override
    public String toString(){
        short t= getType();
        ChunkType type= ChunkType.get(t);
        StringBuilder builder=new StringBuilder();
        if(type!=null){
            builder.append(type.toString());
        }else {
            builder.append("Unknown type=");
            builder.append(String.format("0x%02x", ((int)t)));
        }
        builder.append("{Header=");
        builder.append(getHeaderSize());
        builder.append(", Chunk=");
        builder.append(getChunkSize());
        builder.append("}");
        return builder.toString();
    }

    public interface HeaderLoaded{
        void onChunkTypeLoaded(short type);
        void onHeaderSizeLoaded(int headerSize);
        void onChunkSizeLoaded(int headerSize, int chunkSize);
    }
}
