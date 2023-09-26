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
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.base.*;
import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

public class AnnotationItem extends DataItemEntry
        implements Iterable<AnnotationElement>, SmaliFormat {

    private final ByteItem visibility;
    private final SectionUle128Item<TypeId> typeId;
    private final Ule128Item elementsCount;
    private final BlockArray<AnnotationElement> annotationElements;

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
    }
    public AnnotationItem(){
        this(false);
    }

    public AnnotationElement getElement(String name){
        for(AnnotationElement element : this){
            if(name.equals(element.getName())){
                return element;
            }
        }
        return null;
    }
    @Override
    public AnnotationKey getKey(){
        String type = getTypeName();
        if(type == null){
            return null;
        }
        AnnotationElement[] elements = this.annotationElements.getChildes();
        if(elements == null || elements.length == 0){
            return null;
        }
        int length = elements.length;
        if(length == 1){
            return new AnnotationKey(type, elements[0].getName());
        }
        String[] names = new String[length];
        for(int i = 0; i < length; i++){
            names[i] = elements[i].getName();
        }
        return new AnnotationKey(type, names[0], null, names);
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

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        elementsCount.set(getElementsCount());
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
        } catch (IOException exception) {
        }
        return writer.toString();
    }
}
