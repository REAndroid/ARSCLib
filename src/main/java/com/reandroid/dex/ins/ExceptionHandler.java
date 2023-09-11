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

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.TypeId;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;

public abstract class ExceptionHandler extends DexContainerItem
        implements Iterable<Label>, LabelList {

    private final Ule128Item catchAddress;
    private final Label handlerLabel;
    private final Label startLabel;

    public ExceptionHandler(int childesCount) {
        super(childesCount + 1);
        this.catchAddress = new Ule128Item();
        addChild(childesCount, catchAddress);
        this.handlerLabel = new HandlerLabel(this);
        this.startLabel = new TryStartLabel(this);
    }

    ExceptionHandler() {
        super(0);
        this.catchAddress = null;
        this.handlerLabel = new HandlerLabel(this);
        this.startLabel = new TryStartLabel(this);
    }

    abstract TypeId getTypeId();
    abstract String getOpcodeName();
    Ule128Item getCatchAddressUle128(){
        return catchAddress;
    }

    @Override
    public Iterator<Label> getLabels(){
        return iterator();
    }
    @Override
    public Iterator<Label> iterator(){
        return ArrayIterator.of(new Label[]{
                getStartLabel(),
                getEndLabel(),
                getHandlerLabel(),
                getCatchLabel()
        });
    }

    public int getCatchAddress(){
        return getCatchAddressUle128().get();
    }
    public int getAddress(){
        return getStartAddress() + getCodeUnit();
    }
    public Label getHandlerLabel(){
        return handlerLabel;
    }
    public Label getStartLabel(){
        return startLabel;
    }
    public Label getEndLabel(){
        return new TryEndLabel(this);
    }
    public Label getCatchLabel(){
        return new CatchLabel(this);
    }
    public int getStartAddress(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            return tryItem.getStartAddress();
        }
        return 0;
    }
    public int getCodeUnit(){
        TryItem tryItem = getTryItem();
        if(tryItem != null){
            return tryItem.getCatchCodeUnit();
        }
        return 0;
    }
    TryItem getTryItem(){
        return getParentInstance(TryItem.class);
    }

    @Override
    public String toString() {
        return getHandlerLabel().toString();
    }

    static class HandlerLabel implements Label{
        private final ExceptionHandler handler;

        HandlerLabel(ExceptionHandler handler){
            this.handler = handler;
        }

        @Override
        public int getAddress(){
            return handler.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return handler.getAddress();
        }

        @Override
        public String getLabelName() {
            ExceptionHandler handler = this.handler;
            StringBuilder builder = new StringBuilder();
            builder.append('.');
            builder.append(handler.getOpcodeName());
            builder.append(' ');
            TypeId typeId = handler.getTypeId();
            if(typeId != null){
                builder.append(typeId.getName());
                builder.append(' ');
            }
            builder.append("{");
            builder.append(handler.getStartLabel().getLabelName());
            builder.append(" .. ");
            builder.append(handler.getEndLabel().getLabelName());
            builder.append("} ");
            builder.append(handler.getCatchLabel().getLabelName());
            return builder.toString();
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_EXCEPTION_HANDLER;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            HandlerLabel label = (HandlerLabel) obj;
            return this.handler == label.handler;
        }
        @Override
        public void appendExtra(SmaliWriter writer) throws IOException {
            ExceptionHandler handler = this.handler;
            writer.append('.');
            writer.append(handler.getOpcodeName());
            writer.append(' ');
            TypeId typeId = handler.getTypeId();
            if(typeId != null){
                typeId.append(writer);
                writer.append(' ');
            }
            writer.append("{");
            writer.append(handler.getStartLabel().getLabelName());
            writer.append(" .. ");
            writer.append(handler.getEndLabel().getLabelName());
            writer.append("} ");
            writer.append(handler.getCatchLabel().getLabelName());
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    static class TryStartLabel implements Label{
        private final ExceptionHandler handler;
        TryStartLabel(ExceptionHandler handler){
            this.handler = handler;
        }
        @Override
        public int getAddress(){
            return handler.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return handler.getStartAddress();
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":try_start_", getTargetAddress(), 1);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_TRY_START;
        }

        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            TryStartLabel label = (TryStartLabel) obj;
            if(handler == label.handler){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress();
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    static class TryEndLabel implements Label{
        private final ExceptionHandler handler;
        TryEndLabel(ExceptionHandler handler){
            this.handler = handler;
        }
        @Override
        public int getAddress() {
            return handler.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return handler.getAddress();
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":try_end_", getTargetAddress(), 1);
        }

        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_TRY_END;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            TryEndLabel label = (TryEndLabel) obj;
            if(handler == label.handler){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress();
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }

    static class CatchLabel implements Label{
        private final ExceptionHandler handler;
        CatchLabel(ExceptionHandler handler){
            this.handler = handler;
        }
        @Override
        public int getAddress() {
            return handler.getStartLabel().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return handler.getCatchAddress();
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":" + handler.getOpcodeName() + "_", getTargetAddress(), 1);
        }
        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_CATCH;
        }
        @Override
        public boolean isEqualExtraLine(Object obj) {
            if(obj == this){
                return true;
            }
            if(obj == null || this.getClass() != obj.getClass()){
                return false;
            }
            CatchLabel label = (CatchLabel) obj;
            if(handler == label.handler){
                return true;
            }
            return getTargetAddress() == label.getTargetAddress();
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }
}
