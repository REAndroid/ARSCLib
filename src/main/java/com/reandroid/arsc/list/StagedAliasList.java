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

import com.reandroid.arsc.chunk.StagedAlias;
import com.reandroid.arsc.value.StagedAliasEntry;
import com.reandroid.json.JSONArray;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.util.Iterator;

public class StagedAliasList extends ExtraChunkList<StagedAlias> {

    public StagedAliasList() {
        super(StagedAlias.CREATOR);
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
    public Iterator<StagedAliasEntry> entries() {
        if (isBlank()) {
            return EmptyIterator.of();
        }
        return new IterableIterator<StagedAlias, StagedAliasEntry>(iterator()) {
            @Override
            public Iterator<? extends StagedAliasEntry> iterator(StagedAlias element) {
                return element.iterator();
            }
        };
    }
    public StagedAliasEntry getByStaged(int staged) {
        int size = size();
        for (int i = 0; i < size; i++) {
            StagedAliasEntry entry = get(i).getByStaged(staged);
            if (entry != null) {
                return entry;
            }
        }
        return null;
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
        StagedAlias stagedAlias;
        if (this.isEmpty()) {
            stagedAlias = createNext();
        } else {
            stagedAlias = getLast();
        }
        stagedAlias.fromJson(json);
    }

    @Override
    public void merge(ExtraChunkList<StagedAlias> stagedAliasList) {
        if (stagedAliasList == null || stagedAliasList == this || stagedAliasList.isBlank()) {
            return;
        }
        int size = stagedAliasList.size();
        if (this.isBlank()) {
            setSize(size);
            for (int i = 0; i < size; i++) {
                this.get(i).merge(stagedAliasList.get(i));
            }
        } else {
            StagedAlias last = getLast();
            for (int i = 0; i < size; i++) {
                last.merge(stagedAliasList.get(i));
            }
        }
    }

    @Override
    public String fileNameJson() {
        return StagedAlias.FILE_NAME_JSON;
    }
    @Override
    public String fileNameXml() {
        return StagedAlias.FILE_NAME_XML;
    }
}
