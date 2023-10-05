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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;

public class AnnotationItem extends DataSectionEntry
        implements Iterable<AnnotationElement>, SmaliFormat {

    private final ByteItem visibility;
    private final SectionUle128Item<TypeId> typeId;
    private final Ule128Item elementsCount;
    private final BlockArray<AnnotationElement> annotationElements;

    private final boolean mValueEntry;

    private final AnnotationItemKey mKey;

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
        this.typeId = new SectionUle128Item<>(SectionType.TYPE_ID);
        this.elementsCount = new Ule128Item();
        this.annotationElements = new CountedArray<>(elementsCount,
                AnnotationElement.CREATOR);
        int i = 0;
        if(!valueEntry){
            addChild(i++, visibility);
        }
        addChild(i++, typeId);
        addChild(i++, elementsCount);
        addChild(i, annotationElements);

        this.mKey = new AnnotationItemKey(this);
    }
    public AnnotationItem(){
        this(false);
    }

    public void remove(AnnotationElement element){
        annotationElements.remove(element);
        this.mKey.refresh();
    }
    public AnnotationElement getOrCreateElement(String name){
        AnnotationElement element = getElement(name);
        if(element != null){
            return element;
        }
        element = annotationElements.createNext();
        element.setName(name);
        this.mKey.refresh();
        return element;
    }
    public AnnotationElement getElement(String name){
        for(AnnotationElement element : this){
            if(name.equals(element.getName())){
                return element;
            }
        }
        return null;
    }
    public String[] getNames(){
        AnnotationElement[] elements = annotationElements.getChildes();
        int length = elements.length;
        if(length == 0){
            return null;
        }
        String[] results = new String[length];
        for(int i = 0; i < length; i++){
            results[i] = elements[i].getName();
        }
        return results;
    }
    @Override
    public AnnotationKey getKey(){
        AnnotationItemKey key = this.mKey;
        if(key.isValid()){
            return key;
        }
        return null;
    }
    public void setKey(AnnotationKey key){
        setType(key.getDefining());
        getOrCreateElement(key.getName());
        String[] names = key.getOtherNames();
        if(names != null){
            for(String name : names){
                getOrCreateElement(name);
            }
        }
        this.mKey.refresh();
    }
    @Override
    public Iterator<AnnotationElement> iterator(){
        return annotationElements.iterator();
    }
    public int getElementsCount(){
        return annotationElements.getCount();
    }
    public AnnotationElement getElement(int index){
        return annotationElements.get(index);
    }
    public boolean isValueEntry() {
        return mValueEntry;
    }
    public AnnotationVisibility getVisibility(){
        if(!isValueEntry()){
            return AnnotationVisibility.valueOf(visibility.unsignedInt());
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
        this.visibility.set((byte) visibility);
    }
    public int getVisibilityValue(){
        return this.visibility.unsignedInt();
    }
    public String getTypeName(){
        TypeId typeId = getTypeId();
        if(typeId != null){
            return typeId.getName();
        }
        return null;
    }
    public TypeId getTypeId(){
        return typeId.getItem();
    }
    public void setType(String type){
        typeId.setItem(new TypeKey(type));
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.elementsCount.set(getElementsCount());
        this.mKey.refresh();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        this.mKey.refresh();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        String tag = getTagName();
        writer.append('.');
        writer.append(tag);
        writer.append(' ');
        AnnotationVisibility visibility = getVisibility();
        if(visibility != null){
            writer.append(visibility.getName());
            writer.append(' ');
        }
        getTypeId().append(writer);
        Iterator<AnnotationElement> iterator = annotationElements.iterator();
        writer.indentPlus();
        while (iterator.hasNext()){
            writer.newLine();
            iterator.next().append(writer);
        }
        writer.indentMinus();
        writer.newLine();
        writer.append(".end ");
        writer.append(tag);
    }
    private String getTagName(){
        if(isValueEntry()){
            return "subannotation";
        }
        return "annotation";
    }
    @Override
    public String toString(){
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        try {
            this.append(smaliWriter);
            smaliWriter.close();
        } catch (Exception exception) {
            return exception.getMessage();
        }
        return writer.toString();
    }

    static class AnnotationItemKey extends AnnotationKey {

        private final AnnotationItem annotationItem;
        private boolean refreshed;
        private String defining;
        private String name;
        private String[] otherNames;

        public AnnotationItemKey(AnnotationItem annotationItem) {
            super(null, null);
            this.annotationItem = annotationItem;
        }
        @Override
        public String getDefining() {
            loadNames();
            return this.defining;
        }
        @Override
        public String getName() {
            loadNames();
            return this.name;
        }
        @Override
        public String[] getOtherNames() {
            loadNames();
            return this.otherNames;
        }
        private void loadNames() {
            if(refreshed){
                return;
            }
            this.refreshed = true;
            this.defining = annotationItem.getTypeName();
            String[] names = annotationItem.getNames();
            if(names == null || names.length == 0){
                this.name = null;
                this.otherNames = null;
                return;
            }
            int length = names.length;
            if(length > 1){
                Arrays.sort(names, CompareUtil.getComparableComparator());
            }else {
                this.name = names[0];
                this.otherNames = null;
                return;
            }
            this.name = names[0];
            String[] others = new String[length - 1];
            for(int i = 1; i < length; i++){
                others[i - 1] = names[i];
            }
            this.otherNames = others;
        }
        void refresh(){
            this.refreshed = false;
        }
        boolean isValid(){
            return getDefining() != null;
        }
    }
}
