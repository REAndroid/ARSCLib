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
package com.reandroid.lib.arsc.decoder;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.arsc.value.*;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;
import com.reandroid.lib.common.EntryStore;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class ValueDecoder {

     public static String escapeSpecialCharacter(String text){
         if(text==null || text.length()==0){
             return text;
         }
         if(isSpecialCharacter(text.charAt(0))){
             return '\\' +text;
         }
         return text;
     }
     public static String unEscapeSpecialCharacter(String text){
         if(text==null || text.length()<2){
             return text;
         }
         if(text.charAt(0)!='\\' || !isSpecialCharacter(text.charAt(1))){
             return text;
         }
         return text.substring(1);
     }
     private static boolean isSpecialCharacter(char ch){
         switch (ch){
             case '@':
             case '?':
             case '#':
                 return true;
             default:
                 return false;
         }
     }
     public static EncodeResult encodeGuessAny(String txt){
         if(txt==null){
             return null;
         }
         if("@empty".equals(txt)){
             return new EncodeResult(ValueType.NULL, 0);
         }
         if("@null".equals(txt)){
             return new EncodeResult(ValueType.REFERENCE, 0);
         }
         EncodeResult result=encodeColor(txt);
         if(result!=null){
             return result;
         }
         result=encodeDimensionOrFloat(txt);
         if(result!=null){
             return result;
         }
         result=encodeHexOrInt(txt);
         if(result!=null){
             return result;
         }
         return encodeBoolean(txt);
     }
     public static EncodeResult encodeBoolean(String txt){
         if(txt==null){
             return null;
         }
         txt=txt.trim().toLowerCase();
         if(txt.equals("true")){
             return new EncodeResult(ValueType.INT_BOOLEAN, 0xffffffff);
         }
         if(txt.equals("false")){
             return new EncodeResult(ValueType.INT_BOOLEAN, 0);
         }
         return null;
     }
     public static boolean isInteger(String txt){
         if(txt==null){
             return false;
         }
         return PATTERN_INTEGER.matcher(txt).matches();
     }
     public static boolean isHex(String txt){
         if(txt==null){
             return false;
         }
         return PATTERN_HEX.matcher(txt).matches();
     }
     public static boolean isReference(String txt){
         if(txt==null){
             return false;
         }
         if(isNullReference(txt)){
             return true;
         }
         return PATTERN_REFERENCE.matcher(txt).matches();
     }
     private static boolean isNullReference(String txt){
         if("@null".equals(txt)){
             return true;
         }
         if("@empty".equals(txt)){
             return true;
         }
         return false;
     }
     public static EncodeResult encodeColor(String value){
         if(value==null){
             return null;
         }
         Matcher matcher = PATTERN_COLOR.matcher(value);
         if(!matcher.find()){
             return null;
         }
         value=matcher.group(1);
         ValueType valueType;
         if(value.length()==6){
             valueType=ValueType.INT_COLOR_RGB8;
         }else {
             valueType=ValueType.INT_COLOR_ARGB8;
         }
         return new EncodeResult(valueType, parseHex(value));
     }
     public static EncodeResult encodeHexOrInt(String numString){
         if(numString==null){
             return null;
         }
         if(isHex(numString)){
             return new EncodeResult(ValueType.INT_HEX, parseHex(numString));
         }
         if(isInteger(numString)){
             return new EncodeResult(ValueType.INT_DEC, parseInteger(numString));
         }
         return null;
     }
     public static int parseHex(String hexString){
         boolean negative=false;
         hexString=hexString.trim().toLowerCase();
         if(hexString.startsWith("-")){
             negative=true;
             hexString=hexString.substring(1);
         }
         if(!hexString.startsWith("0x")){
             hexString="0x"+hexString;
         }
         long l=Long.decode(hexString);
         if(negative){
             l=-l;
         }
         return (int) l;
     }
     public static int parseInteger(String intString){
         intString=intString.trim();
         boolean negative=false;
         if(intString.startsWith("-")){
             negative=true;
             intString=intString.substring(1);
         }
         long l=Long.parseLong(intString);
         if(negative){
             l=-l;
         }
         return (int) l;
     }
     public static EncodeResult encodeDimensionOrFloat(String value){
         if(value==null){
             return null;
         }
         Matcher matcher=PATTERN_DIMEN.matcher(value);
         if(!matcher.find()){
             return null;
         }
         String sign = matcher.group(1);
         String number = matcher.group(2);
         String unit = matcher.group(3);
         float fraction = Float.parseFloat(number);
         if("-".equals(sign)){
             fraction=-fraction;
         }
         return encodeDimensionOrFloat(fraction, unit);
     }
     private static EncodeResult encodeDimensionOrFloat(float val, String unit){
         if(unit==null||"".equals(unit)){
             return new EncodeResult(ValueType.FLOAT,
                     Float.floatToIntBits(val));
         }
         ValueType valueType = ValueType.DIMENSION;
         int index=0;
         if("%".equals(unit)||"%p".equals(unit)){
             val=val/100.0f;
             if("%p".equals(unit)){
                 index=1;
             }
             valueType = ValueType.FRACTION;
         }else {
             index=ValueDecoder.getDimensionIndex(unit);
         }
         int result = 0;
         int shift = 0;
         if(val!=0.0f){
             for(int i=0;i<4;i++){
                 float fl = val/ValueDecoder.RADIX_MULTS[i];
                 int fl_int = (int)fl;
                 int last = (fl_int&0xff);
                 if(fl_int!=0 && last==0){
                     shift = i;
                     result = fl_int;
                     break;
                 }
             }
         }
         shift = shift<<4;
         result = result | shift;
         result = result | index;
         return new EncodeResult(valueType, result);
     }

     public static String decodeAttributeName(EntryStore store, PackageBlock currentPackage, int resourceId){
        EntryGroup entryGroup=searchEntryGroup(store, currentPackage, resourceId);
        if(entryGroup==null){
            return String.format("@0x%08x", resourceId);
        }
        EntryBlock entryBlock=entryGroup.pickOne();
        if(entryBlock==null){
            return String.format("@0x%08x", resourceId);
        }
        String prefix=null;
        if(currentPackage!=null){
            String name=currentPackage.getName();
            String other=entryBlock.getPackageBlock().getName();
            if(!name.equals(other)){
                prefix=other+":";
            }
        }
        String name=entryGroup.getSpecName();
        if(prefix!=null){
            name=prefix+name;
        }
        return name;
    }
    public static String decodeAttribute(EntryStore store, int attrResId, int rawValue){
        AttributeBag attributeBag = getAttributeBag(store, attrResId);
        if(attributeBag==null){
            return null;
        }
        return attributeBag.decodeAttributeValue(store, rawValue);
    }

    public static String decodeEntryValue(EntryStore store, PackageBlock currentPackage, ValueType valueType, int data){
        if(store==null || currentPackage==null){
            return null;
        }
        if(valueType==ValueType.STRING){
            return decodeIntEntryString(currentPackage, data);
        }
        boolean is_reference=false;
        boolean is_attribute=false;
        if(valueType==ValueType.REFERENCE){
            if(data==0){
                return "@null";
            }
            is_reference=true;
        }
        if(valueType==ValueType.ATTRIBUTE){
            if(data==0){
                return "?null";
            }
            is_attribute=true;
        }
        if(is_reference || is_attribute){
            String ref=buildReferenceValue(store, valueType, currentPackage.getName(), data);
            if(ref!=null){
                return ref;
            }
            char atOrQues=is_reference?'@':'?';
            ref=atOrQues+toHexResourceId(data);
            return ref;
        }
        return decode(valueType, data);
    }
    public static String decodeIntEntry(EntryStore store, EntryBlock entryBlock){
        if(entryBlock==null){
            return null;
        }
        BaseResValue baseResValue = entryBlock.getResValue();
        if(!(baseResValue instanceof ResValueInt)){
            return null;
        }
        ResValueInt resValueInt=(ResValueInt)baseResValue;
        return decodeIntEntry(store, resValueInt);
    }
    public static String decodeIntEntry(EntryStore store, ResValueInt resValueInt){
        if(resValueInt==null){
            return null;
        }
        EntryBlock parentEntry=resValueInt.getEntryBlock();
        if(parentEntry==null){
            return null;
        }
        ValueType valueType=resValueInt.getValueType();
        int data=resValueInt.getData();
        return decodeIntEntry(store, parentEntry, valueType, data);
    }
    public static String decodeIntEntry(EntryStore store, ResValueBagItem bagItem){
        if(bagItem==null){
            return null;
        }
        EntryBlock parentEntry=bagItem.getEntryBlock();
        if(parentEntry==null){
            return null;
        }
        ValueType valueType=bagItem.getValueType();
        int data=bagItem.getData();
        return decodeIntEntry(store, parentEntry, valueType, data);
    }
    public static String decodeIntEntry(EntryStore store, EntryBlock parentEntry, ValueType valueType, int data){
        if(valueType==ValueType.NULL){
            return "@empty";
        }
        if(valueType==ValueType.STRING){
            return decodeIntEntryString(parentEntry, data);
        }
        boolean is_reference=false;
        boolean is_attribute=false;
        if(valueType==ValueType.REFERENCE){
            if(data==0){
                return "@null";
            }
            is_reference=true;
        }
        if(valueType==ValueType.ATTRIBUTE){
            if(data==0){
                return "?null";
            }
            is_attribute=true;
        }
        if(is_reference || is_attribute){
            String ref=buildReferenceValue(store, parentEntry, valueType, data);
            if(ref!=null){
                return ref;
            }
            char atOrQues=is_reference?'@':'?';
            ref=atOrQues+toHexResourceId(data);
            return ref;
        }
        return decode(valueType, data);
    }
    public static String buildReferenceValue(EntryStore store, EntryBlock entryBlock){
        if(entryBlock==null){
            return null;
        }
        BaseResValue baseResValue = entryBlock.getResValue();
        if(!(baseResValue instanceof ResValueInt)){
            return null;
        }
        ResValueInt resValueInt=(ResValueInt)baseResValue;
        int resourceId=resValueInt.getData();
        ValueType valueType=resValueInt.getValueType();
        return buildReferenceValue(store, entryBlock, valueType, resourceId);
    }
    public static String decode(EntryStore entryStore, int currentPackageId, int nameResourceId, ValueType valueType, int rawVal){
        String currPackageName=getPackageName(entryStore, currentPackageId);
        String result=buildReferenceValue(entryStore, valueType, currPackageName, rawVal);
        if(result!=null){
            return result;
        }
        if(valueType==ValueType.STRING){
            // Should not happen the string could be in ResXmlBlock, but if you are lazy here it goes
            return decodeString(entryStore, currentPackageId, rawVal);
        }
        if(valueType==ValueType.INT_DEC||valueType==ValueType.INT_HEX){
            result=decodeAttribute(entryStore, nameResourceId, rawVal);
            if(result!=null){
                return result;
            }
        }
        return decode(valueType, rawVal);
    }
    public static String decode(ValueType valueType, int data){
        if(valueType==null){
            return null;
        }
        switch (valueType){
            case INT_BOOLEAN:
                return decodeBoolean(data);
            case INT_COLOR_ARGB4:
            case INT_COLOR_ARGB8:
            case INT_COLOR_RGB4:
            case INT_COLOR_RGB8:
                return decodeColor(data);
            case DIMENSION:
            case FLOAT:
            case FRACTION:
                return decodeDimensionOrFloat(valueType, data);
            case INT_HEX:
                return decodeHex(data);
            case INT_DEC:
                return decodeInt(data);
        }
        return null;
    }
    public static String buildReference(String currentPackageName,
                                        String referredPackageName,
                                        char atOrQues,
                                        String typeName,
                                        String resourceName){
        StringBuilder builder=new StringBuilder();
        if(atOrQues!=0){
            builder.append(atOrQues);
        }
        if(!isEqualString(currentPackageName, referredPackageName)){
            if(!isEmpty(currentPackageName) && !isEmpty(referredPackageName)){
                builder.append(referredPackageName);
                if(!referredPackageName.endsWith(":")){
                    builder.append(':');
                }
            }
        }
        if(!isEmpty(typeName)){
            builder.append(typeName);
            builder.append('/');
        }
        builder.append(resourceName);
        return builder.toString();
    }

    private static String buildReferenceValue(EntryStore store, EntryBlock entryBlock, ValueType valueType, int resourceId){
        if(entryBlock==null){
            return null;
        }
        EntryGroup value=searchEntryGroup(store, entryBlock, resourceId);
        if(value==null){
            return null;
        }
        return buildReferenceValue(valueType, entryBlock, value);
    }
    private static String buildReferenceValue(ValueType valueType, EntryBlock entryBlock, EntryGroup value){
        char atOrQues;
        if(valueType==ValueType.REFERENCE){
            atOrQues='@';
        }else if(valueType==ValueType.ATTRIBUTE){
            atOrQues='?';
        }else {
            atOrQues=0;
        }
        String currentPackageName=getPackageName(entryBlock);
        String referredPackageName=getPackageName(value);
        String typeName=value.getTypeName();
        String name=value.getSpecName();
        return buildReference(currentPackageName, referredPackageName, atOrQues, typeName, name);
    }
    private static String buildReferenceValue(EntryStore entryStore, ValueType valueType, String currentPackageName, int resourceId){
        char atOrQues;
        if(valueType==ValueType.REFERENCE){
            if(resourceId==0){
                return "@null";
            }
            atOrQues='@';
        }else if(valueType==ValueType.ATTRIBUTE){
            if(resourceId==0){
                return "@empty";
            }
            atOrQues='?';
        }else {
            return null;
        }
        EntryGroup value=null;
        if(entryStore!=null){
            value=entryStore.getEntryGroup(resourceId);
        }
        if(value==null){
            return atOrQues+toHexResourceId(resourceId);
        }
        String referredPackageName=getPackageName(value);
        String typeName=value.getTypeName();
        String name=value.getSpecName();
        return buildReference(currentPackageName, referredPackageName, atOrQues, typeName, name);
    }
    private static String getPackageName(EntryStore entryStore, int packageOrResourceId){
        if(entryStore==null || packageOrResourceId==0){
            return null;
        }
        int pkgId=(packageOrResourceId>>24)&0xFF;
        if(pkgId==0){
            pkgId=packageOrResourceId;
        }
        Collection<PackageBlock> allPkg = entryStore.getPackageBlocks((byte) pkgId);
        if(allPkg==null){
            return null;
        }
        for(PackageBlock packageBlock:allPkg){
            String name=packageBlock.getName();
            if(name!=null){
                return name;
            }
        }
        return null;
    }
    private static String getPackageName(EntryGroup entryGroup){
        if(entryGroup==null){
            return null;
        }
        return getPackageName(entryGroup.pickOne());
    }
    private static String getPackageName(EntryBlock entryBlock){
        if(entryBlock==null){
            return null;
        }
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        return packageBlock.getName();
    }
    private static EntryGroup searchEntryGroup(EntryStore store, EntryBlock entryBlock, int resourceId){
        EntryGroup entryGroup=searchEntryGroup(entryBlock, resourceId);
        if(entryGroup!=null){
            return entryGroup;
        }
        if(store==null){
            return null;
        }
        return store.getEntryGroup(resourceId);
    }
    private static EntryGroup searchEntryGroup(EntryBlock entryBlock, int resourceId){
        if(entryBlock==null){
            return null;
        }
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        for(PackageBlock pkg:tableBlock.listPackages()){
            EntryGroup entryGroup=pkg.getEntryGroup(resourceId);
            if(entryGroup!=null){
                return entryGroup;
            }
        }
        return null;
    }
    private static EntryGroup searchEntryGroup(EntryStore store, PackageBlock packageBlock, int resourceId){
        if(packageBlock!=null){
            TableBlock tableBlock=packageBlock.getTableBlock();
            if(tableBlock!=null){
                for(PackageBlock pkg:tableBlock.listPackages()){
                    EntryGroup entryGroup=pkg.getEntryGroup(resourceId);
                    if(entryGroup!=null){
                        return entryGroup;
                    }
                }
            }
        }
        if(store!=null){
            return store.getEntryGroup(resourceId);
        }
        return null;
    }
    private static String decodeIntEntryString(EntryBlock entryBlock, int data){
        if(entryBlock==null){
            return null;
        }
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        TableStringPool pool = tableBlock.getTableStringPool();
        TableString tableString=pool.get(data);
        if(tableString==null){
            return null;
        }
        return escapeSpecialCharacter(tableString.getHtml());
    }
    private static String decodeString(EntryStore entryStore, int packageOrResourceId, int stringRef){
        if(entryStore==null||packageOrResourceId==0){
            return null;
        }
        int pkgId=(packageOrResourceId>>24)&0xFF;
        if(pkgId==0){
            pkgId=packageOrResourceId;
        }
        Collection<PackageBlock> allPkg = entryStore.getPackageBlocks((byte) pkgId);
        if(allPkg==null){
            return null;
        }
        TableString tableString=null;
        for(PackageBlock packageBlock:allPkg){
            TableBlock tableBlock=packageBlock.getTableBlock();
            if(tableBlock==null){
                continue;
            }
            TableString ts=tableBlock.getTableStringPool().get(stringRef);
            if(ts==null){
                continue;
            }
            if(tableString==null){
                tableString=ts;
            }else {
                // Duplicate result, could be from split apks
                return null;
            }
        }
        if(tableString!=null){
            return escapeSpecialCharacter(tableString.getHtml());
        }
        return null;
    }
    private static String decodeIntEntryString(PackageBlock packageBlock, int data){
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        TableStringPool pool = tableBlock.getTableStringPool();
        TableString tableString=pool.get(data);
        if(tableString==null){
            return null;
        }
        return escapeSpecialCharacter(tableString.getHtml());
    }

    private static String decodeHex(int rawVal){
        return String.format("0x%x", rawVal);
    }
    private static String decodeInt(int rawVal){
        return String.valueOf(rawVal);
    }

    private static String decodeBoolean(int data){
        if(data == 0xFFFFFFFF){
            return "true";
        }
        return "false";
    }

    private static String decodeColor(int rawVal){
        String hex=String.format("%x", rawVal);
        if(hex.length()<=6){
            return String.format("#%06x", rawVal);
        }
        return String.format("#%08x", rawVal);
    }
    private static String decodeDimensionOrFloat(ValueType valueType, int rawVal){
        return decodeFloat(rawVal, valueType);
    }
    private static String decodeFloat(int val, ValueType valueType){
        if(valueType==ValueType.FLOAT){
            float f=Float.intBitsToFloat(val);
            return Float.toString(f);
        }
        float f=complexToFloat(val);
        String unit="";
        switch (valueType){
            case FRACTION:
                f=f*100;
                if((val & 0x3)==0){
                    unit="%";
                }else {
                    unit="%p";
                }
                break;
            case DIMENSION:
                int i=(val & 0xf);
                unit=getDimensionUnit(i);
                break;
        }
        return Float.toString(f)+unit;
    }
    private static float complexToFloat(int complex) {
        int y=(complex >> 4) & 0x3;
        float result=complex & 0xffffff00;
        float y2=RADIX_MULTS[y];
        result=result * y2;
        return result;
    }
    static String getDimensionUnit(int index){
        if(index<0 || index>DIMENSION_UNIT_STRS.length){
            index=1;
        }
        return DIMENSION_UNIT_STRS[index];
    }
    static int getDimensionIndex(String unit){
        String[] dims=DIMENSION_UNIT_STRS;
        for(int i=0;i<dims.length;i++){
            if(dims[i].equals(unit)){
                return i;
            }
        }
        return 0;
    }
    private static String getResourceName(EntryStore store, EntryBlock entryBlock, int resourceId){
        if(entryBlock!=null){
            EntryGroup group=searchEntryGroup(entryBlock, resourceId);
            if(group!=null){
                String name=group.getSpecName();
                if(name!=null){
                    return name;
                }
            }
        }
        if(store==null){
            return null;
        }
        Collection<EntryGroup> foundGroups = store.getEntryGroups(resourceId);
        return pickResourceName(foundGroups);
    }
    private static String pickResourceName(Collection<EntryGroup> groups){
        if(groups==null){
            return null;
        }
        for(EntryGroup entryGroup:groups){
            String name=entryGroup.getSpecName();
            if(name!=null){
                return name;
            }
        }
        return null;
    }
    private static AttributeBag getAttributeBag(EntryStore store, int resourceId){
        ResValueBag resValueBag=getAttributeValueBag(store, resourceId);
        if(resValueBag==null){
            return null;
        }
        return AttributeBag.create(resValueBag);
    }
    private static ResValueBag getAttributeValueBag(EntryStore store, int resourceId){
        if(store==null){
            return null;
        }
        Collection<EntryGroup> foundGroups = store.getEntryGroups(resourceId);
        ResValueBag best=null;
        for(EntryGroup group:foundGroups){
            ResValueBag valueBag= getAttributeValueBag(group);
            best=chooseBest(best, valueBag);
        }
        return best;
    }
    private static ResValueBag getAttributeValueBag(EntryGroup entryGroup){
        if(entryGroup==null){
            return null;
        }
        ResValueBag best=null;
        Iterator<EntryBlock> iterator=entryGroup.iterator(true);
        while (iterator.hasNext()){
            EntryBlock entryBlock=iterator.next();
            ResValueBag valueBag= getAttributeValueBag(entryBlock);
            best=chooseBest(best, valueBag);
        }
        return best;
    }
    private static ResValueBag getAttributeValueBag(EntryBlock entryBlock){
        if(entryBlock==null){
            return null;
        }
        BaseResValue baseResValue = entryBlock.getResValue();
        if(baseResValue instanceof ResValueBag){
            return (ResValueBag) baseResValue;
        }
        return null;
    }
    private static ResValueBag chooseBest(ResValueBag valueBag1, ResValueBag valueBag2){
        if(valueBag1==null){
            return valueBag2;
        }
        if(valueBag2==null){
            return valueBag1;
        }
        if(valueBag2.getCount()>valueBag1.getCount()){
            return valueBag2;
        }
        return valueBag1;
    }
    private static String toHexResourceId(int resourceId){
        return String.format("0x%08x", resourceId);
    }
    private static boolean isEqualString(String str1, String str2){
        if(isEmpty(str1)){
            return isEmpty(str2);
        }
        return str1.equals(str2);
    }
    private static boolean isEmpty(String str){
        if(str==null){
            return true;
        }
        str=str.trim();
        return str.length()==0;
    }
    public static class EncodeResult{
        public final ValueType valueType;
        public final int value;
        public EncodeResult(ValueType valueType, int value){
            this.valueType=valueType;
            this.value=value;
        }
        @Override
        public String toString(){
            return valueType+": "+String.format("0x%08x", value);
        }
    }

    private static final String[] DIMENSION_UNIT_STRS = new String[] { "px", "dip", "sp", "pt", "in", "mm" };
    private static final float MANTISSA_MULT = 1.0f / (1 << 8);
    static final float[] RADIX_MULTS = new float[] {
            1.0f * MANTISSA_MULT, 1.0f / (1 << 7) * MANTISSA_MULT,
            1.0f / (1 << 15) * MANTISSA_MULT, 1.0f / (1 << 23) * MANTISSA_MULT };

     public static final Pattern PATTERN_COLOR = Pattern.compile("^#([0-9a-fA-F]{6,8})$");
     public static final Pattern PATTERN_DIMEN = Pattern.compile("^(-?)([0-9]+\\.[0-9E+]+)([dimnpstx%]{0,3})$");
     private static final Pattern PATTERN_INTEGER = Pattern.compile("^(-?)([0-9]+)$");
     private static final Pattern PATTERN_HEX = Pattern.compile("^0x[0-9a-fA-F]+$");
     public static final Pattern PATTERN_REFERENCE = Pattern.compile("^([?@])(([^\\s:@?/]+:)?)([^\\s:@?/]+)/([^\\s:@?/]+)$");

 }
