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

import com.reandroid.arsc.chunk.LibraryBlock;
import com.reandroid.arsc.value.LibraryInfo;
import com.reandroid.json.JSONArray;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class LibraryBlockList extends ExtraChunkList<LibraryBlock> {

    public LibraryBlockList() {
        super(LibraryBlock.CREATOR);
    }

    public boolean containsLibrary(String packageName) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (get(i).contains(packageName)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean isBlank() {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (!get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    public int getEntriesCount() {
        int count = 0;
        int size = size();
        for (int i = 0; i < size; i++) {
            count += get(i).size();
        }
        return count;
    }
    public Iterator<LibraryInfo> entries() {
        if (isBlank()) {
            return EmptyIterator.of();
        }
        return new IterableIterator<LibraryBlock, LibraryInfo>(iterator()) {
            @Override
            public Iterator<? extends LibraryInfo> iterator(LibraryBlock element) {
                return element.iterator();
            }
        };
    }
    @Override
    public JSONArray toJson() {
        JSONArray json = null;
        int size = size();
        for (int i = 0; i < size; i++) {
            JSONArray jsonArray = get(i).toJson();
            if (jsonArray == null || jsonArray.length() == 0) {
                continue;
            }
            if (json == null) {
                json = jsonArray;
            } else {
                json.putAll(jsonArray);
            }
        }
        return json;
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json == null || json.length() == 0) {
            return;
        }
        createNext().fromJson(json);
    }

    @Override
    public void merge(ExtraChunkList<LibraryBlock> libraryBlockList) {
        if (libraryBlockList == null || libraryBlockList == this || libraryBlockList.isBlank()) {
            return;
        }
        int size = libraryBlockList.size();
        if (this.isEmpty()) {
            setSize(size);
            for (int i = 0; i < size; i++) {
                this.get(i).merge(libraryBlockList.get(i));
            }
        } else {
            LibraryBlock last = getLast();
            for (int i = 0; i < size; i++) {
                last.merge(libraryBlockList.get(i));
            }
        }
    }

    @Override
    public String fileNameJson() {
        return LibraryBlock.FILE_NAME_JSON;
    }
    @Override
    public String fileNameXml() {
        return LibraryBlock.FILE_NAME_XML;
    }
}
