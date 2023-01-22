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
package com.reandroid.apk.xmlencoder;

import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.value.EntryBlock;

class XMLValuesEncoderCommon extends XMLValuesEncoder{
    XMLValuesEncoderCommon(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeStringValue(EntryBlock entryBlock, String value){
        if(ValueDecoder.isReference(value)){
            entryBlock.setValueAsReference(getMaterials().resolveReference(value));
        }else {
            ValueDecoder.EncodeResult encodeResult=ValueDecoder.encodeGuessAny(value);
            if(encodeResult!=null){
                entryBlock.setValueAsRaw(encodeResult.valueType, encodeResult.value);
            }else {
                entryBlock.setValueAsString(ValueDecoder
                        .unEscapeSpecialCharacter(value));
            }
        }
    }
}
