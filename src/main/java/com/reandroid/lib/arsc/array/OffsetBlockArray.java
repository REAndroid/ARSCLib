package com.reandroid.lib.arsc.array;


import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ByteArray;
import com.reandroid.lib.arsc.item.IntegerArray;
import com.reandroid.lib.arsc.item.IntegerItem;


import java.io.IOException;
import java.io.OutputStream;

public abstract class OffsetBlockArray<T extends Block> extends BlockArray<T> implements BlockLoad {
    private final IntegerArray mOffsets;
    private final IntegerItem mItemStart;
    private final IntegerItem mItemCount;
    private final ByteArray mEnd4Block;
    private byte mEnd4Type;
    public OffsetBlockArray(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart){
        super();
        this.mOffsets=offsets;
        this.mItemCount=itemCount;
        this.mItemStart=itemStart;
        this.mEnd4Block=new ByteArray();
        mItemCount.setBlockLoad(this);
    }
    void setEndBytes(byte b){
        this.mEnd4Type=b;
        this.mEnd4Block.fill(b);
    }
    @Override
    public void clearChildes(){
        super.clearChildes();
        mOffsets.clear();
        mItemStart.set(0);
        mItemCount.set(0);
        mEnd4Block.clear();
    }
    @Override
    public int countBytes(){
        int result=super.countBytes();
        int endCount=mEnd4Block.countBytes();
        return result+endCount;
    }
    @Override
    public void onCountUpTo(BlockCounter counter){
        super.onCountUpTo(counter);
        if(counter.FOUND){
            return;
        }
        mEnd4Block.onCountUpTo(counter);
    }
    @Override
    public byte[] getBytes(){
        byte[] results=super.getBytes();
        if(results==null){
            return null;
        }
        byte[] endBytes=mEnd4Block.getBytes();
        results=addBytes(results, endBytes);
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        int result=super.onWriteBytes(stream);
        if(result==0){
            return 0;
        }
        result+=mEnd4Block.writeBytes(stream);
        return result;
    }
    @Override
    protected void onRefreshed() {
        int count=childesCount();
        mOffsets.setSize(count);
        T[] childes=getChildes();
        int sum=0;
        if(childes!=null){
            int max=childes.length;
            for(int i=0;i<max;i++){
                T item=childes[i];
                int offset;
                if(item==null || item.isNull()){
                    offset=-1;
                }else {
                    // slow but accurate
                    //offset=countUpTo(item);

                    // fast but fails for duplicate items
                    offset=sum;
                    sum+=item.countBytes();
                }
                mOffsets.put(i, offset);
            }
        }
        refreshCount();
        refreshStart();
        refreshEnd4Block();
    }
    public void refreshCountAndStart(){
        refreshCount();
        refreshStart();
    }
    void refreshCount(){
        mItemCount.set(childesCount());
    }
    private void refreshStart(){
        int count=childesCount();
        if(count==0){
            mItemStart.set(0);
            mEnd4Block.clear();
            return;
        }
        Block parent=getParent();
        if(parent==null){
            return;
        }
        int start=parent.countUpTo(this);
        mItemStart.set(start);
    }
    void refreshEnd4Block(BlockReader reader, ByteArray end4Block) throws IOException{
        refreshEnd4Block();
    }
    void refreshEnd4Block(ByteArray end4Block){
        if(childesCount()==0){
            end4Block.clear();
            return;
        }
        int count=countBytes();
        if(count%4==0){
            return;
        }
        end4Block.clear();
        count=countBytes();
        int add=0;
        int rem=count%4;
        while (rem!=0){
            add++;
            count++;
            rem=count%4;
        }
        end4Block.setSize(add);
        end4Block.fill(mEnd4Type);
    }
    private void refreshEnd4Block(){
        refreshEnd4Block(mEnd4Block);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        T[] childes=getChildes();
        if(childes==null||childes.length==0){
            return;
        }
        int[] offsetArray=mOffsets.toArray();
        int max=childes.length;
        int start=mItemStart.get();
        reader.seek(start);
        int zeroPosition=reader.getPosition();
        int maxPos=zeroPosition;
        for(int i=0;i<max;i++){
            T item=childes[i];
            int offset=offsetArray[i];
            if(offset==-1){
                item.setNull(true);
                continue;
            }
            int itemStart=zeroPosition+offset;
            reader.seek(itemStart);
            item.readBytes(reader);
            int pos=reader.getPosition();
            if(pos>maxPos){
                maxPos=pos;
            }
        }
        reader.seek(maxPos);
        refreshEnd4Block(reader, mEnd4Block);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==mItemCount){
            int count=mItemCount.get();
            setChildesCount(count);
            mOffsets.setSize(count);
        }
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": count = ");
        int s= childesCount();
        builder.append(s);
        int count=mItemCount.get();
        if(s!=count){
            builder.append(", countValue=");
            builder.append(count);
        }
        builder.append(", start=");
        builder.append(mItemStart.get());
        return builder.toString();
    }

}
