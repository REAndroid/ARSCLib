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
package com.reandroid.dex.value;

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Iterator;

public abstract class SectionIdValue<T extends IdItem> extends SectionValue<T> {

    public SectionIdValue(SectionType<T> sectionType, DexValueType<?> type) {
        super(sectionType, type);
    }
    public void setKey(Key key) {
        Section<T> section = getSection();
        if(section != null){
            set(section.getOrCreate(key));
        }
    }
    @Override
    public Key getKey(){
        T item = get();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    @Override
    int getSectionValue(T data){
        if(data != null){
            return data.getIndex();
        }
        return 0;
    }
    @Override
    T getSectionData(Section<T> section, int value){
        return section.get(value);
    }

    @Override
    void onDataRefreshed(T data){
        super.onDataRefreshed(data);
        addUsageType(data);
    }
    @Override
    void onDataUpdated(T data){
        super.onDataUpdated(data);
        addUsageType(data);
    }

    private void addUsageType(T data) {
        if(data != null){
            int usage;
            if(getParent(AnnotationElement.class) != null){
                usage = IdItem.USAGE_ANNOTATION;
            }else {
                usage = IdItem.USAGE_ENCODED_VALUE;
            }
            data.addUsageType(usage);
        }
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(get());
    }

}
