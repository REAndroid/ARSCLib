package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.TypeBlockArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;

public class SpecBlock extends BaseTypeBlock implements BlockLoad , JSONConvert<JSONObject> {
    private final IntegerArray mOffsets;
    public SpecBlock() {
        super(ChunkType.SPEC, 1);
        this.mOffsets=new IntegerArray();
        addChild(mOffsets);

        getEntryCountBlock().setBlockLoad(this);
    }
    public TypeBlockArray getTypeBlockArray(){
        SpecTypePair specTypePair=getSpecTypePair();
        if(specTypePair!=null){
            return specTypePair.getTypeBlockArray();
        }
        return null;
    }
    @Override
    void onSetEntryCount(int count) {
        mOffsets.setSize(count);
    }
    @Override
    protected void onChunkRefreshed() {
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        IntegerItem entryCount=getEntryCountBlock();
        if(sender==entryCount){
            mOffsets.setSize(entryCount.get());
        }
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        TypeBlockArray typeBlockArray=getTypeBlockArray();
        if(typeBlockArray!=null){
            builder.append(", typesCount=");
            builder.append(typeBlockArray.childesCount());
        }
        return builder.toString();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", getTypeId());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        setTypeId((byte) json.getInt("id"));
    }
}
