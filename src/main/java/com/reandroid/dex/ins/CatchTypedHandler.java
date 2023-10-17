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
import com.reandroid.dex.reference.Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;

public class CatchTypedHandler extends ExceptionHandler {

    private final Ule128IdItemReference<TypeId> typeId;

    public CatchTypedHandler() {
        super(1);
        this.typeId = new Ule128IdItemReference<>(SectionType.TYPE_ID, UsageMarker.USAGE_INSTRUCTION);
        addChild(0, typeId);
    }
    CatchTypedHandler(boolean forCopy) {
        super();
        this.typeId = null;
    }

    CatchTypedHandler newCopy(){
        CatchTypedHandler catchTypedHandler = new Copy(this);
        catchTypedHandler.setIndex(getIndex());
        return catchTypedHandler;
    }

    @Override
    public TypeId getTypeId(){
        return getTypeUle128().getItem();
    }
    Ule128IdItemReference<TypeId> getTypeUle128(){
        return typeId;
    }
    @Override
    String getOpcodeName(){
        return "catch";
    }

    static class Copy extends CatchTypedHandler {
        private final CatchTypedHandler catchTypedHandler;

        Copy(CatchTypedHandler catchTypedHandler){
            super(true);
            this.catchTypedHandler = catchTypedHandler;
        }

        @Override
        Ule128IdItemReference<TypeId> getTypeUle128(){
            return catchTypedHandler.getTypeUle128();
        }
        @Override
        Ule128Item getCatchAddressUle128(){
            return catchTypedHandler.getCatchAddressUle128();
        }
    }
}
