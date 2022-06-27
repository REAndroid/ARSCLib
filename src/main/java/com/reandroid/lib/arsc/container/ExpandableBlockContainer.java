package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockContainer;

public  class ExpandableBlockContainer extends BlockContainer<Block> {
    private Block[] mChildes;
    private int mCursor;
    public ExpandableBlockContainer(int initialSize){
        super();
        this.mChildes=new Block[initialSize];
    }
    public final void addChild(Block block){
        if(block==null){
            return;
        }
        int index=mCursor;
        ensureCount(index+1);
        mChildes[index]=block;
        block.setIndex(index);
        block.setParent(this);
        mCursor++;
    }
    private void ensureCount(int count){
        if(count<= childesCount()){
            return;
        }
        Block[] old=mChildes;
        mChildes=new Block[count];
        for(int i=0;i<old.length;i++){
            mChildes[i]=old[i];
        }
    }
    @Override
    protected void onRefreshed() {

    }

    @Override
    public final int childesCount() {
        return mChildes.length;
    }
    @Override
    public final Block[] getChildes() {
        return mChildes;
    }
}
