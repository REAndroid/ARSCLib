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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.DataItem;

import java.io.IOException;

public class DataSectionArray<T extends DataItem> extends SectionArray<T> {

    public DataSectionArray(IntegerPair countAndOffset, Creator<T> creator) {
        super(countAndOffset, creator);
    }

    public T getAt(int offset) {
        if (offset <= 0) {
            return null;
        }
        T item = binaryOffsetSearch(offset);
        if (item == null) {
            // should not reach here
            // un ordered offset entries or the offset does not exist
            item = lazyOffsetSearch(offset);
        }
        return item;
    }

    private T binaryOffsetSearch(int offset) {
        // Assumed all entries are ordered in ascending offset
        int start = 0;
        int end = size() - 1;
        while (end >= start) {
            int mid = start + ((end - start) / 2);
            T item = get(mid);
            int test = item.getOffset();
            if (test == offset) {
                return item;
            }
            if (test < offset) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
        }
        return null;
    }
    private T lazyOffsetSearch(int offset) {
        int size = size();
        for (int i = 0; i < size; i++) {
            T item = get(i);
            if (offset == item.getOffset()) {
                return item;
            }
        }
        return null;
    }

    @Override
    public void readChild(BlockReader reader, T item) throws IOException {
        int offset = reader.getPosition();
        item.setPosition(offset);
        item.onReadBytes(reader);
    }

    @Override
    public void onPreRemove(T item) {
        super.onPreRemove(item);
        IntegerReference reference = item.getOffsetReference();
        if (reference != null) {
            reference.set(0);
        }
    }
}
