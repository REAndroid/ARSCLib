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
package com.reandroid.apk.xmldecoder;

import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.common.EntryStore;

import java.io.IOException;

class DecoderResTableEntryMap<OUTPUT> extends DecoderTableEntry<ResTableMapEntry, OUTPUT> {
    private final Object[] decoderList;
    private final BagDecoderCommon<OUTPUT> bagDecoderCommon;

    public DecoderResTableEntryMap(EntryStore entryStore) {
        super(entryStore);
        this.decoderList = new Object[] {
                new BagDecoderAttr<>(entryStore),
                new BagDecoderPlural<>(entryStore),
                new BagDecoderArray<>(entryStore)
        };

        this.bagDecoderCommon = new BagDecoderCommon<>(entryStore);
    }

    @Override
    public OUTPUT decode(ResTableMapEntry tableEntry, EntryWriter<OUTPUT> writer) throws IOException {
        return getFor(tableEntry).decode(tableEntry, writer);
    }
    private BagDecoder<OUTPUT> getFor(ResTableMapEntry mapEntry){
        Object[] decoderList = this.decoderList;
        for(int i = 0; i < decoderList.length; i++){
            BagDecoder<OUTPUT> bagDecoder = (BagDecoder<OUTPUT>) decoderList[i];
            if(bagDecoder.canDecode(mapEntry)){
                return bagDecoder;
            }
        }
        return bagDecoderCommon;
    }
}
