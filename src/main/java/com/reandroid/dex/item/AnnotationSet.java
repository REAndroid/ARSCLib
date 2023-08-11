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

import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationSet extends IntegerList implements SmaliFormat {
    public AnnotationSet(){
        super();
    }
    public AnnotationItem[] toItemArray(){
        if(size() == 0){
            return null;
        }
        return getAt(SectionType.ANNOTATION, toArray());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        AnnotationItem[] annotations = toItemArray();
        if(annotations == null){
            return;
        }
        writer.newLine();
        for(int i = 0; i < annotations.length; i++){
            if(i != 0){
                writer.newLine();
            }
            annotations[i].append(writer);
        }
    }
    @Override
    public String toString() {
        if(getOffsetReference() == null){
            return super.toString();
        }
        int size = size();
        if(size == 0){
            return "";
        }
        SectionList sectionList = getParentInstance(SectionList.class);
        if(sectionList != null){
            Section<AnnotationItem> section = sectionList.get(SectionType.ANNOTATION);
            if(section != null){
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i < size; i++){
                    if(i != 0){
                        builder.append('\n');
                    }
                    builder.append(section.getAt(this.get(i)));
                }
                return builder.toString();
            }
        }
        return super.toString();
    }
}
