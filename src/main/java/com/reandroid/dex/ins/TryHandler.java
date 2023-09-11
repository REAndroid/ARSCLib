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
import com.reandroid.dex.item.SectionUle128Item;
import com.reandroid.dex.sections.SectionType;

public class TryHandler extends ExceptionHandler {
    private final SectionUle128Item<TypeId> typeId;

    public TryHandler() {
        super(1);
        this.typeId = new SectionUle128Item<>(SectionType.TYPE_ID);
        addChild(0, typeId);
    }
    TryHandler(boolean forCopy) {
        super();
        this.typeId = null;
    }
    TryHandler newCopy(){
        TryHandler tryHandler = new Copy(this);
        tryHandler.setIndex(getIndex());
        return tryHandler;
    }

    @Override
    public TypeId getTypeId(){
        return getTypeUle128().getItem();
    }
    SectionUle128Item<TypeId> getTypeUle128(){
        return typeId;
    }
    @Override
    String getOpcodeName(){
        return "catch";
    }

    static class Copy extends TryHandler {
        private final TryHandler tryHandler;

        Copy(TryHandler tryHandler){
            super(true);
            this.tryHandler = tryHandler;
        }

        @Override
        SectionUle128Item<TypeId> getTypeUle128(){
            return tryHandler.getTypeUle128();
        }
        @Override
        Ule128Item getCatchAddressUle128(){
            return tryHandler.getCatchAddressUle128();
        }
    }
}
