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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.index.IndexItemEntry;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class Def<T extends IndexItemEntry> extends DexContainerItem implements SmaliFormat, VisitableInteger {
    private final SectionType<T> sectionType;
    private final Ule128Item relativeId;
    private final Ule128Item accessFlags;
    private T mItem;
    private ClassId classId;
    public Def(int childesCount, SectionType<T> sectionType) {
        super(childesCount + 2);
        this.sectionType = sectionType;
        this.relativeId = new Ule128Item(true);
        this.accessFlags = new Ule128Item();
        addChild(0, relativeId);
        addChild(1, accessFlags);
    }

    @Override
    public void visitIntegers(IntegerVisitor visitor) {
    }

    public Iterator<AnnotationSet> getAnnotations(){
        return null;
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getAnnotationsDirectory();
        }
        return null;
    }
    public boolean appendAnnotations(SmaliWriter writer) throws IOException {
        boolean appendOnce = false;
        Iterator<AnnotationSet> iterator = getAnnotations();
        while (iterator.hasNext()){
            iterator.next().append(writer);
            appendOnce = true;
        }
        return appendOnce;
    }
    public ClassId getClassId() {
        return classId;
    }
    public void setClassId(ClassId classId) {
        this.classId = classId;
    }
    public int getRelativeIdValue() {
        return relativeId.get();
    }
    public int getAccessFlagsValue() {
        return accessFlags.get();
    }
    public boolean isPrivate(){
        return AccessFlag.PRIVATE.isSet(getAccessFlagsValue());
    }
    public boolean isNative(){
        return AccessFlag.NATIVE.isSet(getAccessFlagsValue());
    }
    public boolean isStatic(){
        return AccessFlag.STATIC.isSet(getAccessFlagsValue());
    }

    T getItem(){
        return mItem;
    }
    void setItem(T item) {
        this.mItem = item;
        updateIndex();
    }

    int getIdIndex() {
        DefArray<?> parentArray = getParentInstance(DefArray.class);
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return getRelativeIdValue() + previous.getIdIndex();
            }
        }
        return relativeId.get();
    }
    private int getPreviousIdIndex() {
        DefArray<?> parentArray = getParentInstance(DefArray.class);
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return previous.getIdIndex();
            }
        }
        return 0;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItem();
    }
    private void cacheItem(){
        this.mItem = get(sectionType, getIdIndex());
    }
    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateIndex();
    }
    private void updateIndex(){
        T item = this.mItem;
        if(item == null){
            return;
        }
        int index = getPreviousIdIndex();
        index = item.getIndex() - index;
        relativeId.set(index);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

}
