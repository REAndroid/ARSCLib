package com.reandroid.arsc.value;

import com.reandroid.arsc.item.ByteArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

public class StagedAliasEntry extends ByteArray implements JSONConvert<JSONObject> {
    public StagedAliasEntry(){
        super(8);
    }
    public boolean isEqual(StagedAliasEntry other){
        if(other==null){
            return false;
        }
        if(other==this){
            return true;
        }
        return getStagedResId()==other.getStagedResId()
                && getFinalizedResId()==other.getFinalizedResId();
    }
    public int getStagedResId(){
        return getInteger(0);
    }
    public void setStagedResId(int id){
         putInteger(0, id);
    }
    public int getFinalizedResId(){
        return getInteger(4);
    }
    public void setFinalizedResId(int id){
        putInteger(4, id);
    }
    @Override
    public String toString(){
        return "stagedResId="+String.format("0x%08x",getStagedResId())
                +", finalizedResId="+String.format("0x%08x",getFinalizedResId());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_staged_resource_id, getStagedResId());
        jsonObject.put(NAME_finalized_resource_id, getFinalizedResId());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setStagedResId(json.getInt(NAME_staged_resource_id));
        setFinalizedResId(json.getInt(NAME_finalized_resource_id));
    }
    public static final String NAME_staged_resource_id = "staged_resource_id";
    public static final String NAME_finalized_resource_id = "finalized_resource_id";
}
