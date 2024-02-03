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

import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.xml.StyleSpanEventSet;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.SpanSet;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleElement;

import java.io.IOException;
import java.util.*;

public class StyleItem extends FixedBlockContainer implements
        Iterable<StyleSpan>, SpanSet<StyleSpan>, JSONConvert<JSONObject> {

    private final BlockList<StyleSpan> spanList;
    private final IntegerItem endBlock;

    private StyleIndexReference indexReference;
    private StringItem mStringItem;

    public StyleItem() {
        super(2);
        this.spanList = new BlockList<>();
        this.endBlock = new IntegerItem();
        addChild(0, spanList);
        addChild(1, endBlock);
        endBlock.set(-1);
    }
    public void add(String tag, int start, int end){
        StyleSpan styleSpan = createNext();
        styleSpan.setString(tag);
        styleSpan.setFirstChar(start);
        styleSpan.setLastChar(end);
    }
    public StyleSpan createNext(){
        StyleSpan styleSpan = new StyleSpan();
        this.spanList.add(styleSpan);
        return styleSpan;
    }
    public StyleSpan get(int i){
        return spanList.get(i);
    }
    public int size(){
        return spanList.size();
    }
    @Override
    public Iterator<StyleSpan> iterator() {
        return spanList.clonedIterator();
    }
    @Override
    public Iterator<StyleSpan> getSpans() {
        return iterator();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        BlockList<StyleSpan> spanList = this.spanList;
        while (reader.readInteger() != -1){
            StyleSpan styleSpan = new StyleSpan();
            spanList.add(styleSpan);
            styleSpan.onReadBytes(reader);
        }
        this.endBlock.onReadBytes(reader);
    }

    public StyleDocument build(String text){
        return StyleSpanEventSet.serialize(text, this);
    }
    public void parse(StyleDocument document){
        clearStyle();
        Iterator<StyleElement> iterator = document.getElements();
        while (iterator.hasNext()){
            parse(iterator.next());
        }
    }
    public void parse(StyleElement element){
        add(element.getTagString(), element.getFirstChar(), element.getLastChar());
        Iterator<StyleElement> iterator = element.getElements();
        while (iterator.hasNext()){
            parse(iterator.next());
        }
    }
    protected void clearStyle(){
        if(getParent() == null){
            return;
        }
        for(StyleSpan styleSpan : this){
            styleSpan.onRemoved();
        }
        spanList.clearChildes();
    }
    public void onRemoved(){
        StyleArray parentArray = getParentInstance(StyleArray.class);
        setParent(null);
        setIndex(-1);
        if(parentArray != null){
            parentArray.remove(this);
        }
    }
    public void linkStringsInternal(){
        linkIndexReference();
        for(StyleSpan styleSpan : this){
            styleSpan.link();
        }
    }
    private void linkIndexReference(){
        StringItem stringItem = getStringItem(getIndex());
        unLinkIndexReference(mStringItem);
        if(stringItem == null){
            return;
        }
        StyleIndexReference reference = new StyleIndexReference(this);
        stringItem.addReference(reference);
        this.indexReference = reference;
        this.mStringItem = stringItem;
    }
    private void unLinkIndexReference(StringItem stringItem){
        this.mStringItem = null;
        StyleIndexReference reference = this.indexReference;
        if(reference == null){
            return;
        }
        this.indexReference = null;
        if(stringItem == null){
            return;
        }
        stringItem.removeReference(reference);
    }
    private StringItem getStringItem(int ref){
        StringPool<?> stringPool = getStringPool();
        if(stringPool != null){
            return stringPool.get(ref);
        }
        return null;
    }
    private StringPool<?> getStringPool(){
        return getParentInstance(StringPool.class);
    }

    public String applyStyle(String text, boolean xml, boolean escapeXmlText){
        if(text == null){
            return null;
        }
        StyleDocument styleDocument = build(text);
        if(styleDocument == null){
            return text;
        }
        return styleDocument.getText(xml, escapeXmlText);
    }
    @Override
    public void setNull(boolean is_null){
        if(!is_null){
            return;
        }
        clearStyle();
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(StyleSpan spanInfo:this){
            if(spanInfo==null){
                continue;
            }
            JSONObject jsonObjectSpan=spanInfo.toJson();
            jsonArray.put(i, jsonObjectSpan);
            i++;
        }
        if(i==0){
            return null;
        }
        jsonObject.put(NAME_spans, jsonArray);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setNull(true);
        if(json == null){
            return;
        }
        JSONArray jsonArray = json.getJSONArray(NAME_spans);
        int length = jsonArray.length();
        for(int i = 0; i < length;i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            StyleSpan styleSpan = createNext();
            styleSpan.fromJson(jsonObject);
        }
    }
    public void merge(StyleItem styleItem){
        if(styleItem == null || styleItem == this){
            return;
        }
        for(StyleSpan styleSpan : styleItem){
            add(styleSpan.getString(), styleSpan.getFirstChar(), styleSpan.getLastChar());
        }
    }
    @Override
    public String toString(){
        return "Spans count = " + size();
    }

    static final class StyleIndexReference implements WeakStringReference{
        private final StyleItem styleItem;
        StyleIndexReference(StyleItem styleItem){
            this.styleItem = styleItem;
        }
        @Override
        public void set(int value) {
            StyleItem styleItem = this.styleItem;
            int oldIndex = styleItem.getIndex();
            StyleArray styleArray = styleItem.getParentInstance(StyleArray.class);
            if(styleArray != null){
                StyleItem previous = styleArray.get(oldIndex);
                if(previous == styleItem){
                    styleArray.setItem(oldIndex, null);
                }
                styleArray.setItem(value, styleItem);
            }
        }

        @Override
        public int get() {
            return styleItem.getIndex();
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
            if(parentClass.isInstance(styleItem)){
                return (T1) styleItem;
            }
            return null;
        }
    }

    public static final String NAME_spans = ObjectsUtil.of("spans");
}
