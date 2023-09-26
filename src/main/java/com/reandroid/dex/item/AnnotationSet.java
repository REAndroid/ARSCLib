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
package com.reandroid.dex.item;

import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationSet extends IntegerOffsetSectionList<AnnotationItem> implements SmaliFormat {

    public AnnotationSet(){
        super(SectionType.ANNOTATION);
    }

    public AnnotationItem get(AnnotationKey key){
        if(key == null){
            return null;
        }
        for(AnnotationItem item : this){
            if(key.equals(item.getKey())){
                return item;
            }
        }
        return null;
    }
    @Override
    public AnnotationKey getKey(){
        for(AnnotationItem item : this){
            AnnotationKey key = item.getKey();
            if(key != null){
                return key;
            }
        }
        return null;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        if(size() == 0){
            return;
        }
        for(AnnotationItem item : this){
            writer.newLine();
            item.append(writer);
        }
    }
    @Override
    public String toString() {
        if(getOffsetReference() == null){
            return super.toString();
        }
        int size = size();
        if(size == 0){
            return "EMPTY";
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce = false;
        for(AnnotationItem item : this){
            if(appendOnce){
                builder.append(',');
            }
            builder.append(item);
            appendOnce = true;
        }
        return builder.toString();
    }
}
