package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

public class PackageArray extends BlockArray<PackageBlock> implements BlockLoad, JSONConvert<JSONArray> {
    private final IntegerItem mPackageCount;
    public PackageArray(IntegerItem packageCount){
        this.mPackageCount=packageCount;
        mPackageCount.setBlockLoad(this);
    }
    public PackageBlock getOrCreate(byte pkgId){
        PackageBlock packageBlock=getPackageBlockById(pkgId);
        if(packageBlock!=null){
            return packageBlock;
        }
        packageBlock=createNext();
        packageBlock.setId(pkgId);
        packageBlock.setName("PACKAGE NAME");
        return packageBlock;
    }
    public PackageBlock getPackageBlockById(byte pkgId){
        Iterator<PackageBlock> itr=iterator(true);
        while (itr.hasNext()){
            PackageBlock packageBlock=itr.next();
            if(packageBlock.getId()==pkgId){
                return packageBlock;
            }
        }
        return null;
    }
    @Override
    public PackageBlock newInstance() {
        return new PackageBlock();
    }

    @Override
    public PackageBlock[] newInstance(int len) {
        return new PackageBlock[len];
    }

    @Override
    protected void onRefreshed() {
        refreshPackageCount();
    }
    private void refreshPackageCount(){
        mPackageCount.set(childesCount());
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==mPackageCount){
            setChildesCount(mPackageCount.get());
        }
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(PackageBlock packageBlock:listItems()){
            JSONObject jsonObject= packageBlock.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        int length= json.length();
        clearChildes();
        ensureSize(length);
        for (int i=0;i<length;i++){
            JSONObject jsonObject=json.getJSONObject(i);
            PackageBlock packageBlock=get(i);
            packageBlock.fromJson(jsonObject);
        }
    }
}
