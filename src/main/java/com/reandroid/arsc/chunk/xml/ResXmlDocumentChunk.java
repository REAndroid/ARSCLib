/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.common.BytesOutputStream;

import java.io.*;

public class ResXmlDocumentChunk extends Chunk<HeaderBlock> {

    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private final ResXmlNodeList mNodeList;
    private PackageBlock mPackageBlock;

    public ResXmlDocumentChunk() {
        super(new HeaderBlock(ChunkType.XML),3);

        this.mResXmlStringPool = new ResXmlStringPool(true);
        this.mResXmlIDMap = new ResXmlIDMap();
        this.mNodeList = new ResXmlNodeList();

        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mNodeList);
    }

    public BlockList<ResXmlNode> getNodeListBlockInternal() {
        return mNodeList;
    }

    public void refreshFull() {
        ResXmlStringPool stringPool = getStringPool();
        stringPool.compressDuplicates();
        stringPool.removeUnusedStrings();
        refresh();
    }
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeaderBlock().getChunkSize());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock == null){
            throw new IOException("Not bin xml: " + reader);
        }
        int chunkSize = headerBlock.getChunkSize();
        if(chunkSize < 0){
            throw new IOException("Negative chunk size: " + chunkSize);
        }
        if(chunkSize > reader.available()){
            throw new IOException("Higher chunk size: " + chunkSize
                    + ", available = " + reader.available());
        }
        if(chunkSize < headerBlock.getHeaderSize()){
            throw new IOException("Higher header size: " + headerBlock);
        }
        BlockReader chunkReader = reader.create(chunkSize);
        headerBlock = getHeaderBlock();
        headerBlock.readBytes(chunkReader);
        // android/aapt2 accepts 0x0000 (NULL) chunk type as XML, it could
        // be android's bug and might be fixed in the future until then lets fix it ourselves
        headerBlock.setType(ChunkType.XML);
        document().clear();
        while (chunkReader.isAvailable()){
            boolean readOk = readNext(chunkReader);
            if(!readOk){
                break;
            }
        }
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    @Override
    public void onChunkLoaded(){
        super.onChunkLoaded();
        document().linkStringReferences();
    }
    private boolean readNext(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return false;
        }
        int position = reader.getPosition();
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if (headerBlock == null) {
            return false;
        }
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == ChunkType.STRING && mResXmlStringPool.size() == 0) {
            // If the string pool is not empty then it will be assumed that
            // it is already loaded and consume bytes as unexpected chunk,
            // same goes for ResXmlIDMap below
            mResXmlStringPool.readBytes(reader);
        } else if(chunkType == ChunkType.XML_RESOURCE_MAP && mResXmlIDMap.size() == 0) {
            mResXmlIDMap.readBytes(reader);
        } else if(chunkType == ChunkType.XML_CDATA) {
            document().newText().readBytes(reader);
            return reader.isAvailable();
        } else if(chunkType == ChunkType.XML) {
            document().newDocument().readBytes(reader);
            return reader.isAvailable();
        } else if (chunkType == ChunkType.XML_START_ELEMENT ||
                chunkType==ChunkType.XML_START_NAMESPACE){
            document().newElement().readBytes(reader);
            return reader.isAvailable();
        } else {
            logUnknownChunkOnce(headerBlock);
            document().newUnknown().readBytes(reader);
        }
        return reader.isAvailable() && position != reader.getPosition();
    }
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    public ApkFile getApkFile() {
        return document().getApkFile();
    }
    public PackageBlock getPackageBlock() {
        ApkFile apkFile = this.getApkFile();
        PackageBlock packageBlock = this.mPackageBlock;
        if(apkFile == null || packageBlock != null){
            return packageBlock;
        }
        TableBlock tableBlock = apkFile.getLoadedTableBlock();
        if(tableBlock != null) {
            packageBlock = selectPackageBlock(tableBlock);
            mPackageBlock = packageBlock;
        }
        return packageBlock;
    }
    public void setPackageBlock(PackageBlock packageBlock) {
        this.mPackageBlock = packageBlock;
    }
    PackageBlock selectPackageBlock(TableBlock tableBlock){
        PackageBlock packageBlock = tableBlock.pickOne();
        if(packageBlock == null){
            packageBlock = tableBlock.pickOrEmptyPackage();
        }
        return packageBlock;
    }
    public TableBlock getTableBlock(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            TableBlock tableBlock = packageBlock.getTableBlock();
            if(tableBlock != null){
                return tableBlock;
            }
        }
        ApkFile apkFile = getApkFile();
        if(apkFile != null){
            return apkFile.getLoadedTableBlock();
        }
        return null;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getDocumentElement(){
        return document().getDocumentElement();
    }
    public ResXmlElement newElement() {
        clearEmptyElements();
        return document().newElement();
    }
    public void clearEmptyElements() {
        document().removeElementsIf(ResXmlElement::isUndefined);
    }
    @Override
    protected void onPreRefresh(){
        clearEmptyElements();
        getNodeListBlockInternal().refresh();
        super.onPreRefresh();
    }
    @Override
    protected void onChunkRefreshed() {

    }
    ResXmlDocument document() {
        return getParentInstance(ResXmlDocument.class);
    }

    ResXmlNodeList getNodeList() {
        return mNodeList;
    }

    private static void logUnknownChunkOnce(HeaderBlock headerBlock) {
        if (!UNKNOWN_CHUNK_LOGGED) {
            UNKNOWN_CHUNK_LOGGED = true;
            System.err.println("Read unknown chunk: " + headerBlock);
        }
    }
    private static boolean UNKNOWN_CHUNK_LOGGED;
}
