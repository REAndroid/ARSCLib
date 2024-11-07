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
package com.reandroid.dex.model;

import com.reandroid.dex.sections.Marker;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DexFileInfo implements JSONConvert<JSONObject> {

    private int version;
    private final List<String> markerList;
    private final List<DexFileInfo> layoutList;

    public DexFileInfo() {
        this.version = 35;
        this.markerList = new ArrayCollection<>();
        this.layoutList = new ArrayCollection<>();
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
    }
    public void addMarker(Marker marker) {
        if (marker != null) {
            addMarker(marker.toString());
        }
    }
    public void addMarker(String marker) {
        if (marker != null) {
            markerList.add(marker);
        }
    }
    public void addLayout(DexFileInfo layout) {
        if (layout != null && layout != this) {
            layoutList.add(layout);
        }
    }
    public Iterator<Marker> getMarkers() {
        return ComputeIterator.of(getMarkerList().iterator(), Marker::parse);
    }
    public List<String> getMarkerList() {
        return markerList;
    }
    public List<DexFileInfo> getLayoutList() {
        return layoutList;
    }

    public void saveToDirectory(File dir) throws IOException {
        File file = new File(dir, FILE_NAME);
        toJson().write(file);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_version, getVersion());
        List<String> markerList = getMarkerList();
        if (!markerList.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.putAll(markerList);
            jsonObject.put(NAME_markers, jsonArray);
        }
        List<DexFileInfo> layoutList = getLayoutList();
        if (!layoutList.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (DexFileInfo info : layoutList) {
                jsonArray.put(info.toJson());
            }
            jsonObject.put(NAME_layouts, jsonArray);
        }
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject jsonObject) {
        setVersion(jsonObject.optInt(NAME_version, getVersion()));
        JSONArray jsonArray = jsonObject.optJSONArray(NAME_markers);
        if (jsonArray != null) {
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                addMarker(jsonArray.optString(i));
            }
        }
        jsonArray = jsonObject.optJSONArray(NAME_layouts);
        if (jsonArray != null) {
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                addLayout(convert(jsonArray.getJSONObject(i)));
            }
        }
    }

    public void applyTo(DexFile dexFile) {
        List<DexFileInfo> layoutList = getLayoutList();
        if (layoutList.isEmpty()) {
            applyTo(dexFile.getOrCreateFirst());
            return;
        }
        int size = layoutList.size();
        for (int i = 0; i < size; i++) {
            DexLayout dexLayout = dexFile.getOrCreateAt(i);
            DexFileInfo info = layoutList.get(i);
            info.applyTo(dexLayout);
        }
    }
    public void applyTo(DexLayout dexLayout) {
        dexLayout.setVersion(getVersion());
        Iterator<Marker> iterator = getMarkers();
        while (iterator.hasNext()) {
            Marker marker = iterator.next();
            dexLayout.addMarker(marker);
        }
    }
    @Override
    public String toString() {
        return toJson().toString(2);
    }

    public static DexFileInfo fromDex(DexFile dexFile) {
        DexFileInfo info = new DexFileInfo();
        info.setVersion(dexFile.getVersion());
        if (dexFile.isMultiLayout()) {
            int size = dexFile.size();
            for (int i = 0; i < size; i ++) {
                info.addLayout(fromDex(dexFile.getLayout(i)));
            }
        } else {
            Iterator<Marker> iterator = dexFile.getMarkers();
            while (iterator.hasNext()) {
                info.addMarker(iterator.next());
            }
        }
        return info;
    }
    public static DexFileInfo fromDex(DexLayout dexLayout) {
        DexFileInfo info = new DexFileInfo();
        info.setVersion(dexLayout.getVersion());
        Iterator<Marker> iterator = dexLayout.getMarkers();
        while (iterator.hasNext()) {
            info.addMarker(iterator.next());
        }
        return info;
    }
    public static DexFileInfo readJson(File file) throws IOException {
        return convert(new JSONObject(file));
    }
    public static DexFileInfo convert(JSONObject jsonObject) {
        DexFileInfo info = new DexFileInfo();
        info.fromJson(jsonObject);
        return info;
    }

    public static final String FILE_NAME = ObjectsUtil.of("dex-file.json");

    public static final String NAME_layouts = ObjectsUtil.of("layouts");
    public static final String NAME_markers = ObjectsUtil.of("markers");
    public static final String NAME_version = ObjectsUtil.of("version");
}
