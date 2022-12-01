package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.model.StyleSpanInfo;
import com.reandroid.lib.arsc.pool.BaseStringPool;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class StyleItem extends IntegerArray implements JSONConvert<JSONObject> {
    private List<StyleSpanInfo> mSpanInfoList;
    public StyleItem() {
        super();
    }
    private void setEndValue(int negOne){
        super.put(size()-1, negOne);
    }
    final Integer getEndValue(){
        return super.get(size()-1);
    }
    final Integer getStringRef(int index){
        int i=index * INTEGERS_COUNT + INDEX_STRING_REF;
        return super.get(i);
    }
    final void setStringRef(int index, int val){
        int i=index * INTEGERS_COUNT + INDEX_STRING_REF;
        super.put(i, val);
    }
    final Integer getFirstChar(int index){
        int i=index * INTEGERS_COUNT + INDEX_CHAR_FIRST;
        return super.get(i);
    }
    final void setFirstChar(int index, int val){
        int i=index * INTEGERS_COUNT + INDEX_CHAR_FIRST;
        super.put(i, val);
    }
    final Integer getLastChar(int index){
        int i=index * INTEGERS_COUNT + INDEX_CHAR_LAST;
        return super.get(i);
    }
    final void setLastChar(int index, int val){
        int i=index * INTEGERS_COUNT + INDEX_CHAR_LAST;
        super.put(i, val);
    }
    public void addStylePiece(String tag, int firstChar, int lastChar){
        BaseStringPool<?> stringPool = getStringPool();
        if(stringPool==null){
            throw new IllegalArgumentException("Null string pool, must be added to parent StyleArray first");
        }
        StringItem stringItem=stringPool.getOrCreate(tag);
        addStylePiece(stringItem.getIndex(), firstChar, lastChar);
    }
    public void addStylePiece(int refString, int firstChar, int lastChar){
        int index=getStylePieceCount();
        setStylePieceCount(index+1);
        setStylePiece(index, refString, firstChar, lastChar);
    }
    final void setStylePiece(int index, int refString, int firstChar, int lastChar){
        int i=index * INTEGERS_COUNT;
        super.put(i+ INDEX_STRING_REF, refString);
        super.put(i+ INDEX_CHAR_FIRST, firstChar);
        super.put(i+ INDEX_CHAR_LAST, lastChar);
    }
    final int[] getStylePiece(int index){
        if(index<0||index>= getStylePieceCount()){
            return null;
        }
        int[] result=new int[INTEGERS_COUNT];
        int i=index * INTEGERS_COUNT;
        result[INDEX_STRING_REF]=super.get(i);
        result[INDEX_CHAR_FIRST]=super.get(i+ INDEX_CHAR_FIRST);
        result[INDEX_CHAR_LAST]=super.get(i+ INDEX_CHAR_LAST);
        return result;
    }
    final void setStylePiece(int index, int[] three){
        if(three==null || three.length< INTEGERS_COUNT){
            return;
        }
        int i = index * INTEGERS_COUNT;
        super.put(i + INDEX_STRING_REF, three[INDEX_STRING_REF]);
        super.put(i + INDEX_CHAR_FIRST, three[INDEX_CHAR_FIRST]);
        super.put(i + INDEX_CHAR_LAST, three[INDEX_CHAR_LAST]);
    }
    final void ensureStylePieceCount(int count){
        if(count<0){
            count=0;
        }
        if(count<getStylePieceCount()){
            setStylePieceCount(count);
        }
    }
    final int getStylePieceCount(){
        int sz=size()-1;
        if(sz<0){
            sz=0;
        }
        return sz/ INTEGERS_COUNT;
    }
    final void setStylePieceCount(int count){
        if(count<0){
            count=0;
        }
        int cur = getStylePieceCount();
        if(count==cur){
            return;
        }
        int max=count * INTEGERS_COUNT + 1;
        if(size()==0 || count==0){
            super.setSize(max);
            setEndValue(END_VALUE);
            return;
        }
        List<int[]> copy=new ArrayList<>(getIntSpanInfoList());
        Integer end= getEndValue();
        if(end==null){
            end=END_VALUE;
        }
        super.setSize(max);
        max=count;
        int copyMax=copy.size();
        if(copyMax>max){
            copyMax=max;
        }
        for(int i=0;i<copyMax;i++){
            int[] val=copy.get(i);
            setStylePiece(i, val);
        }
        setEndValue(end);
    }
    private List<int[]> getIntSpanInfoList(){
        return new AbstractList<int[]>() {
            @Override
            public int[] get(int i) {
                return StyleItem.this.getStylePiece(i);
            }
            @Override
            public int size() {
                return StyleItem.this.getStylePieceCount();
            }
        };
    }
    final List<StyleSpanInfo> getSpanInfoList(){
        if(mSpanInfoList!=null){
            return mSpanInfoList;
        }
        mSpanInfoList = new AbstractList<StyleSpanInfo>() {
            @Override
            public StyleSpanInfo get(int i) {
                int ref=getStringRef(i);
                return new StyleSpanInfo(
                        getStringFromPool(ref),
                        getFirstChar(i),
                        getLastChar(i));
            }
            @Override
            public int size() {
                return getStylePieceCount();
            }
        };
        return mSpanInfoList;
    }
    private String getStringFromPool(int ref){
        BaseStringPool<?> stringPool = getStringPool();
        if(stringPool==null){
            return null;
        }
        StringItem stringItem = stringPool.get(ref);
        if(stringItem==null){
            return null;
        }
        return stringItem.get();
    }
    private BaseStringPool<?> getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof BaseStringPool){
                return (BaseStringPool<?>)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }

    public String applyHtml(String str){
        if(str==null){
            return null;
        }
        List<StyleSpanInfo> spanInfoList = getSpanInfoList();
        if(spanInfoList.size()==0){
            return str;
        }
        StringBuilder builder=new StringBuilder();
        char[] allChars=str.toCharArray();
        int max=allChars.length;
        for(int i=0;i<max;i++){
            char ch=allChars[i];
            boolean lastAppend=false;
            for(StyleSpanInfo info:spanInfoList){
                boolean isLast=(info.getLast()==i);
                if(info.getFirst()==i || isLast){
                    if(isLast && !lastAppend){
                        builder.append(ch);
                        lastAppend=true;
                    }
                    if(isLast){
                        builder.append(info.getEndTag());
                    }else {
                        builder.append(info.getStartTag());
                    }
                }
            }
            if(!lastAppend){
                builder.append(ch);
            }
        }
        return builder.toString();
    }
    @Override
    public void onBytesChanged() {
    }
    @Override
    public void setNull(boolean is_null){
        if(!is_null){
            return;
        }
        setStylePieceCount(0);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int nextPos=reader.searchNextIntPosition(4, END_VALUE);
        if(nextPos<0){
            return;
        }
        int len=nextPos-reader.getPosition()+4;
        super.setBytesLength(len, false);
        byte[] bts=getBytesInternal();
        reader.readFully(bts);
        onBytesChanged();
    }
    public void addSpanInfo(String tag, int first, int last){
        int index=getStylePieceCount();
        setStylePieceCount(index+1);
        BaseStringPool<?> stringPool = getStringPool();
        if(stringPool==null){
            throw new IllegalArgumentException("Null string pool, must be added to parent StyleArray first");
        }
        StringItem stringItem=stringPool.getOrCreate(tag);
        setStylePiece(index, stringItem.getIndex(), first, last);
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(StyleSpanInfo spanInfo:getSpanInfoList()){
            JSONObject jsonObjectSpan=spanInfo.toJson();
            jsonArray.put(i, jsonObjectSpan);
            i++;
        }
        jsonObject.put(NAME_spans, jsonArray);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setNull(true);
        if(json==null){
            return;
        }
        JSONArray jsonArray= json.getJSONArray(NAME_spans);
        int length = jsonArray.length();
        for(int i=0;i<length;i++){
            JSONObject jsonObject=jsonArray.getJSONObject(i);
            StyleSpanInfo spanInfo=new StyleSpanInfo(null, 0, 0);
            spanInfo.fromJson(jsonObject);
            addSpanInfo(spanInfo.getTag(), spanInfo.getFirst(), spanInfo.getLast());
        }
    }
    @Override
    public String toString(){
        return "Spans count = "+getSpanInfoList().size();
    }
    private static final int INDEX_STRING_REF = 0;
    private static final int INDEX_CHAR_FIRST = 1;
    private static final int INDEX_CHAR_LAST = 2;

    private static final int INTEGERS_COUNT = 3;

    private static final int END_VALUE=0xFFFFFFFF;
    private static final String NAME_spans="spans";
}
