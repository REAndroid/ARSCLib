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
 package com.reandroid.arsc.item;

 import com.reandroid.arsc.io.BlockReader;

 import java.nio.charset.StandardCharsets;

 public class FixedLengthString  extends StringItem{
     private final int bytesLength;
     public FixedLengthString(int bytesLength){
         super(true);
         this.bytesLength=bytesLength;
         setBytesLength(bytesLength);
     }
     @Override
     byte[] encodeString(String str){
         if(str==null){
             return new byte[bytesLength];
         }
         byte[] bts=getUtf16Bytes(str);
         byte[] results=new byte[bytesLength];
         int len=bts.length;
         if(len>bytesLength){
             len=bytesLength;
         }
         System.arraycopy(bts, 0, results, 0, len);
         return results;
     }
     @Override
     String decodeString(){
         return decodeUtf16Bytes(getBytesInternal());
     }
     @Override
     public StyleItem getStyle(){
         return null;
     }
     @Override
     int calculateReadLength(BlockReader reader){
         return bytesLength;
     }
     private static String decodeUtf16Bytes(byte[] bts){
         if(isNullBytes(bts)){
             return null;
         }
         int len=getEndNullPosition(bts);
         return new String(bts,0, len, StandardCharsets.UTF_16LE);
     }
     private static int getEndNullPosition(byte[] bts){
         int max=bts.length;
         int result=0;
         boolean found=false;
         for(int i=1; i<max;i++){
             byte b0=bts[i-1];
             byte b1=bts[i];
             if(b0==0 && b1==0){
                 if(!found){
                     result=i;
                     found=true;
                 }else if(result<i-1){
                     return result;
                 }
             }else {
                 found=false;
             }
         }
         if(!found){
             return max;
         }
         return result;
     }
 }
