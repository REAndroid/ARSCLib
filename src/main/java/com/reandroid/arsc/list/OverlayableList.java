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

import com.reandroid.arsc.chunk.Overlayable;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.json.JSONArray;
import com.reandroid.utils.ObjectsUtil;

public class OverlayableList extends ExtraChunkList<Overlayable> {

    public OverlayableList() {
        super(Overlayable.CREATOR);
    }

    public Overlayable get(String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            Overlayable overlayable = get(i);
            if (ObjectsUtil.equals(name, overlayable.getName())) {
                return overlayable;
            }
        }
        return null;
    }
    public Overlayable get(String name, String actor) {
        int size = size();
        for (int i = 0; i < size; i++) {
            Overlayable overlayable = get(i);
            if (ObjectsUtil.equals(name, overlayable.getName()) ||
                    ObjectsUtil.equals(actor, overlayable.getActor())) {
                return overlayable;
            }
        }
        return null;
    }

    @Override
    public JSONArray toJson() {
        return BlockList.toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json == null || json.length() == 0) {
            return;
        }
        BlockList.fromJsonArray(this, json);
    }

    @Override
    public void merge(ExtraChunkList<Overlayable> overlayableList) {
        if (overlayableList == null || overlayableList == this) {
            return;
        }
        int size = overlayableList.size();
        if (this.size() == 0) {
            setSize(size);
            for (int i = 0; i < size; i++) {
                get(i).merge(overlayableList.get(i));
            }
        } else {
            ensureCapacity(size);
            for (int i = 0; i < size; i++) {
                Overlayable overlayable = overlayableList.get(i);
                Overlayable exist = get(overlayable.getName(), overlayable.getActor());
                if (exist == null) {
                    exist = get(overlayable.getName());
                }
                if (exist == null) {
                    exist = createNext();
                }
                exist.merge(overlayable);
            }
        }
    }

    @Override
    public String fileNameJson() {
        return Overlayable.FILE_NAME_JSON;
    }
    @Override
    public String fileNameXml() {
        return Overlayable.FILE_NAME_XML;
    }
}
