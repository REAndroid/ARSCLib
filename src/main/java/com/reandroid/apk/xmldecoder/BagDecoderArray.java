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
package com.reandroid.apk.xmldecoder;

import com.reandroid.apk.ApkUtil;
import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.*;
import com.reandroid.common.EntryStore;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class BagDecoderArray<OUTPUT> extends BagDecoder<OUTPUT>{
    public BagDecoderArray(EntryStore entryStore) {
        super(entryStore);
    }

    @Override
    public OUTPUT decode(ResTableMapEntry mapEntry, EntryWriter<OUTPUT> writer) throws IOException {
        Entry entry = mapEntry.getParentEntry();
        String tag = getTagName(mapEntry);
        writer.enableIndent(true);
        writer.startTag(tag);
        writer.attribute("name", entry.getName());

        PackageBlock packageBlock = entry.getPackageBlock();
        EntryStore entryStore = getEntryStore();
        ResValueMap[] resValueMaps = mapEntry.listResValueMap();
        boolean zero_name = isZeroNameArray(resValueMaps);
        for(int i = 0; i < resValueMaps.length; i++){
            ResValueMap valueMap = resValueMaps[i];
            String childTag = "item";
            writer.enableIndent(true);
            writer.startTag(childTag);
            if(zero_name){
                String name = ValueDecoder.decodeAttributeName(
                        entryStore, packageBlock, valueMap.getName());
                writer.attribute("name", name);
            }
            writeText(writer, packageBlock, valueMap);
            writer.endTag(childTag);
        }
        return writer.endTag(tag);
    }
    private String getTagName(ResTableMapEntry mapEntry){
        ResValueMap[] resValueMaps = mapEntry.listResValueMap();
        Set<ValueType> valueTypes = new HashSet<>();
        for(int i = 0; i < resValueMaps.length; i++){
            valueTypes.add(resValueMaps[i].getValueType());
        }
        if(valueTypes.contains(ValueType.STRING)){
            return ApkUtil.TAG_STRING_ARRAY;
        }
        if(valueTypes.size() == 1 && valueTypes.contains(ValueType.INT_DEC)){
            return ApkUtil.TAG_INTEGER_ARRAY;
        }
        return XmlHelper.toXMLTagName(mapEntry.getParentEntry().getTypeName());
    }
    @Override
    public boolean canDecode(ResTableMapEntry mapEntry) {
        return isArrayValue(mapEntry);
    }
    public static boolean isArrayValue(ResTableMapEntry mapEntry){
        int parentId=mapEntry.getParentId();
        if(parentId!=0){
            return false;
        }
        ResValueMap[] valueMapList = mapEntry.listResValueMap();
        if(valueMapList == null || valueMapList.length == 0){
            return false;
        }
        if(isIndexedArray(valueMapList)){
            return true;
        }
        return isZeroNameArray(valueMapList);
    }
    private static boolean isIndexedArray(ResValueMap[] resValueMapList){
        int length = resValueMapList.length;
        for(int i = 0; i < length; i++){
            ResValueMap valueMap = resValueMapList[i];
            int name = valueMap.getName();
            int high = (name >> 16) & 0xffff;
            if(high!=0x0100 && high!=0x0200){
                return false;
            }
            int low = name & 0xffff;
            int id = low - 1;
            if(id!=i){
                return false;
            }
        }
        return true;
    }
    private static boolean isZeroNameArray(ResValueMap[] resValueMapList){
        int length = resValueMapList.length;
        for(int i = 0; i < length; i++){
            if(!isZeroName(resValueMapList[i])){
                return false;
            }
        }
        return true;
    }
    private static boolean isZeroName(ResValueMap resValueMap){
        return resValueMap.getName() == 0;
    }
}
