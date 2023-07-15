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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.OffsetArray;
import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.group.StringGroup;
import com.reandroid.arsc.header.StringPoolHeader;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.util.*;

public abstract class StringPool<T extends StringItem> extends Chunk<StringPoolHeader> implements BlockLoad, JSONConvert<JSONArray> {
    private final Object mLock = new Object();
    private final StringArray<T> mArrayStrings;
    private final StyleArray mArrayStyles;

    private final Map<String, StringGroup<T>> mUniqueMap;
    private boolean stringLinkLocked;

    StringPool(boolean is_utf8, boolean stringLinkLocked){
        super(new StringPoolHeader(), 4);

        OffsetArray offsetStrings = new OffsetArray();
        OffsetArray offsetStyles = new OffsetArray();

        StringPoolHeader header = getHeaderBlock();

        this.mArrayStrings = newInstance(
                offsetStrings,
                header.getCountStrings(),
                header.getStartStrings(),
                is_utf8);

        this.mArrayStyles = new StyleArray(
                offsetStyles,
                header.getCountStyles(),
                header.getStartStyles());


        addChild(offsetStrings);
        addChild(offsetStyles);
        addChild(mArrayStrings);
        addChild(mArrayStyles);

        setUtf8(is_utf8, false);

        header.getFlagUtf8().setBlockLoad(this);

        mUniqueMap = new HashMap<>();
        this.stringLinkLocked = stringLinkLocked;
    }
    StringPool(boolean is_utf8){
        this(is_utf8, true);
    }

    void sort(Comparator<T> comparator){
        ensureStringLinkUnlockedInternal();
        getStringsArray().sort(comparator);
    }
    public boolean isStringLinkLocked(){
        return stringLinkLocked;
    }
    public void ensureStringLinkUnlockedInternal(){
        if(!stringLinkLocked){
            return;
        }
        synchronized (mLock){
            if(!stringLinkLocked){
                return;
            }
            stringLinkLocked = false;
            linkStrings();
        }
    }
    void linkStrings(){
        linkStyles();
    }
    private void linkStyles(){
        StyleArray styleArray = getStyleArray();
        if(styleArray == null){
            return;
        }
        StyleItem[] styles = styleArray.getChildes();
        for(StyleItem styleItem : styles){
            styleItem.linkIfRequiredInternal();
        }
    }

    public void removeString(T item){
        getStringsArray().remove(item);
    }
    public void destroy(){
        getStyleArray().clearChildes();
        getStringsArray().clearChildes();
    }
    public List<String> toStringList(){
        return getStringsArray().toStringList();
    }
    public void addStrings(Collection<String> stringList){
        if(stringList==null || stringList.size()==0){
            return;
        }
        Set<String> uniqueSet;
        if(stringList instanceof HashSet){
            uniqueSet=(HashSet<String>)stringList;
        }else {
            uniqueSet=new HashSet<>(stringList);
        }
        refreshUniqueIdMap();
        Set<String> keySet=mUniqueMap.keySet();
        for(String key:keySet){
            uniqueSet.remove(key);
        }
        List<String> sortedList=new ArrayList<>(uniqueSet);
        sortedList.sort(CompareUtil.STRING_COMPARATOR);
        insertStringList(sortedList);
    }
    private void insertStringList(List<String> stringList){
        StringArray<T> stringsArray = getStringsArray();
        int initialSize=stringsArray.childesCount();
        stringsArray.ensureSize(initialSize + stringList.size());
        int size=stringsArray.childesCount();
        int j=0;
        for (int i=initialSize;i<size;i++){
            T item=stringsArray.get(i);
            item.set(stringList.get(j));
            j++;
        }
        refreshUniqueIdMap();
    }
    public Map<String, T> insertStrings(List<String> stringList){
        Map<String, T> results=new HashMap<>();
        StringArray<T> stringsArray = getStringsArray();
        int initialSize=stringsArray.childesCount();
        stringsArray.ensureSize(initialSize + stringList.size());
        int size=stringsArray.childesCount();
        int j=0;
        for (int i=initialSize;i<size;i++){
            T item=stringsArray.get(i);
            String str=stringList.get(j);
            item.set(str);
            results.put(str, item);
            j++;
        }
        refreshUniqueIdMap();
        return results;
    }
    // call this after modifying string values
    public void refreshUniqueIdMap(){
        mUniqueMap.clear();
        T[] stringsArray = getStrings();
        if(stringsArray == null){
            return;
        }
        for(int i=0; i < stringsArray.length; i++){
            T item = stringsArray[i];
            if(item == null){
                continue;
            }
            String str = item.getXml();
            if(str == null){
                continue;
            }
            StringGroup<T> group = mUniqueMap.get(str);
            if(group == null){
                group = new StringGroup<>(mArrayStrings, str, item);
                mUniqueMap.put(str, group);
            }else {
                group.add(item);
            }
        }
    }
    void updateUniqueIdMap(T item){
        if(item == null){
            return;
        }
        String str = item.getXml();
        if(str == null){
            str = "";
        }
        StringGroup<T> group = mUniqueMap.get(str);
        if(group == null){
            group = new StringGroup<>(mArrayStrings, str, item);
            mUniqueMap.put(str, group);
        }else {
            group.add(item);
        }
    }
    public List<T> removeUnusedStrings(){
        return getStringsArray().removeUnusedStrings();
    }
    public List<T> listUnusedStrings(){
        return getStringsArray().listUnusedStrings();
    }
    public Collection<T> listStrings(){
        return getStringsArray().listItems();
    }
    public StyleArray getStyleArray(){
        return mArrayStyles;
    }
    public StringArray<T> getStringsArray(){
        return mArrayStrings;
    }
    public T removeReference(ReferenceItem ref){
        if(ref==null){
            return null;
        }
        T item=get(ref.get());
        if(item!=null){
            item.removeReference(ref);
            return item;
        }
        return null;
    }
    public void addReference(ReferenceItem ref){
        if(ref==null){
            return;
        }
        T item=get(ref.get());
        if(item!=null){
            item.addReference(ref);
        }
    }

