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

import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArraySort;

import java.util.Comparator;

public class SpanAttributesEncoder {

    public static String encodeAttributes(StyleElement element) {
        StyleAttribute[] attributes = getSortedAttributes(element);
        if (attributes == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        int length = attributes.length;
        String rawTagName = Span.RAW_STYLE_TAG_ATTRIBUTE;
        for (int i = 0; i < length; i++) {
            StyleAttribute attribute = attributes[i];
            if (!rawTagName.equals(attribute.getName())) {
                builder.append(';');
                builder.append(attribute.getName());
                builder.append('=');
            }
            builder.append(attribute.getValueAsString());
        }
        return builder.toString();
    }

    private static StyleAttribute[] getSortedAttributes(StyleElement element) {
        int count = element.getAttributeCount();
        if (count == 0) {
            return null;
        }
        StyleAttribute[] attributes = new StyleAttribute[count];
        for (int i = 0; i < count; i++) {
            attributes[i] = element.getAttributeAt(i);
        }
        if (count != 1) {
            ArraySort.sort(attributes, (Comparator<StyleAttribute>) (attribute1, attribute2) ->
                    CompareUtil.compare(attribute1.getName(), attribute2.getName()));
        }
        return attributes;
    }

}
