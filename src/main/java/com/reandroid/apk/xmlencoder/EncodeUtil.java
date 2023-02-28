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

 import com.reandroid.apk.ApkUtil;

 import java.io.File;
 import java.util.Comparator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;

 public class EncodeUtil {
     public static void sortStrings(List<String> stringList){
         Comparator<String> cmp=new Comparator<String>() {
             @Override
             public int compare(String s1, String s2) {
                 return s1.compareTo(s2);
             }
         };
         stringList.sort(cmp);
     }
     public static boolean isPublicXml(File file){
         if(!ApkUtil.FILE_NAME_PUBLIC_XML.equals(file.getName())){
             return false;
         }
         File dir = file.getParentFile();
         return dir!=null && dir.getName().equals("values");
     }
     public static void sortPublicXml(List<File> fileList){
         Comparator<File> cmp=new Comparator<File>() {
             @Override
             public int compare(File f1, File f2) {
                 String n1=f1.getAbsolutePath();
                 String n2=f2.getAbsolutePath();
                 return n1.compareTo(n2);
             }
         };
         fileList.sort(cmp);
     }
     public static void sortValuesXml(List<File> fileList){
         Comparator<File> cmp=new Comparator<File>() {
             @Override
             public int compare(File f1, File f2) {
                 String n1=getValuesXmlCompare(f1);
                 String n2=getValuesXmlCompare(f2);
                 return n1.compareTo(n2);
             }
         };
         fileList.sort(cmp);
     }
     private static String getValuesXmlCompare(File file){
         String name=file.getName().toLowerCase();
         if(name.equals("public.xml")){
             return "0";
         }
         if(name.equals("ids.xml")){
             return "1";
         }
         if(name.contains("attr")){
             return "2";
         }
         return "3 "+name;
     }
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
     public static String getEntryPathFromResFile(File resFile){
         File typeDir=resFile.getParentFile();
         File resDir=typeDir.getParentFile();
         return resDir.getName()
                 +"/"+typeDir.getName()
                 +"/"+resFile.getName();
     }
     public static String getEntryNameFromResFile(File resFile){
         String name=resFile.getName();
         String ninePatch=".9.png";
         if(name.endsWith(ninePatch)){
             return name.substring(0, name.length()-ninePatch.length());
         }
         int i=name.lastIndexOf('.');
         if(i>0){
             name = name.substring(0, i);
         }
         return name;
     }
     public static String getQualifiersFromResFile(File resFile){
         String name=resFile.getParentFile().getName();
         int i=name.indexOf('-');
         if(i>0){
             return name.substring(i);
         }
         return "";
     }
     public static String getTypeNameFromResFile(File resFile){
         String name=resFile.getParentFile().getName();
         int i=name.indexOf('-');
         if(i>0){
             name=name.substring(0, i);
         }
         if(!name.equals("plurals") && name.endsWith("s")){
             name=name.substring(0, name.length()-1);
         }
         return name;
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
         if(type.startsWith("^attr")){
             return type;
         }
         Matcher matcher=PATTERN_TYPE.matcher(type);
         if(!matcher.find()){
             return "";
         }
         return matcher.group(1);
     }
     public static final String NULL_PACKAGE_NAME = "NULL_PACKAGE_NAME";
     private static final Pattern PATTERN_TYPE=Pattern.compile("^([a-z]+)[^a-z]*.*$");

     public static final String URI_ANDROID = "http://schemas.android.com/apk/res/android";
     public static final String URI_APP = "http://schemas.android.com/apk/res-auto";
     public static final String PREFIX_ANDROID = "android";
     public static final String PREFIX_APP = "app";
}
