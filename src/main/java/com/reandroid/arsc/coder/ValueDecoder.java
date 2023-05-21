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
package com.reandroid.arsc.coder;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.*;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.common.EntryStore;

import java.util.Collection;
import java.util.Iterator;

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
    public static String unEscapeUnQuote(String text){
        if(text==null || text.length()<2){
            return text;
        }
        char first = text.charAt(0);
        if(first == '"'){
            return unQuoteWhitespace(text);
        }
        if(first != '\\' || !isSpecialCharacter(text.charAt(1))){
            return text;
        }
        return text.substring(1);
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
    public static String quoteWhitespace(String text){
        if(!isWhiteSpace(text)){
            return text;
        }
        return "\"" + text + "\"";
    }
    public static String unQuoteWhitespace(String text){
        if(text == null || text.length() < 3){
            return text;
        }
        if(text.charAt(0) != '"' || text.charAt(text.length()-1) != '"'){
            return text;
        }
        String unQuoted = text.substring(1, text.length()-1);
        if(!isWhiteSpace(unQuoted)){
            return text;
        }
        return unQuoted;
    }
    private static boolean isWhiteSpace(String text){
        if(text == null || text.length() == 0){
            return false;
        }
        char[] chars = text.toCharArray();
        for(int i = 0; i < chars.length; i++){
            if(!isWhiteSpace(chars[i])){
                return false;
            }
        }
        return true;
    }
    private static boolean isWhiteSpace(char ch){
        switch (ch){
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                return true;
            default:
                return false;
        }
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
    public static EncodeResult encodeColor(String value){
        return ValueCoder.encode(value, CommonType.COLOR.valueTypes());
    }
    public static EncodeResult encodeDimensionOrFraction(String value){
        EncodeResult encodeResult = ValueCoder.encode(value, ValueType.DIMENSION);
        if(encodeResult == null){
            encodeResult = ValueCoder.encode(value, ValueType.FRACTION);
        }
        return encodeResult;
    }

    public static String decodeAttributeName(EntryStore store, PackageBlock currentPackage, int resourceId){
        EntryGroup entryGroup = searchEntryGroup(store, currentPackage, resourceId);
        if(entryGroup==null){
            return ValueCoder.decodeUnknownResourceId(true, resourceId);
        }
        Entry entry = entryGroup.pickOne();
        if(entry == null){
            return ValueCoder.decodeUnknownResourceId(true, resourceId);
        }
        String prefix=null;
        if(currentPackage!=null){
            String name=currentPackage.getName();
            String other= entry.getPackageBlock().getName();
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
            return ValueCoder.decodeUnknownResourceId(is_reference, data);
        }
        return decode(valueType, data);
    }
    public static String decode(EntryStore entryStore, int currentPackageId, Value value){

        ValueType valueType = value.getValueType();
        if(valueType == ValueType.STRING){
            return value.getValueAsString();
        }
        int data = value.getData();
        if(valueType==ValueType.REFERENCE || valueType==ValueType.ATTRIBUTE){
            String currentPackageName = getPackageName(entryStore, currentPackageId);
            return buildReferenceValue(entryStore,
                    valueType, currentPackageName,
                    data);
        }
        return decode(valueType, data);
    }
    public static String decode(EntryStore entryStore, int currentPackageId, AttributeValue attributeValue){
        ValueType valueType = attributeValue.getValueType();
        if(valueType == ValueType.STRING){
            return attributeValue.getValueAsString();
        }
        int data = attributeValue.getData();
        if(valueType==ValueType.REFERENCE || valueType==ValueType.ATTRIBUTE){
            String currentPackageName = getPackageName(entryStore, currentPackageId);
            return buildReferenceValue(entryStore,
                    valueType, currentPackageName,
                    data);
        }
        if(valueType==ValueType.INT_DEC || valueType==ValueType.INT_HEX){
            String result = decodeAttribute(entryStore,
                    attributeValue.getNameResourceID(),
                    attributeValue.getData());
            if(result!=null){
                return result;
            }
        }
        return decode(valueType, data);
    }
    public static String decode(ValueType valueType, int data){
        return ValueCoder.decode(valueType, data);
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

    private static String buildReferenceValue(EntryStore entryStore, ValueType valueType, String currentPackageName, int resourceId){
        char atOrQues;
        if(valueType==ValueType.REFERENCE){
            if(resourceId==0){
                return "@null";
            }
            atOrQues='@';
        }else if(valueType==ValueType.ATTRIBUTE){
            if(resourceId==0){
                return "?null";
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
            return ValueCoder.decodeUnknownResourceId(valueType != ValueType.ATTRIBUTE, resourceId);
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
        pkgId = pkgId & 0xff;
        Collection<PackageBlock> allPkg = entryStore.getPackageBlocks(pkgId);
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
    private static String getPackageName(Entry entry){
        if(entry ==null){
            return null;
        }
        PackageBlock packageBlock= entry.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        return packageBlock.getName();
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
    private static AttributeBag getAttributeBag(EntryStore store, int resourceId){
        ResTableMapEntry mapEntry=getAttributeValueBag(store, resourceId);
        if(mapEntry==null){
            return null;
        }
        return AttributeBag.create(mapEntry.getValue());
    }
    private static ResTableMapEntry getAttributeValueBag(EntryStore store, int resourceId){
        if(store==null){
            return null;
        }
        Collection<EntryGroup> foundGroups = store.getEntryGroups(resourceId);
        ResTableMapEntry best=null;
        for(EntryGroup group:foundGroups){
            ResTableMapEntry valueBag = getAttributeValueBag(group);
            best=chooseBest(best, valueBag);
        }
        return best;
    }
    private static ResTableMapEntry getAttributeValueBag(EntryGroup entryGroup){
        if(entryGroup==null){
            return null;
        }
        ResTableMapEntry best=null;
        Iterator<Entry> iterator=entryGroup.iterator(true);
        while (iterator.hasNext()){
            Entry entry =iterator.next();
            ResTableMapEntry valueBag = getAttributeValueBag(entry);
            best=chooseBest(best, valueBag);
        }
        return best;
    }
    private static ResTableMapEntry getAttributeValueBag(Entry entry){
        if(entry ==null){
            return null;
        }
        TableEntry<?, ?> tableEntry = entry.getTableEntry();
        if(tableEntry instanceof ResTableMapEntry){
            return (ResTableMapEntry) tableEntry;
        }
        return null;
    }
    private static ResTableMapEntry chooseBest(ResTableMapEntry entry1, ResTableMapEntry entry2){
        if(entry1==null){
            return entry2;
        }
        if(entry2==null){
            return entry1;
        }
        if(entry2.getValue().childesCount()>entry1.getValue().childesCount()){
            return entry2;
        }
        return entry1;
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
}
