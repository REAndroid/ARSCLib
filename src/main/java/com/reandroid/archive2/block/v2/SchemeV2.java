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
package com.reandroid.archive2.block.v2;

import com.reandroid.archive2.block.SignatureId;
import com.reandroid.archive2.block.SignatureScheme;

public class SchemeV2 extends SignatureScheme {
    private final V2SignedDataList signedDataList;
    public SchemeV2(){
        super(1, SignatureId.V2);
        this.signedDataList = new V2SignedDataList();
        addChild(this.signedDataList);
    }

}
