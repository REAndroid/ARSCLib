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

    private final IntegerItem countStrings;
    private final IntegerItem countStyles;
    private final ByteItem flagSorted;
    private final ByteItem flagUtf8;
    private final ShortItem flagExtra;
    private final IntegerItem startStrings;
    private final IntegerItem startStyles;

    private EncodingChangedListener encodingChangedListener;

    public StringPoolHeader() {
        super(7, ChunkType.STRING.ID);

        this.countStrings = new IntegerItem();
        this.countStyles = new IntegerItem();
        this.flagSorted = new ByteItem();
        this.flagUtf8 = new ByteItem();
        this.flagExtra = new ShortItem();
        this.startStrings = new IntegerItem();
        this.startStyles = new IntegerItem();

        addChild(3, countStrings);
        addChild(4, countStyles);
        addChild(5, flagSorted);
        addChild(6, flagUtf8);
        addChild(7, flagExtra);
        addChild(8, startStrings);
        addChild(9, startStyles);
    }

    public IntegerReference getCountStrings() {
        return countStrings;
    }
    public IntegerReference getCountStyles() {
        return countStyles;
    }
    public IntegerReference getFlagUtf8() {
        return flagUtf8;
    }
    public IntegerReference getFlagSorted() {
        return flagSorted;
    }
    public IntegerReference getFlagExtra() {
        return flagExtra;
    }
    public IntegerReference getStartStrings() {
        return startStrings;
    }
    public IntegerReference getStartStyles() {
        return startStyles;
    }

    public boolean isUtf8() {
        return getFlagUtf8().get() != 0;
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
        getFlagUtf8().set(utf8 ? 0x01 : 0x00);
    }
    public boolean isSorted() {
        return getFlagSorted().get() !=0;
    }
    public void setSorted(boolean sorted) {
        getFlagSorted().set(sorted ? 0x01 : 0x00);
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
                +" {strings=" + getCountStrings()
                +", styles=" + getCountStyles()
                +", utf8=" + isUtf8()
                +", sorted=" + isSorted()
                +", flagExtra="+ getFlagExtra()
                +", offset-strings=" + getStartStrings().get()
                +", offset-styles=" + getStartStyles().get() + '}';
    }

    public interface EncodingChangedListener {
        void onEncodingChanged(boolean utf8);
    }
}
