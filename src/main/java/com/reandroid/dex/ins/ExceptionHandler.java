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
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class ExceptionHandler extends DexContainerItem implements ExtraLine {
    private final Ule128Item catchAddress;
    private final Label startLabel;

    ExceptionHandler(int childesCount) {
        super(childesCount + 1);
        this.catchAddress = new Ule128Item();
        addChild(childesCount, catchAddress);
        this.startLabel = new TryStartLabel(this);
    }

    public int getCatchAddress(){
        return catchAddress.get();
    }
    public int getAddress(){
        return getStartAddress() + getCodeUnit();
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
        return getParent(TryItem.class);
    }

    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_EXCEPTION_HANDLER;
    }
    @Override
    public final void appendExtra(SmaliWriter writer) throws IOException {
        writer.append('.');
        writer.append(getOpcodeName());
        writer.append(" ");
        appendType(writer);
        writer.append("{");
        writer.append(getStartLabel().getLabelName());
        writer.append(" .. ");
        writer.append(getEndLabel().getLabelName());
        writer.append("} ");
        writer.append(getCatchLabel().getLabelName());
    }

    @Override
    public boolean isEqualExtraLine(Object obj) {
        return obj == this;
    }

    String getOpcodeName(){
        return null;
    }
    void appendType(SmaliWriter writer) throws IOException {
    }

    @Override
    public String toString() {
        return getStartLabel().toString();
    }

    static class TryStartLabel implements Label{
        private final ExceptionHandler exceptionHandler;
        TryStartLabel(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
        }
        @Override
        public int getAddress(){
            return exceptionHandler.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return exceptionHandler.getStartAddress();
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
        public String toString() {
            return getLabelName();
        }
    }

    static class TryEndLabel implements Label{
        private final ExceptionHandler exceptionHandler;
        TryEndLabel(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
        }
        @Override
        public int getAddress() {
            return exceptionHandler.getAddress();
        }
        @Override
        public int getTargetAddress() {
            return exceptionHandler.getAddress();
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
        public String toString() {
            return getLabelName();
        }
    }

    static class CatchLabel implements Label{
        private final ExceptionHandler exceptionHandler;
        CatchLabel(ExceptionHandler exceptionHandler){
            this.exceptionHandler = exceptionHandler;
        }
        @Override
        public int getAddress() {
            return exceptionHandler.getStartLabel().getAddress();
        }
        @Override
        public int getTargetAddress() {
            return exceptionHandler.getCatchAddress();
        }
        @Override
        public String getLabelName() {
            return HexUtil.toHex(":" + exceptionHandler.getOpcodeName() + "_", getTargetAddress(), 1);
        }
        @Override
        public int getSortOrder() {
            return ExtraLine.ORDER_CATCH;
        }
        @Override
        public String toString() {
            return getLabelName();
        }
    }
}
