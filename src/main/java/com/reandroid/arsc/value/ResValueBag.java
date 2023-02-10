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
package com.reandroid.arsc.value;

import com.reandroid.arsc.array.ResValueBagItemArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.json.JSONObject;

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
        mResValueBagItemArray.setChildesCount(count);
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
        mCount.set(getResValueBagItemArray().childesCount());
    }
    @Override
    protected int onWriteBytes(OutputStream writer) throws IOException {
        if(isNull()){
            return 0;
        }
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
    public void merge(ResValueBag resValueBag){
        if(resValueBag==null || resValueBag==this || resValueBag.getCount()==0){
            return;
        }
        setParentId(resValueBag.getParentId());
        getResValueBagItemArray().merge(resValueBag.getResValueBagItemArray());
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
