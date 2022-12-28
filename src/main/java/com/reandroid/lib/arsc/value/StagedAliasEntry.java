package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.item.ByteArray;

public class StagedAliasEntry extends ByteArray {
    public StagedAliasEntry(){
        super(8);
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
}
