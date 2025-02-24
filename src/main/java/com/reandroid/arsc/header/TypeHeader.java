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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class TypeHeader extends HeaderBlock {
    
    private final ByteItem id;
    private final ByteItem flags;
    private final IntegerItem count;
    private final IntegerItem entriesStart;
    private final ResConfig config;

    private OffsetTypeChangedListener offsetTypeChangedListener;

    public TypeHeader() {
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
    }

    public TypeHeader(boolean sparse, boolean offset16) {
        this();
        setSparse(sparse);
        setOffset16(offset16);
    }
    
    public boolean isSparse() {
        return (getFlags().get() & 0x3) == 0x1;
    }
    public void setSparse(boolean sparse) {
        if (sparse != isSparse()) {
            setOffsetType(sparse ? OFFSET_SPARSE : OFFSET_32);
        }
    }
    public boolean isOffset16() {
        return (getFlags().get() & 0x3)  == 0x2;
    }
    public void setOffset16(boolean offset16) {
        if (offset16 != isOffset16()) {
            setOffsetType(offset16 ? OFFSET_16 : OFFSET_32);
        }
    }
    public int getOffsetType() {
        return getFlags().get() & 0x3;
    }
    public void setOffsetType(int type) {
        if (type != getOffsetType()) {
            if (type != OFFSET_32 && type != OFFSET_16 && type != OFFSET_SPARSE) {
                throw new IllegalArgumentException("Invalid offset type: " + type);
            }
            getFlags().set((getFlags().get() & 0xfc) | type);
            notifyOffsetTypeChanged();
        }
    }
    public void setOffsetType(boolean sparse, boolean offset16) {
        int type;
        if (sparse) {
            type = OFFSET_SPARSE;
        } else if (offset16) {
            type = OFFSET_16;
        } else {
            type = OFFSET_32;
        }
        setOffsetType(type);
    }

    @Override
    public int getMinimumSize() {
        //typeHeader.countBytes() - getConfig().countBytes() + ResConfig.SIZE_16
        return 36;
    }
    public ByteItem getId() {
        return id;
    }
    public ByteItem getFlags() {
        return flags;
    }
    public IntegerReference getCountItem() {
        return count;
    }
    public IntegerReference getEntriesStart() {
        return entriesStart;
    }
    public ResConfig getConfig() {
        return config;
    }

    private void notifyOffsetTypeChanged() {
        OffsetTypeChangedListener listener = this.offsetTypeChangedListener;
        if (listener != null) {
            listener.onOffsetTypeChanged(getOffsetType());
        }
    }
    public void setOffsetTypeChangedListener(OffsetTypeChangedListener listener) {
        this.offsetTypeChangedListener = listener;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        notifyOffsetTypeChanged();
    }

    @Override
    public String toString() {
        if (getChunkType() != ChunkType.TYPE) {
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {id="+getId().toHex()
                +", flags=" + getFlags().toHex()
                +", count=" + getCountItem()
                +", entriesStart=" + getEntriesStart()
                +", config=" + getConfig() + '}';
    }
    public static TypeHeader read(BlockReader reader) throws IOException {
        TypeHeader typeHeader = new TypeHeader(false, false);
        if (reader.available() < typeHeader.getMinimumSize()) {
            throw new IOException("Too few bytes to read type header, available = " + reader.available());
        }
        int pos = reader.getPosition();
        typeHeader.readBytes(reader);
        reader.seek(pos);
        return typeHeader;
    }

    public interface OffsetTypeChangedListener {
        void onOffsetTypeChanged(int offsetType);
    }

    public static final int OFFSET_32 = ObjectsUtil.of(0);
    public static final int OFFSET_SPARSE = ObjectsUtil.of(1);
    public static final int OFFSET_16 = ObjectsUtil.of(2);
}
