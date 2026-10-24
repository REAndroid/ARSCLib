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
package com.reandroid.arsc.container;

import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.OverlayablePolicy;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.list.ExtraChunkList;
import com.reandroid.arsc.list.LibraryBlockList;
import com.reandroid.arsc.list.OverlayableList;
import com.reandroid.arsc.list.StagedAliasList;

import java.io.IOException;

public class PackageBody extends FixedBlockContainer {

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlockList mLibraryList;
    private final StagedAliasList mStagedAliasList;
    private final OverlayableList mOverlayableList;
    private final BlockList<OverlayablePolicy> mOverlayablePolicyList;
    private final BlockList<UnknownChunk> mUnknownChunkList;

    public PackageBody() {
        super(6);
        
        this.mSpecTypePairArray = new SpecTypePairArray();
        this.mLibraryList = new LibraryBlockList();
        this.mStagedAliasList = new StagedAliasList();
        this.mOverlayableList = new OverlayableList();
        this.mOverlayablePolicyList = new BlockList<>(OverlayablePolicy.CREATOR);
        this.mUnknownChunkList = new BlockList<>(UnknownChunk.CREATOR);

        setupBlock(0, mSpecTypePairArray);
        setupBlock(1, mLibraryList);
        setupBlock(2, mStagedAliasList);
        setupBlock(3, mOverlayableList);
        setupBlock(4, mOverlayablePolicyList);
        setupBlock(5, mUnknownChunkList);
    }

    public boolean isEmpty() {
        return  getSpecTypePairArray().isBlank() &&
                getLibraryList().size() == 0 &&
                getStagedAliasList().size() == 0 &&
                getOverlayableList().size() == 0 &&
                getUnknownChunkList().size() == 0;
    }

    public void destroy() {
        getSpecTypePairArray().clear();
        getLibraryList().clearBlocks();
        getStagedAliasList().clearBlocks();
        getOverlayableList().clearBlocks();
        getUnknownChunkList().clearBlocks();
    }

    public SpecTypePairArray getSpecTypePairArray() {
        return mSpecTypePairArray;
    }
    public ExtraChunkList<?> extraChunkListForFile(String name) {
        ExtraChunkList<?> chunkList = getLibraryList();
        if (chunkList.matchesFileName(name)) {
            return chunkList;
        }
        chunkList = getOverlayableList();
        if (chunkList.matchesFileName(name)) {
            return chunkList;
        }
        chunkList = getStagedAliasList();
        if (chunkList.matchesFileName(name)) {
            return chunkList;
        }
        return null;
    }
    public LibraryBlockList getLibraryList() {
        return mLibraryList;
    }
    public OverlayableList getOverlayableList() {
        return mOverlayableList;
    }
    public StagedAliasList getStagedAliasList() {
        return mStagedAliasList;
    }

    public BlockList<UnknownChunk> getUnknownChunkList() {
        return mUnknownChunkList;
    }

    @Override
    public void readBytes(BlockReader reader) throws IOException {
        InfoHeader infoHeader = new InfoHeader();
        while (infoHeader.readInfo(reader)) {
            if (!readNextBlock(infoHeader, reader)) {
                break;
            }
        }
    }
    private boolean readNextBlock(InfoHeader infoHeader, BlockReader reader) throws IOException {
        int position = reader.getPosition();
        ChunkType chunkType = infoHeader.getChunkType();
        if (chunkType == ChunkType.SPEC || chunkType == ChunkType.TYPE) {
            readSpecOrType(infoHeader, reader);
        } else if (chunkType == ChunkType.LIBRARY) {
            readLibraryBlock(reader);
        } else if (chunkType == ChunkType.OVERLAYABLE) {
            readOverlayable(reader);
        } else if (chunkType == ChunkType.OVERLAYABLE_POLICY) {
            readOverlayablePolicy(reader);
        } else if (chunkType == ChunkType.STAGED_ALIAS) {
            readStagedAlias(reader);
        } else {
            readUnknownChunk(reader);
        }
        return position != reader.getPosition();
    }
    private void readSpecOrType(InfoHeader infoHeader, BlockReader reader) throws IOException {
        mSpecTypePairArray.getOrCreate(infoHeader.getId()).readBytes(reader);
    }
    private void readLibraryBlock(BlockReader reader) throws IOException {
        mLibraryList.createNext().readBytes(reader);
    }
    private void readStagedAlias(BlockReader reader) throws IOException {
        mStagedAliasList.createNext().readBytes(reader);
    }
    private void readOverlayable(BlockReader reader) throws IOException {
        mOverlayableList.createNext().readBytes(reader);
    }
    private void readOverlayablePolicy(BlockReader reader) throws IOException {
        mOverlayablePolicyList.createNext().readBytes(reader);
    }
    private void readUnknownChunk(BlockReader reader) throws IOException {
        mUnknownChunkList.createNext().readBytes(reader);
    }
}
