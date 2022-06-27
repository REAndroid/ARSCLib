package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.model.StyleSpanInfo;
import com.reandroid.lib.arsc.pool.BaseStringPool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StyleItem extends IntegerArray{
    private List<int[]> mStyleList;
    public StyleItem() {
        super();
        mStyleList=createStyleList();
    }
    private void setEndValue(int negOne){
        super.put(size()-1, negOne);
    }
    final Integer getEndValue(){
        return super.get(size()-1);
    }
    final Integer getStringRef(int index){
        int i=index*STYLE_PIECE_COUNT+STRING_REF;
        return super.get(i);
    }
    final void setStringRef(int index, int val){
        int i=index*STYLE_PIECE_COUNT+STRING_REF;
        super.put(i, val);
    }
    final Integer getFirstChar(int index){
        int i=index*STYLE_PIECE_COUNT+CHAR_FIRST;
        return super.get(i);
    }
    final void setFirstChar(int index, int val){
        int i=index*STYLE_PIECE_COUNT+CHAR_FIRST;
        super.put(i, val);
    }
    final Integer getLastChar(int index){
        int i=index*STYLE_PIECE_COUNT+CHAR_LAST;
        return super.get(i);
    }
    final void setLastChar(int index, int val){
        int i=index*STYLE_PIECE_COUNT+CHAR_LAST;
        super.put(i, val);
    }
    final void setStylePiece(int index, int refString, int firstChar, int lastChar){
        int i=index*STYLE_PIECE_COUNT;
        super.put(i+STRING_REF, refString);
        super.put(i+CHAR_FIRST, firstChar);
        super.put(i+CHAR_LAST, lastChar);
    }
    final int[] getStylePiece(int index){
        if(index<0||index>= getStylePieceCount()){
            return null;
        }
        int[] result=new int[STYLE_PIECE_COUNT];
        int i=index*STYLE_PIECE_COUNT;
        result[STRING_REF]=super.get(i);
        result[CHAR_FIRST]=super.get(i+CHAR_FIRST);
        result[CHAR_LAST]=super.get(i+CHAR_LAST);
        return result;
    }
    final void setStylePiece(int index, int[] three){
        if(three==null || three.length<STYLE_PIECE_COUNT){
            return;
        }
        int i=index*STYLE_PIECE_COUNT;
        super.put(i+STRING_REF, three[STRING_REF]);
        super.put(i+CHAR_FIRST, three[CHAR_FIRST]);
        super.put(i+CHAR_LAST, three[CHAR_LAST]);
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
        return sz/STYLE_PIECE_COUNT;
    }
    final void setStylePieceCount(int count){
        if(count<0){
            count=0;
        }
        int cur= getStylePieceCount();
        if(count==cur){
            return;
        }
        int max=count*STYLE_PIECE_COUNT+1;
        if(size()==0 || count==0){
            super.setSize(max);
            setEndValue(END_VALUE);
            return;
        }
        List<int[]> copy=new ArrayList<>(mStyleList);
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
    final List<int[]> getStyleList(){
        return mStyleList;
    }
    private List<int[]> createStyleList(){
        List<int[]> results=new ArrayList<>();
        int max=getStylePieceCount();
        for(int i=0;i<max;i++){
            results.add(getStylePiece(i));
        }
        return results;
    }
    final List<StyleSpanInfo> getSpanInfo(){
        BaseStringPool stringPool= getStringPool();
        if(stringPool==null){
            return null;
        }
        List<int[]> allPiece=getStyleList();
        List<StyleSpanInfo> results=new ArrayList<>();
        for(int[] piece:allPiece){
            StringItem stringItem=stringPool.get(piece[STRING_REF]);
            if(stringItem==null){
                return null;
            }
            StyleSpanInfo info=new StyleSpanInfo(stringItem.get(), piece[CHAR_FIRST], piece[CHAR_LAST]);
            results.add(info);
        }
        if(results.size()==0){
            return null;
        }
        return results;
    }
    private BaseStringPool getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof BaseStringPool){
                return (BaseStringPool)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }

    public String applyHtml(String str){
        if(str==null){
            return null;
        }
        List<StyleSpanInfo> allInfo=getSpanInfo();
        if(allInfo==null){
            return str;
        }
        StringBuilder builder=new StringBuilder();
        char[] allChars=str.toCharArray();
        int max=allChars.length;
        for(int i=0;i<max;i++){
            char ch=allChars[i];
            boolean lastAppend=false;
            for(StyleSpanInfo info:allInfo){
                boolean isLast=(info.LAST==i);
                if(info.FIRST==i || isLast){
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
        mStyleList=createStyleList();
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
    private static final int STRING_REF=0;
    private static final int CHAR_FIRST=1;
    private static final int CHAR_LAST=2;

    private static final int STYLE_PIECE_COUNT=3;

    private static final int END_VALUE=0xFFFFFFFF;
}
