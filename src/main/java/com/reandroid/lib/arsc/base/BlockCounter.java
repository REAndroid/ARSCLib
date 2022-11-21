package com.reandroid.lib.arsc.base;

public class BlockCounter {
    public final Block END;
    public boolean FOUND;
    int COUNT;
    BlockCounter(Block end){
        this.END=end;
    }
    public void addCount(int val){
        if(FOUND){
            return;
        }
        COUNT+=val;
    }
    @Override
    public String toString(){
        if(FOUND){
            return "FOUND="+COUNT;
        }
        return String.valueOf(COUNT);
    }
}
