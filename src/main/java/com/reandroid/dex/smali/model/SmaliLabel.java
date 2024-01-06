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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliParseException;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;

public class SmaliLabel extends SmaliCode{

    private String label;

    public SmaliLabel(){
        super();
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public int getAddress(){
        SmaliCodeSet codeSet = getCodeSet();
        if(codeSet == null){
            return -1;
        }
        if(codeSet != getParent()){
            int i = codeSet.indexOf(this);
            if(i < 0){
                return -1;
            }
            SmaliLabel label = (SmaliLabel) codeSet.get(i);
            return label.getAddress();
        }
        Iterator<SmaliCode> iterator = codeSet.iterator(codeSet.indexOf(this) + 1);
        SmaliInstruction next = CollectionUtil.getFirst(
                InstanceIterator.of(iterator, SmaliInstruction.class));
        if(next != null){
            return next.getAddress();
        }
        return -1;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(':');
        writer.append(getLabel());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, ':');
        int i1 = reader.indexOfWhiteSpaceOrComment();
        int i2 = reader.indexOfBeforeLineEnd('}');
        int i;
        if(i2 >= 0 && i2 < i1){
            i = i2;
        }else {
            i = i1;
        }
        int length = i - reader.position();
        setLabel(reader.readString(length));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SmaliLabel)) {
            return false;
        }
        SmaliLabel other = (SmaliLabel) obj;
        return ObjectsUtil.equals(getLabel(), other.getLabel());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getLabel());
    }
}