    public boolean contains(String str){
        return mUniqueMap.containsKey(str);
    }
    public final T get(int index){
        return mArrayStrings.get(index);
    }
    public final T getLast(){
        return mArrayStrings.getLast();
    }
    public final StringGroup<T> get(String str){
        return mUniqueMap.get(str);
    }
    public T getOrCreate(String str){
        if(str == null){
            str = "";
        }
        StringGroup<T> group = mUniqueMap.get(str);
        T item;
        if(group == null){
            item = createNewString(str);
            group = new StringGroup<>(mArrayStrings, str, item);
            mUniqueMap.put(str, group);
        }else if(group.size() == 0){
            item = createNewString(str);
            group.add(item);
        }else {
            item = group.get(0);
        }
        return item;
    }
    private T createNewString(String str){
        T item = mArrayStrings.createNext();
        item.set(str);
        //getHeaderBlock().getCountStrings().set(mArrayStrings.childesCount());
        return item;
    }
    public final StyleItem getStyle(int index){
        return mArrayStyles.get(index);
    }
    public final int countStrings(){
        return mArrayStrings.childesCount();
    }
    public final int countStyles(){
        return mArrayStyles.childesCount();
    }
    public final T[] getStrings(){
        return mArrayStrings.getChildes();
    }
    public final StyleItem[] getStyles(){
        return mArrayStyles.getChildes();
    }
    public boolean isUtf8(){
        return getHeaderBlock().isUtf8();
    }
    public void setUtf8(boolean is_utf8){
        setUtf8(is_utf8, true);
    }
    private void setUtf8(boolean is_utf8, boolean updateAll){
        StringPoolHeader header = getHeaderBlock();
        if(is_utf8 == header.isUtf8()){
            return;
        }
        ByteItem flagUtf8 = header.getFlagUtf8();
        if(is_utf8){
            flagUtf8.set((byte) 0x01);
        }else {
            flagUtf8.set((byte) 0x00);
        }
        if(!updateAll){
            return;
        }
        mArrayStrings.setUtf8(is_utf8);
    }
    public void setFlagSorted(boolean sorted){
        getHeaderBlock().setSorted(sorted);
    }

    abstract StringArray<T> newInstance(OffsetArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8);
    @Override
    protected void onChunkRefreshed() {
        mArrayStrings.refreshCountAndStart();
        mArrayStyles.refreshCountAndStart();
    }
    @Override
    public void onChunkLoaded() {
        refreshUniqueIdMap();
        StyleItem[] styles = getStyles();
        if(styles!=null){
            for(StyleItem styleItem:styles){
                styleItem.onDataLoaded();
            }
        }
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        StringPoolHeader header = getHeaderBlock();
        if(sender == header.getFlagUtf8()){
            mArrayStrings.setUtf8(header.isUtf8());
        }
    }
    @Override
    public JSONArray toJson() {
        return getStringsArray().toJson();
    }
    //Only for styled strings
    @Override
    public void fromJson(JSONArray json) {
        if(json==null){
            return;
        }
        JsonStringPoolHelper<T> helper=new JsonStringPoolHelper<>(this);
        helper.loadStyledStrings(json);
        refresh();
    }

}
