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
package com.reandroid.dex.debug;

import com.reandroid.dex.index.StringData;
import com.reandroid.dex.sections.SectionType;

public class DebugSetSourceFile extends DebugElement {
    private final Base1Ule128Item<StringData> nameData;

    public DebugSetSourceFile() {
        super(1, DebugElementType.SET_SOURCE_FILE);
        this.nameData = new Base1Ule128Item<>(SectionType.STRING_DATA);
        addChild(1, nameData);
    }

    public StringData getName(){
        return nameData.getItem();
    }

    @Override
    public String toString() {
        StringData stringData = getName();
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(", ");
        if(stringData == null){
            builder.append("name index = ");
            builder.append(nameData.get());
        }else {
            builder.append('"');
            builder.append(stringData.getString());
            builder.append('"');
        }
        return builder.toString();
    }
}
