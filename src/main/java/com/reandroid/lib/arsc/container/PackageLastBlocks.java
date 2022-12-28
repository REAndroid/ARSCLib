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

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.SpecTypePairArray;
import com.reandroid.lib.arsc.chunk.LibraryBlock;
import com.reandroid.lib.arsc.chunk.StagedAlias;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;

public class PackageLastBlocks extends FixedBlockContainer {

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlock mLibraryBlock;
    private final StagedAlias mStagedAlias;
    public PackageLastBlocks(SpecTypePairArray specTypePairArray,
                             LibraryBlock libraryBlock,
                             StagedAlias stagedAlias){
        super(3);
        this.mSpecTypePairArray=specTypePairArray;
        this.mLibraryBlock=libraryBlock;
        this.mStagedAlias=stagedAlias;
        addChild(0, mSpecTypePairArray);
        addChild(1, mLibraryBlock);
        addChild(2, mStagedAlias);
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
        }else if(chunkType==ChunkType.STAGED_ALIAS){
            readStagedAlias(reader);
        }else {
            readUnexpectedBlock(reader, headerBlock);
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
        mStagedAlias.addStagedAliasEntries(stagedAlias);
    }
    private void readUnexpectedBlock(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException(reader.getActualPosition()+", Unexpected block: "+headerBlock.toString());
    }
}
