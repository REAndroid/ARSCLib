package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ResXmlID;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ResXmlIDArray extends BlockArray<ResXmlID>  {
    private final HeaderBlock mHeaderBlock;
    private final Map<Integer, ResXmlID> mResIdMap;
    private boolean mUpdated;
    public ResXmlIDArray(HeaderBlock headerBlock){
        super();
        this.mHeaderBlock=headerBlock;
        this.mResIdMap=new HashMap<>();
    }
    public void addResourceId(int index, int resId){
        if(index<0){
            return;
        }
        ensureSize(index+1);
        ResXmlID xmlID=get(index);
        if(xmlID!=null){
            xmlID.set(resId);
        }
    }
    public ResXmlID getOrCreate(int resId){
        updateIdMap();
        ResXmlID xmlID=mResIdMap.get(resId);
        if(xmlID!=null){
            return xmlID;
        }
        xmlID=new ResXmlID(resId);
        add(xmlID);
        mUpdated=true;
        mResIdMap.put(resId, xmlID);
        return xmlID;
    }
    public ResXmlID getByResId(int resId){
        updateIdMap();
        return mResIdMap.get(resId);
    }
    private void updateIdMap(){
        if(mUpdated){
            return;
        }
        mUpdated=true;
        mResIdMap.clear();
        ResXmlID[] allChildes=getChildes();
        if(allChildes==null||allChildes.length==0){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            ResXmlID xmlID=allChildes[i];
            mResIdMap.put(xmlID.get(), xmlID);
        }
    }
    @Override
    public ResXmlID newInstance() {
        mUpdated=false;
        return new ResXmlID();
    }
    @Override
    public ResXmlID[] newInstance(int len) {
        mUpdated=false;
        return new ResXmlID[len];
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int count=calculateCountFromHeaderBlock();
        setChildesCount(count);
        super.onReadBytes(reader);
        updateIdMap();
    }
    private int calculateCountFromHeaderBlock(){
        int count=mHeaderBlock.getChunkSize()-mHeaderBlock.getHeaderSize();
        count=count/4;
        return count;
    }
}
