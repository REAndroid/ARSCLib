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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.InfoHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.BytesOutputStream;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.*;

public class ResXmlDocument extends Chunk<HeaderBlock>
        implements MainChunk, ParentChunk, JSONConvert<JSONObject> {
    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private ResXmlElement mResXmlElement;
    private final SingleBlockContainer<ResXmlElement> mResXmlElementContainer;
    private ApkFile mApkFile;
    private PackageBlock mPackageBlock;
    private boolean mDestroyed;

    public ResXmlDocument() {
        super(new HeaderBlock(ChunkType.XML),3);
        this.mResXmlStringPool=new ResXmlStringPool(true);
        this.mResXmlIDMap=new ResXmlIDMap();
        this.mResXmlElement=new ResXmlElement();
        this.mResXmlElementContainer=new SingleBlockContainer<>();
        this.mResXmlElementContainer.setItem(mResXmlElement);
        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mResXmlElementContainer);
    }

    /**
     * Iterates every attribute on root element and on child elements recursively
     * */
    // Experimental
    public Iterator<ResXmlAttribute> recursiveAttributes(){
        ResXmlElement element = getResXmlElement();
        if(element != null){
            return element.recursiveAttributes();
        }
        return EmptyIterator.of();
    }
    /**
     * Iterates every element and child elements recursively
     * */
    // Experimental
    public Iterator<ResXmlElement> recursiveElements(){
        ResXmlElement element = getResXmlElement();
        if(element != null){
            return element.recursiveElements();
        }
        return EmptyIterator.of();
    }

    public int autoSetAttributeNamespaces(){
        ResXmlElement root = getResXmlElement();
        if(root == null){
            return 0;
        }
        int changedCount = root.autoSetAttributeNamespaces();
        if(changedCount > 0){
            removeUnusedNamespaces();
            getStringPool().removeUnusedStrings();
        }
        return changedCount;
    }
    public int autoSetAttributeNames(){
        ResXmlElement root = getResXmlElement();
        if(root == null){
            return 0;
        }
        int changedCount = root.autoSetAttributeNames();
        if(changedCount > 0){
            removeUnusedNamespaces();
            getStringPool().removeUnusedStrings();
        }
        return changedCount;
    }
    public void autoSetLineNumber(){
        ResXmlElement root = getResXmlElement();
        if(root == null){
            return;
        }
        root.autoSetLineNumber();
    }
    public int removeUnusedNamespaces(){
        ResXmlElement root = getResXmlElement();
        if(root != null){
            return root.removeUnusedNamespaces();
        }
        return 0;
    }
    public String refreshFull(){
        int sizeOld = getHeaderBlock().getChunkSize();
        StringBuilder message = new StringBuilder();
        boolean appendOnce = false;
        ResXmlElement root = getResXmlElement();
        int count;
        if(root != null){
            count = root.removeUndefinedAttributes();
            if(count != 0){
                message.append("Removed undefined attributes = ");
                message.append(count);
                appendOnce = true;
            }
        }
        count = removeUnusedNamespaces();
        if(count != 0){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Removed unused namespaces = ");
            message.append(count);
            appendOnce = true;
        }
        count = getStringPool().removeUnusedStrings().size();
        if(count != 0){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Removed xml strings = ");
            message.append(count);
            appendOnce = true;
        }
        refresh();
        int sizeNew = getHeaderBlock().getChunkSize();
        if(sizeOld != sizeNew){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Xml size changed = ");
            message.append(sizeOld);
            message.append(", ");
            message.append(sizeNew);
            appendOnce = true;
        }
        if(appendOnce){
            return message.toString();
        }
        return null;
    }
    public void destroy(){
        synchronized (this){
            if(mDestroyed){
                return;
            }
            mDestroyed = true;
            ResXmlElement root = getResXmlElement();
            if(root != null){
                root.clearChildren();
                setResXmlElement(null);
            }
            getResXmlIDMap().destroy();
            getStringPool().destroy();
            refresh();
        }
    }
    public void setAttributesUnitSize(int size, boolean setToAll){
        ResXmlElement root = getResXmlElement();
        if(root!=null){
            root.setAttributesUnitSize(size, setToAll);
        }
    }
    public ResXmlElement getOrCreateElement(String tag){
        ResXmlElement element = getResXmlElement();
        if(element == null){
            element = createRootElement(tag);
        }else if(tag != null){
            element.setName(tag);
        }
        return element;
    }
    public ResXmlElement createRootElement(String tag){
        int lineNo=1;
        ResXmlElement resXmlElement=new ResXmlElement();
        resXmlElement.newStartElement(lineNo);

        setResXmlElement(resXmlElement);

        if(tag != null){
            resXmlElement.setName(tag);
        }
        return resXmlElement;
    }
    void linkStringReferences(){
        ResXmlElement element=getResXmlElement();
        if(element!=null){
            element.linkStringReferences();
        }
    }
    /*
     * method Block.addBytes is inefficient for large size byte array
     * so let's override here because this block is the largest
     */
    @Override
    public byte[] getBytes(){
        BytesOutputStream outputStream = new BytesOutputStream(
                getHeaderBlock().getChunkSize());
        try {
            writeBytes(outputStream);
            outputStream.close();
        } catch (IOException ignored) {
        }
        return outputStream.toByteArray();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if(headerBlock == null){
            throw new IOException("Not bin xml: " + reader);
        }
        int chunkSize = headerBlock.getChunkSize();
        if(chunkSize < 0){
            throw new IOException("Negative chunk size: " + chunkSize);
        }
        if(chunkSize > reader.available()){
            throw new IOException("Higher chunk size: " + chunkSize
                    + ", available = " + reader.available());
        }
        if(chunkSize < headerBlock.getHeaderSize()){
            throw new IOException("Higher header size: " + headerBlock);
        }
        BlockReader chunkReader = reader.create(chunkSize);
        headerBlock = getHeaderBlock();
        headerBlock.readBytes(chunkReader);
        // android/aapt2 accepts 0x0000 (NULL) chunk type as XML, it could
        // be android's bug and might be fixed in the future until then lets fix it ourselves
        headerBlock.setType(ChunkType.XML);
        while (chunkReader.isAvailable()){
            boolean readOk=readNext(chunkReader);
            if(!readOk){
                break;
            }
        }
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    @Override
    public void onChunkLoaded(){
        super.onChunkLoaded();
        linkStringReferences();
    }
    private boolean readNext(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return false;
        }
        int position=reader.getPosition();
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.STRING){
            mResXmlStringPool.readBytes(reader);
        }else if(chunkType==ChunkType.XML_RESOURCE_MAP){
            mResXmlIDMap.readBytes(reader);
        }else if(isElementChunk(chunkType)){
            mResXmlElementContainer.readBytes(reader);
            return reader.isAvailable();
        }else {
            throw new IOException("Unexpected chunk "+headerBlock);
        }
        return reader.isAvailable() && position!=reader.getPosition();
    }
    private boolean isElementChunk(ChunkType chunkType){
        if(chunkType==ChunkType.XML_START_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_END_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_START_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_END_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_CDATA){
            return true;
        }
        if(chunkType==ChunkType.XML_LAST_CHUNK){
            return true;
        }
        return false;
    }
    @Override
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    @Override
    public ApkFile getApkFile(){
        return mApkFile;
    }
    @Override
    public void setApkFile(ApkFile apkFile){
        this.mApkFile = apkFile;
    }
    @Override
    public PackageBlock getPackageBlock(){
        ApkFile apkFile = this.mApkFile;
        PackageBlock packageBlock = this.mPackageBlock;
        if(apkFile == null || packageBlock != null){
            return packageBlock;
        }
        TableBlock tableBlock = apkFile.getLoadedTableBlock();
        if(tableBlock != null){
            packageBlock = selectPackageBlock(tableBlock);
            mPackageBlock = packageBlock;
        }
        return packageBlock;
    }
    public void setPackageBlock(PackageBlock packageBlock) {
        this.mPackageBlock = packageBlock;
    }
    PackageBlock selectPackageBlock(TableBlock tableBlock){
        return tableBlock.pickOne();
    }
    @Override
    public TableBlock getTableBlock(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null){
            TableBlock tableBlock = packageBlock.getTableBlock();
            if(tableBlock != null){
                return tableBlock;
            }
        }
        ApkFile apkFile = getApkFile();
        if(apkFile != null){
            return apkFile.getLoadedTableBlock();
        }
        return null;
    }
    @Override
    public StringPool<?> getSpecStringPool() {
        return null;
    }
    @Override
    public MainChunk getMainChunk(){
        return this;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getResXmlElement(){
        return mResXmlElement;
    }
    public void setResXmlElement(ResXmlElement resXmlElement){
        this.mResXmlElement=resXmlElement;
        this.mResXmlElementContainer.setItem(resXmlElement);
    }
    @Override
    protected void onPreRefreshRefresh(){
        ResXmlElement root = getResXmlElement();
        if(root != null){
            root.refresh();
        }
        super.onPreRefreshRefresh();
    }
    @Override
    protected void onChunkRefreshed() {

    }
    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        OutputStream outputStream=new FileOutputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if(mDestroyed){
            throw new IOException("Destroyed document");
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            throw new IOException("Can not decode without package");
        }
        setPackageBlock(packageBlock);
        int event = parser.getEventType();
        if(event == XmlPullParser.START_DOCUMENT){
            setResXmlElement(null);
            event = parser.next();
        }
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT){
            event = parser.next();
        }
        if(event == XmlPullParser.START_TAG){
            ResXmlElement element = getOrCreateElement(null);
            if(element != null){
                element.parse(parser);
            }
        }
        refreshFull();
    }
    public String serializeToXml() throws IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        serialize(serializer);
        writer.close();
        writer.flush();
        return writer.toString();
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        if(mDestroyed){
            throw new IOException("Destroyed document");
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            throw new IOException("Can not decode without package");
        }
        ResXmlElement.setIndent(serializer, true);
        setPackageBlock(packageBlock);
        serializer.startDocument("utf-8", null);
        ResXmlElement element = getResXmlElement();
        if(element != null){
            autoSetAttributeNamespaces();
            element.serialize(serializer);
        }
        serializer.endDocument();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(ResXmlDocument.NAME_element, getResXmlElement().toJson());
        JSONArray pool = getStringPool().toJson();
        if(pool!=null){
            jsonObject.put(ResXmlDocument.NAME_styled_strings, pool);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        onFromJson(json);
        ResXmlElement xmlElement=getResXmlElement();
        xmlElement.fromJson(json.optJSONObject(ResXmlDocument.NAME_element));
        refresh();
    }
    public XMLDocument decodeToXml() {
        XMLDocument xmlDocument = new XMLDocument();
        XMLElement xmlElement = getResXmlElement()
                .decodeToXml();
        xmlDocument.setDocumentElement(xmlElement);
        return xmlDocument;
    }
    private void onFromJson(JSONObject json){
        List<JSONObject> attributeList=recursiveAttributes(json.optJSONObject(ResXmlDocument.NAME_element));
        buildResourceIds(attributeList);
        Set<String> allStrings=recursiveStrings(json.optJSONObject(ResXmlDocument.NAME_element));
        ResXmlStringPool stringPool = getStringPool();
        stringPool.addStrings(allStrings);
        stringPool.refresh();
    }
    private void buildResourceIds(List<JSONObject> attributeList){
        ResIdBuilder builder=new ResIdBuilder();
        for(JSONObject attribute:attributeList){
            int id=attribute.getInt(ResXmlAttribute.NAME_id);
            if(id==0){
                continue;
            }
            String name=attribute.getString(ResXmlAttribute.NAME_name);
            builder.add(id, name);
        }
        builder.buildTo(getResXmlIDMap());
    }
    private List<JSONObject> recursiveAttributes(JSONObject elementJson){
        List<JSONObject> results = new ArrayList<>();
        if(elementJson==null){
            return results;
        }
        JSONArray attributes = elementJson.optJSONArray(ResXmlElement.NAME_attributes);
        if(attributes != null){
            int length = attributes.length();
            for(int i=0; i<length; i++){
                JSONObject attr=attributes.optJSONObject(i);
                if(attr!=null){
                    results.add(attr);
                }
            }
        }
        JSONArray childElements = elementJson.optJSONArray(ResXmlElement.NAME_children);
        if(childElements!=null){
            int length=childElements.length();
            for(int i=0;i<length;i++){
                JSONObject child=childElements.getJSONObject(i);
                results.addAll(recursiveAttributes(child));
            }
        }
        return results;
    }
    private Set<String> recursiveStrings(JSONObject elementJson){
        Set<String> results = new HashSet<>();
        if(elementJson==null){
            return results;
        }
        results.add(elementJson.optString(ResXmlElement.NAME_namespace_uri));
        results.add(elementJson.optString(ResXmlElement.NAME_name));
        JSONArray namespaces=elementJson.optJSONArray(ResXmlElement.NAME_namespaces);
        if(namespaces != null){
            int length = namespaces.length();
            for(int i=0; i<length; i++){
                JSONObject nsObject=namespaces.getJSONObject(i);
                results.add(nsObject.getString(ResXmlElement.NAME_namespace_uri));
                results.add(nsObject.getString(ResXmlElement.NAME_namespace_prefix));
            }
        }
        JSONArray attributes = elementJson.optJSONArray(ResXmlElement.NAME_attributes);
        if(attributes != null){
            int length = attributes.length();
            for(int i=0; i<length; i++){
                JSONObject attr=attributes.optJSONObject(i);
                if(attr==null){
                    continue;
                }
                results.add(attr.getString(ResXmlAttribute.NAME_name));
                ValueType valueType=ValueType.fromName(attr.getString(ResXmlAttribute.NAME_value_type));
                if(valueType==ValueType.STRING){
                    results.add(attr.optString(ResXmlAttribute.NAME_data));
                }
            }
        }
        JSONArray childElements = elementJson.optJSONArray(ResXmlElement.NAME_children);
        if(childElements!=null){
            int length=childElements.length();
            for(int i=0;i<length;i++){
                JSONObject child=childElements.getJSONObject(i);
                results.addAll(recursiveStrings(child));
            }
        }
        return results;
    }
    void addEvents(ParserEventList parserEventList){
        ResXmlElement xmlElement = getResXmlElement();
        parserEventList.add(new ParserEvent(ParserEvent.START_DOCUMENT, xmlElement));
        if(xmlElement!=null){
            xmlElement.addEvents(parserEventList);
        }
        parserEventList.add(new ParserEvent(ParserEvent.END_DOCUMENT, xmlElement));
    }

    public static boolean isResXmlBlock(File file){
        if(file==null){
            return false;
        }
        try {
            InfoHeader infoHeader = InfoHeader.readHeaderBlock(file);
            return isResXmlBlock(infoHeader);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(InputStream inputStream){
        try {
            HeaderBlock headerBlock=BlockReader.readHeaderBlock(inputStream);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(byte[] bytes){
        try {
            HeaderBlock headerBlock = BlockReader.readHeaderBlock(bytes);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.XML;
    }
    private static final String NAME_element ="element";
    private static final String NAME_styled_strings="styled_strings";
}
