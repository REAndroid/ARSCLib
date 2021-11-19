package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.xml.ResXmlStartElement;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.item.ShortItem;

import java.io.IOException;

public class ResXmlAttributeArray extends BlockArray<ResXmlAttribute> {
    private final HeaderBlock mHeaderBlock;
    private final ShortItem mAttributeStart;
    private final ShortItem mAttributeCount;
    public ResXmlAttributeArray(HeaderBlock headerBlock, ShortItem attributeStart, ShortItem attributeCount){
        this.mHeaderBlock=headerBlock;
        this.mAttributeStart=attributeStart;
        this.mAttributeCount=attributeCount;
    }
    private void refreshCount(){
        short count= (short) childesCount();
        mAttributeCount.set(count);
    }
    private void refreshStart(){
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int start = parent.countUpTo(this);
        start=start-mHeaderBlock.countBytes();
        mAttributeStart.set((short) start);
    }
    @Override
    public ResXmlAttribute newInstance() {
        return new ResXmlAttribute();
    }
    @Override
    public ResXmlAttribute[] newInstance(int len) {
        return new ResXmlAttribute[len];
    }
    @Override
    protected void onRefreshed() {
        refreshCount();
        refreshStart();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int start=mHeaderBlock.getHeaderSize()+mAttributeStart.get();
        reader.seek(start);
        setChildesCount(mAttributeCount.get());
        super.onReadBytes(reader);
    }
}
