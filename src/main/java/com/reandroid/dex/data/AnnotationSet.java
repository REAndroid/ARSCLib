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
package com.reandroid.dex.data;

import com.reandroid.dex.base.PositionAlignedItem;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.*;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationSet extends IntegerDataItemList<AnnotationItem>
        implements KeyItemCreate, SmaliFormat, PositionAlignedItem {

    private final DataKey<AnnotationSet> mKey;

    public AnnotationSet(){
        super(SectionType.ANNOTATION_ITEM);
        this.mKey = new DataKey<>(this);
    }

    @Override
    public DataKey<AnnotationSet> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key){
        DataKey<AnnotationSet> dataKey = (DataKey<AnnotationSet>) key;
        merge(dataKey.getItem());
    }

    public AnnotationItem getOrCreate(Key key){
        AnnotationItem item = getItemByKey(key);
        if(item != null){
            return item;
        }
        return addNew(key);
    }
    public AnnotationItem getOrCreateByType(TypeKey typeKey){
        AnnotationItem item = CollectionUtil.getFirst(getItemsByType(typeKey));
        if(item != null){
            return item;
        }
        return addNewItem(typeKey);
    }
    public AnnotationItem addNewItem(TypeKey typeKey){
        AnnotationItem item = addNew();
        item.setType(typeKey);
        return item;
    }

    public AnnotationItem getItemByKey(Key key) {
        for(AnnotationItem item : this){
            if(key.equals(item.getKey())){
                return item;
            }
        }
        return null;
    }
    public AnnotationItem getItemByType(TypeKey typeKey) {
        return CollectionUtil.getFirst(getItemsByType(typeKey));
    }
    public Iterator<AnnotationItem> getItemsByType(String typeName) {
        return FilterIterator.of(iterator(), item -> typeName.equals(item.getTypeName()));
    }
    public Iterator<AnnotationItem> getItemsByType(TypeKey typeKey) {
        return FilterIterator.of(iterator(), item -> item.equalsType(typeKey));
    }
    public boolean contains(String typeName) {
        for(AnnotationItem item : this){
            if(typeName.equals(item.getTypeName())){
                return true;
            }
        }
        return false;
    }
    public AnnotationItem getOrCreate(String type, String name){
        AnnotationItem item = get(type, name);
        if(item != null){
            return item;
        }
        return addNew(type, name);
    }
    public AnnotationItem addNew(String type, String name){
        AnnotationItem item = addNew();
        item.setType(type);
        item.getOrCreateElement(name);
        return item;
    }
    public AnnotationItem get(String type, String name){
        for (AnnotationItem item : this) {
            if (type.equals(item.getTypeName())
                    && item.containsName(name)) {
                return item;
            }
        }
        return null;
    }
    public AnnotationItem get(DataKey<AnnotationItem> key){
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

    public Iterator<IdItem> usedIds(){
        return new IterableIterator<AnnotationItem, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationItem element) {
                return element.usedIds();
            }
        };
    }
    public void merge(AnnotationSet annotationSet){
        if(annotationSet == this){
            return;
        }
        for(AnnotationItem coming : annotationSet){
            addNew(coming.getKey());
        }
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
