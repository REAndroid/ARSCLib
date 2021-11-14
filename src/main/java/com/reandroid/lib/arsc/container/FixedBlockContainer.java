package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockContainer;

public class FixedBlockContainer extends BlockContainer<Block> {
    private final Block[] mChildes;
    public FixedBlockContainer(int childesCount){
        super();
        mChildes=new Block[childesCount];
    }
    public void addChild(int index, Block block){
        mChildes[index]=block;
        block.setIndex(index);
        block.setParent(this);
    }
    @Override
    protected void onRefreshed(){
    }
    @Override
    public int childesCount() {
        return mChildes.length;
    }
    @Override
    public Block[] getChildes() {
        return mChildes;
    }
}
