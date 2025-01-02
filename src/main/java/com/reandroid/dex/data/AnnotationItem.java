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
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.*;
import com.reandroid.dex.program.AnnotatedProgram;
import com.reandroid.dex.reference.Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.SmaliAnnotationElement;
import com.reandroid.dex.smali.model.SmaliAnnotationItem;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;


public class AnnotationItem extends DataItem
        implements Comparable<AnnotationItem>, Iterable<AnnotationElement>,
        KeyReference, SmaliRegion {

    private final ByteItem visibility;
    private final Ule128IdItemReference<TypeId> typeId;
    private final CountedBlockList<AnnotationElement> annotationElements;

    private final boolean mValueEntry;

    public AnnotationItem(boolean valueEntry) {
        super(valueEntry? 3 : 4);
        this.mValueEntry = valueEntry;
        ByteItem visibility;
        if(valueEntry){
            visibility = null;
        }else {
            visibility = new ByteItem();
        }
        this.visibility = visibility;
        this.typeId = new Ule128IdItemReference<>(SectionType.TYPE_ID, UsageMarker.USAGE_ANNOTATION);
        Ule128Item elementsCount = new Ule128Item();
        this.annotationElements = new CountedBlockList<>(
                AnnotationElement.CREATOR, elementsCount);
        int i = 0;
        if(!valueEntry){
            addChildBlock(i++, visibility);
        }

        addChildBlock(i++, typeId);
        addChildBlock(i++, elementsCount);
        addChildBlock(i, annotationElements);
    }
    public AnnotationItem(){
        this(false);
    }

    @Override
    public SectionType<AnnotationItem> getSectionType() {
        return SectionType.ANNOTATION_ITEM;
    }
    public boolean removeIf(org.apache.commons.collections4.Predicate<AnnotationElement> filter){
        return annotationElements.removeIf(filter);
    }
    public void remove(AnnotationElement element){
        annotationElements.remove(element);
    }
    public void clear() {
        annotationElements.clearChildes();
    }
    public AnnotationElement getOrCreateElement(String name){
        AnnotationElement element = getElement(name);
        if(element != null){
            return element;
        }
        element = createNewElement();
        element.setName(name);
        element.getOrCreateValue(DexValueType.NULL);
        return element;
    }
    public AnnotationElement createNewElement(){
        return annotationElements.createNext();
    }
    public boolean containsName(String name){
        for(AnnotationElement element : this){
            if(name.equals(element.getName())){
                return true;
            }
        }
        return false;
    }
    public DexValueBlock<?> getElementValue(String name){
        AnnotationElement element = getElement(name);
        if(element != null){
            return element.getValueBlock();
        }
        return null;
    }
    public AnnotationElement getElement(String name){
        for(AnnotationElement element : this){
            if(name.equals(element.getName())){
                return element;
            }
        }
        return null;
    }
    public AnnotationElementKey[] getElements() {
        int length = getElementsCount();
        AnnotationElementKey[] results = new AnnotationElementKey[length];
        for (int i = 0; i < length; i++) {
            results[i] = getElement(i).getKey();
        }
        return results;
    }
    public void addElement(AnnotationElementKey element) {
        createNewElement().setKey(element);
    }
    public String[] getNames(){
        BlockList<AnnotationElement> elements = annotationElements;
        int length = elements.size();
        if(length == 0){
            return null;
        }
        String[] results = new String[length];
        for(int i = 0; i < length; i++){
            results[i] = elements.get(i).getName();
        }
        return results;
    }
    @Override
    public AnnotationItemKey getKey(){
        return checkKey(AnnotationItemKey.create(getVisibility(), getType(), getElements()));
    }
    @Override
    public void setKey(Key key){
        AnnotationItemKey itemKey = (AnnotationItemKey) key;
        setVisibility(itemKey.getVisibility());
        setType(itemKey.getType());
        clear();
        for (AnnotationElementKey elementKey : itemKey) {
            addElement(elementKey);
        }
    }
    @Override
    public Iterator<AnnotationElement> iterator(){
        return annotationElements.iterator();
    }
    public Iterator<AnnotationElement> clonedIterator(){
        return annotationElements.clonedIterator();
    }
    public int getElementsCount(){
        return annotationElements.size();
    }
    public boolean isEmpty() {
        return annotationElements.size() == 0;
    }
    public AnnotationElement getElement(int index){
        return annotationElements.get(index);
    }
    public boolean isValueEntry() {
        return mValueEntry;
    }
    public AnnotationVisibility getVisibility(){
        if(!isValueEntry()){
            return AnnotationVisibility.valueOf(visibility.get());
        }
        return null;
    }
    public void setVisibility(AnnotationVisibility visibility){
        int value;
        if(visibility != null){
            value = visibility.getValue();
        }else {
            value = 0;
        }
        setVisibility(value);
    }
    public void setVisibility(int visibility){
        if(this.visibility != null){
            this.visibility.set((byte) visibility);
        }
    }
    public int getVisibilityValue(){
        if(this.visibility == null){
            return -1;
        }
        return this.visibility.get();
    }
    public TypeKey getType() {
        return (TypeKey) typeId.getKey();
    }
    public TypeId getTypeId(){
        return typeId.getItem();
    }
    public void setType(String type){
        setType(TypeKey.create(type));
    }
    public void setType(TypeKey typeKey){
        typeId.setKey(typeKey);
    }

    public void replaceKeys(Key search, Key replace) {
        AnnotationItemKey itemKey = getKey();
        AnnotationItemKey update = itemKey.replaceKey(search, replace);
        if (itemKey != update) {
            setKey(update);
        }
    }
    public Iterator<IdItem> usedIds(){
        TypeKey typeKey = getType();
        if(typeKey.getTypeName().startsWith("Ldalvik/annotation/")){
            return EmptyIterator.of();
        }
        return CombiningIterator.singleOne(getTypeId(),
                new IterableIterator<AnnotationElement, IdItem>(iterator()) {
                    @Override
                    public Iterator<IdItem> iterator(AnnotationElement element) {
                        return element.usedIds();
                    }
                });
    }

    @Override
    public void editInternal(Block user) {
        super.editInternal(user);
        // AnnotationElement are unique (not shared)
    }

    public AnnotatedProgram asAnnotated() {
        return new WarpedAnnotation(this);
    }

    public void merge(AnnotationItem annotationItem){
        if(annotationItem == this){
            return;
        }
        setVisibility(annotationItem.getVisibilityValue());
        setType(annotationItem.getType());
        annotationElements.ensureCapacity(annotationItem.getElementsCount());
        for(AnnotationElement coming : annotationItem){
            createNewElement().merge(coming);
        }
    }
    public void fromSmali(SmaliAnnotationItem smaliAnnotationItem){
        setType(smaliAnnotationItem.getType());
        setVisibility(smaliAnnotationItem.getVisibility());
        Iterator<SmaliAnnotationElement> iterator = smaliAnnotationItem.iterator();
        while (iterator.hasNext()){
            SmaliAnnotationElement smaliAnnotationElement = iterator.next();
            AnnotationElement element = createNewElement();
            element.fromSmali(smaliAnnotationElement);
        }
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendOptional(getVisibility());
        getTypeId().append(writer);
        writer.indentPlus();
        writer.appendAllWithDoubleNewLine(iterator());
        writer.indentMinus();
        getSmaliDirective().appendEnd(writer);
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        if(isValueEntry()){
            return SmaliDirective.SUB_ANNOTATION;
        }
        return SmaliDirective.ANNOTATION;
    }

    @Override
    public int compareTo(AnnotationItem other){
        if(other == null){
            return -1;
        }
        if(other == this){
            return 0;
        }
        int i = SectionTool.compareIdx(getTypeId(), other.getTypeId());
        if(i != 0) {
            return i;
        }
        return CompareUtil.compare(getVisibilityValue(), other.getVisibilityValue());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AnnotationItem item = (AnnotationItem) obj;
        if(!ObjectsUtil.equals(this.getType(), item.getType())){
            return false;
        }
        if(this.getVisibilityValue() != item.getVisibilityValue()){
            return false;
        }
        return this.annotationElements.equals(item.annotationElements);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(
                getVisibility(),
                getType(),
                annotationElements);
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append('@');
        builder.append(getType());
        boolean appendOnce = false;
        for(AnnotationElement element : this){
            if(appendOnce){
                builder.append(", ");
            }else {
                builder.append('(');
            }
            builder.append(element);
            appendOnce = true;
        }
        if(appendOnce){
            builder.append(')');
        }
        return builder.toString();
    }

    static class WarpedAnnotation implements AnnotatedProgram {

        private final AnnotationItem mItem;

        public WarpedAnnotation(AnnotationItem item) {
            this.mItem = item;
        }
        private AnnotationItemKey getItemKey() {
            if (!mItem.isRemoved()) {
                return mItem.getKey();
            }
            return null;
        }
        @Override
        public AnnotationSetKey getAnnotation() {
            AnnotationItemKey key = getItemKey();
            if (key != null) {
                return AnnotationSetKey.of(key);
            }
            return AnnotationSetKey.empty();
        }
        @Override
        public void setAnnotation(AnnotationSetKey annotationSet) {
            if (annotationSet.isEmpty()) {
                return;
            }
            if (mItem.isRemoved()) {
                throw new IllegalArgumentException("AnnotationItem was removed");
            }
            if (annotationSet.size() > 1) {
                throw new IllegalArgumentException("Multiple AnnotationItem");
            }
            mItem.setKey(annotationSet.get(0));
        }
        @Override
        public void clearAnnotations() {
            mItem.removeSelf();
        }
        @Override
        public boolean hasAnnotations() {
            return !mItem.isRemoved();
        }
        @Override
        public boolean hasAnnotation(TypeKey typeKey) {
            AnnotationItemKey itemKey = getItemKey();
            if (itemKey != null && itemKey.getType().equals(typeKey)) {
                return true;
            }
            return false;
        }
        @Override
        public AnnotationItemKey getAnnotation(TypeKey typeKey) {
            AnnotationItemKey itemKey = getItemKey();
            if (itemKey != null && itemKey.getType().equals(typeKey)) {
                return itemKey;
            }
            return null;
        }
        @Override
        public boolean removeAnnotation(TypeKey typeKey) {
            AnnotationItemKey itemKey = getItemKey();
            if (itemKey != null && itemKey.getType().equals(typeKey)) {
                clearAnnotations();
                return true;
            }
            return false;
        }
        @Override
        public boolean removeAnnotationIf(org.apache.commons.collections4.Predicate<? super AnnotationItemKey> predicate) {
            AnnotationItemKey itemKey = getItemKey();
            if (itemKey != null && predicate.evaluate(itemKey)) {
                clearAnnotations();
                return true;
            }
            return false;
        }
    }
}
