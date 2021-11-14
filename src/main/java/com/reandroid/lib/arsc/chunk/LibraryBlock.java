package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.LibraryInfoArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.value.LibraryInfo;

public class LibraryBlock extends BaseChunk {
    private final IntegerItem mLibCount;
    private final LibraryInfoArray mLibraryInfoArray;
    public LibraryBlock() {
        super(ChunkType.LIBRARY,1);
        this.mLibCount=new IntegerItem();
        this.mLibraryInfoArray=new LibraryInfoArray(mLibCount);

        addToHeader(mLibCount);
        addChild(mLibraryInfoArray);
    }
    public LibraryInfo[] getAllInfo(){
        return mLibraryInfoArray.getChildes();
    }
    public void addLibraryInfo(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        LibraryInfo[] allInfo=libraryBlock.getAllInfo();
        if (allInfo==null){
            return;
        }
        for(LibraryInfo info:allInfo){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        if(info==null){
            return;
        }
        mLibraryInfoArray.add(info);
        mLibCount.set(mLibraryInfoArray.childesCount());
    }
    @Override
    public boolean isNull(){
        return mLibraryInfoArray.childesCount()==0;
    }
    public int getLibraryCount(){
        return mLibraryInfoArray.childesCount();
    }
    public void setLibraryCount(int count){
        mLibCount.set(count);
        mLibraryInfoArray.setChildesCount(count);
    }

    @Override
    protected void onChunkRefreshed() {
        mLibCount.set(mLibraryInfoArray.childesCount());
    }

}
