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
package com.reandroid.dex.debug;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.id.IdItem;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

public class DebugSequence extends FixedDexContainer implements Collection<DebugElement> {

    private final IntegerReference lineStart;
    private BlockList<DebugElement> elementList;
    private final DebugEndSequence endSequence;

    public DebugSequence(IntegerReference lineStart){
        super(2);
        this.lineStart = lineStart;
        this.elementList = BlockList.empty();
        this.endSequence = DebugEndSequence.INSTANCE;
        addChild(0, elementList);
        addChild(1, endSequence);
    }

    public void removeInvalid(){
        elementList.remove(new Predicate<DebugElement>() {
            @Override
            public boolean test(DebugElement element) {
                if(element.isValid()){
                    return false;
                }
                return true;
            }
        });
    }
    public int getLineStart() {
        return lineStart.get();
    }
    public void setLineStart(int start) {
        if(start == getLineStart()){
            return;
        }
        setLineStartInternal(start);
        cacheValues();
    }
    void setLineStartInternal(int start){
        lineStart.set(start);
    }

    public Iterator<DebugElement> getExtraLines(){
        return new FilterIterator<>(iterator(),
                element -> (!(element instanceof DebugAdvance)));
    }
    public int getLast(){
        DebugLineNumber previous = null;
        Iterator<DebugLineNumber> iterator = iterator(DebugElementType.LINE_NUMBER);
        while (iterator.hasNext()){
            previous = iterator.next();
        }
        if(previous != null){
            return previous.getLineNumber();
        }
        return 0;
    }
    public void fixDebugLineNumbers(){
        Predicate<DebugElement> filter = new Predicate<DebugElement>() {
            DebugElement previous = null;
            @Override
            public boolean test(DebugElement element) {
                if (element.getElementType() != DebugElementType.LINE_NUMBER) {
                    return false;
                }
                if (previous != null && previous.getTargetAddress() == element.getTargetAddress()) {
                    previous = element;
                    return true;
                }
                previous = element;
                return false;
            }
        };
        removeAll(filter);
    }
    public boolean removeInvalid(InstructionList instructionList){
        return removeAll(element -> element.getElementType() == DebugElementType.LINE_NUMBER &&
                       instructionList.getAtAddress(element.getTargetAddress()) == null);
    }
    public boolean removeAll(Predicate<? super DebugElement> filter) {
        boolean removedOnce = false;
        Iterator<DebugElement> iterator = FilterIterator.of(clonedIterator(), filter);
        while (iterator.hasNext()){
            boolean removed = removeInternal(iterator.next());
            if(removed){
                removedOnce = true;
            }
        }
        if(removedOnce){
            updateValues();
        }
        return removedOnce;
    }
    public boolean removeAll(Iterator<? extends DebugElement> collection) {
        return removeAll(CollectionUtil.toList(collection));
    }
    public boolean remove(DebugElement element){
        if(element == null || element.getParent(getClass()) != this){
            return false;
        }
        boolean removed = removeInternal(element);
        if(removed){
            updateValues();
        }
        return removed;
    }
    private boolean removeInternal(DebugElement element){
        element.onPreRemove(this);
        boolean removed = getElementList().remove(element);
        if(removed){
            element.setParent(null);
            element.setIndex(-1);
        }
        return removed;
    }
    public<T1 extends DebugElement> T1 createAtPosition(DebugElementType<T1> type, int index){
        T1 element = type.newInstance();
        add(index, element);
        return element;
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DebugElement> T1 createNext(DebugElementType<T1> type){
        if(type == DebugElementType.END_SEQUENCE){
            return (T1) this.endSequence;
        }
        T1 element = type.newInstance();
        add(element);
        return element;
    }

    public DebugElement getAtAddress(int address){
        for(DebugElement element : this){
            if(address == element.getTargetAddress()){
                return element;
            }
        }
        return null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int position = reader.getPosition();
        int count = 0;
        while (reader.read() != 0){
            count++;
        }
        if(count == 0){
            return;
        }
        reader.seek(position);
        unlockElementList().ensureCapacity(count);
        DebugElementType<?> type = readNext(reader);
        while (!type.is(DebugElementType.END_SEQUENCE)){
            type = readNext(reader);
        }
        elementList.trimToSize();
        cacheValues();
    }

    private DebugElementType<?> readNext(BlockReader reader) throws IOException {
        DebugElementType<?> type = DebugElementType.readFlag(reader);
        DebugElement debugElement;
        if(type == DebugElementType.END_SEQUENCE){
            debugElement = this.endSequence;
        }else {
            debugElement = type.newInstance();
            unlockElementList().add(debugElement);
        }
        debugElement.readBytes(reader);
        return type;
    }

    private void cacheValues(){
        DebugElement previous = null;
        for(DebugElement element : this){
            element.cacheValues(this, previous);
            previous = element;
        }
    }
    private void updateValues(){
        DebugElement previous = null;
        Iterator<DebugElement> iterator = clonedIterator();
        while (iterator.hasNext()){
            DebugElement element = iterator.next();
            element.updateValues(this, previous);
            previous = element;
        }
    }

    public DebugElement get(int i){
        return getElementList().get(i);
    }
    public void add(int i, DebugElement element) {
        unlockElementList().add(i, element);
    }
    @SuppressWarnings("unchecked")
    public<T1 extends DebugElement> Iterator<T1> iterator(DebugElementType<T1> type) {
        return ComputeIterator.of(iterator(), element -> {
            if(element.getElementType() == type){
                return (T1) element;
            }
            return null;
        });
    }
    @Override
    public int size() {
        return getElementList().getCount();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean contains(Object obj) {
        return getElementList().contains(obj);
    }
    @Override
    public Iterator<DebugElement> iterator() {
        return getElementList().iterator();
    }
    public Iterator<DebugElement> clonedIterator() {
        return getElementList().clonedIterator();
    }
    @Override
    public Object[] toArray() {
        return getElementList().toArray();
    }
    @Override
    public <T> T[] toArray(T[] ts) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public boolean add(DebugElement element) {
        if(element == null){
            return false;
        }
        if(element.getClass() == DebugEndSequence.class){
            return false;
        }
        return unlockElementList().add(element);
    }
    @Override
    public boolean remove(Object obj) {
        return remove((DebugElement) obj);
    }
    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public boolean addAll(Collection<? extends DebugElement> collection) {
        if(collection.size() == 0){
            return false;
        }
        unlockElementList();
        for(DebugElement coming : collection){
            DebugElement element = createNext(coming.getElementType());
            element.merge(coming);
        }
        cacheValues();
        return collection.size() != 0;
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public void clear() {
        getElementList().clearChildes();
    }


    public Iterator<IdItem> usedIds(){
        return new IterableIterator<DebugElement, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(DebugElement element) {
                return element.usedIds();
            }
        };
    }

    private BlockList<DebugElement> unlockElementList() {
        BlockList<DebugElement> elementList = this.elementList;
        if(BlockList.isImmutableEmpty(elementList)){
            elementList = new BlockList<>();
            this.elementList = elementList;
            addChild(0, elementList);
        }
        return elementList;
    }
    private BlockList<DebugElement> getElementList() {
        return elementList;
    }

    public void merge(DebugSequence sequence){
        this.lineStart.set(sequence.lineStart.get());
        int size = sequence.size();
        if(size == 0){
            return;
        }
        unlockElementList().ensureCapacity(size);
        for(int i = 0; i < size; i++){
            DebugElement coming = sequence.get(i);
            DebugElement element = createNext(coming.getElementType());
            element.merge(coming);
        }
        cacheValues();
        getElementList().trimToSize();
    }

    @Override
    public int hashCode() {
        return elementList.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugSequence sequence = (DebugSequence) obj;
        return elementList.equals(sequence.elementList);
    }
    @Override
    public String toString() {
        return "start=" + lineStart + ", elements=" + elementList;
    }
}
