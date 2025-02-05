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

import com.reandroid.utils.StringsUtil;

public class SpanAttributesDecoder {

    private final StyleElement element;
    private final String encodedAttributes;
    private final int length;
    private boolean error;
    private int index;

    public SpanAttributesDecoder(StyleElement element, String encodedAttributes) {
        this.element = element;
        this.encodedAttributes = encodedAttributes;
        this.length = encodedAttributes.length();
    }

    public boolean decode() {
        reset();
        while (true) {
            if (!parseNext()) {
                break;
            }
        }
        return error;
    }
    private boolean parseNext() {
        String name = pickAttributeName();
        String value = pickAttributeValue();
        if (name == null && value == null) {
            return false;
        }
        if (name == null || value == null) {
            StyleAttribute last = getLastAttribute();
            if (last != null) {
                StringBuilder builder = new StringBuilder();
                builder.append(last.getValueAsString());
                builder.append(';');
                if (name == null) {
                    builder.append(value);
                } else {
                    builder.append(name);
                    builder.append('=');
                }
                last.setValue(builder.toString());
            } else {
                onError();
                return false;
            }
        } else {
            StyleAttribute attribute = new StyleAttribute();
            attribute.set(name, value);
            element.addAttribute(attribute);
        }
        return true;
    }
    private void onError() {
        element.clearAttributes();
        index = length;
        error = true;
        StyleAttribute attribute = new StyleAttribute();
        attribute.set(Span.RAW_STYLE_TAG_ATTRIBUTE, encodedAttributes);
        element.addAttribute(attribute);
    }
    private StyleAttribute getLastAttribute() {
        int count = element.getAttributeCount();
        if (count != 0) {
            return element.getAttributeAt(count - 1);
        }
        return null;
    }
    private String pickAttributeName() {
        int length = this.length - 1;
        for (int i = index; i < length; i++) {
            char c = encodedAttributes.charAt(i);
            if (c == '=') {
                String name = encodedAttributes.substring(index, i);
                if (isAttributeName(name)) {
                    index = i + 1;
                    return name;
                }
                return null;
            }
        }
        return null;
    }
    private String pickAttributeValue() {
        int length = this.length - 1;
        for (int i = index; i < length; i++) {
            char c = encodedAttributes.charAt(i);
            if (c == ';') {
                String result = encodedAttributes.substring(index, i);
                index = i + 1;
                return result;
            }
        }
        length = this.length;
        if (index < length) {
            int i = index;
            index = length;
            return encodedAttributes.substring(i);
        }
        return null;
    }
    private void reset() {
        element.clearAttributes();
        index = 0;
        error = false;
    }
    private boolean isAttributeName(String name) {
        int length = name.length();
        if (length == 0) {
            return false;
        }
        if (!isFirstAttributeChar(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < length; i++) {
            if (!isAttributeChar(name.charAt(i))) {
                return false;
            }
        }
        StyleAttribute last = getLastAttribute();
        if (last != null) {
            if (name.compareTo(last.getName()) < 0) {
                return false;
            }
        }
        return true;
    }
    private boolean isFirstAttributeChar(char c) {
        return StringsUtil.isAz(c);
    }
    private boolean isAttributeChar(char c) {
        if (StringsUtil.isAzOrDigits(c)) {
            return true;
        }
        return c == '_';
    }
}
