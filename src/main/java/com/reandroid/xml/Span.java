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

import com.reandroid.utils.ObjectsUtil;

public interface Span {

    String getTagName();
    int getFirstChar();
    int getLastChar();
    int getSpanOrder();
    String getSpanAttributes();

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
    String RAW_STYLE_TAG_ATTRIBUTE = ObjectsUtil.of("raw_style_tag_attribute");
}
