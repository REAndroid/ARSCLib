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
package com.reandroid.arsc.array;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResXmlAttributeArray extends BlockArray<ResXmlAttribute>
        implements Comparator<ResXmlAttribute>, JSONConvert<JSONArray> {
    private final HeaderBlock mHeaderBlock;
    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeCount;
    private final ShortItem mAttributesUnitSize;
    public ResXmlAttributeArray(HeaderBlock headerBlock,
                                ShortItem attributeStart,
                                ShortItem attributeCount,
                                ShortItem attributesUnitSize){
        this.mHeaderBlock=headerBlock;
        this.mAttributeStart=attributeStart;
        this.mAttributeCount=attributeCount;
        this.mAttributesUnitSize=attributesUnitSize;
    }

    public int removeUndefinedAttributes(){
        List<ResXmlAttribute> undefinedAttributes = listUndefined();
        super.remove(undefinedAttributes);
        return undefinedAttributes.size();
    }
    public List<ResXmlAttribute> listUndefined(){
        List<ResXmlAttribute> results = new ArrayList<>();
        ResXmlAttribute[] attributes = getChildren();
        for(int i = 0; i < attributes.length; i++){
            ResXmlAttribute attribute = attributes[i];
            if(attribute != null && attribute.isUndefined()){
                results.add(attribute);
            }
        }
        return results;
    }
    public void setAttributesUnitSize(int size){
        ResXmlAttribute[] attributes=getChildren();
        for(int i=0;i<attributes.length;i++){
            attributes[i].setAttributesUnitSize(size);
        }
        mAttributesUnitSize.set(size);
    }
    public void sortAttributes(){
        sort(this);
    }
    private void refreshCount(){
        mAttributeCount.set(getChildrenCount());
    }
    private void refreshStart(){
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int start = parent.countUpTo(this);
        start = start - mHeaderBlock.countBytes();
        mAttributeStart.set(start);
    }
    @Override
    public ResXmlAttribute newInstance() {
        return new ResXmlAttribute(mAttributesUnitSize.unsignedInt());
    }
    @Override
    public ResXmlAttribute[] newInstance(int len) {
        if(len == 0){
            return EMPTY;
        }
        return new ResXmlAttribute[len];
    }
    @Override
    protected void onRefreshed() {
        refreshCount();
        refreshStart();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int start = mHeaderBlock.getHeaderSize() + mAttributeStart.get();
        reader.seek(start);
        setChildrenCount(mAttributeCount.get());
        int attributeSize = mAttributesUnitSize.unsignedInt();
        ResXmlAttribute[] children = getChildren();
        for(int i=0;i<children.length;i++){
            int position = reader.getPosition();
            ResXmlAttribute attribute = children[i];
            attribute.readBytes(reader);
            int remaining = attributeSize - (reader.getPosition() - position);
            reader.offset(remaining);
        }
    }
    @Override
    public void clearChildren(){
        ResXmlAttribute[] children = getChildren();
        if(children==null || children.length==0){
            super.clearChildren();
            return;
        }
        int length = children.length;
        for(int i=0;i<length;i++){
            ResXmlAttribute child = children[i];
            if(child==null){
                continue;
            }
            child.onRemoved();
        }
        super.clearChildren();
    }
    @Override
    public int compare(ResXmlAttribute attr1, ResXmlAttribute attr2) {
        return attr1.compareTo(attr2);
    }

    @Override
    public JSONArray toJson() {
        sortAttributes();
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(ResXmlAttribute attr:listItems()){
            JSONObject jsonObject = attr.toJson();
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clearChildren();
        if(json == null){
            return;
        }
        int length = json.length();
        ensureSize(length);
        for(int i = 0; i < length; i++){
            ResXmlAttribute attribute = get(i);
            JSONObject jsonObject = json.getJSONObject(i);
            attribute.fromJson(jsonObject);
        }
    }

    private static final ResXmlAttribute[] EMPTY = new ResXmlAttribute[0];
}
