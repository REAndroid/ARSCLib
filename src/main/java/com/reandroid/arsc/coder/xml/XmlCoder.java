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
package com.reandroid.arsc.coder.xml;

import com.reandroid.apk.XmlHelper;
import com.reandroid.apk.xmldecoder.XMLDecodeHelper;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.*;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class XmlCoder {
    private static XmlCoder sInstance;

    public final ValuesXml VALUES_XML = new ValuesXml();
    public XmlCoder(){
    }

    public static class ValuesXml{
        private final BagRootAttribute BAG_ROOT_ATTRIBUTE = new BagRootAttribute();
        private final BagChild BAG_CHILD = new BagChild();
        public ValuesXml(){
        }

        public void decodeTable(File resourcesDir,
                                TableBlock tableBlock,
                                Predicate<Entry> decodedEntries) throws IOException {
            ValuesDirectorySerializer directorySerializer =
                    new ValuesDirectorySerializer(resourcesDir);
            decodeTable(directorySerializer, tableBlock, decodedEntries);
        }
        public void decodeTable(ValuesSerializerFactory serializerFactory,
                                  TableBlock tableBlock,
                                  Predicate<Entry> decodedEntries) throws IOException {
            for (PackageBlock packageBlock : tableBlock.listPackages()){
                decodePackage(serializerFactory, packageBlock, decodedEntries);
            }
        }
        public void decodePackage(ValuesSerializerFactory serializerFactory,
                           PackageBlock packageBlock,
                           Predicate<Entry> decodedEntries) throws IOException {

            packageBlock.sortTypes();

            Collection<SpecTypePair> specs = packageBlock.listSpecTypePairs();

            for (SpecTypePair specTypePair : specs){
                Set<ResConfig> configs = specTypePair.listResConfig();
                for(ResConfig resConfig : configs){
                    TypeBlock typeBlock = specTypePair.getTypeBlock(resConfig);
                    XmlSerializer serializer = serializerFactory.createSerializer(typeBlock);
                    int entriesCount = decode(serializer, specTypePair, resConfig, decodedEntries);
                    serializerFactory.onFinish(serializer, entriesCount);
                }
            }
        }
        public int decode(XmlSerializer serializer,
                           SpecTypePair specTypePair,
                           ResConfig resConfig,
                           Predicate<Entry> decodedEntries) throws IOException {
            Iterator<ResourceEntry> resources = specTypePair.getResources();
            return decode(serializer, resources, resConfig, decodedEntries);
        }
        public int decode(XmlSerializer serializer,
                           Iterator<ResourceEntry> resources,
                           ResConfig resConfig,
                           Predicate<Entry> decodedEntries) throws IOException {
            int entriesCount = 0;
            while (resources.hasNext()){
                ResourceEntry resourceEntry = resources.next();
                boolean decoded = decode(serializer,
                        resourceEntry,
                        resConfig,
                        decodedEntries);
                if(decoded){
                    entriesCount++;
                }
            }
            return entriesCount;
        }
        public boolean decode(XmlSerializer serializer,
                           ResourceEntry resourceEntry,
                           ResConfig resConfig,
                           Predicate<Entry> decodedEntries) throws IOException {
            Entry entry = resourceEntry.get(resConfig);
            if(entry == null || decodedEntries.test(entry)){
                return false;
            }
            if(entry.isComplex()){
                decodeBag(serializer, entry);
            }else if (!entry.isNull()){
                decodeScalar(serializer, entry);
            }else {
                return false;
            }
            return true;
        }
        private void decodeBag(XmlSerializer serializer, Entry entry) throws IOException {
            String tag = getTag(entry);
            XmlDecodeUtil.entryIndent(serializer);
            serializer.startTag(null, tag);
            serializer.attribute(null, "name", entry.getName());
            BAG_ROOT_ATTRIBUTE.decode(serializer, entry);
            int childCount = BAG_CHILD.decode(serializer, entry);
            if(childCount != 0){
                XmlDecodeUtil.entryIndent(serializer);
            }
            serializer.endTag(null, tag);
        }
        private String getTag(Entry entry){
            String tag = XmlDecodeUtil.toXMLTagName(entry.getTypeName());
            if(!tag.contains("array")){
                return tag;
            }
            ResValueMap[] valueMaps = entry.getResValueMapArray().getChildes();
            if(valueMaps.length == 0){
                return tag;
            }
            Set<ValueType> valueTypeSet = new HashSet<>();
            for(ResValueMap valueMap:valueMaps){
                valueTypeSet.add(valueMap.getValueType());
            }
            if(valueTypeSet.size() != 1){
                return tag;
            }
            if(valueTypeSet.contains(ValueType.STRING)){
                return "string-" + tag;
            }
            if(valueTypeSet.contains(ValueType.DEC)){
                return "integer-" + tag;
            }
            return tag;
        }
        private void decodeScalar(XmlSerializer serializer, Entry entry) throws IOException {
            String tag = XmlDecodeUtil.toXMLTagName(entry.getTypeName());
            XmlDecodeUtil.entryIndent(serializer);
            serializer.startTag(null, tag);
            serializer.attribute(null, "name", entry.getName());
            if(ignoreIdValue(entry)){
                serializer.endTag(null, tag);
                return;
            }

            ResValue resValue = entry.getResValue();
            if(resValue.getValueType() == ValueType.STRING){
                XmlDecodeUtil.text(serializer, resValue.getDataAsPoolString());
            }else {
                serializer.text(resValue.decodeValue());
            }
            serializer.endTag(null, tag);
        }
        private boolean ignoreIdValue(Entry entry){
            if(!TypeString.isTypeId(entry.getTypeName())){
                return false;
            }
            ResValue resValue = entry.getResValue();
            ValueType valueType = resValue.getValueType();
            if(valueType == ValueType.BOOLEAN){
                return true;
            }
            if(valueType == ValueType.STRING){
                String value = resValue.getValueAsString();
                return value == null || value.length() == 0;
            }
            return false;
        }
        public void encode(File valuesXmlFile, PackageBlock packageBlock) throws IOException, XmlPullParserException {
            XmlPullParser parser = XMLFactory.newPullParser(valuesXmlFile);

            TypeBlock typeBlock = packageBlock.getOrCreateTypeBlock(
                    XmlEncodeUtil.getQualifiersFromValuesXml(valuesXmlFile),
                    XmlEncodeUtil.getTypeFromValuesXml(valuesXmlFile));

            encode(parser, typeBlock);
        }
        public void encode(XmlPullParser parser, TypeBlock typeBlock) throws IOException, XmlPullParserException {
            int event = parser.getEventType();
            boolean documentStarted = false;
            if(event == XmlPullParser.START_DOCUMENT){
                documentStarted = true;
                parser.next();
            }
            event = XMLUtil.ensureStartTag(parser);
            if(event != XmlPullParser.START_TAG){
                throw new XmlEncodeException("Expecting xml state START_TAG but found: "
                        + XMLUtil.toEventName(parser.getEventType()));
            }
            if(PackageBlock.TAG_resources.equals(parser.getName())){
                parser.next();
            }else if(documentStarted){
                throw new XmlEncodeException("Expecting <resources> tag but found: " + parser.getName());
            }
            while (XMLUtil.ensureStartTag(parser) == XmlPullParser.START_TAG){
                XMLElement element = XMLElement.parseElement(parser);
                encodeEntry(element, typeBlock);
            }
            IOUtil.close(parser);
        }
        public void encodeEntry(XMLElement entryElement, TypeBlock typeBlock) throws IOException{
            Entry entry = typeBlock.getOrCreateDefinedEntry(entryElement.getAttributeValue("name"));
            if(entry == null){
                throw new XmlEncodeException("Undefined entry name: " + entryElement.getDebugText());
            }
            if(isBag(entryElement)){
                encodeBag(entryElement, entry);
            }else {
                encodeScalar(entryElement, entry);
            }
        }
        public void encodeScalar(XMLElement element, Entry entry) throws IOException{
            entry.ensureComplex(false);
            if(isTypeId(element)){
                encodeScalarId(entry);
            }else {
                encodeScalarAny(element, entry);
            }
            checkVisibility(entry);
        }

        public void encodeBag(XMLElement element, Entry entry) throws IOException{
            BAG_ROOT_ATTRIBUTE.encode(element, entry);
            Iterator<? extends XMLElement> childes = element.getElements();
            while (childes.hasNext()){
                BAG_CHILD.encode(childes.next(), entry);
            }
            sortMap(entry);
            checkVisibility(entry);
        }
        private boolean isBag(XMLElement element){
            String tag = element.getName();
            if("string".equals(tag)){
                return false;
            }
            if(element.hasChildElements()){
                return true;
            }
            if(element.hasTextNode()){
                return false;
            }
            return element.hasAttribute("parent")
                    || element.hasAttribute("formats");
        }
        private void encodeScalarId(Entry entry){
            entry.setValueAsBoolean(false);
            entry.getHeader().setWeak(true);
        }
        private void encodeScalarAny(XMLElement element, Entry entry) throws IOException{
            ResValue resValue = entry.getResValue();
            if(element.hasChildElements()){
                resValue.setValueAsString(StyleDocument.copyInner(element));
                return;
            }
            String text = element.getTextContent();
            EncodeResult encodeResult = ValueCoder.encodeReference(entry.getPackageBlock(), text);
            if(encodeResult == null){
                AttributeDataFormat dataFormat = AttributeDataFormat.fromValueTypeName(
                        element.getAttributeValue("type"));
                encodeResult = ValueCoder.encode(text, dataFormat);
                if(encodeResult == null && dataFormat != null && !dataFormat.contains(ValueType.STRING)){
                    throw new XmlEncodeException("Invalid value: " + element);
                }
            }
            if(encodeResult != null){
                if(encodeResult.isError()){
                    throw new XmlEncodeException(encodeResult.getError()+ ": " + element.getDebugText());
                }
                resValue.setValue(encodeResult);
            }else {
                resValue.setValueAsString(StyleDocument.copyInner(element));
            }
        }

        private boolean isTypeId(XMLElement element){
            if(element.hasChildElements()){
                return false;
            }
            return TypeString.isTypeId(element.getName());
        }
        private void checkVisibility(Entry entry){
            ValueHeader valueHeader = entry.getHeader();
            if(valueHeader == null){
                return;
            }
            TypeBlock typeBlock = entry.getTypeBlock();
            if(typeBlock == null){
                return;
            }
            if(typeBlock.isTypeAttr() || typeBlock.isTypeId()){
                valueHeader.setPublic(true);
            }
        }
        private void sortMap(Entry entry){
            ResValueMapArray mapArray = entry.getResValueMapArray();
            if(mapArray != null){
                mapArray.sort();
            }
        }
    }
    public static class BagChild {
        public void encode(XMLElement child, Entry entry) throws IOException{
            ChildType childType = ChildType.getType(child);
            if(childType == null){
                throw new XmlEncodeException("Unknown child bag: " + child.getDebugText());
            }
            switch (childType){
                case ATTR:
                    encodeAttr(child, entry);
                    break;
                case ARRAY:
                    encodeArray(child, entry);
                    break;
                case PLURAL:
                    encodePlural(child, entry);
                    break;
                case STYLE:
                    encodeStyle(child, entry);
                    break;
            }
        }
        public int decode(XmlSerializer serializer, Entry entry) throws IOException{
            ChildType childType = ChildType.getType(entry);
            switch (childType){
                case ATTR:
                    return decodeAttr(serializer, entry);
                case ARRAY:
                    return decodeArray(serializer, entry);
                case PLURAL:
                    return decodePlural(serializer, entry);
                case STYLE:
                    return decodeStyle(serializer, entry);
            }
            return 0;
        }
        public int decodeAttr(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();

            ResValueMap formatsMap = mapEntry.getByType(AttributeType.FORMATS);
            AttributeDataFormat bagType = AttributeDataFormat.typeOfBag(formatsMap.getData());
            ResValueMap[] bagItems = mapEntry.listResValueMap();

            int childCount = 0;
            for(int i = 0; i < bagItems.length; i++){
                ResValueMap valueMap = bagItems[i];
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType != null){
                    continue;
                }
                XmlDecodeUtil.bagIndent(serializer);
                assert bagType != null;
                serializer.startTag(null, bagType.getName());
                serializer.attribute(null,"name", valueMap.decodeName());
                serializer.text(valueMap.decodeValue());

                serializer.endTag(null, bagType.getName());
                childCount ++;
            }
            return childCount;
        }
        public int decodePlural(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildes()){
                if(valueMap == null){
                    continue;
                }
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType == null){
                    continue;
                }
                XmlDecodeUtil.bagIndent(serializer);
                serializer.startTag(null, "item");
                serializer.attribute(null, "quantity", attributeType.getName());
                if(valueMap.getValueType() == ValueType.STRING){
                    XmlDecodeUtil.text(serializer,
                            valueMap.getDataAsPoolString());
                }else {
                    serializer.text(valueMap.decodeValue());
                }
                serializer.endTag(null, "item");
                childCount++;
            }
            return childCount;
        }
        public int decodeStyle(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildes()){
                if(valueMap == null){
                    continue;
                }
                XmlDecodeUtil.bagIndent(serializer);
                serializer.startTag(null, "item");
                serializer.attribute(null, "name",
                        valueMap.decodeName(true));
                if(valueMap.getValueType() == ValueType.STRING){
                    XmlDecodeUtil.text(serializer,
                            valueMap.getDataAsPoolString());
                }else {
                    serializer.text(valueMap.decodeValue());
                }
                serializer.endTag(null, "item");
                childCount ++;
            }
            return childCount++;
        }
        public int decodeArray(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildes()){
                if(valueMap == null){
                    continue;
                }
                XmlDecodeUtil.bagIndent(serializer);
                serializer.startTag(null, "item");
                if(valueMap.getValueType() == ValueType.STRING){
                    XmlDecodeUtil.text(serializer,
                            valueMap.getDataAsPoolString());
                }else {
                    serializer.text(valueMap.decodeValue());
                }
                serializer.endTag(null, "item");
                childCount ++;
            }
            return childCount;
        }
        public void encodeAttr(XMLElement child, Entry entry) throws IOException{
            AttributeDataFormat dataFormat = AttributeDataFormat.fromBagTypeName(child.getName());
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();
            ResValueMap formats = mapArray.getOrCreateType(AttributeType.FORMATS);
            formats.addAttributeTypeFormat(dataFormat);
            ResValueMap valueMap = mapArray.createNext();
            XMLAttribute attribute = child.getAttribute("name");

            ResourceEntry resourceEntry = valueMap.encodeIdName(attribute.getPrefix(),
                    attribute.getValue());
            if(resourceEntry == null){
                throw new XmlEncodeException("Unknown " + dataFormat.getName()
                        + " name: " + child.getDebugText());
            }

            EncodeResult encodeResult = ValueCoder.encode(child.getTextContent().trim());
            if(encodeResult == null){
                // TODO: unbelievable! we always expect INT/HEX on enum/flags lets throw exception to see
                throw new XmlEncodeException("Unexpected value: " + child.getDebugText());
            }
            if(encodeResult.isError()){
                throw new XmlEncodeException(encodeResult.getError() + ": " + child.getDebugText());
            }
            valueMap.setValue(encodeResult);
            entry.getHeader().setPublic(true);
        }
        public void encodeArray(XMLElement child, Entry entry) throws IOException{
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();

            ResValueMap valueMap = mapArray.createNext();
            valueMap.setArrayIndex();

            String text = child.getTextContent();
            EncodeResult encodeResult = ValueCoder.encodeReference(entry.getPackageBlock(), text);
            if(encodeResult == null){
                AttributeDataFormat dataFormat = getParentArrayType(child);
                encodeResult = ValueCoder.encode(text, dataFormat);
            }
            if(encodeResult != null){
                if(encodeResult.isError()){
                    throw new XmlEncodeException("Unexpected array value: " + child.getDebugText());
                }
                valueMap.setValue(encodeResult);
            }
            valueMap.setValueAsString(StyleDocument.copyInner(child));
        }
        private AttributeDataFormat getParentArrayType(XMLElement child){
            XMLElement parent = child.getParentElement();
            if(parent == null){
                return null;
            }
            String name = parent.getName();
            int i = name.indexOf('-');
            if(i < 0){
                return null;
            }
            name = name.substring(0, i);
            return AttributeDataFormat.fromValueTypeName(name);
        }
        public void encodePlural(XMLElement child, Entry entry) throws IOException{
            AttributeType attributeType = AttributeType.fromName(child.getAttributeValue("quantity"));
            if(attributeType == null){
                throw new XmlEncodeException("Failed to get plural attribute: " + child.getDebugText());
            }
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();
            ResValueMap valueMap = mapArray.createNext();
            valueMap.setAttributeType(attributeType);

            String text = child.getTextContent();
            EncodeResult encodeResult = ValueCoder.encodeReference(entry.getPackageBlock(), text);
            if(encodeResult == null){
                encodeResult = ValueCoder.encode(text);
            }
            if(encodeResult != null){
                if(encodeResult.isError()){
                    throw new XmlEncodeException(encodeResult.getError() + ": " + child.getDebugText());
                }
                valueMap.setValue(encodeResult);
            }
            valueMap.setValueAsString(StyleDocument.copyInner(child));
        }
        public void encodeStyle(XMLElement child, Entry entry) throws IOException{
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();

            ResValueMap valueMap = mapArray.createNext();
            EncodeResult encodeResult = valueMap.encodeStyle(child);
            if(encodeResult.isError()){
                throw new XmlEncodeException(encodeResult.getError() + ": " + child.getDebugText());
            }
        }
    }
    public static class BagRootAttribute {
        public void decode(XmlSerializer serializer, Entry entry) throws IOException {
            decodeParent(serializer, entry);
            decodeAttrTypes(serializer, entry);
        }
        public void decodeAttrTypes(XmlSerializer serializer, Entry entry) throws IOException {
            if(!entry.getTypeBlock().isTypeAttr()){
                return;
            }
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            for(ResValueMap valueMap : mapArray.getChildes()){
                if(valueMap == null){
                    continue;
                }
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType == null){
                    continue;
                }
                if(valueMap.getValueType() == ValueType.STRING){
                    XmlDecodeUtil.attribute(serializer,
                            attributeType.getName(),
                            valueMap.getDataAsPoolString());
                }else {
                    String value = valueMap.decodeValue();
                    if(value.length() > 0 || mapArray.childesCount() < 2){
                        serializer.attribute(null,
                                attributeType.getName(),
                                value);
                    }
                }
            }
        }
        public void decodeParent(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            String parent = mapEntry.decodeParentId();
            if(parent == null){
                return;
            }
            serializer.attribute(null, "parent", parent);
        }
        public void encode(XMLElement element, Entry entry) throws IOException{
            Iterator<? extends XMLAttribute> attributes = element.getAttributes();
            while (attributes.hasNext()){
                XMLAttribute xmlAttribute = attributes.next();
                encode(xmlAttribute, entry);
            }
        }
        public void encode(XMLAttribute xmlAttribute, Entry entry) throws IOException{
            if(xmlAttribute.getPrefix() != null){
                throw new XmlEncodeException("Unknown root attribute: " + xmlAttribute.getDebugText());
            }
            encode(xmlAttribute.getName(false),
                    xmlAttribute.getValue(false), entry);
        }
        public void encode(String name, String value, Entry entry) throws IOException{
            if(name.equals("name")){
                encodeName(value, entry);
            }else if(name.equals("parent")){
                encodeParent(value, entry);
            }else if(name.equals("formats")){
                encodeFormats(value, entry);
            }else {
                AttributeType attributeType = AttributeType.fromName(name);
                if(attributeType != null){
                    encodeType(attributeType, value, entry);
                }else{
                    // TODO: what could be ?
                }
            }
        }
        public void encodeType(AttributeType attributeType, String value, Entry entry) throws IOException{
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();
            ResValueMap valueMap = mapArray.getOrCreateType(attributeType);
            EncodeResult encodeResult = ValueCoder.encodeReference(entry.getPackageBlock(), value);
            if(encodeResult != null){
                if(encodeResult.isError()){
                    throw new XmlEncodeException(encodeResult.getError());
                }
                valueMap.setValue(encodeResult);
                return;
            }
            encodeResult = ValueCoder.encode(value);
            if(encodeResult != null){
                if(encodeResult.isError()){
                    throw new XmlEncodeException(encodeResult.getError());
                }
                valueMap.setValue(encodeResult);
                return;
            }
            // TODO: will never reach here, we are expecting min, max and l10n only
            value = XmlSanitizer.unEscapeUnQuote(value);
            valueMap.setValueAsString(value);
        }
        public void encodeFormats(String value, Entry entry) {
            entry.ensureComplex(true);
            ResValueMapArray mapArray = entry.getResValueMapArray();
            ResValueMap valueMap = mapArray.getOrCreateType(AttributeType.FORMATS);
            // TODO: validate unknown formats
            AttributeDataFormat[] dataFormats = AttributeDataFormat.parseValueTypes(value);
            valueMap.setData(AttributeDataFormat.sum(dataFormats));
        }
        public void encodeParent(String value, Entry entry) throws IOException{
            PackageBlock packageBlock = entry.getPackageBlock();
            EncodeResult parent = ValueCoder.encodeReference(packageBlock,
                    value);
            if(parent != null){
                if(parent.isError()){
                    throw new XmlEncodeException(parent.getError());
                }
                entry.ensureComplex(true);
                entry.getResTableMapEntry().setParentId(parent.value);
            }
        }
        public void encodeName(String value, Entry entry) {
            if(entry.isDefined()){
                return;
            }
            // TODO: confirm will never reach here

            if(value == null || value.length() == 0){
                return;
            }
            entry.setName(value);
        }
    }
    enum ChildType{
        ATTR,
        ARRAY,
        PLURAL,
        STYLE;
        public static ChildType getType(XMLElement child){
            String tag = child.getName(false);
            if(AttributeDataFormat.fromBagTypeName(tag) != null){
                return ATTR;
            }
            if(!"item".equals(tag)){
                return null;
            }
            if(child.getAttribute("name") != null){
                return STYLE;
            }
            int count = child.getAttributeCount();
            if(count == 0 || (count == 1 && child.getAttribute("type") != null)){
                return ARRAY;
            }

            AttributeType attributeType = AttributeType.fromName(child.getAttributeValue("quantity"));
            if(attributeType != null && attributeType.isPlural()){
                return PLURAL;
            }
            return STYLE;
        }
        public static ChildType getType(Entry entry){
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            if(mapEntry.isAttr()){
                return ATTR;
            }
            if(mapEntry.isArray()){
                return ARRAY;
            }
            if(mapEntry.isPlural()){
                return PLURAL;
            }
            return STYLE;
        }
    }

    public static XmlCoder getInstance(){
        if(sInstance != null){
            return sInstance;
        }
        synchronized (XmlCoder.class){
            if(sInstance == null){
                sInstance = new XmlCoder();
            }
            return sInstance;
        }
    }
}
