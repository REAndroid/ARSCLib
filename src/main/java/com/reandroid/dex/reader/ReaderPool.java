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
package com.reandroid.dex.reader;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.OffsetReference;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.*;

public class ReaderPool<T extends OffsetReference> {
    private final Object mLock = new Object();
    private final Map<Integer, T> itemsMap;
    private T noOffsetItem;
    private int requestCount;
    private int min = Integer.MAX_VALUE;
    private int max = 0;
    public ReaderPool(){
        this.itemsMap = new HashMap<>();
    }
    public T getOrRead(BlockReader reader, T item) throws IOException{
        return getOrRead(reader, item, false);
    }
    public T getOrRead(BlockReader reader, T item, boolean keepPosition) throws IOException {
        synchronized (mLock){
            int i = item.getOffsetReference().get();
            if(i == 0){
                T exist = this.noOffsetItem;
                if(exist == null){
                    this.noOffsetItem = item;
                    exist = item;
                }
                return exist;
            }
            requestCount ++;

            Integer offset = i;
            T exist = itemsMap.get(offset);
            if(exist == null){
                itemsMap.put(offset, item);
                exist = item;
            }else if(keepPosition){
                return exist;
            }
            item.readBytes(reader);
            if(i < min){
                min = i;
            }
            if(i > max){
                max = i;
            }
            return exist;
        }
    }
    public T getNoOffsetItem() {
        return noOffsetItem;
    }
    public int sumSize(){
        int result = 0;
        for(T item : itemsMap.values()){
            result += ((Block) item).countBytes();
        }
        return result;
    }
    public List<T> findDuplicates(){
        List<T> duplicates = new ArrayList<>();
        if(itemsMap.size() > 1){
            return duplicates;
        }
        Map<Integer, T> unique = new HashMap<>();
        for(T item : itemsMap.values()){
            int hash = Arrays.hashCode(((Block) item).getBytes());
            if(unique.containsKey(hash)){
                T exist = unique.get(hash);
                duplicates.add(exist);
                continue;
            }
            unique.put(hash, item);
        }
        return duplicates;
    }

    public List<Integer> sortedOffsets(){
        synchronized (mLock){
            List<Integer> results = new ArrayList<>(itemsMap.keySet());
            results.sort(CompareUtil.getComparableComparator());
            return results;
        }
    }

    @Override
    public String toString() {
        return  "size=" + itemsMap.size() +
                ", requestCount=" + requestCount
                + ", min=" + min
                + ", max=" + max + ", diff = " + (max - min)
                + ", noOffset=" + noOffsetItem + ", sum=" + sumSize();
    }
}
