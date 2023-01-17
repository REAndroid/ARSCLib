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
package com.reandroid.lib.apk.xmlencoder;

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.EntryBlock;

class XMLValuesEncoderColor extends XMLValuesEncoder{
    XMLValuesEncoderColor(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeStringValue(EntryBlock entryBlock, String value){
        ValueDecoder.EncodeResult encodeResult=ValueDecoder.encodeColor(value);
        if(encodeResult!=null){
            entryBlock.setValueAsRaw(encodeResult.valueType, encodeResult.value);
        }else {
            // If reaches here the value might be
            // file path e.g. res/color/something.xml
            entryBlock.setValueAsString(value);
        }
    }
}
