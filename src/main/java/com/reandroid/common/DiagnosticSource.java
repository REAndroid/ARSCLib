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
package com.reandroid.common;

import com.reandroid.utils.StringsUtil;

public class DiagnosticSource {

    private DiagnosticSource parent;
    private final String name;
    private String separator = ":";

    public DiagnosticSource(DiagnosticSource parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public DiagnosticSource getParent() {
        return parent;
    }
    public void setParent(DiagnosticSource parent) {
        this.parent = parent;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
    public String getSeparator() {
        String separator = this.separator;
        if(separator == null) {
            separator = StringsUtil.EMPTY;
        }
        return separator;
    }

    public boolean isRoot() {
        return getParent() == null;
    }
    public DiagnosticSource[] toArray() {
        int count = 0;
        DiagnosticSource source = this;
        while (!source.isRoot()) {
            count ++;
            source = source.getParent();
        }
        DiagnosticSource[] result = new DiagnosticSource[count];
        count = count - 1;
        source = this;
        while (count >= 0) {
            result[count] = source;
            source = source.getParent();
            count --;
        }
        return result;
    }

    public DiagnosticSource createChild(String name) {
        return new DiagnosticSource(this, name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        DiagnosticSource[] sources = toArray();
        int length = sources.length;
        for(int i = 0; i < length; i++) {
            DiagnosticSource source = sources[i];
            if(i != 0) {
                builder.append(source.getSeparator());
            }
            builder.append(source.getName());
        }
        return builder.toString();
    }

    public static DiagnosticSource newRoot() {
        return new DiagnosticSource(null, StringsUtil.EMPTY);
    }
}
