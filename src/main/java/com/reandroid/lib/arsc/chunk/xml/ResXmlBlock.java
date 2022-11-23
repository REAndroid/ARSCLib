package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.chunk.BaseChunk;
import com.reandroid.lib.arsc.container.SingleBlockContainer;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

import java.io.*;

public class ResXmlBlock extends BaseChunk {
    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private ResXmlElement mResXmlElement;
    private final SingleBlockContainer<ResXmlElement> mResXmlElementContainer;
    public ResXmlBlock() {
        super(ChunkType.XML,3);
        this.mResXmlStringPool=new ResXmlStringPool(true);
        this.mResXmlIDMap=new ResXmlIDMap();
        this.mResXmlElement=new ResXmlElement();
        this.mResXmlElementContainer=new SingleBlockContainer<>();
        this.mResXmlElementContainer.setItem(mResXmlElement);
        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mResXmlElementContainer);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return;
        }
        BlockReader chunkReader=reader.create(reader.getPosition(), headerBlock.getChunkSize());
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.XML){
            getHeaderBlock().readBytes(chunkReader);
        }else {
            throw new IOException("Not ResXmlBlock: "+reader+", Header="+headerBlock);
        }
        while (chunkReader.isAvailable()){
            boolean readOk=readNext(chunkReader);
            if(!readOk){
                break;
            }
        }
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    private boolean readNext(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return false;
        }
        int position=reader.getPosition();
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.STRING){
            mResXmlStringPool.readBytes(reader);
        }else if(chunkType==ChunkType.XML_RESOURCE_MAP){
            mResXmlIDMap.readBytes(reader);
        }else if(isElementChunk(chunkType)){
            mResXmlElementContainer.readBytes(reader);
            return reader.isAvailable();
        }else {
            throw new IOException("Unexpected chunk "+headerBlock);
        }
        return reader.isAvailable() && position!=reader.getPosition();
    }
    private boolean isElementChunk(ChunkType chunkType){
        if(chunkType==ChunkType.XML_START_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_END_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_START_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_END_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_CDATA){
            return true;
        }
        if(chunkType==ChunkType.XML_LAST_CHUNK){
            return true;
        }
        return false;
    }
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getResXmlElement(){
        return mResXmlElement;
    }
    public void setResXmlElement(ResXmlElement resXmlElement){
        this.mResXmlElement=resXmlElement;
        this.mResXmlElementContainer.setItem(resXmlElement);
    }
    @Override
    protected void onChunkRefreshed() {

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

    public static boolean isResXmlBlock(File file){
        if(file==null){
            return false;
        }
        try {
            InputStream inputStream=new FileInputStream(file);
            return isResXmlBlock(inputStream);
        } catch (FileNotFoundException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(InputStream inputStream){
        try {
            HeaderBlock headerBlock=BlockReader.readHeaderBlock(inputStream);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.XML;
    }
}
