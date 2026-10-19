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

import com.reandroid.arsc.io.BlockReader;

import java.io.IOException;
import java.io.InputStream;

public class CountedBlockItem extends BlockItem {

    private final IntegerReference countReference;

    public CountedBlockItem(IntegerReference countReference) {
        super(countReference == null ? 0 : countReference.get());
        this.countReference = countReference;
    }

    public int size() {
        return countBytes();
    }
    public void setSize(int size) {
        setBytesLength(size);
    }
    public IntegerReference countReference() {
        return countReference;
    }

    @Override
    protected void onLengthChanged(int length) {
        super.onLengthChanged(length);
        IntegerReference countReference = countReference();
        if (countReference.get() != length) {
            countReference.set(length);
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setBytesLength(countReference().get(), false);
        super.onReadBytes(reader);
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        setBytesLength(countReference().get(), false);
        return super.readBytes(inputStream);
    }
}
