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
package com.reandroid.dex.instruction;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Sle128Item;
import com.reandroid.dex.item.DexContainerItem;

import java.io.IOException;
import java.util.Iterator;

public class TryItem extends DexContainerItem {
    private final HandlerOffsetArray handlerOffsetArray;

    private final Sle128Item handlersCount;
    private final BlockList<TryHandler> tryHandlerList;
    private CatchAllHandler catchAllHandler;

    public TryItem(HandlerOffsetArray handlerOffsetArray) {
        super(3);
        this.handlerOffsetArray = handlerOffsetArray;

        this.handlersCount = new Sle128Item();
        this.tryHandlerList = new BlockList<>();

        addChild(0, handlersCount);
        addChild(1, tryHandlerList);
    }

    public Iterator<TryHandler> getTryHandlers(){
        return tryHandlerList.iterator();
    }
    public int getTryHandlersCount(){
        return tryHandlerList.size();
    }
    public CatchAllHandler getCatchAllHandler(){
        return catchAllHandler;
    }
    private CatchAllHandler initCatchAllHandler(){
        CatchAllHandler catchAllHandler = this.catchAllHandler;
        if(catchAllHandler == null){
            catchAllHandler = new CatchAllHandler();
            addChild(2, catchAllHandler);
            this.catchAllHandler = catchAllHandler;
        }
        return catchAllHandler;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int maxPosition = reader.getPosition();

        HandlerOffsetArray handlerOffsetArray = this.handlerOffsetArray;
        int position = handlerOffsetArray.getItemsStart()
                + handlerOffsetArray.getOffset(this.getIndex());
        reader.seek(position);

        handlersCount.readBytes(reader);
        int count = handlersCount.get();
        boolean hasCatchAll = false;
        if(count <= 0){
            count = -count;
            hasCatchAll = true;
        }
        BlockList<TryHandler> handlerList = this.tryHandlerList;
        for(int i = 0; i < count; i++){
            TryHandler handler = new TryHandler();
            handlerList.add(handler);
            handler.readBytes(reader);
        }
        if(hasCatchAll){
            initCatchAllHandler().readBytes(reader);
        }
        if(maxPosition > reader.getPosition()){
            reader.seek(maxPosition);
        }
    }

}
