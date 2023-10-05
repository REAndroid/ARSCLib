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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Creator;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.StringData;

public class StringIdArray extends IdSectionArray<StringId> {

    public StringIdArray(IntegerPair countAndOffset, Creator<StringId> creator) {
        super(countAndOffset, creator);
    }

    public void link(StringDataArray dataArray){
        StringId[] childes = getChildes();
        int length = childes.length;
        for(int i = 0; i < length; i++){
            StringId stringId = childes[i];
            StringData stringData = dataArray.getAt(stringId.get());
            stringId.linkStringData(stringData);
        }
    }
}
