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

import java.util.*;
import java.util.function.Predicate;


public abstract class BlockArray<T extends Block> extends BlockContainer<T> implements BlockArrayCreator<T>  {
    private T[] elementData;
    public BlockArray(){
        elementData= newInstance(0);
    }

    public void removeAllNull(int start){
        removeAll(start, true);
    }
    public void removeAll(int start){
        removeAll(start, false);
    }
    private void removeAll(int start, boolean check_null){
        List<T> removeList = subList(start);
        if(removeList.size()==0 || (check_null && !isAllNull(removeList))){
            return;
        }
        T[] itemArray = this.elementData;
        for(T item:removeList){
            if(item==null){
                continue;
            }
            if(!item.isNull()){
                item.setNull(true);
            }
            int index = item.getIndex();
            if(index>=0 && itemArray[index]==item){
                item.setIndex(-1);
                item.setParent(null);
                itemArray[index] = null;
            }
        }
        setChildesCount(start);
    }
    public List<T> subList(int start){
        return subList(start, -1);
    }
    public List<T> subList(int start, int count){
        T[] items = this.elementData;
        if(items==null){
            return new ArrayList<>();
        }
        int length = items.length;
        if(start>=length){
            return new ArrayList<>();
        }
        if(start < 0){
            start=0;
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
        List<T> results = new ArrayList<>(end - start);
        for(int i=start; i<end; i++){
            results.add(items[i]);
        }
        return results;
    }
    public Collection<T> listItems(){
        return listItems(false);
    }
    public Collection<T> listItems(boolean skipNullBlocks){
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
                return BlockArray.this.childesCount();
            }
        };
    }
    @Override
    public T[] getChildes(){
        return elementData;
    }
    public void ensureSize(int size){
        if(size<= childesCount()){
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
        int diff = count - childesCount();
        if(diff==0){
            return;
        }
        changeSize(diff);
    }
    public void clearChildes(){
        T[] allChildes=elementData;
        if(allChildes==null || allChildes.length==0){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            T block=allChildes[i];
            if(block==null){
                continue;
            }
            block.setIndex(-1);
            block.setParent(null);
            allChildes[i]=null;
        }
        elementData=newInstance(0);
    }
    public void addAll(T[] blocks){
        if(blocks==null||blocks.length==0){
            return;
        }
        T[] old=elementData;
        if(old==null){
            old=newInstance(0);
        }
        int oldLen=old.length;
        int len=blocks.length;
        T[] update= newInstance(oldLen+len);
        if(oldLen>0){
            System.arraycopy(old, 0, update, 0, oldLen);
        }
        boolean foundNull=false;
        for(int i=0;i<len;i++){
            T item=blocks[i];
            if(item==null){
                foundNull=true;
                continue;
            }
            int index=oldLen + i;
            update[index]=item;
            item.setParent(this);
            item.setIndex(index);
        }
        elementData=update;
        if(foundNull){
            trimNullBlocks();
        }
    }
    public void sort(Comparator<T> comparator){
        T[] data=this.elementData;
        if(comparator==null || data==null || data.length<2){
            return;
        }
        Arrays.sort(data, 0, data.length, comparator);
        for(int i=0;i<data.length;i++){
            data[i].setIndex(i);
        }
    }
    public void insertItem(int index, T item){
        int count = childesCount();
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
        ensureSize(index+1);
        elementData[index]=item;
        item.setIndex(index);
        item.setParent(this);
    }
    public void add(T block){
        if(block==null){
            return;
        }
        T[] old=elementData;
        int index=old.length;
        elementData= newInstance(index+1);
        if(index>0){
            System.arraycopy(old, 0, elementData, 0, index);
        }
        elementData[index]=block;
        block.setIndex(index);
        block.setParent(this);
    }
    public final int countNonNull(){
        return countNonNull(true);
    }
    public final int childesCount(){
        return elementData.length;
    }
    public final T createNext(){
        T block=newInstance();
        add(block);
        return block;
    }
    public final T get(int i){
        if(i >= childesCount() || i<0){
            return null;
        }
        return elementData[i];
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
        return new BlockIterator(skipNullBlock);
    }
    public Iterator<T> iterator(Predicate<T> tester) {
        return new PredicateIterator(tester);
    }
    public boolean contains(Object block){
        T[] items=elementData;
        if(block==null || items==null){
            return false;
        }
        int len=items.length;
        for(int i=0;i<len;i++){
            if(isEqual(items[i], block)){
                return true;
            }
        }
        return false;
    }
    protected boolean isEqual(T item, Object obj){
        return obj == item;
    }
    public void remove(Collection<T> blockList){
        T[] items=elementData;
        if(items==null || items.length==0){
            return;
        }
        int len=items.length;
        for(T block:blockList){
            if(block==null){
                continue;
            }
            int i=block.getIndex();
            if(i<0 || i>=len){
                continue;
            }
            if(items[i]!=block){
                continue;
            }
            items[i]=null;
        }
        trimNullBlocks();
    }
    public boolean remove(T block){
        return remove(block, true);
    }
    protected boolean remove(T block, boolean trim){
        T[] items=elementData;
        if(block==null||items==null){
            return false;
        }
        boolean found=false;
        int len=items.length;
        for(int i=0;i<len;i++){
            if(block==items[i]){
                items[i]=null;
                found=true;
            }
        }
        if(found && trim){
            trimNullBlocks();
        }
        return found;
    }
    protected void trimNullBlocks(){
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
        T[] old=elementData;
        int index=old.length;
        int size=index+amount;
        T[] update= newInstance(size);
        int end;
        if(index>size){
            end=size;
        }else {
            end=index;
        }
        if(end>0){
            System.arraycopy(old, 0, update, 0, end);
        }
        for(int i=end;i<size;i++){
            T item=newInstance();
            update[i]=item;
            item.setIndex(i);
            item.setParent(this);
        }
        elementData=update;
    }

    @Override
    public String toString(){
        return "count="+ childesCount();
    }

    public static boolean isAllNull(Collection<? extends Block> itemsList){
        for(Block item:itemsList){
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
            mMaxSize=BlockArray.this.childesCount();
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
        private final Predicate<T> mTester;
        PredicateIterator(Predicate<T> tester){
            this.mTester = tester;
            mCursor = 0;
            mMaxSize = BlockArray.this.childesCount();
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
            Predicate<T> tester = mTester;
            if(tester != null){
                return tester.test(item);
            }
            return true;
        }
    }
}
