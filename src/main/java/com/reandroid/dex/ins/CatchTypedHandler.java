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
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.reference.Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.model.SmaliCodeCatch;
import com.reandroid.dex.smali.model.SmaliCodeExceptionHandler;


public class CatchTypedHandler extends ExceptionHandler {

    private final Ule128IdItemReference<TypeId> typeId;

    public CatchTypedHandler() {
        super(1);
        this.typeId = new Ule128IdItemReference<>(SectionType.TYPE_ID, UsageMarker.USAGE_INSTRUCTION);
        addChild(0, typeId);
    }
    CatchTypedHandler(Ule128IdItemReference<TypeId> nullForCompact) {
        super();
        this.typeId = nullForCompact;
    }

    CatchTypedHandler newCompact(TryItem parent) {
        CatchTypedHandler catchTypedHandler = new Compact(this);
        catchTypedHandler.setIndex(getIndex());
        catchTypedHandler.setParent(parent);
        return catchTypedHandler;
    }

    @Override
    public boolean traps(TypeKey typeKey) {
        return typeKey != null && typeKey.equals(getKey());
    }
    @Override
    public TypeId getTypeId() {
        return getTypeUle128().getItem();
    }
    @Override
    public TypeKey getKey() {
        return (TypeKey) getTypeUle128().getKey();
    }
    @Override
    public void setKey(TypeKey typeKey) {
        getTypeUle128().setKey(typeKey);
    }
    @Override
    public boolean isCatchAll() {
        return false;
    }

    Ule128IdItemReference<TypeId> getTypeUle128() {
        return typeId;
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.CATCH;
    }

    @Override
    public void onRemove() {
        super.onRemove();
        this.typeId.setItem(null);
    }

    @Override
    public void merge(ExceptionHandler handler) {
        super.merge(handler);
        CatchTypedHandler typedHandler = (CatchTypedHandler) handler;
        typeId.setKey(typedHandler.typeId.getKey());
    }

    @Override
    public void fromSmali(SmaliCodeExceptionHandler smaliCodeExceptionHandler) {
        SmaliCodeCatch smaliCodeCatch = (SmaliCodeCatch) smaliCodeExceptionHandler;
        typeId.setKey(smaliCodeCatch.getType());
        super.fromSmali(smaliCodeExceptionHandler);
    }

    static class Compact extends CatchTypedHandler {

        private final CatchTypedHandler catchTypedHandler;

        Compact(CatchTypedHandler catchTypedHandler) {
            super(null);
            this.catchTypedHandler = catchTypedHandler;
        }

        @Override
        public boolean isRemoved() {
            return super.isRemoved() || catchTypedHandler.isRemoved();
        }

        @Override
        Ule128IdItemReference<TypeId> getTypeUle128() {
            return catchTypedHandler.getTypeUle128();
        }
        @Override
        Ule128Item getCatchAddressUle128() {
            return catchTypedHandler.getCatchAddressUle128();
        }

        @Override
        public void merge(ExceptionHandler handler) {
        }
    }
}
