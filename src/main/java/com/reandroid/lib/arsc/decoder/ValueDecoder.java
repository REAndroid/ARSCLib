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

public class ValueDecoder {

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
        if(valueType==ValueType.FIRST_INT||valueType==ValueType.INT_HEX){
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
            case FIRST_COLOR_INT:
            case INT_COLOR_ARGB4:
            case INT_COLOR_ARGB8:
            case INT_COLOR_RGB4:
            case INT_COLOR_RGB8:
            case LAST_COLOR_INT:
                return decodeColor(data);
            case DIMENSION:
            case FLOAT:
            case FRACTION:
                return decodeDimensionOrFloat(valueType, data);
            case INT_HEX:
                return decodeHex(data);
            case INT_DEC:
            case FIRST_INT:
            case LAST_INT:
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
            atOrQues='@';
        }else if(valueType==ValueType.ATTRIBUTE){
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
        return tableString.getHtml();
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
            return tableString.getHtml();
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
        return tableString.getHtml();
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
    private static String getDimensionUnit(int index){
        if(index<0 || index>DIMENSION_UNIT_STRS.length){
            index=1;
        }
        return DIMENSION_UNIT_STRS[index];
    }
    private static int getDimensionIndex(String unit){
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

    private static final String[] DIMENSION_UNIT_STRS = new String[] { "px", "dip", "sp", "pt", "in", "mm" };
    private static final float MANTISSA_MULT = 1.0f / (1 << 8);
    private static final float[] RADIX_MULTS = new float[] {
            1.0f * MANTISSA_MULT, 1.0f / (1 << 7) * MANTISSA_MULT,
            1.0f / (1 << 15) * MANTISSA_MULT, 1.0f / (1 << 23) * MANTISSA_MULT };
}
