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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class LibraryInfo extends Block implements JSONConvert<JSONObject> {
    private final IntegerItem mPackageId;
    private final FixedLengthString mPackageName;

    public LibraryInfo(){
        super();
        this.mPackageId=new IntegerItem();
        this.mPackageName = new FixedLengthString(256);
        mPackageId.setIndex(0);
        mPackageId.setParent(this);
        mPackageName.setIndex(1);
        mPackageName.setParent(this);
    }

    public int getPackageId(){
        return mPackageId.get();
    }
    public void setPackageId(int id){
        mPackageId.set(id);
    }
    public String getPackageName(){
        return mPackageName.get();
    }
    public void setPackageName(String packageName){
        mPackageName.set(packageName);
    }

    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return addBytes(mPackageId.getBytes(), mPackageName.getBytes());
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return mPackageId.countBytes()+mPackageName.countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        mPackageId.onCountUpTo(counter);
        mPackageName.onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        int result=mPackageId.writeBytes(stream);
        result+=mPackageName.writeBytes(stream);
        return result;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        mPackageId.readBytes(reader);
        mPackageName.readBytes(reader);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", getPackageId());
        jsonObject.put("name", getPackageName());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setPackageId(json.getInt("id"));
        setPackageName(json.getString("name"));
    }
    public void merge(LibraryInfo info){
        if(info==null||info==this){
            return;
        }
        if(getPackageId()!=info.getPackageId()){
            throw new IllegalArgumentException("Can not add different id libraries: "
                    +getPackageId()+"!="+info.getPackageId());
        }
        setPackageName(info.getPackageName());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append("LIBRARY{");
        builder.append(HexUtil.toHex2((byte) getPackageId()));
        builder.append(':');
        String name=getPackageName();
        if(name==null){
            name="NULL";
        }
        builder.append(name);
        builder.append('}');
        return builder.toString();
    }
}
