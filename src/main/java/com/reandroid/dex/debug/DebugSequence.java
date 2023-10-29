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

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.CreatorArray;
import com.reandroid.dex.base.FixedDexContainer;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.id.IdItem;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class DebugSequence extends FixedDexContainer implements Collection<DebugElement> {

    private final IntegerReference lineStart;
    private final BlockList<DebugElement> elementArray;
    private final DebugEndSequence endSequence;

    public DebugSequence(IntegerReference lineStart){
        super(2);
        this.lineStart = lineStart;
        this.elementArray = new BlockList<>();
        this.endSequence = new DebugEndSequence();
        addChild(0, elementArray);
        addChild(1, endSequence);
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
        if(size() < 2){
            return EmptyIterator.of();
        }
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
    public void cleanDuplicates(){
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
        boolean removed = removeInternal(element);
        if(removed){
            updateValues();
        }
        return removed;
    }
    private boolean removeInternal(DebugElement element){
        boolean removed = elementArray.remove(element);
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
        reader.seek(position);
        elementArray.ensureCapacity(count);
        int i = 0;
        DebugElement element = readNext(reader, i);
        while (element.getElementType() != DebugElementType.END_SEQUENCE){
            i++;
            element = readNext(reader, i);
        }
        elementArray.trimToSize();
        cacheValues();
    }

    private DebugElement readNext(BlockReader reader, int i) throws IOException {
        DebugElementType<?> type = DebugElementType.readFlag(reader);
        DebugElement debugElement;
        if(type == DebugElementType.END_SEQUENCE){
            debugElement = this.endSequence;
        }else {
            debugElement = type.newInstance();
            elementArray.add(debugElement);
        }
        debugElement.readBytes(reader);
        return debugElement;
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
        return elementArray.get(i);
    }
    public void add(int i, DebugElement element) {
        elementArray.add(i, element);
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
        return elementArray.getCount();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean contains(Object obj) {
        return elementArray.contains(obj);
    }
    @Override
    public Iterator<DebugElement> iterator() {
        return elementArray.iterator();
    }
    public Iterator<DebugElement> clonedIterator() {
        return elementArray.clonedIterator();
    }
    @Override
    public Object[] toArray() {
        return elementArray.toArray();
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
        return elementArray.add(element);
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
        for(DebugElement coming : collection){
            DebugElement element = createNext(coming.getElementType());
            element.merge(coming);
        }
        cacheValues();
        return collection.size() != 0;
    }
    @SuppressWarnings("unchecked")
    @Override
    public boolean removeAll(Collection<?> collection) {
        /*boolean removed = elementArray.re((Collection<DebugElement>) collection) != 0;
        if(removed){
            updateValues();
        }*/
        return false;
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public void clear() {
        elementArray.clearChildes();
    }


    public Iterator<IdItem> usedIds(){
        return new IterableIterator<DebugElement, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(DebugElement element) {
                return element.usedIds();
            }
        };
    }
    public void merge(DebugSequence sequence){
        this.lineStart.set(sequence.lineStart.get());
        int size = sequence.size();
        if(size > 1){
            size = 1;
        }
        elementArray.ensureCapacity(size);
        for(int i = 0; i < size; i++){
            DebugElement coming = sequence.get(i);
            DebugElement element = createNext(coming.getElementType());
            element.merge(coming);
        }
        cacheValues();
        elementArray.trimToSize();
    }

    @Override
    public int hashCode() {
        return elementArray.hashCode();
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
        return elementArray.equals(sequence.elementArray);
    }

    @Override
    public String toString() {
        return "start=" + lineStart + ", elements=" + elementArray;
    }

    private static final Creator<DebugElement> CREATOR = new Creator<DebugElement>() {
        @Override
        public DebugElement[] newInstance(int length) {
            return new DebugElement[length];
        }
        @Override
        public DebugElement newInstance() {
            return PLACE_HOLDER;
        }
    };

    private static final DebugElement PLACE_HOLDER = new DebugEndSequence();
}
