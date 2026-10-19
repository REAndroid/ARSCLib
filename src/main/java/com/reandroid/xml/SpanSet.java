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

import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.Iterator;

public interface SpanSet<T extends Span> {

    Iterator<? extends T> getSpans();
    int getSpansCount();
    String getRawText();
    String getXml();
    default boolean hasSpans() {
        return getSpans().hasNext();
    }

    default boolean equalsSpans(SpanSet<?> spanSet) {
        if (spanSet == null) {
            return false;
        }
        if (spanSet == this) {
            return true;
        }
        if (this.getSpansCount() != spanSet.getSpansCount()) {
            return false;
        }
        if (!ObjectsUtil.equals(getRawText(), spanSet.getRawText())) {
            return false;
        }
        Iterator<? extends Span> iterator1 = getSpans();
        Iterator<? extends Span> iterator2 = spanSet.getSpans();
        while (iterator1.hasNext() && iterator2.hasNext()) {
            iterator1.next().equalsValue(iterator2.next());
        }
        return !iterator1.hasNext() && !iterator2.hasNext();
    }

    class Builder implements SpanSet<Span.Builder>, JSONConvert<Object> {

        private ArrayCollection<Span.Builder> spanList;

        private String rawText;

        public Builder() {

        }
        @Override
        public Iterator<Span.Builder> getSpans() {
            ArrayCollection<Span.Builder> spanList = this.spanList;
            if (spanList != null) {
                return spanList.iterator();
            }
            return EmptyIterator.of();
        }

        @Override
        public int getSpansCount() {
            ArrayCollection<Span.Builder> spanList = this.spanList;
            if (spanList != null) {
                return spanList.size();
            }
            return 0;
        }

        @Override
        public String getRawText() {
            return rawText;
        }
        public Builder setRawText(String rawText) {
            this.rawText = rawText;
            return this;
        }
        public Builder addAll(Iterator<? extends Span> iterator) {
            while (iterator.hasNext()) {
                add(iterator.next());
            }
            return this;
        }
        public Builder add(Span span) {
            if (span == null) {
                return this;
            }
            ArrayCollection<Span.Builder> spanList = this.spanList;
            if (spanList == null) {
                spanList = new ArrayCollection<>(4);
                this.spanList = spanList;
            }
            if (!spanList.containsExact(span)) {
                Span.Builder builder;
                if (span.getClass() == Span.Builder.class) {
                    builder = (Span.Builder) span;
                } else {
                    builder = new Span.Builder(span);
                }
                builder.setSpanOrder(spanList.size());
                spanList.add(builder);
            }
            return this;
        }
        public void clear() {
            this.spanList = null;
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
        public Object toJson() {
            String raw = getRawText();
            ArrayCollection<Span.Builder> spanList = this.spanList;
            if (spanList == null || spanList.size() == 0) {
                return raw;
            }
            JSONObject json = new JSONObject();
            json.put("string", raw);
            int size = spanList.size();
            JSONArray jsonArray = new JSONArray(size);
            for (int i = 0; i < size; i++) {
                jsonArray.put(spanList.get(i).toJson());
            }
            json.put("spans", jsonArray);
            return json;
        }

        @Override
        public void fromJson(Object obj) {
            clear();
            if (obj == null) {
                setRawText(null);
                return;
            }
            if (obj instanceof String) {
                setRawText((String) obj);
                return;
            }
            JSONObject json = (JSONObject) obj;
            setRawText(json.optString("string"));
            fromJsonSpans(json.optJSONArray("spans"));
        }
        public void fromJsonSpans(JSONArray jsonArray) {
            clear();
            if (jsonArray == null) {
                return;
            }
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                add(new Span.Builder(jsonArray.getJSONObject(i)));
            }
        }

        public static Builder of(SpanSet<?> spanSet) {
            if (spanSet == null) {
                return null;
            }
            if (!spanSet.hasSpans()) {
                return null;
            }
            Builder builder = new Builder();
            builder.setRawText(spanSet.getRawText());
            builder.addAll(spanSet.getSpans());
            return builder;
        }
        public static Builder of(JSONObject json) {
            if (json == null) {
                return null;
            }
            if (!json.has("string")) {
                return null;
            }
            return of(json.optString("string", null),
                    json.optJSONArray("spans"));
        }
        public static Builder of(String str, JSONArray jsonArray) {
            if (str == null && jsonArray == null) {
                return null;
            }
            Builder builder = new Builder();
            builder.setRawText(str);
            builder.fromJsonSpans(jsonArray);
            return builder;
        }
    }
}
