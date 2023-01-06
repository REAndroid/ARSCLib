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
package com.reandroid.lib.arsc.pool;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.array.StyleArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.group.StringGroup;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.model.StyleSpanInfo;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.*;


public abstract class BaseStringPool<T extends StringItem> extends BaseChunk implements BlockLoad, JSONConvert<JSONArray>, Comparator<String> {
    private final IntegerItem mCountStrings;
    private final IntegerItem mCountStyles;
    private final ShortItem mFlagUtf8;
    private final ShortItem mFlagSorted;
    private final IntegerItem mStartStrings;
    private final IntegerItem mStartStyles;
    private final IntegerArray mOffsetStrings;
    private final IntegerArray mOffsetStyles;
    private final StringArray<T> mArrayStrings;
    private final StyleArray mArrayStyles;

    private final Map<String, StringGroup<T>> mUniqueMap;


    BaseStringPool(boolean is_utf8){
        super(ChunkType.STRING, 4);

        this.mCountStrings=new IntegerItem(); //header
        this.mCountStyles=new IntegerItem(); //header
        this.mFlagUtf8 =new ShortItem(); //header
        this.mFlagSorted=new ShortItem(); //header
        this.mStartStrings=new IntegerItem(); //header
        this.mStartStyles=new IntegerItem(); //header

        this.mOffsetStrings=new IntegerArray();//1
        this.mOffsetStyles=new IntegerArray(); //2
        this.mArrayStrings=newInstance(mOffsetStrings, mCountStrings, mStartStrings, is_utf8); //3
        this.mArrayStyles=new StyleArray(mOffsetStyles, mCountStyles, mStartStyles); //4

        addToHeader(mCountStrings);
        addToHeader(mCountStyles);
        addToHeader(mFlagUtf8);
        addToHeader(mFlagSorted);
        addToHeader(mStartStrings);
        addToHeader(mStartStyles);

        addChild(mOffsetStrings);
        addChild(mOffsetStyles);
        addChild(mArrayStrings);
        addChild(mArrayStyles);

        setUtf8(is_utf8, false);

        mFlagUtf8.setBlockLoad(this);

        mUniqueMap=new HashMap<>();

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
        List<String> sortedList=new ArrayList<>(stringList);
        sortedList.sort(this);
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
        T[] allChildes=getStrings();
        if(allChildes==null){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            T item=allChildes[i];
            if(item==null){
                continue;
            }
            String str=item.getHtml();
            if(str==null){
                continue;
            }
            StringGroup<T> group= getOrCreateGroup(str);
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
    public void removeReferences(Collection<ReferenceItem> referenceList){
        if(referenceList==null){
            return;
        }
        for(ReferenceItem ref:referenceList){
            removeReference(ref);
        }
    }
    public boolean removeReference(ReferenceItem ref){
        if(ref==null){
            return false;
        }
        T item=get(ref.get());
        if(item!=null){
            return item.removeReference(ref);
        }
        return false;
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
    public void addReferences(Collection<ReferenceItem> referenceList){
        if(referenceList==null){
            return;
        }
        for(ReferenceItem ref:referenceList){
            addReference(ref);
        }
    }

    public boolean contains(String str){
        return mUniqueMap.containsKey(str);
    }
    public final T get(int index){
        return mArrayStrings.get(index);
    }
    public final StringGroup<T> get(String str){
        return mUniqueMap.get(str);
    }
    public T getOrCreate(String str){
        StringGroup<T> group=getOrCreateGroup(str);
        T[] items=group.getItems();
        if(items.length==0){
            T t=createNewString(str);
            group.add(t);
            items=group.getItems();
        }
        return items[0];
    }
    private StringGroup<T> getOrCreateGroup(String str){
        StringGroup<T> group=get(str);
        if(group!=null){
            return group;
        }
        group=new StringGroup<>(mArrayStrings, str);
        mUniqueMap.put(str, group);
        return group;
    }
    private T createNewString(String str){
        T item=mArrayStrings.createNext();
        item.set(str);
        mCountStrings.set(mArrayStrings.childesCount());
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
    private void setUtf8Flag(short flag){
        mFlagUtf8.set(flag);
    }
    public void setUtf8(boolean is_utf8){
        setUtf8(is_utf8, true);
    }
    private void setSortedFlag(short flag){
        mFlagSorted.set(flag);
    }
    public final void setSorted(boolean sorted){
        if(sorted){
            setSortedFlag(FLAG_SORTED);
        }else {
            setSortedFlag((short)0);
        }
    }
    private void setUtf8(boolean is_utf8, boolean updateAll){
        boolean old= isUtf8Flag();
        if(is_utf8){
            setUtf8Flag(UTF8_FLAG_VALUE);
        }else {
            setUtf8Flag((short) 0);
        }
        if(!updateAll || old == isUtf8Flag()){
            return;
        }
        mArrayStrings.setUtf8(is_utf8);
    }
    private boolean isUtf8Flag(){
        return (mFlagUtf8.get() & FLAG_UTF8) !=0;
    }
    private boolean isSortedFlag(){
        return (mFlagSorted.get() & FLAG_SORTED) !=0;
    }


    abstract StringArray<T> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8);
    @Override
    protected void onChunkRefreshed() {
        mArrayStrings.refreshCountAndStart();
        mArrayStyles.refreshCountAndStart();
    }
    @Override
    public void onChunkLoaded() {
        refreshUniqueIdMap();
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender== mFlagUtf8){
            mArrayStrings.setUtf8(isUtf8Flag());
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
        loadStyledStrings(json);
        refresh();
    }
    public void loadStyledStrings(JSONArray jsonArray) {
        //Styled strings should be at first rows of string pool thus we clear all before adding
        getStringsArray().clearChildes();
        getStyleArray().clearChildes();

        List<StyledString> styledStringList = StyledString.fromJson(jsonArray);
        loadText(styledStringList);
        Map<String, Integer> tagIndexMap = loadStyleTags(styledStringList);
        loadStyles(styledStringList, tagIndexMap);
        refreshUniqueIdMap();
    }
    private void loadText(List<StyledString> styledStringList) {
        StringArray<T> stringsArray = getStringsArray();
        int size=styledStringList.size();
        stringsArray.ensureSize(size);
        for(int i=0;i<size;i++){
            StyledString styledString=styledStringList.get(i);
            T item=stringsArray.get(i);
            item.set(styledString.text);
        }
    }
    private Map<String, Integer> loadStyleTags(List<StyledString> styledStringList) {
        Map<String, Integer> indexMap=new HashMap<>();
        List<String> tagList=new ArrayList<>(getStyleTags(styledStringList));
        tagList.sort(this);
        StringArray<T> stringsArray = getStringsArray();
        int tagsSize = tagList.size();
        int initialSize = stringsArray.childesCount();
        stringsArray.ensureSize(initialSize + tagsSize);
        for(int i=0;i<tagsSize;i++){
            String tag = tagList.get(i);
            T item = stringsArray.get(initialSize + i);
            item.set(tag);
            indexMap.put(tag, item.getIndex());
        }
        return indexMap;
    }
    private void loadStyles(List<StyledString> styledStringList, Map<String, Integer> tagIndexMap){
        StyleArray styleArray = getStyleArray();
        int size=styledStringList.size();
        styleArray.ensureSize(size);
        for(int i=0;i<size;i++){
            StyledString ss = styledStringList.get(i);
            StyleItem styleItem = styleArray.get(i);
            for(StyleSpanInfo spanInfo:ss.spanInfoList){
                int tagIndex=tagIndexMap.get(spanInfo.getTag());
                styleItem.addStylePiece(tagIndex, spanInfo.getFirst(), spanInfo.getLast());
            }
        }
    }
    private static Set<String> getStyleTags(List<StyledString> styledStringList){
        Set<String> results=new HashSet<>();
        for(StyledString ss:styledStringList){
            for(StyleSpanInfo spanInfo:ss.spanInfoList){
                results.add(spanInfo.getTag());
            }
        }
        return results;
    }
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }

    private static class StyledString{
        final String text;
        final List<StyleSpanInfo> spanInfoList;
        StyledString(String text, List<StyleSpanInfo> spanInfoList){
            this.text=text;
            this.spanInfoList=spanInfoList;
        }
        @Override
        public String toString(){
            return text;
        }
        static List<StyledString> fromJson(JSONArray jsonArray){
            int length = jsonArray.length();
            List<StyledString> results=new ArrayList<>();
            for(int i=0;i<length;i++){
                StyledString styledString=fromJson(jsonArray.getJSONObject(i));
                results.add(styledString);
            }
            return results;
        }
        static StyledString fromJson(JSONObject jsonObject){
            String text= jsonObject.getString(StringItem.NAME_string);
            JSONObject style=jsonObject.getJSONObject(StringItem.NAME_style);
            JSONArray spansArray=style.getJSONArray(StyleItem.NAME_spans);
            List<StyleSpanInfo> spanInfoList = toSpanInfoList(spansArray);
            return new StyledString(text, spanInfoList);
        }
        private static List<StyleSpanInfo> toSpanInfoList(JSONArray jsonArray){
            int length = jsonArray.length();
            List<StyleSpanInfo> results=new ArrayList<>(length);
            for(int i=0;i<length;i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                StyleSpanInfo spanInfo=new StyleSpanInfo(null, 0,0);
                spanInfo.fromJson(jsonObject);
                results.add(spanInfo);
            }
            return results;
        }
    }
    private static final short UTF8_FLAG_VALUE=0x0100;

    private static final short FLAG_UTF8 = 0x0100;
    private static final short FLAG_SORTED = 0x0100;
}
