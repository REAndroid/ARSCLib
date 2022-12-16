package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ByteArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.StyleItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;

import java.io.IOException;

public class StyleArray extends OffsetBlockArray<StyleItem> implements JSONConvert<JSONArray> {
    public StyleArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart) {
        super(offsets, itemCount, itemStart);
        setEndBytes(END_BYTE);
    }
    @Override
    void refreshEnd4Block(BlockReader reader, ByteArray end4Block) throws IOException {
        end4Block.clear();
        if(reader.available()<4){
            return;
        }
        IntegerItem integerItem=new IntegerItem();
        while (reader.available()>=4){
            int pos=reader.getPosition();
            integerItem.readBytes(reader);
            if(integerItem.get()!=0xFFFFFFFF){
                reader.seek(pos);
                break;
            }
            end4Block.add(integerItem.getBytes());
        }
    }
    @Override
    void refreshEnd4Block(ByteArray end4Block) {
        super.refreshEnd4Block(end4Block);
        if(childesCount()==0){
            return;
        }
        end4Block.ensureArraySize(8);
        end4Block.fill(END_BYTE);
    }
    @Override
    protected void refreshChildes(){
        // Not required
    }
    @Override
    public StyleItem newInstance() {
        return new StyleItem();
    }
    @Override
    public StyleItem[] newInstance(int len) {
        return new StyleItem[len];
    }

    @Override
    public JSONArray toJson() {
        if(childesCount()==0){
            return null;
        }
        return null;
    }
    @Override
    public void fromJson(JSONArray json) {

    }
    public void merge(StyleArray styleArray){
        if(styleArray==null||styleArray==this){
            return;
        }
        if(childesCount()!=0){
            return;
        }
        int count=styleArray.childesCount();
        ensureSize(count);
        for(int i=0;i<count;i++){
            StyleItem exist=get(i);
            StyleItem coming=styleArray.get(i);
            exist.merge(coming);
        }
    }
    private static final byte END_BYTE= (byte) 0xFF;
}
