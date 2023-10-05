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
import com.reandroid.dex.index.IdSectionEntry;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class Def<T extends IdSectionEntry> extends DexContainerItem implements
        SmaliFormat, VisitableInteger, DefIndex {
    private final SectionType<T> sectionType;
    private final Ule128Item relativeId;
    private final Ule128Item accessFlags;
    private T mItem;
    private ClassId classId;
    private int mCachedIndex;
    private boolean mCachedIndexUpdated;
    
    public Def(int childesCount, SectionType<T> sectionType) {
        super(childesCount + 2);
        this.sectionType = sectionType;
        this.relativeId = new Ule128Item(true);
        this.accessFlags = new Ule128Item();
        addChild(0, relativeId);
        addChild(1, accessFlags);
    }

    public void removeSelf(){
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            array.remove(this);
        }
    }
    public Key getKey(){
        T item = getItem();
        if(item != null){
            return item.getKey();
        }
        return null;
    }
    public void setKey(Key key){
        setItem(key);
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
    }

    public Iterator<AnnotationSet> getAnnotations(){
        AnnotationsDirectory directory = getAnnotationsDirectory();
        if(directory != null){
            return directory.getAnnotations(this);
        }
        return EmptyIterator.of();
    }
    public AnnotationsDirectory getAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getAnnotationsDirectory();
        }
        return null;
    }
    public AnnotationsDirectory getUniqueAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getUniqueAnnotationsDirectory();
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

    public void addAccessFlag(AccessFlag flag) {
        setAccessFlagsValue(getAccessFlagsValue() | flag.getValue());
    }
    public void addAccessFlags(AccessFlag flag1, AccessFlag flag2) {
        int value = getAccessFlagsValue();
        if(flag1 != null){
            value |= flag1.getValue();
        }
        if(flag2 != null){
            value |= flag2.getValue();
        }
        setAccessFlagsValue(value);
    }
    public void addAccessFlags(AccessFlag flag1, AccessFlag flag2, AccessFlag flag3) {
        int value = getAccessFlagsValue();
        if(flag1 != null){
            value |= flag1.getValue();
        }
        if(flag2 != null){
            value |= flag2.getValue();
        }
        if(flag3 != null){
            value |= flag3.getValue();
        }
        setAccessFlagsValue(value);
    }
    public void removeAccessFlag(AccessFlag accessFlag){
        setAccessFlagsValue(getAccessFlagsValue() & ~accessFlag.getValue());
    }
    public void setAccessFlagsValue(int value) {
        accessFlags.set(value);
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
    void setItem(Key key) {
        T item = getItem();
        if(item != null && key.equals(item.getKey())){
            return;
        }
        item = getSection(sectionType).getOrCreate(key);
        setItem(item);
    }
    void setItem(T item) {
        this.mItem = item;
        updateIndex();
    }

    @Override
    public int getDefinitionIndex() {
        if(!mCachedIndexUpdated){
            mCachedIndexUpdated = true;
            mCachedIndex = calculateDefinitionIndex();
        }
        return mCachedIndex;
    }
    private int calculateDefinitionIndex(){
        DefArray<?> parentArray = getParentArray();
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return getRelativeIdValue() + previous.getDefinitionIndex();
            }
        }
        return relativeId.get();
    }
    private int getPreviousIdIndex() {
        DefArray<?> parentArray = getParentArray();
        if(parentArray != null){
            Def<?> previous = parentArray.get(getIndex() - 1);
            if(previous != null){
                return previous.getDefinitionIndex();
            }
        }
        return 0;
    }
    @SuppressWarnings("unchecked")
    private DefArray<Def<T>> getParentArray() {
        return getParentInstance(DefArray.class);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItem();
    }
    private void cacheItem(){
        this.mItem = get(sectionType, getDefinitionIndex());
        if(this.mItem != null){
            this.mItem.addUsageType(IdSectionEntry.USAGE_DEFINITION);
        }
    }
    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        updateIndex();
    }
    private void updateIndex(){
        resetIndex();
        T item = this.mItem;
        if(item == null){
            return;
        }
        int index = getPreviousIdIndex();
        index = item.getIndex() - index;
        relativeId.set(index);
        item.addUsageType(IdSectionEntry.USAGE_DEFINITION);
    }
    void resetIndex(){
        mCachedIndexUpdated = false;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

}
