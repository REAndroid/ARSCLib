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
import com.reandroid.dex.common.IdUsageIterator;
import com.reandroid.dex.data.FixedDexContainerWithTool;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.model.SmaliCodeCatch;
import com.reandroid.dex.smali.model.SmaliCodeCatchAll;
import com.reandroid.dex.smali.model.SmaliCodeTryItem;
import com.reandroid.dex.smali.model.SmaliSet;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class TryItem extends FixedDexContainerWithTool implements
        Iterable<Label>, IdUsageIterator {

    private final HandlerOffsetArray handlerOffsetArray;

    final Sle128Item handlersCount;
    private final BlockList<CatchTypedHandler> catchTypedHandlerList;
    private CatchAllHandler catchAllHandler;

    private HandlerOffset mHandlerOffset;

    public TryItem(HandlerOffsetArray handlerOffsetArray) {
        super(3);

        this.handlerOffsetArray = handlerOffsetArray;
        this.handlersCount = new Sle128Item();
        this.catchTypedHandlerList = new BlockList<>();

        addChild(0, handlersCount);
        addChild(1, catchTypedHandlerList);
    }
    private TryItem() {
        super(0);
        this.handlerOffsetArray = null;

        this.handlersCount = null;
        this.catchTypedHandlerList = null;
    }

    public boolean isCompact(){
        return false;
    }
    InstructionList getInstructionList(){
        return getTryBlock().getInstructionList();
    }
    TryBlock getTryBlock(){
        return getParent(TryBlock.class);
    }

    TryItem newCompact(){
        return new Compact(this);
    }

    public boolean compactWith(TryItem similar) {
        if (!isSimilarTo(similar)) {
            return false;
        }
        int index = similar.getIndex();
        TryBlock tryBlock = this.getTryBlock();
        TryItem replace = tryBlock.createNextCopy(this);
        replace.merge(similar);
        tryBlock.remove(similar);
        tryBlock.moveTo(replace, index);
        return true;
    }
    private boolean isSimilarTo(TryItem tryItem) {
        if (tryItem == this || this.isCompact() || tryItem.isCompact()) {
            return false;
        }
        if (getParent() != tryItem.getParent()) {
            return false;
        }
        if (!ExceptionHandler.areSimilar(
                this.getCatchAllHandler(),
                tryItem.getCatchAllHandler())) {
            return false;
        }
        int count = this.getCatchTypedHandlersCount();
        if (count != tryItem.getCatchTypedHandlersCount()) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!ExceptionHandler.areSimilar(this.getCatchTypedHandler(i),
                    tryItem.getCatchTypedHandler(i))) {
                return false;
            }
        }
        return true;
    }
    public boolean flatten() {
        return false;
    }
    public boolean splitHandlers() {
        if (!hasMultipleHandlers()) {
            return false;
        }
        int index = getIndex() + 1;
        int count = getCatchTypedHandlersCount();
        for (int i = 1; i < count; i++) {
            CatchTypedHandler handler = getCatchTypedHandler(1);
            index = transferHandlerToNewTryItem(handler, index);
        }
        transferHandlerToNewTryItem(getCatchAllHandler(), index);
        refresh();
        return true;
    }
    private int transferHandlerToNewTryItem(ExceptionHandler handler, int index) {
        if (handler == null) {
            return index;
        }
        TryBlock tryBlock = getTryBlock();
        TryItem destination = tryBlock.createNext();
        destination.mergeOffset(this);
        destination.mergeHandler(handler);
        remove(handler);
        tryBlock.moveTo(destination, index);
        destination.refresh();
        return index + 1;
    }
    public boolean combineWith(TryItem tryItem) {
        if (!equalsOffsetAndCodeUnit(tryItem)) {
            return false;
        }
        Iterator<ExceptionHandler> iterator = tryItem.getExceptionHandlers();
        while (iterator.hasNext()) {
            mergeHandler(iterator.next());
        }
        return true;
    }
    private boolean equalsOffsetAndCodeUnit(TryItem tryItem) {
        if (tryItem == this || this.isCompact() || tryItem.isCompact()) {
            return false;
        }
        if (getParent() != tryItem.getParent()) {
            return false;
        }
        HandlerOffset offset = getHandlerOffset();
        HandlerOffset other = tryItem.getHandlerOffset();
        return offset.getStartAddress() == other.getStartAddress() &&
                offset.getCatchCodeUnit() == other.getCatchCodeUnit();
    }
    HandlerOffset getHandlerOffset() {
        HandlerOffset handlerOffset = this.mHandlerOffset;
        if(handlerOffset == null){
            handlerOffset = getHandlerOffsetArray().getOrCreate(getIndex());
            this.mHandlerOffset = handlerOffset;
            handlerOffset.setTryItem(this);
        }
        return handlerOffset;
    }
    HandlerOffsetArray getHandlerOffsetArray(){
        return handlerOffsetArray;
    }
    BlockList<CatchTypedHandler> getCatchTypedHandlerBlockList(){
        return catchTypedHandlerList;
    }
    Iterator<CatchTypedHandler> getCatchTypedHandlers(){
        return catchTypedHandlerList.iterator();
    }
    TryItem getTryItem(){
        return this;
    }
    void updateCount(){
        Sle128Item handlersCount = this.handlersCount;
        if(handlersCount == null){
            return;
        }
        int count = catchTypedHandlerList.size();
        if(hasCatchAllHandler()){
            count = -count;
        }
        handlersCount.set(count);
    }

    @Override
    public Iterator<Label> iterator(){
        return new ExpandIterator<>(getExceptionHandlers());
    }
    public boolean isEmpty() {
        return getCatchAllHandler() == null &&
                getCatchTypedHandlersCount() == 0;
    }
    public int getCatchTypedHandlersCount() {
        return getCatchTypedHandlerBlockList().size();
    }
    public CatchTypedHandler getCatchTypedHandler(int i) {
        return getCatchTypedHandlerBlockList().get(i);
    }
    public boolean traps(TypeKey typeKey) {
        return getExceptionHandler(typeKey) != null;
    }
    public boolean traps(TypeKey typeKey, int address) {
        return getExceptionHandler(typeKey, address) != null;
    }
    public boolean hasExceptionHandlersForAddress(int address) {
        return getExceptionHandlersForAddress(address).hasNext();
    }
    public Iterator<ExceptionHandler> getExceptionHandlersForAddress(int address) {
        return FilterIterator.of(getExceptionHandlers(),
                handler -> handler.isAddressBounded(address));
    }
    public Iterator<ExceptionHandler> getExceptionHandlers(){
        Iterator<ExceptionHandler> iterator1 = EmptyIterator.of();
        ExceptionHandler handler = getCatchAllHandler();
        if(handler != null){
            iterator1 = SingleIterator.of(handler);
        }
        return new CombiningIterator<>(getCatchTypedHandlers(), iterator1);
    }
    public ExceptionHandler getExceptionHandler(TypeKey typeKey) {
        Iterator<ExceptionHandler> iterator = getExceptionHandlers();
        while (iterator.hasNext()) {
            ExceptionHandler handler = iterator.next();
            if(handler.traps(typeKey)){
                return handler;
            }
        }
        return null;
    }
    public ExceptionHandler getExceptionHandler(TypeKey typeKey, int address) {
        Iterator<ExceptionHandler> iterator = getExceptionHandlers();
        while (iterator.hasNext()) {
            ExceptionHandler handler = iterator.next();
            if(handler.traps(typeKey) && handler.isAddressBounded(address)){
                return handler;
            }
        }
        return null;
    }
    public int getStartAddress(){
        return getHandlerOffset().getStartAddress();
    }
    public void setStartAddress(int address){
        getHandlerOffset().setStartAddress(address);
    }
    public int getCatchCodeUnit(){
        return getHandlerOffset().getCatchCodeUnit();
    }
    public void setCatchCodeUnit(int codeUnit){
        getHandlerOffset().setCatchCodeUnit(codeUnit);
    }

    public boolean hasMultipleHandlers() {
        int typed = getCatchTypedHandlersCount();
        if (typed == 1) {
            return getCatchAllHandler() != null;
        }
        return typed != 0;
    }
    public boolean hasCatchAllHandler(){
        return getCatchAllHandler() != null;
    }
    public CatchAllHandler getCatchAllHandler(){
        return catchAllHandler;
    }
    public CatchAllHandler getOrCreateCatchAll(){
        CatchAllHandler handler = getCatchAllHandler();
        if(handler == null){
            initCatchAllHandler();
            handler = getCatchAllHandler();
        }
        return handler;
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
    protected void onRefreshed() {
        super.onRefreshed();
        updateCount();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int maxPosition = reader.getPosition();

        int position = getHandlerOffsetArray().getItemsStart()
                + getHandlerOffset().getOffset();
        reader.seek(position);
        this.handlersCount.readBytes(reader);
        int count = this.handlersCount.get();
        boolean hasCatchAll = false;
        if(count <= 0){
            count = -count;
            hasCatchAll = true;
        }
        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        handlerList.ensureCapacity(count);
        for(int i = 0; i < count; i++){
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.readBytes(reader);
        }
        if(hasCatchAll){
            initCatchAllHandler().readBytes(reader);
        }
        if(maxPosition > reader.getPosition()){
            // Should never reach here
            reader.seek(maxPosition);
        }
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        Block end = counter.END;
        if(end instanceof Compact){
            TryItem tryItem = ((Compact) end).getTryItem();
            if(tryItem == this){
                counter.FOUND = true;
                return;
            }
        }
        super.onCountUpTo(counter);
    }
    public void removeSelf(){
        TryBlock tryBlock = getTryBlock();
        if(tryBlock != null){
            tryBlock.remove(this);
        }
    }
    public boolean isRemoved() {
        if (getParent() == null) {
            return true;
        }
        TryBlock tryBlock = getTryBlock();
        return tryBlock == null || tryBlock.getParent() == null;
    }
    public void remove(ExceptionHandler handler){
        if(handler == null){
            return;
        }
        if(handler == this.catchAllHandler){
            handler.onRemove();
            this.catchAllHandler = null;
        }else if(handler instanceof CatchTypedHandler && this.catchTypedHandlerList != null){
            if(catchTypedHandlerList.contains(handler)){
                catchTypedHandlerList.remove((CatchTypedHandler) handler);
                handler.onRemove();
            }
        }
    }
    public void onRemove(){
        HandlerOffset handlerOffset = this.mHandlerOffset;
        BlockList<CatchTypedHandler> list = this.catchTypedHandlerList;
        if(list != null){
            int size = list.size();
            for(int i = 0; i < size; i++){
                CatchTypedHandler handler = list.get(i);
                handler.onRemove();
                handler.setParent(null);
            }
            list.destroy();
        }
        remove(this.catchAllHandler);
        if(handlerOffset != null){
            this.mHandlerOffset = null;
            handlerOffset.removeSelf();
        }
        setParent(null);
    }
    public void merge(TryItem tryItem){
        mergeOffset(tryItem);
        mergeHandlers(tryItem);
    }
    public CatchTypedHandler createNext() {
        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        CatchTypedHandler handler = new CatchTypedHandler();
        handlerList.add(handler);
        updateCount();
        return handler;
    }
    void mergeHandlers(TryItem tryItem){
        BlockList<CatchTypedHandler> comingList = tryItem.getCatchTypedHandlerBlockList();
        int size = comingList.size();
        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        handlerList.ensureCapacity(size);
        for (int i = 0; i < size; i++){
            CatchTypedHandler coming = comingList.get(i);
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.merge(coming);
        }
        if(tryItem.hasCatchAllHandler()){
            initCatchAllHandler().merge(tryItem.getCatchAllHandler());
        }
        updateCount();
    }
    void mergeHandler(ExceptionHandler handler) {
        ExceptionHandler newHandler;
        if (handler instanceof CatchTypedHandler) {
            newHandler = createNext();
        } else {
            newHandler = getOrCreateCatchAll();
        }
        newHandler.merge(handler);
    }
    void mergeOffset(TryItem tryItem){

        HandlerOffset coming = tryItem.getHandlerOffset();
        HandlerOffset handlerOffset = getHandlerOffset();

        handlerOffset.setCatchCodeUnit(coming.getCatchCodeUnit());
        handlerOffset.setStartAddress(coming.getStartAddress());
    }
    public void fromSmali(SmaliCodeTryItem smaliCodeTryItem){
        setStartAddress(smaliCodeTryItem.getStartAddress());
        SmaliSet<SmaliCodeCatch> smaliCodeCatchSet = smaliCodeTryItem.getCatchSet();

        BlockList<CatchTypedHandler> handlerList = this.getCatchTypedHandlerBlockList();
        int size = smaliCodeCatchSet.size();
        for(int i = 0; i < size; i++){
            SmaliCodeCatch smaliCodeCatch = smaliCodeCatchSet.get(i);
            CatchTypedHandler handler = new CatchTypedHandler();
            handlerList.add(handler);
            handler.fromSmali(smaliCodeCatch);
        }
        SmaliCodeCatchAll smaliCodeCatchAll = smaliCodeTryItem.getCatchAll();
        if(smaliCodeCatchAll != null){
            CatchAllHandler catchAllHandler = initCatchAllHandler();
            catchAllHandler.fromSmali(smaliCodeCatchAll);
        }
        updateCount();
    }

    @Override
    public Iterator<IdItem> usedIds() {
        return ComputeIterator.of(getCatchTypedHandlers(),
                CatchTypedHandler::getTypeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TryItem tryItem = (TryItem) obj;
        return ObjectsUtil.equals(getCatchTypedHandlerBlockList(),
                tryItem.getCatchTypedHandlerBlockList()) &&
                ObjectsUtil.equals(getCatchAllHandler(), tryItem.getCatchAllHandler());
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(this.getCatchTypedHandlerBlockList(),
                this.getCatchAllHandler());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<ExceptionHandler> handlers = getExceptionHandlers();
        while (handlers.hasNext()) {
            if(builder.length() != 0){
                builder.append('\n');
            }
            builder.append(handlers.next());
        }
        return builder.toString();
    }
    static class Compact extends TryItem {

        private final TryItem tryItem;

        public Compact(TryItem tryItem) {
            super();
            this.tryItem = tryItem;
        }

        @Override
        public boolean isCompact(){
            return true;
        }
        @Override
        TryBlock getTryBlock() {
            return tryItem.getTryBlock();
        }

        @Override
        TryItem newCompact() {
            return tryItem.newCompact();
        }

        @Override
        public boolean compactWith(TryItem similar) {
            return false;
        }
        @Override
        public boolean flatten() {
            TryBlock tryBlock = getTryBlock();
            if (tryBlock != null) {
                TryItem self = this;
                int index = self.getIndex();
                TryItem replace = tryBlock.createNext();
                replace.merge(self);
                tryBlock.remove(self);
                tryBlock.moveTo(replace, index);
                replace.refresh();
                return true;
            }
            return false;
        }
        @Override
        HandlerOffsetArray getHandlerOffsetArray(){
            return tryItem.getHandlerOffsetArray();
        }
        @Override
        Iterator<CatchTypedHandler> getCatchTypedHandlers() {
            Iterator<CatchTypedHandler> iterator = getCatchTypedHandlerBlockList()
                    .iterator();
            final TryItem parent = this;
            return ComputeIterator.of(iterator, handler -> handler.newCompact(parent));
        }
        @Override
        public CatchTypedHandler getCatchTypedHandler(int i) {
            return super.getCatchTypedHandler(i).newCompact(this);
        }

        @Override
        BlockList<CatchTypedHandler> getCatchTypedHandlerBlockList() {
            return tryItem.getCatchTypedHandlerBlockList();
        }
        @Override
        TryItem getTryItem(){
            return tryItem.getTryItem();
        }
        @Override
        public CatchAllHandler getCatchAllHandler() {
            CatchAllHandler catchAllHandler = tryItem.getCatchAllHandler();
            if (catchAllHandler != null) {
                catchAllHandler = catchAllHandler.newCompact(this);
            }
            return catchAllHandler;
        }

        @Override
        public CatchAllHandler getOrCreateCatchAll() {
            tryItem.getOrCreateCatchAll();
            return getCatchAllHandler();
        }

        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        public int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
        @Override
        public byte[] getBytes() {
            return null;
        }
        @Override
        protected void onPreRefresh() {
        }
        @Override
        protected void onRefreshed() {
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        void updateCount(){
        }
        @Override
        void mergeHandlers(TryItem tryItem) {
        }
        @Override
        void mergeHandler(ExceptionHandler handler) {
        }

        @Override
        public int hashCode() {
            return ObjectsUtil.hash(getClass(), super.hashCode());
        }

        @Override
        public String toString() {
            if (getParent() == null) {
                return "NULL";
            }
            return super.toString();
        }
    }
}
