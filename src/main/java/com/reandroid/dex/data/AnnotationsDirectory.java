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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.dex.common.DefIndex;
import com.reandroid.dex.common.SectionItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.AnnotationGroupKey;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.AnnotationSetKey;
import com.reandroid.dex.key.DataKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.DataItemIndirectReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;

public class AnnotationsDirectory extends DataItem implements KeyReference {

    private final Header header;

    private final DirectoryMap<FieldDef, AnnotationSet> fieldsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationSet> methodsAnnotationMap;
    private final DirectoryMap<MethodDef, AnnotationGroup> parametersAnnotationMap;

    private final DataKey<AnnotationsDirectory> mKey;

    public AnnotationsDirectory() {
        super(4);
        this.header = new Header();

        this.fieldsAnnotationMap = new DirectoryMap<>(header.fieldCount, CREATOR_FIELDS);
        this.methodsAnnotationMap = new DirectoryMap<>(header.methodCount, CREATOR_METHODS);
        this.parametersAnnotationMap = new DirectoryMap<>(header.parameterCount, CREATOR_PARAMS);

        this.mKey = new DataKey<>(this);

        addChildBlock(0, header);

        addChildBlock(1, fieldsAnnotationMap);
        addChildBlock(2, methodsAnnotationMap);
        addChildBlock(3, parametersAnnotationMap);
    }

