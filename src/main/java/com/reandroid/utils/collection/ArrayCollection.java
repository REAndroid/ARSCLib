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
package com.reandroid.utils.collection;

import com.reandroid.common.ArraySupplier;

import java.util.*;
import java.util.function.Predicate;

public class ArrayCollection<T> implements ArraySupplier<T>, Iterable<T>, Collection<T> {

    private Object[] mElements;
    private int size;
    private int mLastGrow;

    public ArrayCollection(int initialCapacity){
        Object[] elements;
        if(initialCapacity == 0){
            elements = EMPTY;
        }else {
            elements = new Object[initialCapacity];
        }
        this.mElements = elements;
        this.size = 0;
    }
    public ArrayCollection(T[] elements){
        this.mElements = elements;
        this.size = elements.length;
    }
    public ArrayCollection(){
        this(0);
    }

    @SuppressWarnings("unchecked")
    public void sort(Comparator<? extends T> comparator){
        trimToSize();
        if(size() < 2){
            return;
        }
        Comparator<? super Object> cmp = (Comparator<? super Object>) comparator;
        Arrays.sort(mElements, cmp);
    }

    public Object[] getElements() {
        trimToSize();
        return mElements;
    }

    @Override
    public boolean contains(Object obj) {
        if(obj == null){
            return false;
        }
        Object[] elements = this.mElements;
        if(elements == null){
            return false;
        }
        for(Object element : elements){
            if(obj.equals(element)){
                return true;
            }
        }
        return false;
    }
    public boolean isEmpty(){
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get(int i){
        Object[] elements = this.mElements;
        if(elements != null){
            return (T) elements[i];
        }
        return null;
    }
    public int size(){
        return size;
    }
    @Override
    public int getCount() {
        return size();
    }
    @Override
    public Iterator<T> iterator() {
        return ArraySupplierIterator.of(this);
    }

    @Override
    public Object[] toArray() {
        trimToSize();
        return this.mElements;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T1> T1[] toArray(T1[] out) {
        int size = this.size();
        if(size == 0){
            return out;
        }
        Object[] elements = this.mElements;
        int length = out.length;
        if (length > 0 && length <= size) {
            for(int i = 0; i < length; i++){
                out[i] = (T1)elements[i];
            }
            return out;
        }
        return (T1[]) Arrays.copyOf(elements, size, out.getClass());
    }

    @SuppressWarnings("unchecked")
    public T removeItem(Object item){
        if (item == null){
            return null;
        }
        Object[] elements = this.mElements;
        if(elements == null){
            return null;
        }
        int length = this.size;
        if(length == 0){
            return null;
        }
        T result = null;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(obj == null){
                continue;
            }
            if(item.equals(obj)){
                elements[i] = null;
                result = (T) obj;
                break;
            }
        }
        if(result == null){
            return null;
        }
        this.size --;
        Object[] update = new Object[this.size];
        int count = 0;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(obj == null){
                continue;
            }
            update[count] = obj;
            count++;
        }
        this.size = count;
        this.mElements = update;
        return result;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for(Object obj : collection){
            if(!contains(obj)){
                return false;
            }
        }
        return !collection.isEmpty();
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        int size = this.size();
        if(size == 0){
            this.mElements = collection.toArray();
            this.size = mElements.length;
            return true;
        }
        size = collection.size();
        boolean result = false;
        for (T item : collection) {
            if (availableCapacity() == 0) {
                ensureCapacity(size);
            }
            boolean added = add(item);
            if (added) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        Object[] elements = this.mElements;
        if(elements == null){
            return false;
        }
        int length = this.size;
        if(length == 0){
            return false;
        }
        int result = 0;
        for(Object obj : collection){
            if(length == result){
                break;
            }
            for(int i = 0; i < length; i++){
                Object item = elements[i];
                if(item == obj){
                    elements[i] = null;
                    result ++;
                }
            }
        }
        if(result == 0){
            return false;
        }
        this.size -= result;
        if(this.size == 0){
            this.mElements = EMPTY;
            return true;
        }
        Object[] update = new Object[this.size];
        int count = 0;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(obj == null){
                continue;
            }
            update[count] = obj;
            count++;
        }
        this.size = count;
        this.mElements = update;
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {
        this.mElements = EMPTY;
        this.mLastGrow = 0;
    }


    @Override
    public boolean remove(Object obj) {
        return removeItem(obj) != null;
    }
    @SuppressWarnings("unchecked")
    public void remove(Predicate<? super T> filter){
        Object[] elements = this.mElements;
        if(elements == null){
            return;
        }
        int length = this.size;
        if(length == 0){
            return;
        }
        int result = 0;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(obj == null){
                continue;
            }
            if(filter.test((T)obj)){
                elements[i] = null;
                result ++;
            }
        }
        if(result == 0){
            return;
        }
        this.size -= result;
        if(this.size == 0){
            this.mElements = EMPTY;
            return;
        }
        Object[] update = new Object[this.size];
        int count = 0;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(obj == null){
                continue;
            }
            update[count] = obj;
            count++;
        }
        this.size = count;
        this.mElements = update;
    }
    public void add(Iterator<? extends T> iterator){
        while (iterator.hasNext()){
            add(iterator.next());
        }
    }
    @Override
    public boolean add(T item){
        if (item == null || replace(item)){
            return false;
        }
        ensureCapacity();
        this.mElements[size] = item;
        this.size ++;
        return true;
    }
    public boolean replace(T item){
        if (item == null){
            return false;
        }
        Object[] elements = this.mElements;
        int length = this.size;
        for(int i = 0; i < length; i++){
            Object obj = elements[i];
            if(item == obj){
                elements[i] = item;
                return true;
            }
        }
        return false;
    }
    public void trimToSize(){
        if(availableCapacity() == 0){
            return;
        }
        int size = this.size;
        if(size == 0){
            this.mElements = EMPTY;
            return;
        }
        Object[] update = new Object[size];
        System.arraycopy(this.mElements, 0, update, 0, size);
        this.mElements = update;
    }
    private void ensureCapacity(){
        if(availableCapacity() > 0){
            return;
        }
        int amount;
        if(this.size == 0){
            amount = 1;
        }else {
            amount = this.mLastGrow;
            if(amount == 0){
                amount = 1;
            }
            amount = amount << 1;
            if(amount > 32){
                amount = amount << 1;
            }
            if(amount > 32 && amount < 256){
                amount = amount << 1;
            }
            if((amount & 0xffff0000) != 0){
                amount = 0xffff;
            }
            mLastGrow = amount;
            if(this.size < 4){
                amount = 1;
            }
        }
        ensureCapacity(amount);
    }
    public void ensureCapacity(int capacity) {
        if(availableCapacity() >= capacity){
            return;
        }
        int size = this.size;
        int length = size + capacity;
        Object[] update = new Object[length];
        Object[] elements = this.mElements;
        if(elements.length == 0 || size == 0){
            this.mElements = update;
            return;
        }
        System.arraycopy(elements, 0, update, 0, size);
        this.mElements = update;
    }
    private int availableCapacity(){
        return this.mElements.length - size;
    }

    @Override
    public String toString() {
        if(size() == 0){
            return "EMPTY";
        }
        return size() + "{" + get(0) + "}";
    }
    @SuppressWarnings("unchecked")
    public static<T> ArrayCollection<T> of(Iterable<? extends T> iterable){
        ArrayCollection<T> collection = new ArrayCollection<>();
        if(iterable instanceof Collection){
            collection.addAll((Collection<? extends T>) iterable);
            return collection;
        }
        collection.add(iterable.iterator());
        collection.trimToSize();
        return collection;
    }
    public static<T> ArrayCollection<T> of(Iterator<? extends T> iterator){
        ArrayCollection<T> collection = new ArrayCollection<>();
        collection.add(iterator);
        collection.trimToSize();
        return collection;
    }

    private static final Object[] EMPTY = new Object[0];
}
