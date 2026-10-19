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
package com.reandroid.xml;

import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;

public interface Span {

    String getTagName();
    String getTagWithAttributes();
    int getFirstChar();
    int getLastChar();
    int getSpanOrder();
    String getSpanAttributes();

    default boolean equalsValue(Span span) {
        if (span == null) {
            return false;
        }
        if (span == this) {
            return true;
        }
        return getFirstChar() == span.getFirstChar()
                && getLastChar() == span.getLastChar()
                && ObjectsUtil.equals(getTagWithAttributes(), span.getTagWithAttributes());
    }
    default StyleElement toElement() {
        StyleElement element = new StyleElement();
        element.setName(getTagName());
        String attributes = getSpanAttributes();
        if (attributes != null) {
            new SpanAttributesDecoder(element, attributes).decode();
        }
        return element;
    }

    static String splitTagName(String raw) {
        if (raw == null) {
            return null;
        }
        int i = raw.indexOf(';');
        if (i < 0) {
            i = raw.indexOf(' ');
        }
        if (i < 0) {
            return raw;
        }
        return raw.substring(0, i);
    }
    static String splitAttribute(String tagWithAttribute) {
        if (tagWithAttribute == null || tagWithAttribute.length() == 0) {
            return null;
        }
        if (tagWithAttribute.charAt(0) == ' ') {
            tagWithAttribute = tagWithAttribute.trim();
        }
        int i = tagWithAttribute.indexOf(';');
        int i2 = tagWithAttribute.indexOf(' ');
        if (i < 0) {
            i = i2;
        } else if (i2 >= 0 && i2 < i) {
            i = i2;
        }
        if (i < 0) {
            return null;
        }
        String result = tagWithAttribute.substring(i + 1);
        if (result.length() == 0) {
            result = null;
        }
        return result;
    }

    class Builder implements Span {

        private String tag;
        private int first;
        private int last;
        private int order;

        public Builder(String tag, int first, int last) {
            this.tag = tag;
            this.first = first;
            this.last = last;
        }
        public Builder(JSONObject json) {
            this(json.optString(NAME_tag), json.optInt(NAME_first), json.optInt(NAME_last));
        }
        public Builder(Span span) {
            this(span.getTagWithAttributes(), span.getFirstChar(), span.getLastChar());
            this.order = span.getSpanOrder();
        }
        public Builder() {
            this(null, 0, 0);
        }

        @Override
        public String getTagName() {
            return Span.splitTagName(getTagWithAttributes());
        }
        @Override
        public String getTagWithAttributes() {
            return tag;
        }
        public Builder setTagWithAttributes(String tagWithAttributes) {
            this.tag = tagWithAttributes;
            return this;
        }
        @Override
        public int getFirstChar() {
            return first;
        }
        public Builder setFirstChar(int firstChar) {
            this.first = firstChar;
            return this;
        }
        @Override
        public int getLastChar() {
            return last;
        }
        public Builder setLastChar(int lastChar) {
            this.last = lastChar;
            return this;
        }
        @Override
        public int getSpanOrder() {
            return order;
        }
        public Builder setSpanOrder(int order) {
            this.order = order;
            return this;
        }
        @Override
        public String getSpanAttributes() {
            return Span.splitAttribute(getTagWithAttributes());
        }

        public void fromJson(JSONObject jsonObject) {
            if (jsonObject == null) {
                return;
            }
            setTagWithAttributes(jsonObject.optString(NAME_tag, getTagWithAttributes()));
            setFirstChar(jsonObject.optInt(NAME_first, getFirstChar()));
            setLastChar(jsonObject.optInt(NAME_last, getLastChar()));
        }
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put(NAME_tag, getTagWithAttributes());
            json.put(NAME_first, getFirstChar());
            json.put(NAME_last, getFirstChar());
            return json;
        }

        @Override
        public String toString() {
            return "SpanBuilder{" +
                    "tag='" + tag + '\'' +
                    ", first=" + first +
                    ", last=" + last +
                    '}';
        }
    }

    String RAW_STYLE_TAG_ATTRIBUTE = ObjectsUtil.of("raw_style_tag_attribute");

    String NAME_tag = ObjectsUtil.of("tag");
    String NAME_first = ObjectsUtil.of("first");
    String NAME_last = ObjectsUtil.of("last");

}
