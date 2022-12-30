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
package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.common.Frameworks;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableBlock extends BaseChunk implements JSONConvert<JSONObject> {
    private final IntegerItem mPackageCount;
    private final TableStringPool mTableStringPool;
    private final PackageArray mPackageArray;
    private final Set<TableBlock> mFrameWorks=new HashSet<>();
    public TableBlock() {
        super(ChunkType.TABLE, 2);
        this.mPackageCount=new IntegerItem();
        this.mTableStringPool=new TableStringPool(true);
        this.mPackageArray=new PackageArray(mPackageCount);
        addToHeader(mPackageCount);
        addChild(mTableStringPool);
        addChild(mPackageArray);
    }
    public void sortPackages(){
        getPackageArray().sort();
    }
    public Collection<PackageBlock> listPackages(){
        return getPackageArray().listItems();
    }
    public TableStringPool getTableStringPool(){
        return mTableStringPool;
    }
    public PackageBlock getPackageBlockById(int pkgId){
        return getPackageArray().getPackageBlockById(pkgId);
    }
    public PackageArray getPackageArray(){
        return mPackageArray;
    }

    private void refreshPackageCount(){
        int count = getPackageArray().childesCount();
        mPackageCount.set(count);
    }
    @Override
    protected void onChunkRefreshed() {
        refreshPackageCount();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        reader.close();
    }

    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        OutputStream outputStream=new FileOutputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", packages=");
        int pkgCount=mPackageArray.childesCount();
        builder.append(pkgCount);
        return builder.toString();
    }
    public EntryGroup search(int resourceId){
        if(resourceId==0){
            return null;
        }
        int pkgId=resourceId>>24;
        pkgId=pkgId&0xff;
        PackageBlock packageBlock=getPackageBlockById(pkgId);
        if(packageBlock!=null){
            EntryGroup entryGroup=packageBlock.getEntryGroup(resourceId);
            if(entryGroup!=null){
                return entryGroup;
            }
        }
        for(TableBlock tableBlock:getFrameWorks()){
            EntryGroup entryGroup= tableBlock.search(resourceId);
            if(entryGroup!=null){
                return entryGroup;
            }
        }
        return null;
    }
    public Set<TableBlock> getFrameWorks(){
        return mFrameWorks;
    }
    public void addFramework(TableBlock tableBlock){
        if(tableBlock==null||tableBlock==this){
            return;
        }
        for(TableBlock frm:tableBlock.getFrameWorks()){
            if(frm==this){
                return;
            }
        }
        mFrameWorks.add(tableBlock);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_packages, getPackageArray().toJson());
        JSONArray jsonArray = getTableStringPool().toJson();
        if(jsonArray!=null){
            jsonObject.put(NAME_styled_strings, jsonArray);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getPackageArray().fromJson(json.getJSONArray(NAME_packages));
        refresh();
    }
    public void merge(TableBlock tableBlock){
        if(tableBlock==null||tableBlock==this){
            return;
        }
        if(getPackageArray().childesCount()==0){
            getTableStringPool().merge(tableBlock.getTableStringPool());
        }
        getPackageArray().merge(tableBlock.getPackageArray());
        refresh();
    }
    public static TableBlock loadWithAndroidFramework(InputStream inputStream) throws IOException{
        TableBlock tableBlock=load(inputStream);
        tableBlock.addFramework(Frameworks.getAndroid());
        return tableBlock;
    }
    public static TableBlock load(File file) throws IOException{
        return load(new FileInputStream(file));
    }
    public static TableBlock load(InputStream inputStream) throws IOException{
        TableBlock tableBlock=new TableBlock();
        tableBlock.readBytes(inputStream);
        return tableBlock;
    }

    public static boolean isResTableBlock(File file){
        if(file==null){
            return false;
        }
        boolean result=false;
        try {
            InputStream inputStream=new FileInputStream(file);
            result=isResTableBlock(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return result;
    }
    public static boolean isResTableBlock(InputStream inputStream){
        try {
            HeaderBlock headerBlock= BlockReader.readHeaderBlock(inputStream);
            return isResTableBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResTableBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResTableBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResTableBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.TABLE;
    }
    public static final String FILE_NAME="resources.arsc";

    private static final String NAME_packages="packages";
    public static final String NAME_styled_strings="styled_strings";
}
