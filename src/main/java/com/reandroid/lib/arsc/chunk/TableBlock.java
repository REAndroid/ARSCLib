package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.pool.TableStringPool;

import java.io.*;
import java.util.Collection;

public class TableBlock extends BaseChunk  {
    private final IntegerItem mPackageCount;
    private final TableStringPool mTableStringPool;
    private final PackageArray mPackageArray;
    public TableBlock() {
        super(ChunkType.TABLE, 2);
        this.mPackageCount=new IntegerItem();
        this.mTableStringPool=new TableStringPool(true);
        this.mPackageArray=new PackageArray(mPackageCount);
        addToHeader(mPackageCount);
        addChild(mTableStringPool);
        addChild(mPackageArray);
    }
    public Collection<PackageBlock> listPackages(){
        return getPackageArray().listItems();
    }
    public TableStringPool getTableStringPool(){
        return mTableStringPool;
    }
    public PackageBlock getPackageBlockById(byte pkgId){
        return getPackageArray().getPackageBlockById(pkgId);
    }
    public PackageArray getPackageArray(){
        return mPackageArray;
    }

    private void refreshPackageCount(){
        int count = getPackageArray().childesCount();
        mPackageCount.set(count);
    }
    @Override
    protected void onChunkRefreshed() {
        refreshPackageCount();
    }

    @Override
    public int onWriteBytes(OutputStream stream) throws IOException{
        int result=super.onWriteBytes(stream);
        stream.flush();
        stream.close();
        return result;
    }
    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        OutputStream outputStream=new FileOutputStream(file);
        return super.writeBytes(outputStream);
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", packages=");
        int pkgCount=mPackageArray.childesCount();
        builder.append(pkgCount);
        return builder.toString();
    }


    public static boolean isResTableBlock(File file){
        if(file==null){
            return false;
        }
        try {
            InputStream inputStream=new FileInputStream(file);
            return isResTableBlock(inputStream);
        } catch (FileNotFoundException ignored) {
            return false;
        }
    }
    public static boolean isResTableBlock(InputStream inputStream){
        try {
            HeaderBlock headerBlock= BlockReader.readHeaderBlock(inputStream);
            return isResTableBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResTableBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResTableBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResTableBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.TABLE;
    }

}
