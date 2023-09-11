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
package com.reandroid.dex.ins;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Sle128Item;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Function;

public class TryItem extends DexContainerItem implements Iterable<Label>{
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
    private TryItem() {
        super(0);
        this.handlerOffsetArray = null;

        this.handlersCount = null;
        this.tryHandlerList = null;
    }

    TryItem newCopy(){
        return new Copy(this);
    }
    HandlerOffsetArray getHandlerOffsetArray(){
        return handlerOffsetArray;
    }
    BlockList<TryHandler> getTryHandlerBlockList(){
        return tryHandlerList;
    }
    Iterator<TryHandler> getTryHandlers(){
        return tryHandlerList.iterator();
    }
    Sle128Item getHandlersCountItem(){
        return handlersCount;
    }
    TryItem getTryItem(){
        return this;
    }

    @Override
    public Iterator<Label> iterator(){
        return new ExpandIterator<>(getExceptionHandlers());
    }
    public Iterator<ExceptionHandler> getExceptionHandlers(){
        Iterator<ExceptionHandler> iterator1 = EmptyIterator.of();
        ExceptionHandler handler = getCatchAllHandler();
        if(handler != null){
            iterator1 = SingleIterator.of(handler);
        }
        return new CombiningIterator<>(getTryHandlers(), iterator1);
    }
    public int getStartAddress(){
        return getHandlerOffsetArray().getStartAddress(getIndex());
    }
    public int getCatchCodeUnit(){
        return getHandlerOffsetArray().getCatchCodeUnit(getIndex());
    }

    public CatchAllHandler getCatchAllHandler(){
        return catchAllHandler;
    }
    private CatchAllHandler initCatchAllHandler(){
        CatchAllHandler catchAllHandler = this.getCatchAllHandler();
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

        HandlerOffsetArray handlerOffsetArray = this.getHandlerOffsetArray();
        int position = handlerOffsetArray.getItemsStart()
                + handlerOffsetArray.getOffset(this.getIndex());
        reader.seek(position);
        Sle128Item countItem = getHandlersCountItem();
        countItem.readBytes(reader);
        int count = countItem.get();
        boolean hasCatchAll = false;
        if(count <= 0){
            count = -count;
            hasCatchAll = true;
        }
        BlockList<TryHandler> handlerList = this.getTryHandlerBlockList();
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
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        Block end = counter.END;
        if(end instanceof TryItem.Copy){
            TryItem tryItem = ((TryItem.Copy) end).getTryItem();
            if(tryItem == this){
                counter.FOUND = true;
                return;
            }
        }
        super.onCountUpTo(counter);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<ExceptionHandler> handlers = getExceptionHandlers();
        while (handlers.hasNext()){
            if(builder.length() > 0){
                builder.append("\n");
            }
            builder.append(handlers.next());
        }
        return builder.toString();
    }
    static class Copy extends TryItem{
        private final TryItem tryItem;
        public Copy(TryItem tryItem) {
            super();
            this.tryItem = tryItem;
        }
        @Override
        HandlerOffsetArray getHandlerOffsetArray(){
            return tryItem.getHandlerOffsetArray();
        }
        @Override
        Iterator<TryHandler> getTryHandlers(){
            return ComputeIterator.of(tryItem.getTryHandlers(), new Function<TryHandler, TryHandler>() {
                @Override
                public TryHandler apply(TryHandler tryHandler) {
                    TryHandler copy = tryHandler.newCopy();
                    copy.setParent(Copy.this);
                    return copy;
                }
            });
        }
        @Override
        Sle128Item getHandlersCountItem(){
            return tryItem.getHandlersCountItem();
        }
        @Override
        TryItem getTryItem(){
            return tryItem.getTryItem();
        }
        @Override
        public CatchAllHandler getCatchAllHandler(){
            CatchAllHandler catchAllHandler = tryItem.getCatchAllHandler();
            if(catchAllHandler != null){
                CatchAllHandler copy = catchAllHandler.newCopy();
                copy.setParent(this);
                return copy;
            }
            return null;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
        }
    }
}
