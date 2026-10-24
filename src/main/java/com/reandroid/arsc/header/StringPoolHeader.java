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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;

public class StringPoolHeader extends HeaderBlock {

    private EncodingChangedListener encodingChangedListener;

    public StringPoolHeader() {
        super(7, ChunkType.STRING.ID);

        setupBlock(3, new IntegerItem()); // countStrings
        setupBlock(4, new IntegerItem()); // countStyles
        setupBlock(5, new ByteItem());    // flagSorted
        setupBlock(6, new ByteItem());    // flagUtf8
        setupBlock(7, new ShortItem());   // flagExtra
        setupBlock(8, new IntegerItem()); // startStrings
        setupBlock(9, new IntegerItem()); // startStyles
    }

    public IntegerReference countStrings() {
        return block(3);
    }
    public IntegerReference countStyles() {
        return block(4);
    }
    public IntegerReference flagSorted() {
        return block(5);
    }
    public IntegerReference flagUtf8() {
        return block(6);
    }
    public IntegerReference flagExtra() {
        return block(7);
    }
    public IntegerReference startStrings() {
        return block(8);
    }
    public IntegerReference startStyles() {
        return block(9);
    }

    public boolean isUtf8() {
        return flagUtf8().get() != 0;
    }
    public void setUtf8(boolean utf8) {
        boolean changed = isUtf8() != utf8;
        setUtf8Flag(utf8);
        if (changed) {
            EncodingChangedListener listener = this.encodingChangedListener;
            if (listener != null) {
                listener.onEncodingChanged(utf8);
            }
        }
    }
    public void setUtf8Flag(boolean utf8) {
        flagUtf8().set(utf8 ? 0x01 : 0x00);
    }
    public boolean isSorted() {
        return flagSorted().get() !=0;
    }
    public void setSorted(boolean sorted) {
        flagSorted().set(sorted ? 0x01 : 0x00);
    }

    public void setEncodingChangedListener(EncodingChangedListener listener) {
        this.encodingChangedListener = listener;
    }

    @Override
    public String toString() {
        if (getChunkType() != ChunkType.STRING) {
            return super.toString();
        }
        return getClass().getSimpleName()
                +" {strings=" + countStrings()
                +", styles=" + countStyles()
                +", sorted=" + isSorted()
                +", utf8=" + isUtf8()
                +", flagExtra="+ flagExtra()
                +", offset-strings=" + startStrings().get()
                +", offset-styles=" + startStyles().get() + '}';
    }

    public interface EncodingChangedListener {
        void onEncodingChanged(boolean utf8);
    }
}
