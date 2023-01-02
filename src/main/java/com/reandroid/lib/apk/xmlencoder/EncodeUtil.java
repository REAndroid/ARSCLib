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

 import java.io.File;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 public class EncodeUtil {
     public static boolean isEmpty(String text){
         if(text==null){
             return true;
         }
         text=text.trim();
         return text.length()==0;
     }
     public static String getQualifiersFromValuesXml(File valuesXml){
         String dirName=valuesXml.getParentFile().getName();
         int i=dirName.indexOf('-');
         if(i>0){
             return dirName.substring(i);
         }
         return "";
     }
     public static String getTypeNameFromValuesXml(File valuesXml){
         String name=valuesXml.getName();
         name=name.substring(0, name.length()-4);
         if(!name.equals("plurals") && name.endsWith("s")){
             name=name.substring(0, name.length()-1);
         }
         return name;
     }
     public static String sanitizeType(String type){
         Matcher matcher=PATTERN_TYPE.matcher(type);
         if(!matcher.find()){
             return "";
         }
         return matcher.group(1);
     }
     public static final String NULL_PACKAGE_NAME = "NULL_PACKAGE_NAME";
     private static final Pattern PATTERN_TYPE=Pattern.compile("^([a-z]+)[^a-z]*.*$");
}
