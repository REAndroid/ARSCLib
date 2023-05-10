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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.*;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlIDMap;
import com.reandroid.arsc.group.StringGroup;
import com.reandroid.arsc.item.*;

import java.util.Objects;

public class ResXmlStringPool extends StringPool<ResXmlString> {
    public ResXmlStringPool(boolean is_utf8) {
        super(is_utf8, false);
    }
    @Override
    public ResXmlString removeReference(ReferenceItem referenceItem){
        if(referenceItem==null){
            return null;
        }
        ResXmlString stringItem = super.removeReference(referenceItem);
        removeNotUsedItem(stringItem);
        return stringItem;
    }
    private void removeNotUsedItem(ResXmlString xmlString){
        if(xmlString == null || xmlString.hasReference()){
            return;
        }
        ResXmlIDMap idMap = getResXmlIDMap();
        int lastIdIndex = -1;
        if(idMap!=null){
            lastIdIndex = idMap.countId() - 1;
        }
        if(idMap!=null && xmlString.getIndex()>lastIdIndex){
            removeString(xmlString);
        }else {
            xmlString.set("");
        }
    }
    @Override
    StringArray<ResXmlString> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new ResXmlStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public ResXmlString getOrCreate(String str){
        return getOrCreateAttribute(0, str);
    }
    public ResXmlString createNew(String str){
        StringArray<ResXmlString> stringsArray = getStringsArray();
        ResXmlString xmlString = stringsArray.createNext();
        xmlString.set(str);
        return xmlString;
    }
    public ResXmlString getOrCreateAttribute(int resourceId, String str){
        ResXmlIDMap resXmlIDMap = getResXmlIDMap();
        if(resXmlIDMap == null){
            return super.getOrCreate(str);
        }
        ResXmlIDArray idArray = resXmlIDMap.getResXmlIDArray();
        int count = idArray.childesCount();
        if(resourceId == 0){
            return getOrCreateAfter(count, str);
        }
        StringArray<ResXmlString> stringsArray = getStringsArray();
        ResXmlID xmlID = idArray.getByResId(resourceId);
        if(xmlID != null){
            ResXmlString xmlString = stringsArray.get(xmlID.getIndex());
            if(xmlString!=null && Objects.equals(str, xmlString.get())){
                return xmlString;
            }
        }
        count = idArray.childesCount() + 1;
        stringsArray.ensureSize(count);
        idArray.setChildesCount(count);
        int index = count - 1;
        xmlID = idArray.get(index);
        xmlID.set(resourceId);
        idArray.refreshIdMap();

        ResXmlString xmlString = stringsArray.newInstance();
        xmlString.set(str);
        stringsArray.insertItem(index, xmlString);

        updateUniqueIdMap(xmlString);
        return xmlString;
    }
    private ResXmlString getOrCreateAfter(int position, String str){
        if(position<0){
            position=0;
        }
        StringGroup<ResXmlString> group = get(str);
        if(group!=null){
            for(ResXmlString xmlString:group.listItems()){
                int index = xmlString.getIndex();
                if(index > position || (position==0 && position == index)){
                    return xmlString;
                }
            }
        }
        StringArray<ResXmlString> stringsArray = getStringsArray();
        int count = stringsArray.childesCount();
        if(count < position){
            count = position;
        }
        stringsArray.ensureSize(count+1);
        ResXmlString xmlString = stringsArray.get(count);
        xmlString.set(str);
        super.updateUniqueIdMap(xmlString);
        return xmlString;
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlDocument resXmlDocument = getParentInstance(ResXmlDocument.class);
        if(resXmlDocument!=null){
            return resXmlDocument.getResXmlIDMap();
        }
        return null;
    }
    public ResXmlString getOrCreateAttributeName(int idMapCount, String str){
        StringGroup<ResXmlString> group = get(str);
        if(group!=null){
            for(ResXmlString xmlString:group.listItems()){
                if(xmlString.getIndex()>idMapCount){
                    return xmlString;
                }
            }
        }
        StringArray<ResXmlString> stringsArray = getStringsArray();
        stringsArray.ensureSize(idMapCount);
        int i=stringsArray.childesCount();
        stringsArray.ensureSize(i+1);
        ResXmlString xmlString=stringsArray.get(i);
        xmlString.set(str);
        refreshUniqueIdMap();
        return xmlString;
    }
    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        StyleArray styleArray = getStyleArray();
        if(styleArray.childesCount()>0){
            notifyResXmlStringPoolHasStyles(styleArray.childesCount());
        }
    }
    private static void notifyResXmlStringPoolHasStyles(int styleArrayCount){
        if(HAS_STYLE_NOTIFIED){
            return;
        }
        String msg="Not expecting ResXmlStringPool to have styles count="
                +styleArrayCount+",\n please create issue along with this apk/file on https://github.com/REAndroid/ARSCEditor";
        System.err.println(msg);
        HAS_STYLE_NOTIFIED=true;
    }
    private static boolean HAS_STYLE_NOTIFIED;
}
