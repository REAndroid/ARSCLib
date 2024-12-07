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
package com.reandroid.dex.key;

import com.reandroid.utils.collection.ArrayCollection;

public class SignatureStringsBuilder {

    private final ArrayCollection<StringKey> results;
    private final StringBuilder stringBuilder;
    private boolean flushMarked;

    public SignatureStringsBuilder() {
        this.results = new ArrayCollection<>();
        this.stringBuilder = new StringBuilder();
    }

    public ArrayValueKey build() {
        ArrayCollection<StringKey> results = this.results;
        return ArrayValueKey.of(results.toArrayFill(new Key[results.size()]));
    }
    public void append(char c) {
        this.stringBuilder.append(c);
    }
    public void append(String text) {
        this.stringBuilder.append(text);
    }
    public void append(Object obj) {
        if (obj != null) {
            this.stringBuilder.append(obj);
        }
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }
    public void markFlush() {
        this.flushMarked = true;
    }
    public void flushPending() {
        if (flushMarked) {
            flush();
        }
    }
    public void flush() {
        flushMarked = false;
        StringBuilder builder = this.stringBuilder;
        int length = builder.length();
        if (length == 0) {
            return;
        }
        this.results.add(StringKey.create(builder.toString()));
        builder.delete(0, length);
    }
}
