package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.PackageArray;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.common.Frameworks;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TableBlock extends BaseChunk  {
    private final IntegerItem mPackageCount;
    private final TableStringPool mTableStringPool;
    private final PackageArray mPackageArray;
    private final Set<TableBlock> mFrameWorks=new HashSet<>();
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
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
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
    public EntryGroup search(int resourceId){
        if(resourceId==0){
            return null;
        }
        int pkgId=resourceId>>24;
        pkgId=pkgId&0xff;
        PackageBlock packageBlock=getPackageBlockById((byte) pkgId);
        if(packageBlock!=null){
            EntryGroup entryGroup=packageBlock.getEntryGroup(resourceId);
            if(entryGroup!=null){
                return entryGroup;
            }
        }
        for(TableBlock tableBlock:getFrameWorks()){
            EntryGroup entryGroup= tableBlock.search(resourceId);
            if(entryGroup!=null){
                return entryGroup;
            }
        }
        return null;
    }
    public Set<TableBlock> getFrameWorks(){
        return mFrameWorks;
    }
    public void addFramework(TableBlock tableBlock){
        if(tableBlock==null||tableBlock==this){
            return;
        }
        for(TableBlock frm:tableBlock.getFrameWorks()){
            if(frm==this){
                return;
            }
        }
        mFrameWorks.add(tableBlock);
    }
    public static TableBlock loadWithAndroidFramework(InputStream inputStream) throws IOException{
        TableBlock tableBlock=new TableBlock();
        tableBlock.readBytes(inputStream);
        tableBlock.addFramework(Frameworks.getAndroid());
        return tableBlock;
    }

    public static boolean isResTableBlock(File file){
        if(file==null){
            return false;
        }
        boolean result=false;
        try {
            InputStream inputStream=new FileInputStream(file);
            result=isResTableBlock(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return result;
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
    public static final String FILE_NAME="resources.arsc";

}
