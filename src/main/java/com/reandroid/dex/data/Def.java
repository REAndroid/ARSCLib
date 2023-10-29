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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerVisitor;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.arsc.item.VisitableInteger;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class Def<T extends IdItem> extends DexContainerItem implements
        SmaliFormat, VisitableInteger, DefIndex {
    private final SectionType<T> sectionType;
    private final Ule128Item relativeId;
    private final Ule128Item accessFlags;
    private T mItem;
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
    void onRemove(){
        mCachedIndexUpdated = true;
        mItem = null;
        relativeId.set(0);
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

    public void addAnnotationSet(AnnotationSet annotationSet){
        AnnotationsDirectory directory = getOrCreateUniqueAnnotationsDirectory();
        addAnnotationSet(directory, annotationSet);
    }
    void addAnnotationSet(AnnotationsDirectory directory, AnnotationSet annotationSet){

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
    public AnnotationsDirectory getOrCreateUniqueAnnotationsDirectory(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getOrCreateUniqueAnnotationsDirectory();
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
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            ClassId classId = array.getClassId();
            if(classId != null){
                return classId;
            }
        }
        String className = getClassName();
        if(className == null){
            return null;
        }
        DexSectionPool<ClassId> pool = getPool(SectionType.CLASS_ID);
        if(pool == null){
            return null;
        }
        ClassId classId = pool.get(TypeKey.create(className));
        if(classId == null) {
            return null;
        }
        ClassData classData = getClassData();
        if(classData == null){
            return null;
        }
        classData.setClassId(classId);
        return classId;
    }
    public String getClassName(){
        return null;
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
    public boolean isFinal(){
        return AccessFlag.FINAL.isSet(getAccessFlagsValue());
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
    private ClassData getClassData(){
        DefArray<Def<T>> array = getParentArray();
        if(array != null){
            return array.getClassData();
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private DefArray<Def<T>> getParentArray() {
        return (DefArray<Def<T>>) getParent();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        cacheItem();
    }
    private void cacheItem(){
        this.mItem = get(sectionType, getDefinitionIndex());
        if(this.mItem != null){
            this.mItem.addUsageType(IdItem.USAGE_DEFINITION);
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
        item.addUsageType(IdItem.USAGE_DEFINITION);
    }
    void resetIndex(){
        mCachedIndexUpdated = false;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {

    }

    public Iterator<IdItem> usedIds(){
        return SingleIterator.of(getItem());
    }

    public void merge(Def<?> def){
        setItem(def.getKey());
        setAccessFlagsValue(def.getAccessFlagsValue());
    }
}
