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
package com.reandroid.arsc.pool;

import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.ResXmlStringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.group.StringGroup;
import com.reandroid.arsc.item.IntegerArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlString;

public class ResXmlStringPool extends BaseStringPool<ResXmlString> {
    public ResXmlStringPool(boolean is_utf8) {
        super(is_utf8);
    }
    @Override
    StringArray<ResXmlString> newInstance(IntegerArray offsets, IntegerItem itemCount, IntegerItem itemStart, boolean is_utf8) {
        return new ResXmlStringArray(offsets, itemCount, itemStart, is_utf8);
    }
    public ResXmlString getOrCreateAttributeName(int idMapCount, String str){
        StringGroup<ResXmlString> group = get(str);
        if(group!=null){
            for(ResXmlString xmlString:group.listItems()){
                if(xmlString.getIndex()>idMapCount){
                    return xmlString;
                }
            }
        }
        StringArray<ResXmlString> stringsArray = getStringsArray();
        stringsArray.ensureSize(idMapCount);
        int i=stringsArray.childesCount();
        stringsArray.ensureSize(i+1);
        ResXmlString xmlString=stringsArray.get(i);
        xmlString.set(str);
        refreshUniqueIdMap();
        return xmlString;
    }
    @Override
    public void onChunkLoaded() {
        super.onChunkLoaded();
        StyleArray styleArray = getStyleArray();
        if(styleArray.childesCount()>0){
            notifyResXmlStringPoolHasStyles(styleArray.childesCount());
        }
    }
    private static void notifyResXmlStringPoolHasStyles(int styleArrayCount){
        if(HAS_STYLE_NOTIFIED){
            return;
        }
        String msg="Not expecting ResXmlStringPool to have styles count="
                +styleArrayCount+",\n please create issue along with this apk/file on https://github.com/REAndroid/ARSCEditor";
        System.err.println(msg);
        HAS_STYLE_NOTIFIED=true;
    }
    private static boolean HAS_STYLE_NOTIFIED;
}
