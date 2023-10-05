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
package com.reandroid.arsc.base;

import com.reandroid.common.ArraySupplier;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.util.*;
import java.util.function.Predicate;

public abstract class BlockArray<T extends Block> extends BlockContainer<T>
        implements BlockArrayCreator<T>, ArraySupplier<T> {
    private T[] elementData;
    private int mFreeSpace;
    private int mAllocateStep;
    public BlockArray(){
        elementData = newInstance(0);
    }
    public BlockArray(T[] elementData){
        this.elementData = elementData;
    }

    public void removeAllNull(int start){
        removeAll(start, true);
    }
    public void removeAll(int start){
        removeAll(start, false);
    }
    private void removeAll(int start, boolean check_null){
        T[] removeList = subArray(start);
        if(removeList.length == 0 || (check_null && !isAllNull(removeList))){
            return;
        }
        T[] elementData = this.elementData;
        for(T item:removeList){
            if(item == null){
                continue;
            }
            if(!item.isNull()){
                item.setNull(true);
            }
            int index = item.getIndex();
            if(index>=0 && elementData[index]==item){
                item.setIndex(-1);
                item.setParent(null);
                elementData[index] = null;
            }
        }
        setChildesCount(start);
    }
    private T[] subArray(int start){
        return subArray(start, -1);
    }
    private T[] subArray(int start, int count){
        T[] items = this.elementData;
        int length = items.length;
        if(start < 0){
            start = 0;
        }
        if(start >= length){
            return newInstance(0);
        }
        int end = count;
        if(end < 0){
            end = items.length;
        }else {
            end = start + count;
            if(end > length){
                end=length;
            }
        }
        T[] results = newInstance(end - start);
        int index = 0;
        for(int i = start; i < end; i++){
            results[index] = items[i];
            index ++;
        }
        return results;
    }
    public Collection<T> listItems(){
        return listItems(false);
    }
    public Collection<T> listItems(boolean skipNullBlocks){
        trimAllocatedFreeSpace();
        return new AbstractCollection<T>() {
            @Override
            public Iterator<T> iterator(){
                return BlockArray.this.iterator(skipNullBlocks);
            }
            @Override
            public boolean contains(Object o){
                return BlockArray.this.contains(o);
            }
            @Override
            public int size() {
                return BlockArray.this.getChildesCount();
            }
        };
    }
    @Override
    public T[] getChildes(){
        return elementData;
    }
    public void ensureSize(int size){
        if(size <= getChildesCount()){
            return;
        }
        setChildesCount(size);
    }
    public void setChildesCount(int count){
        if(count<0){
            count=0;
        }
        if(count==0){
            clearChildes();
            return;
        }
        int diff = count - getChildesCount();
        if(diff==0){
            return;
        }
        changeSize(diff);
    }
    public void clearChildes(){
        T[] elementData = this.elementData;
        int length = elementData.length;
        if(length == 0){
            return;
        }
        for(int i = 0; i < length; i++){
            T block = elementData[i];
            if(block == null){
                continue;
            }
            block.setIndex(-1);
            block.setParent(null);
            elementData[i]=null;
        }
        this.elementData = newInstance(0);
    }
    public void addAll(T[] blocks){
        if(blocks == null || blocks.length == 0){
            return;
        }
        T[] old = elementData;
        int oldLength = 0;
        if(old != null){
            oldLength = old.length;
        }
        int len = blocks.length;
        T[] update = newInstance(oldLength + len);
        if(oldLength > 0){
            System.arraycopy(old, 0, update, 0, oldLength);
        }
        boolean foundNull=false;
        for(int i=0; i < len; i++){
            T item = blocks[i];
            if(item == null){
                foundNull=true;
                continue;
            }
            int index = oldLength + i;
            update[index]=item;
            item.setParent(this);
            item.setIndex(index);
        }
        elementData = update;
        if(foundNull){
            trimNullBlocks();
        }
    }
    public boolean needsSort(Comparator<? super T> comparator) {
        T[] elementData = this.elementData;
        if(comparator == null){
            return false;
        }
        int length = elementData.length;
        if(length < 2){
            return false;
        }
        T previous = elementData[0];
        for(int i = 1; i < length; i++){
            T item = elementData[i];
            if(comparator.compare(previous, item) > 0){
                return true;
            }
            previous = item;
        }
        return false;
    }
    public boolean sort(Comparator<? super T> comparator){
        T[] elementData = this.elementData;
        if(comparator == null || elementData.length < 2){
            return false;
        }
        Arrays.sort(elementData, 0, elementData.length, comparator);
        boolean changed = false;
        for(int i=0 ; i < elementData.length; i++){
            T item = elementData[i];
            if(item != null && item.getIndex() != i){
                item.setIndex(i);
                changed = true;
            }
        }
        return changed;
    }
    public void insertItem(int index, T item){
        int count = getChildesCount();
        if(count < index){
            count = index;
        }
        ensureSize(count + 1);
        T[] childes = getChildes();
        int lastIndex = childes.length - 2;
        for(int i = lastIndex; i >= index; i--){
            T exist = childes[i];
            childes[i] = null;
            int newIndex = i + 1;
            childes[newIndex] = exist;
            exist.setIndex(newIndex);
        }
        childes[index] = item;
        item.setParent(this);
        item.setIndex(index);
    }
    public void setItem(int index, T item){
        ensureSize(index + 1);
        elementData[index] = item;
        if(item != null){
            item.setIndex(index);
            item.setParent(this);
        }
    }
    public void addInternal(int index, T block){
        if(isFlexible()){
            allocateIfFull();
        }else {
            ensureSize(index + 1);
        }
        addAt(index, block);
    }
    private void addAt(int index, T block){
        onPreShifting();
        T[] elementData = this.elementData;
        int start = elementData.length - 1;
        for(int i = start; i > index; i--){
            int left = i - 1;
            T exist = elementData[left];
            elementData[left] = null;
            elementData[i] = exist;
            if(exist != null){
                exist.setIndex(i);
            }
        }
        elementData[index] = block;
        if(block != null){
            block.setIndex(index);
            block.setParent(this);
        }
        onPostShift(index);
        if(isFlexible()){
            mFreeSpace--;
        }
    }
    protected void onPreShifting(){
    }
    protected void onPostShift(int index){
    }
    public boolean add(T block){
        if(block == null){
            return false;
        }
        if(isFlexible()){
            addAtNull(block);
            return true;
        }
        T[] oldElementData = elementData;
        int index = oldElementData.length;
        elementData = newInstance(index+1);
        if(index>0){
            System.arraycopy(oldElementData, 0, elementData, 0, index);
        }
        elementData[index] = block;
        block.setIndex(index);
        block.setParent(this);
        return true;
    }
    private void addAtNull(T block){
        allocateIfFull();
        T[] elementData = this.elementData;
        int index = elementData.length - mFreeSpace;
        elementData[index]=block;
        block.setIndex(index);
        block.setParent(this);
        mFreeSpace --;
    }
    private int calculateAllocate(){
        mAllocateStep++;
        int amount = getChildesCount() / 4;
        if(amount < 10){
            amount = 10;
        }else if(amount > 100){
            amount = 100;
        }
        amount = amount * mAllocateStep;
        if(amount > 8000){
            amount = 8000;
        }
        return amount;
    }
    protected boolean isFlexible(){
        return false;
    }
    protected void trimAllocatedFreeSpace(){
        if(mFreeSpace <= 0){
            return;
        }
        int length = elementData.length - mFreeSpace;
        T[] update = newInstance(length);
        if (length > 0) {
            System.arraycopy(elementData, 0, update, 0, length);
        }
        elementData = update;
        mFreeSpace = 0;
    }
    public final int countNonNull(){
        return countNonNull(true);
    }
    @Override
    public final int getChildesCount(){
        return elementData.length;
    }
    public T createNext(){
        T block=newInstance();
        add(block);
        return block;
    }
    @Override
    public final T get(int i){
        if(i >= getChildesCount() || i<0){
            return null;
        }
        return elementData[i];
    }
    @Override
    public int getCount(){
        return getChildesCount();
    }
    public final T getLast(){
        return get(getChildesCount() - mFreeSpace - 1);
    }
    public int indexOf(Object block){
        T[] items=elementData;
        if(items==null){
            return -1;
        }
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                return i;
            }
        }
        return -1;
    }
    public int lastIndexOf(Object block){
        T[] items=elementData;
        if(items==null){
            return -1;
        }
        int len=items.length;
        int result=-1;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                result=i;
            }
        }
        return result;
    }

    public Iterator<T> iterator() {
        return iterator(false);
    }
    public Iterator<T> iterator(boolean skipNullBlock) {
        trimAllocatedFreeSpace();
        int count = getChildesCount();
        if(count == 0){
            return EmptyIterator.of();
        }
        if(count == 1){
            T item = get(0);
            if(skipNullBlock && (item == null || item.isNull())){
                return EmptyIterator.of();
            }
            return SingleIterator.of(item);
        }
        return new BlockIterator(skipNullBlock);
    }
    public Iterator<T> iterator(Predicate<? super T> tester) {
        trimAllocatedFreeSpace();
        int count = getChildesCount();
        if(count == 0){
            return EmptyIterator.of();
        }
        return new PredicateIterator(tester);
    }
    public boolean contains(Object block){
        T[] items = elementData;
        if(block == null || items==null){
            return false;
        }
        int length = items.length;
        for(int i = 0; i < length; i++){
            if(items[i] == block){
                return true;
            }
        }
        return false;
    }
    public int remove(Predicate<? super T> predicate){
        return remove(CollectionUtil.toList(iterator(predicate)), null);
    }
    public int remove(Collection<T> blockList){
        return remove(blockList, null);
    }
    protected int remove(Collection<T> blockList, Collection<T> removedList){
        int count = 0;
        T[] items = elementData;
        if(items == null || blockList == null){
            return count;
        }
        int length = items.length;
        if(length == 0){
            return count;
        }
        Iterator<T> iterator = blockList.iterator();
        while (iterator.hasNext()){
            T block = iterator.next();
            if(block == null){
                continue;
            }
            int index = block.getIndex();
            if(index < 0 || index >= length){
                continue;
            }
            T item = items[index];
            if(item != block){
                continue;
            }
            items[index] = null;
            onPreRemove(item);
            if(removedList != null){
                removedList.add(item);
            }
            count ++;
        }
        trimNullBlocks();
        return count;
    }
    public void onPreRemove(T block){

    }
    public boolean remove(T block){
        return remove(block, true);
    }
    protected boolean remove(T block, boolean trim){
        T[] items = elementData;
        if(block == null){
            return false;
        }
        boolean found=false;
        int length = items.length;
        for(int i = 0; i < length; i++){
            T item = items[i];
            if(block == item){
                items[i] = null;
                found = true;
                onPreRemove(item);
            }
        }
        if(found && trim){
            trimNullBlocks();
        }
        return found;
    }
    protected void trimNullBlocks(){
        mFreeSpace = 0;
        T[] items=elementData;
        if(items==null){
            return;
        }
        int count=countNonNull(false);
        int len=items.length;
        if(count==len){
            return;
        }
        T[] update=newInstance(count);
        int index=0;
        for(int i=0;i<len;i++){
            T block=items[i];
            if(block!=null){
                update[index]=block;
                block.setIndex(index);
                index++;
            }
        }
        elementData=update;
    }
    private int countNonNull(boolean is_null_check){
        T[] items=elementData;
        if(items==null){
            return 0;
        }
        int result=0;
        for(T block:items){
            if(block!=null){
                if(is_null_check && block.isNull()){
                    continue;
                }
                result++;
            }
        }
        return result;
    }
    private void changeSize(int amount){
        mFreeSpace = 0;
        mAllocateStep = 0;
        T[] old=elementData;
        int index = 0;
        if(old != null){
            index = old.length;
        }
        int size = index + amount;
        T[] update = newInstance(size);
        int end;
        if(index>size){
            end=size;
        }else {
            end = index;
        }
        if(end > 0){
            System.arraycopy(old, 0, update, 0, end);
        }
        for(int i = end; i < size; i++){
            T item = update[i];
            if(item == null){
                item = newInstance();
                update[i] = item;
            }
            item.setIndex(i);
            item.setParent(this);
        }
        elementData = update;
    }
    private void allocateIfFull(){
        if(mFreeSpace > 0){
            return;
        }
        allocate(calculateAllocate());
    }
    private void allocate(int amount){
        if(amount <= 0 || mFreeSpace > 0){
            return;
        }
        mFreeSpace = amount;
        T[] old = elementData;
        int index = old.length;
        int size = index + amount;
        T[] update = newInstance(size);
        if(index == 0){
            elementData = update;
            return;
        }
        System.arraycopy(old, 0, update, 0, index);
        elementData = update;
    }

    @Override
    public String toString(){
        return "count="+ getChildesCount();
    }

    private static boolean isAllNull(Block[] itemsList){

        for(Block item : itemsList){
            if(item!=null && !item.isNull()){
                return false;
            }
        }
        return true;
    }

    private class BlockIterator implements Iterator<T> {
        private int mCursor;
        private final int mMaxSize;
        private final boolean mSkipNullBlock;
        BlockIterator(boolean skipNullBlock){
            mSkipNullBlock=skipNullBlock;
            mCursor=0;
            mMaxSize=BlockArray.this.getChildesCount();
        }
        @Override
        public boolean hasNext() {
            checkCursor();
            return !isFinished();
        }
        @Override
        public T next() {
            if(!isFinished()){
                T item=BlockArray.this.get(mCursor);
                mCursor++;
                checkCursor();
                return item;
            }
            return null;
        }
        private boolean isFinished(){
            return mCursor>=mMaxSize;
        }
        private void checkCursor(){
            if(!mSkipNullBlock || isFinished()){
                return;
            }
            T item = BlockArray.this.get(mCursor);
            while (item == null || item.isNull()){
                mCursor++;
                item = BlockArray.this.get(mCursor);
                if(mCursor>=mMaxSize){
                    break;
                }
            }
        }
    }


    private class PredicateIterator implements Iterator<T> {
        private int mCursor;
        private final int mMaxSize;
        private final Predicate<? super T> mTester;
        PredicateIterator(Predicate<? super T> tester){
            this.mTester = tester;
            mCursor = 0;
            mMaxSize = BlockArray.this.getChildesCount();
        }
        @Override
        public boolean hasNext() {
            checkCursor();
            return hasItems();
        }
        @Override
        public T next() {
            if(hasItems()){
                T item=BlockArray.this.get(mCursor);
                mCursor++;
                checkCursor();
                return item;
            }
            return null;
        }
        private boolean hasItems(){
            return mCursor < mMaxSize;
        }
        private void checkCursor(){
            if(mTester == null){
                return;
            }
            while (hasItems() && !test(BlockArray.this.get(getCursor()))){
                mCursor++;
            }
        }
        private int getCursor(){
            return mCursor;
        }
        private boolean test(T item){
            Predicate<? super T> tester = mTester;
            if(tester != null){
                return tester.test(item);
            }
            return true;
        }
    }
}
