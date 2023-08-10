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

import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.container.SpecTypePair;
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
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

public class XmlCoder {
    private static XmlCoder sInstance;

    public final ValuesXml VALUES_XML = new ValuesXml();
    private XmlCoderLogger mLogger;
    public XmlCoder(){
    }

    public void setLogger(XmlCoderLogger logger) {
        this.mLogger = logger;
        VALUES_XML.setLogger(logger);
    }
    public XmlCoderLogger getLogger() {
        return mLogger;
    }
    private void logMessage(String tag, String message){
        XmlCoderLogger logger = getLogger();
        if(logger != null){
            logger.logMessage(tag, message);
        }
    }
    private void logVerbose(String tag, String message){
        XmlCoderLogger logger = getLogger();
        if(logger != null){
            logger.logMessage(tag, message);
        }
    }

    public static class ValuesXml{
        private final BagRootAttribute BAG_ROOT_ATTRIBUTE = new BagRootAttribute();
        private final BagChild BAG_CHILD = new BagChild();
        private XmlCoderLogger mLogger;
        public ValuesXml(){
        }

        public void decodeTable(File resourcesDir,
                                TableBlock tableBlock,
                                Predicate<Entry> decodedEntries) throws IOException {
            logMessage("Decoding", "Resource table ...");
            ValuesDirectorySerializer directorySerializer =
                    new ValuesDirectorySerializer(resourcesDir);
            decodeTable(directorySerializer, tableBlock, decodedEntries);
            logMessage("Decoding", "Finished resource table");
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
                    logVerbose("Decoding", packageBlock.getName()
                            + ":" + typeBlock.getTypeName() + resConfig.getQualifiers());
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
            String tag = startEntry(serializer, entry);
            BAG_ROOT_ATTRIBUTE.decode(serializer, entry);
            int childCount = BAG_CHILD.decode(serializer, entry);
            endEntry(serializer, tag, childCount != 0);
        }
        private void decodeScalar(XmlSerializer serializer, Entry entry) throws IOException {
            String tag = startEntry(serializer, entry);
            if(ignoreIdValue(entry)){
                endEntry(serializer, tag);
                return;
            }
            entry.getResValue().serializeText(serializer);
            endEntry(serializer, tag);
        }
        private String startEntry(XmlSerializer serializer, Entry entry) throws IOException {
            String tag = entry.getXmlTag();
            XmlDecodeUtil.entryIndent(serializer);
            serializer.startTag(null, tag);
            serializer.attribute(null, ATTR_name, entry.getName());
            return tag;
        }
        private void endEntry(XmlSerializer serializer, String tag) throws IOException {
            endEntry(serializer, tag, false);
        }
        private void endEntry(XmlSerializer serializer, String tag, boolean indent) throws IOException {
            if(indent){
                XmlDecodeUtil.entryIndent(serializer);
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
            Entry entry = typeBlock.getOrCreateDefinedEntry(
                    entryElement.getAttributeValue(ATTR_name));
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
            entry.ensureComplex(true);
            BAG_ROOT_ATTRIBUTE.encode(element, entry);
            Iterator<? extends XMLElement> children = element.getElements();
            while (children.hasNext()){
                BAG_CHILD.encode(children.next(), entry);
            }
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
            return element.hasAttribute(ATTR_parent)
                    || element.hasAttribute(ATTR_formats)
                    || TypeString.isTypeArray(tag);
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
                        element.getAttributeValue(ATTR_type));
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


        public void setLogger(XmlCoderLogger logger) {
            this.mLogger = logger;
        }
        public XmlCoderLogger getLogger() {
            return mLogger;
        }
        private void logMessage(String tag, String message){
            XmlCoderLogger logger = getLogger();
            if(logger != null){
                logger.logMessage(tag, message);
            }
        }
        private void logVerbose(String tag, String message){
            XmlCoderLogger logger = getLogger();
            if(logger != null){
                logger.logMessage(tag, message);
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
            for(ResValueMap valueMap : bagItems){
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType != null){
                    continue;
                }
                assert bagType != null;
                startTag(serializer, bagType.getName());
                serializer.attribute(null,ATTR_name, valueMap.decodeName());
                serializer.text(valueMap.decodeValue());
                endTag(serializer, bagType.getName());
                childCount ++;
            }
            return childCount;
        }
        public int decodePlural(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildren()){
                if(valueMap == null){
                    continue;
                }
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType == null){
                    continue;
                }
                startTag(serializer, TAG_item);
                serializer.attribute(null, ATTR_quantity, attributeType.getName());
                valueMap.serializeText(serializer);
                endTag(serializer, TAG_item);
                childCount++;
            }
            return childCount;
        }
        public int decodeStyle(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildren()){
                if(valueMap == null){
                    continue;
                }
                startTag(serializer, TAG_item);
                serializer.attribute(null, ATTR_name,
                        valueMap.decodeName(true));
                valueMap.serializeText(serializer);
                endTag(serializer, TAG_item);
                childCount ++;
            }
            return childCount;
        }
        public int decodeArray(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            ResValueMapArray mapArray = mapEntry.getValue();
            int childCount = 0;
            for(ResValueMap valueMap : mapArray.getChildren()){
                if(valueMap == null){
                    continue;
                }
                startTag(serializer, TAG_item);
                valueMap.serializeText(serializer);
                endTag(serializer, TAG_item);
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
            XMLAttribute attribute = child.getAttribute(ATTR_name);

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
                return;
            }
            valueMap.setValueAsString(StyleDocument.copyInner(child));
        }
        private void startTag(XmlSerializer serializer, String name) throws IOException {
            XmlDecodeUtil.bagIndent(serializer);
            serializer.startTag(null, name);
        }
        private void endTag(XmlSerializer serializer, String name) throws IOException {
            serializer.endTag(null, name);
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
            AttributeType attributeType = AttributeType.fromName(
                    child.getAttributeValue(ATTR_quantity));
            if(attributeType == null){
                throw new XmlEncodeException("Failed to get attribute '"
                        + ATTR_quantity + "'" + child.getDebugText());
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
                return;
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
            for(ResValueMap valueMap : mapArray.getChildren()){
                if(valueMap == null){
                    continue;
                }
                AttributeType attributeType = valueMap.getAttributeType();
                if(attributeType == null){
                    continue;
                }
                boolean ignore_empty = mapArray.getChildrenCount() > 1;
                valueMap.serializeAttribute(serializer,
                        attributeType.getName(), ignore_empty);
            }
        }
        public void decodeParent(XmlSerializer serializer, Entry entry) throws IOException {
            ResTableMapEntry mapEntry = entry.getResTableMapEntry();
            String parent = mapEntry.decodeParentId();
            if(parent != null){
                serializer.attribute(null, ATTR_parent, parent);
                return;
            }
            if(mapEntry.isStyle()){
                if(mapEntry.childrenCount() == 0){
                    serializer.attribute(null, ATTR_parent, "");
                }
            }
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
            if(name.equals(ATTR_name)){
                encodeName(value, entry);
            }else if(name.equals(ATTR_parent)){
                encodeParent(value, entry);
            }else if(name.equals(ATTR_formats)){
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
            if(!TAG_item.equals(tag)){
                return null;
            }
            if(child.getAttribute(ATTR_name) != null){
                return STYLE;
            }
            int count = child.getAttributeCount();
            if(count == 0 || (count == 1 && child.getAttribute(ATTR_type) != null)){
                return ARRAY;
            }

            AttributeType attributeType = AttributeType.fromName(
                    child.getAttributeValue(ATTR_quantity));
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

    static final String ATTR_name = "name";
    static final String ATTR_formats = "formats";
    static final String ATTR_parent = "parent";
    static final String ATTR_quantity = "quantity";
    static final String ATTR_type = "type";

    static final String TAG_item = "item";
}
