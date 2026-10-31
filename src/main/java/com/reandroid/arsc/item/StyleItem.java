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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockListWithFooter;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.common.Mergeable;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;
import com.reandroid.xml.Span;
import com.reandroid.xml.SpanSet;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleElement;
import com.reandroid.xml.StyleSpanEventSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public class StyleItem extends BlockListWithFooter<StyleSpan> implements
        SpanSet<StyleSpan>, Comparable<StyleItem>, Mergeable<SpanSet<?>>, JSONConvert<JSONArray> {

    private StyleIndexReference indexReference;
    private StringItem mStringItem;

    public StyleItem() {
        super(END_BLOCK, StyleSpan.CREATOR);
    }

    public void add(String tag, int first, int last) {
        createNext().set(tag, first, last);
    }
    public void add(Span span) {
        add(span.getTagWithAttributes(), span.getFirstChar(), span.getLastChar());
    }

    @Override
    public int getSpansCount() {
        return size();
    }
    @Override
    public boolean hasSpans() {
        return size() != 0;
    }
    @Override
    public Iterator<StyleSpan> getSpans() {
        return super.iterator();
    }

    @Override
    public String getRawText() {
        StringItem stringItem = getStringItemInternal();
        if (stringItem != null) {
            return stringItem.get();
        }
        return null;
    }

    @Override
    public String getXml() {
        String raw = getRawText();
        if (raw != null && hasSpans()) {
            StyleDocument document = StyleSpanEventSet.serialize(raw, this);
            if (document != null) {
                return document.getXml();
            }
        }
        return raw;
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        while (!isEndBlock(reader)) {
            createNext().readBytes(reader);
        }
        footer().readBytes(reader);
    }

    public StyleDocument build(String text) {
        return StyleSpanEventSet.serialize(text, this);
    }
    public void parse(StyleDocument document) {
        clearSpans();
        Iterator<StyleElement> iterator = document.getElements();
        while (iterator.hasNext()) {
            parse(iterator.next());
        }
    }
    public void parse(StyleElement element) {
        add(element.getTagWithAttributes(), element.getFirstChar(), element.getLastChar());
        Iterator<StyleElement> iterator = element.getElements();
        while (iterator.hasNext()) {
            parse(iterator.next());
        }
    }
    protected void clearStyle() {
        StringItem stringItem = getStringItemInternal();
        if (stringItem != null) {
            stringItem.unlinkStyleItemInternal(this);
        }
        clearSpans();
    }
    protected void clearSpans() {
        if (getParent() == null) {
            return;
        }
        Iterator<StyleSpan> iterator = clonedIterator();
        while (iterator.hasNext()) {
            iterator.next().onRemoved();
        }
        clearBlocks();
    }
    public void onRemoved() {
        clearStyle();
    }
    public void linkStringsInternal() {
        Iterator<StyleSpan> iterator = clonedIterator();
        while (iterator.hasNext()) {
            iterator.next().link();
        }
    }
    public void setStringItemInternal(StringItem stringItem) {
        if (stringItem == null) {
            StringItem exist = this.mStringItem;
            this.mStringItem = null;
            unLinkIndexReference(exist);
            return;
        }
        if (this.mStringItem != null) {
            if (stringItem == this.mStringItem) {
                return;
            }
            throw new IllegalStateException("Different string item");
        }
        this.mStringItem = stringItem;
        StyleIndexReference reference = new StyleIndexReference(this);
        stringItem.addReference(reference);
        this.indexReference = reference;
    }
    public StringItem getStringItemInternal() {
        return mStringItem;
    }
    public boolean isEmpty() {
        StringItem stringItem = getStringItemInternal();
        if (stringItem == null) {
            return true;
        }
        return !hasSpans();
    }

    private void unLinkIndexReference(StringItem stringItem) {
        StyleIndexReference reference = this.indexReference;
        if (reference == null) {
            return;
        }
        this.indexReference = null;
        if (stringItem == null) {
            return;
        }
        stringItem.removeReference(reference);
    }

    public String applyStyle(String text, boolean xml, boolean escapeXmlText) {
        if (text == null) {
            return null;
        }
        StyleDocument styleDocument = build(text);
        if (styleDocument == null) {
            return text;
        }
        return styleDocument.getText(xml, escapeXmlText);
    }
    @Override
    public void setNull(boolean is_null) {
        if (!is_null) {
            return;
        }
        clearStyle();
    }
    @Override
    public JSONArray toJson() {
        int size = getSpansCount();
        if (isNull() || size == 0) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(size);
        for (int i = 0; i < size; i++) {
            jsonArray.put(get(i).toJson());
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        clearSpans();
        if (json == null || json.length() == 0) {
            clearStyle();
            return;
        }
        int length = json.length();
        for (int i = 0; i < length; i++) {
            createNext().fromJson(json.getJSONObject(i));
        }
    }
    @Override
    public void merge(SpanSet<?> spanSet) {
        if (spanSet == null || spanSet == this) {
            return;
        }
        setSize(spanSet.getSpansCount());
        Iterator<? extends Span> iterator = spanSet.getSpans();
        int i = 0;
        while (iterator.hasNext()) {
            get(i).merge(iterator.next());
            i ++;
        }
    }

    @Override
    public int compareTo(StyleItem styleItem) {
        if (styleItem == this) {
            return 0;
        }
        if (styleItem == null) {
            return -1;
        }
        StringItem stringItem1 = this.getStringItemInternal();
        StringItem stringItem2 = styleItem.getStringItemInternal();
        int i = CompareUtil.compare(stringItem1 == null, stringItem2 == null);
        if (i != 0 || stringItem1 == null || stringItem2 == null) {
            return i;
        }
        return CompareUtil.compareUnsigned(stringItem1.getIndex(), stringItem2.getIndex());
    }

    @Override
    public String toString() {
        return "spans = " + getSpansCount();
    }

    private static boolean isEndBlock(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        int index = 0;
        while (index < 4 && reader.read() == 0xff) {
            index ++;
        }
        reader.seek(position);
        return index == 4;
    }
    static final class StyleIndexReference implements WeakStringReference {

        private final StyleItem styleItem;
        private int index;

        StyleIndexReference(StyleItem styleItem) {
            this.styleItem = styleItem;
            this.index = styleItem.getIndex();
        }
        @Override
        public void set(int value) {
            this.index = value;
        }
        @Override
        public int get() {
            return index;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T1 extends Block> T1 getReferredParent(Class<T1> parentClass) {
            if (parentClass.isInstance(styleItem)) {
                return (T1) styleItem;
            }
            return null;
        }
    }
    private static final EndBlock END_BLOCK = new EndBlock();

    static class EndBlock extends Block {

        private final byte[] bytes;

        public EndBlock() {
            super();
            byte b = (byte) 0xff;
            this.bytes = new byte[]{b, b, b, b};
        }


        @Override
        public int countBytes() {
            return 4;
        }
        @Override
        public byte[] getBytes() {
            return bytes;
        }
        @Override
        public void readBytes(BlockReader reader) throws IOException {
            reader.offset(4);
        }
        public int readBytes(InputStream inputStream) throws IOException {
            return inputStream.read(bytes.clone(), 0, 4);
        }
        @Override
        protected int onWriteBytes(OutputStream stream) throws IOException {
            stream.write(bytes, 0, 4);
            return 4;
        }
        @Override
        public String toString() {
            return "END";
        }
    }

    public static final Creator<StyleItem> CREATOR = StyleItem::new;

}
