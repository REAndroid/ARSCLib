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
import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.FilterIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class AnnotationSet extends IntegerDataItemList<AnnotationItem>
        implements SmaliFormat, PositionAlignedItem {

    public AnnotationSet(){
        super(SectionType.ANNOTATION);
    }


    public AnnotationItem getOrCreate(TypeKey typeKey){
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

    public AnnotationItem getItemByType(TypeKey typeKey) {
        return CollectionUtil.getFirst(getItemsByType(typeKey));
    }
    public Iterator<AnnotationItem> getItemsByType(String typeName) {
        return FilterIterator.of(iterator(), new Predicate<AnnotationItem>() {
            @Override
            public boolean test(AnnotationItem item) {
                return typeName.equals(item.getTypeName());
            }
        });
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
    public AnnotationItem getOrCreate(AnnotationKey key){
        AnnotationItem item = get(key);
        if(item != null){
            return item;
        }
        item = addNew();
        item.setKey(key);
        return item;
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
        return null;
    }
    public AnnotationKey getKeyOld(){
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
    static class AnnotationSetBlockKey extends AnnotationKey {

        private final AnnotationSet annotationSet;

        public AnnotationSetBlockKey(AnnotationSet annotationSet) {
            super(null, null);
            this.annotationSet = annotationSet;
        }
        @Override
        public String getDefining() {
            for(AnnotationItem annotationItem : annotationSet){
                AnnotationKey key = annotationItem.getKey();
                if(key != null){
                    return key.getDefining();
                }
            }
            return null;
        }
        public boolean containsDefining(String defining) {
            if(defining == null){
                return false;
            }
            for(AnnotationItem annotationItem : annotationSet){
                AnnotationKey key = annotationItem.getKey();
                if(key != null && defining.equals(key.getDefining())){
                    return true;
                }
            }
            return false;
        }
        @Override
        public String getName() {
            for(AnnotationItem annotationItem : annotationSet){
                AnnotationKey key = annotationItem.getKey();
                if(key != null){
                    return key.getName();
                }
            }
            return null;
        }
        @Override
        public String[] getOtherNames() {
            for(AnnotationItem annotationItem : annotationSet){
                AnnotationKey key = annotationItem.getKey();
                if(key != null){
                    return key.getOtherNames();
                }
            }
            return null;
        }

        private int mHash;
        @Override
        public int hashCode() {
            if(mHash == 0 || mHash == 1){
                mHash = super.hashCode();
            }
            return mHash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AnnotationKey)) {
                return false;
            }
            AnnotationKey key = (AnnotationKey) obj;
            return containsDefining(key.getDefining()) &&
                    (Objects.equals(getName(), key.getName()) || containsName(key.getName()));
        }

        @Override
        public String toString() {
            return hashCode() + " " + super.toString();
        }
    }
}
