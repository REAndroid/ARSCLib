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
package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.chunk.*;
import com.reandroid.lib.arsc.array.SpecTypePairArray;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;

public class PackageBody extends FixedBlockContainer {

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlock mLibraryBlock;
    private final BlockList<StagedAlias> mStagedAliasList;
    private final BlockList<Overlayable> mOverlayableList;
    private final BlockList<OverlayablePolicy> mOverlayablePolicyList;
    private final BlockList<UnknownChunk> mUnknownChunkList;
    public PackageBody(){
        super(6);
        this.mSpecTypePairArray = new SpecTypePairArray();
        this.mLibraryBlock = new LibraryBlock();
        this.mStagedAliasList = new BlockList<>();
        this.mOverlayableList = new BlockList<>();
        this.mOverlayablePolicyList = new BlockList<>();
        this.mUnknownChunkList = new BlockList<>();

        addChild(0, mSpecTypePairArray);
        addChild(1, mLibraryBlock);
        addChild(2, mStagedAliasList);
        addChild(3, mOverlayableList);
        addChild(4, mOverlayablePolicyList);
        addChild(5, mUnknownChunkList);
    }
    public BlockList<Overlayable> getOverlayableList() {
        return mOverlayableList;
    }
    public BlockList<OverlayablePolicy> getOverlayablePolicyList() {
        return mOverlayablePolicyList;
    }
    public BlockList<StagedAlias> getStagedAliasList() {
        return mStagedAliasList;
    }
    public LibraryBlock getLibraryBlock(){
        return mLibraryBlock;
    }
    public SpecTypePairArray getSpecTypePairArray() {
        return mSpecTypePairArray;
    }
    public BlockList<UnknownChunk> getUnknownChunkList(){
        return mUnknownChunkList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        boolean readOk=true;
        while (readOk){
            readOk=readNextBlock(reader);
        }
    }
    private boolean readNextBlock(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        int pos=reader.getPosition();
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.SPEC){
            readSpecBlock(reader);
        }else if(chunkType==ChunkType.LIBRARY){
            readLibraryBlock(reader);
        }else if(chunkType==ChunkType.OVERLAYABLE){
            readOverlayable(reader);
        }else if(chunkType==ChunkType.OVERLAYABLE_POLICY){
            readOverlayablePolicy(reader);
        }else if(chunkType==ChunkType.STAGED_ALIAS){
            readStagedAlias(reader);
        }else {
            readUnknownChunk(reader);
        }
        return pos!=reader.getPosition();
    }
    private void readSpecBlock(BlockReader reader) throws IOException{
        SpecTypePair specTypePair=mSpecTypePairArray.createNext();
        specTypePair.readBytes(reader);
    }
    private void readLibraryBlock(BlockReader reader) throws IOException{
        LibraryBlock libraryBlock=new LibraryBlock();
        libraryBlock.readBytes(reader);
        mLibraryBlock.addLibraryInfo(libraryBlock);
    }
    private void readStagedAlias(BlockReader reader) throws IOException{
        StagedAlias stagedAlias = new StagedAlias();
        stagedAlias.readBytes(reader);
        mStagedAliasList.add(stagedAlias);
    }
    private void readOverlayable(BlockReader reader) throws IOException{
        Overlayable overlayable = new Overlayable();
        overlayable.readBytes(reader);
        mOverlayableList.add(overlayable);
    }
    private void readOverlayablePolicy(BlockReader reader) throws IOException{
        OverlayablePolicy overlayablePolicy = new OverlayablePolicy();
        overlayablePolicy.readBytes(reader);
        mOverlayablePolicyList.add(overlayablePolicy);
    }
    private void readUnknownChunk(BlockReader reader) throws IOException{
        UnknownChunk unknownChunk = new UnknownChunk();
        unknownChunk.readBytes(reader);
        mUnknownChunkList.add(unknownChunk);
    }
}