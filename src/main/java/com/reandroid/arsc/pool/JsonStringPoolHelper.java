package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.arsc.model.StyleSpanInfo;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;

import java.util.*;

class JsonStringPoolHelper<T extends StringItem> {

    private final BaseStringPool<T> stringPool;
    JsonStringPoolHelper(BaseStringPool<T> stringPool){
        this.stringPool=stringPool;
    }
    void loadStyledStrings(JSONArray jsonArray) {
        //Styled strings should be at first rows of string pool thus we clear all before adding
        stringPool.getStringsArray().clearChildes();
        stringPool.getStyleArray().clearChildes();

        List<StyledString> styledStringList = StyledString.fromJson(jsonArray);
        loadText(styledStringList);
        Map<String, Integer> tagIndexMap = loadStyleTags(styledStringList);
        loadStyles(styledStringList, tagIndexMap);
        stringPool.refreshUniqueIdMap();
    }
    private void loadText(List<StyledString> styledStringList) {
        StringArray<T> stringsArray = stringPool.getStringsArray();
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
        tagList.sort(stringPool);
        StringArray<T> stringsArray = stringPool.getStringsArray();
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
        StyleArray styleArray = stringPool.getStyleArray();
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
}
