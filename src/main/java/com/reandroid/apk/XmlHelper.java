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
package com.reandroid.apk;

import com.reandroid.arsc.item.StringItem;
import com.reandroid.xml.*;

public class XmlHelper {

    public static void setTextContent(XMLElement element, StringItem stringItem){
        if(stringItem==null){
            element.clearChildNodes();
            return;
        }
        if(!stringItem.hasStyle()){
            element.setTextContent(stringItem.get());
        }else {
            element.setSpannableText(stringItem.getXml());
        }
    }
    public static String toXMLTagName(String typeName){
        // e.g ^attr-private
        if(typeName.length()>0 && typeName.charAt(0)=='^'){
            typeName = typeName.substring(1);
        }
        return typeName;
    }

    public static final String RESOURCES_TAG = "resources";
}