    @Override
    public DataKey<AnnotationsDirectory> getKey() {
        return mKey;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setKey(Key key) {
        DataKey<AnnotationsDirectory> dataKey = (DataKey<AnnotationsDirectory>) key;
        merge(dataKey.getItem());
    }

    @Override
    public SectionType<AnnotationsDirectory> getSectionType() {
        return SectionType.ANNOTATION_DIRECTORY;
    }

    @Override
    public boolean isBlank() {
        return isEmpty();
    }
    public boolean isEmpty() {
        AnnotationSet annotationSet = header.classAnnotation.getItem();
        if (annotationSet != null && !annotationSet.isEmpty()) {
            return false;
        }
        return fieldsAnnotationMap.isEmpty() &&
                methodsAnnotationMap.isEmpty() &&
                parametersAnnotationMap.isEmpty();
    }

    public void sortFields() {
        fieldsAnnotationMap.sort();
    }
    public void sortMethods() {
        methodsAnnotationMap.sort();
        parametersAnnotationMap.sort();
    }
    public void link(Def<?> def) {
        if (def instanceof FieldDef) {
            fieldsAnnotationMap.link((FieldDef) def);
        } else if (def instanceof MethodDef) {
            MethodDef methodDef = (MethodDef) def;
            methodsAnnotationMap.link(methodDef);
            parametersAnnotationMap.link(methodDef);
        }
    }
    public void clear(DefIndex defIndex) {
        if (defIndex instanceof ClassId) {
            header.classAnnotation.setKey(null);
        } else if (defIndex instanceof FieldDef) {
            fieldsAnnotationMap.remove((FieldDef) defIndex);
        } else if (defIndex instanceof MethodDef) {
            MethodDef methodDef = (MethodDef) defIndex;
            methodsAnnotationMap.remove(methodDef);
            parametersAnnotationMap.remove(methodDef);
        }
    }
    public AnnotationSetKey get(DefIndex defIndex) {
        if (defIndex instanceof ClassId) {
            AnnotationSetKey key = (AnnotationSetKey) header.classAnnotation.getKey();
            if (key == null) {
                key = AnnotationSetKey.empty();
            }
            return key;
        }
        Iterator<?> result;
        if (defIndex instanceof FieldDef) {
            result = fieldsAnnotationMap.getValueKeys((FieldDef) defIndex);
        } else if (defIndex instanceof MethodDef) {
            result = methodsAnnotationMap.getValueKeys((MethodDef) defIndex);
        } else if (defIndex instanceof MethodParameterDef) {
            return getParameter((MethodParameterDef) defIndex);
        } else {
            result = EmptyIterator.of();
        }
        return AnnotationSetKey.combine(ObjectsUtil.cast(result));
    }
    public void put(DefIndex defIndex, AnnotationSetKey value) {
        if (value != null && value.isEmpty()) {
            value = null;
        }
        if (defIndex instanceof ClassId) {
            header.classAnnotation.setKey(value);
        } else if (defIndex instanceof FieldDef) {
            fieldsAnnotationMap.put((FieldDef) defIndex, value);
        } else if (defIndex instanceof MethodDef) {
            methodsAnnotationMap.put((MethodDef) defIndex, value);
        } else if (defIndex instanceof MethodParameterDef) {
            setParameter((MethodParameterDef) defIndex, value);
        }
    }
    public void remove(DefIndex defIndex) {
        put(defIndex, null);
    }
    public AnnotationItemKey getAnnotation(DefIndex defIndex, TypeKey typeKey) {
        AnnotationItem item = getAnnotationItem(defIndex, typeKey);
        if (item != null) {
            return item.getKey();
        }
        return null;
    }
    public Key getAnnotationValue(DefIndex defIndex, TypeKey typeKey, String name) {
        AnnotationItem item = getAnnotationItem(defIndex, typeKey);
        if (item != null) {
            AnnotationElement element = item.getElement(name);
            if (element != null) {
                return element.getValue();
            }
        }
        return null;
    }
    private AnnotationItem getAnnotationItem(DefIndex defIndex, TypeKey typeKey) {
        if (defIndex instanceof ClassId) {
            AnnotationSet set = header.classAnnotation.getItem();
            if (set != null) {
                return set.get(typeKey);
            }
            return null;
        }
        Iterator<AnnotationSet> iterator;
        if (defIndex instanceof FieldDef) {
            iterator = fieldsAnnotationMap.getValues((FieldDef) defIndex);
        } else if (defIndex instanceof MethodDef) {
            iterator = methodsAnnotationMap.getValues((MethodDef) defIndex);
        } else if (defIndex instanceof MethodParameterDef) {
            MethodParameterDef parameter = (MethodParameterDef) defIndex;
            int index = parameter.getDefinitionIndex();
            iterator = ComputeIterator.of(
                    parametersAnnotationMap.getValues(parameter.getMethodDef()),
                    group -> group.getItem(index));
        } else {
            iterator = EmptyIterator.of();
        }
        while (iterator.hasNext()) {
            AnnotationItem item = iterator.next().get(typeKey);
            if (item != null) {
                return item;
            }
        }
        return null;
    }
    public AnnotationSetKey getParameter(MethodParameterDef parameter) {
        int index = parameter.getDefinitionIndex();
        return AnnotationSetKey.combine(ComputeIterator.of(
                parametersAnnotationMap.getValues(parameter.getMethodDef()),
                group -> group.getItemKey(index)));
    }
    public void setParameter(MethodParameterDef parameter, AnnotationSetKey key) {
        setParameters(
                parameter.getMethodDef(),
                getParameters(parameter.getMethodDef())
                        .set(parameter.getDefinitionIndex(), key)
        );
    }
    public AnnotationGroupKey getParameters(MethodDef methodDef) {
        return AnnotationGroupKey.combine(ComputeIterator.of(
                parametersAnnotationMap.getValues(methodDef),
                AnnotationGroup::getKey));
    }
    public void setParameters(MethodDef methodDef, AnnotationGroupKey key) {
        if (key == null || key.isBlank()) {
            key = null;
        } else {
            key = key.setParametersCount(methodDef.getParametersCount());
        }
        parametersAnnotationMap.put(methodDef, key);
    }
    public boolean contains(DefIndex defIndex) {
        if (defIndex instanceof ClassId) {
            AnnotationSet set = header.classAnnotation.getItem();
            return set != null && !set.isEmpty();
        }
        if (defIndex instanceof FieldDef) {
            return fieldsAnnotationMap.contains((FieldDef) defIndex);
        } else if (defIndex instanceof MethodDef) {
            return methodsAnnotationMap.contains((MethodDef) defIndex);
        } else if (defIndex instanceof MethodParameterDef) {
            Iterator<AnnotationGroup> iterator = parametersAnnotationMap.getValues(
                    ((MethodParameterDef) defIndex).getMethodDef());
            int index = defIndex.getDefinitionIndex();
            while (iterator.hasNext()) {
                AnnotationGroup groupBlock = iterator.next();
                if (!groupBlock.isEmptyAt(index)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public void replaceKeys(Key search, Key replace) {
        AnnotationSet set = header.classAnnotation.getItem();
        if (set != null) {
            set.replaceKeys(search, replace);
        }
        Iterator<AnnotationSet> iterator = fieldsAnnotationMap.getValues();
        while (iterator.hasNext()) {
            iterator.next().replaceKeys(search, replace);
        }
        iterator = methodsAnnotationMap.getValues();
        while (iterator.hasNext()) {
            iterator.next().replaceKeys(search, replace);
        }
        Iterator<AnnotationGroup> groupIterator = parametersAnnotationMap.getValues();
        while (groupIterator.hasNext()) {
            groupIterator.next().replaceKeys(search, replace);
        }
    }

    @Override
    public void editInternal(Block user) {
        this.header.editInternal(user);
        this.fieldsAnnotationMap.editInternal();
        this.parametersAnnotationMap.editInternal();
    }

    public Iterator<IdItem> usedIds() {
        AnnotationSet classAnnotation = header.classAnnotation.getItem();
        Iterator<IdItem> iterator1;
        if (classAnnotation == null) {
            iterator1 = EmptyIterator.of();
        } else {
            iterator1 = classAnnotation.usedIds();
        }
        Iterator<IdItem> iterator2 = new IterableIterator<AnnotationSet, IdItem>(
                fieldsAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
        Iterator<IdItem> iterator3 = new IterableIterator<AnnotationSet, IdItem>(
                methodsAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationSet element) {
                return element.usedIds();
            }
        };
        Iterator<IdItem> iterator4 = new IterableIterator<AnnotationGroup, IdItem>(
                parametersAnnotationMap.getValues()) {
            @Override
            public Iterator<IdItem> iterator(AnnotationGroup element) {
                return element.usedIds();
            }
        };
        return CombiningIterator.four(
                iterator1,
                iterator2,
                iterator3,
                iterator4
        );
    }

    @Override
    protected void onPreRefresh() {
        header.refresh();
        super.onPreRefresh();
    }

    public void merge(AnnotationsDirectory directory) {
        if (directory == this) {
            return;
        }
        header.merge(directory.header);
        fieldsAnnotationMap.merge(directory.fieldsAnnotationMap);
        methodsAnnotationMap.merge(directory.methodsAnnotationMap);
        parametersAnnotationMap.merge(directory.parametersAnnotationMap);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnnotationsDirectory directory = (AnnotationsDirectory) obj;
        return ObjectsUtil.equals(header, directory.header) &&
                ObjectsUtil.equals(fieldsAnnotationMap, directory.fieldsAnnotationMap) &&
                ObjectsUtil.equals(methodsAnnotationMap, directory.methodsAnnotationMap) &&
                ObjectsUtil.equals(parametersAnnotationMap, directory.parametersAnnotationMap);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + header.hashCode();
        hash = hash * 31 + fieldsAnnotationMap.hashCode();
        hash = hash * 31 + methodsAnnotationMap.hashCode();
        hash = hash * 31 + parametersAnnotationMap.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return  header +
                ", fields=" + fieldsAnnotationMap +
                ", methods=" + methodsAnnotationMap +
                ", parameters=" + parametersAnnotationMap;
    }

    static class Header extends SectionItem implements BlockRefresh {

        final DataItemIndirectReference<AnnotationSet> classAnnotation;
        final IndirectInteger fieldCount;
        final IndirectInteger methodCount;
        final IndirectInteger parameterCount;

        public Header() {
            super(16);

            this.classAnnotation = new DataItemIndirectReference<>(SectionType.ANNOTATION_SET,
                    this, 0, UsageMarker.USAGE_ANNOTATION);
            this.fieldCount = new IndirectInteger(this, 4);
            this.methodCount = new IndirectInteger(this, 8);
            this.parameterCount = new IndirectInteger(this, 12);
        }

        public boolean isEmpty() {
            return classAnnotation.get() == 0
                    && fieldCount.get() == 0
                    && methodCount.get() == 0
                    && parameterCount.get() == 0;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            super.onReadBytes(reader);
            cacheItems();
        }
        private void cacheItems() {
            this.classAnnotation.pullItem();
            this.classAnnotation.addUniqueUser(this);
        }

        @Override
        public void refresh() {
            this.classAnnotation.refresh();
            this.classAnnotation.addUniqueUser(this);
        }

        public void merge(Header header) {
            classAnnotation.setKey(header.classAnnotation.getKey());
        }

        @Override
        public void editInternal(Block user) {
            classAnnotation.getUniqueItem(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Header header = (Header) obj;
            return ObjectsUtil.equals(classAnnotation.getItem(), header.classAnnotation.getItem());
        }

        @Override
        public int hashCode() {
            return 31 + ObjectsUtil.hash(classAnnotation.getItem());
        }

        @Override
        public String toString() {
            return  "class=" + classAnnotation +
                    ", fields=" + fieldCount +
                    ", methods=" + methodCount +
                    ", parameters=" + parameterCount;
        }

    }

    private static final Creator<DirectoryEntry<FieldDef, AnnotationSet>> CREATOR_FIELDS = () ->
            new DirectoryEntry<>(SectionType.ANNOTATION_SET);
    private static final Creator<DirectoryEntry<MethodDef, AnnotationSet>> CREATOR_METHODS = () ->
            new DirectoryEntry<>(SectionType.ANNOTATION_SET);
    private static final Creator<DirectoryEntry<MethodDef, AnnotationGroup>> CREATOR_PARAMS = () ->
            new DirectoryEntry<>(SectionType.ANNOTATION_GROUP);
}
