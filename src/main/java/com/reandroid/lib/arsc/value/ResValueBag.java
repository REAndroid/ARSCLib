package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.array.ResValueBagItemArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ResValueBag extends BaseResValue {

    private final IntegerItem mParentId;
    private final IntegerItem mCount;
    private final ResValueBagItemArray mResValueBagItemArray;
    public ResValueBag(){
        super(0);
        mParentId =new IntegerItem();
        mCount=new IntegerItem();
        mResValueBagItemArray=new ResValueBagItemArray();

        mParentId.setParent(this);
        mParentId.setIndex(0);
        mCount.setParent(this);
        mCount.setIndex(1);
        mResValueBagItemArray.setParent(this);
        mResValueBagItemArray.setIndex(2);
    }
    public ResValueBagItem[] getBagItems(){
        return getResValueBagItemArray().getChildes();
    }
    public List<ReferenceItem> getTableStringReferences(){
        List<ReferenceItem> results=null;
        for(ResValueBagItem bagItem:getResValueBagItemArray().listItems()){
            ReferenceItem ref=bagItem.getTableStringReference();
            if(ref==null){
                continue;
            }
            if(results==null){
                results=new ArrayList<>();
            }
            results.add(ref);
        }
        return results;
    }
    public ResValueBagItemArray getResValueBagItemArray(){
        return mResValueBagItemArray;
    }

    public int getParentId(){
        return mParentId.get();
    }
    public void setParentId(int id){
        mParentId.set(id);
    }
    public int getCount(){
        return mCount.get();
    }
    public void setCount(int count){
        if(count<0){
            count=0;
        }
        mCount.set(count);
    }

    @Override
    public byte[] getBytes() {
        byte[] results = mParentId.getBytes();
        results=addBytes(results, mCount.getBytes());
        results=addBytes(results, mResValueBagItemArray.getBytes());
        return results;
    }
    @Override
    public int countBytes() {
        int result;
        result = mParentId.countBytes();
        result+=mCount.countBytes();
        result+=mResValueBagItemArray.countBytes();
        return result;
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(countSubChildes(counter, mParentId)){
            return;
        }
        if(countSubChildes(counter, mCount)){
            return;
        }
        countSubChildes(counter, mResValueBagItemArray);
    }
    private boolean countSubChildes(BlockCounter counter, Block child){
        if(counter.FOUND){
            return true;
        }
        if(counter.END==child){
            counter.FOUND=true;
            return true;
        }
        int c=child.countBytes();
        counter.addCount(c);
        return false;
    }

    private void refreshCount(){
        setCount(getResValueBagItemArray().childesCount());
    }
    @Override
    protected int onWriteBytes(OutputStream writer) throws IOException {
        if(isNull()){
            return 0;
        }
        refreshCount();
        int result;
        result=mParentId.writeBytes(writer);
        result+=mCount.writeBytes(writer);
        result+=mResValueBagItemArray.writeBytes(writer);
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        mResValueBagItemArray.clearChildes();
        mParentId.readBytes(reader);
        mCount.readBytes(reader);
        mResValueBagItemArray.setChildesCount(mCount.get());
        mResValueBagItemArray.readBytes(reader);
    }
    @Override
    void onDataLoaded() {
        for(ResValueBagItem item: mResValueBagItemArray.listItems()){
            item.refreshTableReference();
        }
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_parent, getParentId());
        jsonObject.put(NAME_items, getResValueBagItemArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setParentId(json.getInt(NAME_parent));
        getResValueBagItemArray().fromJson(json.getJSONArray(NAME_items));
        refreshCount();
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": parent=");
        builder.append(String.format("0x%08x", getParentId()));
        builder.append(", count=");
        builder.append(getCount());
        return builder.toString();
    }

}
