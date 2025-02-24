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
package com.reandroid.arsc.list;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.base.DirectStreamReader;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.OffsetItem;

import java.io.IOException;
import java.io.InputStream;

public class OffsetReferenceList<T extends OffsetItem> extends BlockList<T> implements DirectStreamReader {

    private final IntegerReference countReference;

    public OffsetReferenceList(IntegerReference countReference, Creator<? extends T> creator) {
        super(creator);
        this.countReference = countReference;
    }

    public IntegerReference getCountReference() {
        return countReference;
    }

    public void clear() {
        clearChildes();
    }
    @Override
    public void setSize(int size) {
        super.setSize(size);
        getCountReference().set(size);
    }

    @Override
    protected boolean hasSimilarEntries() {
        return true;
    }

    @Override
    protected void refreshChildes() {
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        int size = getCountReference().get();
        setSize(size);
        for (int i = 0; i < size; i++) {
            get(i).readBytes(reader);
        }
    }

    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        int size = getCountReference().get();
        setSize(size);
        int count = 0;
        for (int i = 0; i < size; i++) {
            count += get(i).readBytes(inputStream);
        }
        return count;
    }
}
