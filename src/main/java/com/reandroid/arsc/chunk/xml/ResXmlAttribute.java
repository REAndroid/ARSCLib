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

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.coder.*;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.common.Namespace;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.AttributeValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Objects;

public class ResXmlAttribute extends AttributeValue implements Comparable<ResXmlAttribute>{
    private ReferenceItem mNSReference;
    private ReferenceItem mNameReference;
    private ReferenceItem mNameIdReference;
    private ReferenceItem mValueStringReference;
    private ResXmlStartNamespace mLinkedNamespace;

    public ResXmlAttribute(int attributeUnitSize) {
        super(attributeUnitSize, OFFSET_SIZE);
        byte[] bts = getBytesInternal();
        putInteger(bts, OFFSET_NS, -1);
        putInteger(bts, OFFSET_NAME, -1);
        putInteger(bts, OFFSET_STRING, -1);
    }
    public ResXmlAttribute() {
        this(20);
    }

    public boolean autoSetNamespace(){
        if(getNameId() == 0){
            return setNamespace(null, null);
        }
        ResourceEntry nameEntry = resolveName();
        if(nameEntry == null){
            return false;
        }
        PackageBlock packageBlock = nameEntry.getPackageBlock();
        return setNamespace(packageBlock.getUri(), packageBlock.getPrefix());
    }
    public boolean autoSetName(){
        int resourceId = getNameId();
        if(getNameId() == 0){
            return setNamespace(null, null);
        }
        ResourceEntry nameEntry = resolveName();
        if(nameEntry == null || nameEntry.isEmpty()){
            return false;
        }
        String name = nameEntry.getName();
        if(name == null){
            return false;
        }
        PackageBlock packageBlock = nameEntry.getPackageBlock();
        boolean nsChanged = setNamespace(packageBlock.getUri(), packageBlock.getPrefix());
        String nameOld = getName();
        setName(name, resourceId);
        return nsChanged | Objects.equals(nameOld, name);
    }
    @Override
    public boolean isUndefined(){
        return getNameReference() < 0;
    }
    public String getUri(){
        return getString(getNamespaceReference());
    }
    /**
     * Use getName(true)
     * */
    @Deprecated
    public String getFullName(){
        return getName(true);
    }
    public boolean equalsName(String name){
        if(name == null){
            return getName() == null;
        }
        String prefix = XMLUtil.splitPrefix(name);
        if(prefix != null && !prefix.equals(getNamePrefix())){
            return false;
        }
        return name.equals(getName());
    }
    public String getName(boolean includePrefix){
        String name = getName();
        if(name == null || !includePrefix){
            return name;
        }
        String prefix = getNamePrefix();
        if(prefix == null){
            return name;
        }
        return prefix + ":" + name;
    }
    public String getName(){
        return getString(getNameReference());
    }
    @Override
    public String decodePrefix(){
        int resourceId = getNameId();
        if(resourceId == 0){
            return null;
        }
        return getNamePrefix();
    }
    @Override
    public String decodeName(boolean includePrefix){
        int resourceId = getNameId();
        if(resourceId == 0){
            return getName();
        }
        ResourceEntry resourceEntry = resolveName();
        if(resourceEntry == null || !resourceEntry.isDeclared()){
            String name = ValueCoder.decodeUnknownNameId(resourceId);
            if(includePrefix){
                String prefix = getNamePrefix();
                if(prefix != null){
                    name = prefix + ":" + name;
                }
            }
            return name;
        }
        String name = resourceEntry.getName();
        if(includePrefix && name != null){
            String prefix = getNamePrefix();
            if(prefix != null){
                name = prefix + ":" + name;
            }
        }
        return name;
    }
    public String getNamePrefix(){
        ResXmlStartNamespace namespace = getStartNamespace();
        if(namespace != null){
            return namespace.getPrefix();
        }
        return null;
    }
    public ResXmlNamespace getNamespace(){
        return getStartNamespace();
    }
    public void setNamespace(Namespace namespace){
        if(namespace != null){
            setNamespace(namespace.getUri(), namespace.getPrefix());
        }else {
            setNamespace(null, null);
        }
    }
    public String getValueString(){
        return getString(getValueStringReference());
    }
    @Override
    public int getNameId(){
        ResXmlID xmlID = getResXmlID();
        if(xmlID != null){
            return xmlID.get();
        }
        return 0;
    }
    @Override
    public void setNameId(int resourceId){
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        if(xmlIDMap==null){
            return;
        }
        ResXmlID xmlID = xmlIDMap.getOrCreate(resourceId);
        setNameReference(xmlID.getIndex());
    }
    public void setName(String name, int resourceId){
        if(Objects.equals(name, getName()) && resourceId== getNameId()){
            return;
        }
        unlink(mNameReference);
        unLinkNameId(getResXmlID());
        ResXmlString xmlString = getOrCreateAttributeName(name, resourceId);
        if(xmlString==null){
            return;
        }
        setNameReference(xmlString.getIndex());
        mNameReference = link(OFFSET_NAME);
        linkNameId();
        return;
    }
    private void linkStartNameSpace(){
        unLinkStartNameSpace();
        ResXmlStartNamespace startNamespace = getStartNamespace();
        if(startNamespace == null){
            return;
        }
        mLinkedNamespace = startNamespace;
        startNamespace.addAttributeReference(this);
    }
    private void unLinkStartNameSpace(){
        ResXmlStartNamespace namespace = mLinkedNamespace;
        if(namespace == null){
            return;
        }
        this.mLinkedNamespace = null;
        namespace.removeAttributeReference(this);
    }
    private ResXmlStartNamespace getStartNamespace(){
        int uriRef = getNamespaceReference();
        if(uriRef < 0){
            return null;
        }
        ResXmlStartNamespace namespace = mLinkedNamespace;
        if(namespace != null && namespace.getUriReference() == uriRef){
            return namespace;
        }
        ResXmlElement parentElement = getParentResXmlElement();
        if(parentElement != null){
            return parentElement.getStartNamespaceByUriRef(uriRef);
        }
        return null;
    }
    private ResXmlString getOrCreateAttributeName(String name, int resourceId){
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool==null){
            return null;
        }
        return stringPool.getOrCreateAttribute(resourceId, name);
    }
    public boolean removeSelf(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent != null){
            return parent.removeAttribute(this);
        }
        return false;
    }
    public ResXmlElement getParentResXmlElement(){
        return getParent(ResXmlElement.class);
    }
    public int getAttributesUnitSize(){
        return OFFSET_SIZE + super.getSize();
    }
    public void setAttributesUnitSize(int size){
        int eight = size - OFFSET_SIZE;
        super.setSize(eight);
    }
    private String getString(int ref){
        StringPool<?> stringPool = getStringPool();
        if(stringPool == null){
            return null;
        }
        StringItem stringItem = getStringItem(ref);
        if(stringItem == null){
            return null;
        }
        return stringItem.getHtml();
    }
    private StringItem getStringItem(int ref){
        if(ref<0){
            return null;
        }
        StringPool<?> stringPool = getStringPool();
        if(stringPool == null){
            return null;
        }
        return stringPool.get(ref);
    }
    private ResXmlID getResXmlID(){
        ResXmlIDMap xmlIDMap = getResXmlIDMap();
        if(xmlIDMap == null){
            return null;
        }
        return xmlIDMap.getResXmlIDArray().get(getNameReference());
    }
    private ResXmlIDMap getResXmlIDMap(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getResXmlIDMap();
        }
        return null;
    }

    int getNamespaceReference(){
        return getInteger(getBytesInternal(), OFFSET_NS);
    }
    public boolean setNamespace(String uri, String prefix){
        uri = StringsUtil.emptyToNull(uri);
        prefix = StringsUtil.emptyToNull(prefix);
        if(uri == null && prefix == null){
            return setNamespaceReference(-1);
        }
        ResXmlElement parentElement = getParentResXmlElement();
        if(parentElement == null){
            return false;
        }
        ResXmlNamespace namespace;
        if(uri != null && prefix != null){
            namespace = parentElement.getOrCreateNamespace(uri, prefix);
        }else if(uri != null){
            namespace = parentElement.getStartNamespaceByUri(uri);
        }else{
            namespace = parentElement.getNamespaceByPrefix(prefix);
        }
        if(namespace == null){
            // TODO: should throw ?
            return false;
        }
        return setNamespaceReference(namespace.getUriReference());
    }
    public boolean setNamespaceReference(int ref){
        if(ref == getNamespaceReference()){
            return false;
        }
        setUriReference(ref);
        linkStartNameSpace();
        return true;
    }
    void setUriReference(int ref){
        StringItem stringItem = getStringItem(getNamespaceReference());
        putInteger(getBytesInternal(), OFFSET_NS, ref);
        if(stringItem != null){
            stringItem.removeReference(mNSReference);
        }
        mNSReference = link(OFFSET_NS);
    }
    int getNameReference(){
        return getInteger(getBytesInternal(), OFFSET_NAME);
    }
    void setNameReference(int ref){
        if(ref == getNameReference()){
            return;
        }
        unLinkNameId(getResXmlID());
        unlink(mNameReference);
        putInteger(getBytesInternal(), OFFSET_NAME, ref);
        mNameReference = link(OFFSET_NAME);
        linkNameId();
    }
    int getValueStringReference(){
        return getInteger(getBytesInternal(), OFFSET_STRING);
    }
    void setValueStringReference(int ref){
        if(ref == getValueStringReference() && mValueStringReference!=null){
            return;
        }
        StringPool<?> stringPool = getStringPool();
        if(stringPool == null){
            return;
        }
        StringItem stringItem = stringPool.get(ref);
        unlink(mValueStringReference);
        if(stringItem!=null){
            ref = stringItem.getIndex();
        }
        putInteger(getBytesInternal(), OFFSET_STRING, ref);
        ReferenceItem referenceItem = null;
        if(stringItem!=null){
            referenceItem = new ReferenceBlock<>(this, OFFSET_STRING);
            stringItem.addReference(referenceItem);
        }
        mValueStringReference = referenceItem;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        super.onDataLoaded();
        linkAll();
        linkStartNameSpace();
    }
    @Override
    public void onRemoved(){
        super.onRemoved();
        unLinkStartNameSpace();
        unlinkAll();
    }
    @Override
    protected void onUnlinkDataString(ReferenceItem referenceItem){
        unlink(referenceItem);
    }
    @Override
    protected void onDataChanged(){
        if(getValueType()==ValueType.STRING){
            setValueStringReference(getData());
        }else {
            setValueStringReference(-1);
        }
    }
    @Override
    public ResXmlDocument getParentChunk() {
        ResXmlElement element = getParentResXmlElement();
        if(element!=null){
            return element.getParentDocument();
        }
        return null;
    }

    private void linkNameId(){
        ResXmlID xmlID = getResXmlID();
        if(xmlID==null){
            return;
        }
        unLinkNameId(xmlID);
        ReferenceItem referenceItem = new ReferenceBlock<>(this, OFFSET_NAME);
        xmlID.addReference(referenceItem);
        mNameIdReference = referenceItem;
    }
    private void unLinkNameId(ResXmlID xmlID){
        ReferenceItem referenceItem = mNameIdReference;
        if(referenceItem==null || xmlID == null){
            return;
        }
        xmlID.removeReference(referenceItem);
        mNameIdReference = null;
        if(xmlID.hasReference()){
            return;
        }
        ResXmlIDMap xmlIDMap = getResXmlIDMap();
        if(xmlIDMap == null){
            return;
        }
        xmlIDMap.removeSafely(xmlID);
    }
    private void linkAll(){
        unlink(mNSReference);
        mNSReference = link(OFFSET_NS);
        unlink(mNameReference);
        mNameReference = link(OFFSET_NAME);
        unlink(mValueStringReference);
        mValueStringReference = link(OFFSET_STRING);

        linkNameId();
    }
    private void unlinkAll(){
        unlink(mNSReference);
        unlink(mNameReference);
        unlink(mValueStringReference);
        mNSReference = null;
        mNameReference = null;
        mValueStringReference = null;

        unLinkNameId(getResXmlID());
    }
    private ReferenceItem link(int offset){
        if(offset<0){
            return null;
        }
        StringPool<?> stringPool = getStringPool();
        if(stringPool == null){
            return null;
        }
        int ref = getInteger(getBytesInternal(), offset);
        StringItem stringItem = stringPool.get(ref);
        if(stringItem == null){
            return null;
        }
        ReferenceItem referenceItem = new ReferenceBlock<>(this, offset);
        stringItem.addReference(referenceItem);
        return referenceItem;
    }
    private void unlink(ReferenceItem reference){
        if(reference == null){
            return;
        }
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool==null){
            return;
        }
        stringPool.removeReference(reference);
    }
    @Override
    public ResXmlStringPool getStringPool(){
        StringPool<?> stringPool = super.getStringPool();
        if(stringPool instanceof ResXmlStringPool){
            return (ResXmlStringPool) stringPool;
        }
        return null;
    }
    @Override
    public int compareTo(ResXmlAttribute other) {
        int id1= getNameId();
        int id2=other.getNameId();
        if(id1==0 && id2!=0){
            return 1;
        }
        if(id2==0 && id1!=0){
            return -1;
        }
        if(id1!=0){
            return Integer.compare(id1, id2);
        }
        String name1=getName();
        if(name1==null){
            name1="";
        }
        String name2=other.getName();
        if(name2==null){
            name2="";
        }
        return name1.compareTo(name2);
    }
    public void serialize(XmlSerializer serializer) throws IOException {
        String value;
        if(getValueType() == ValueType.STRING){
            value = XmlSanitizer.escapeSpecialCharacter(getValueAsString());
            if(getNameId() == 0 || resolveName() == null){
                value = XmlSanitizer.escapeDecodedValue(value);
            }
        }else {
            value = decodeValue();
        }
        serializer.attribute(getUri(), decodeName(false), value);
    }
    public ResourceEntry encodeAttributeName(String uri, String prefix, String name) throws IOException {
        setNamespace(uri, prefix);
        ResourceEntry resourceEntry = super.encodeAttrName(prefix, name);
        if(resourceEntry != null){
            return resourceEntry;
        }
        prefix = StringsUtil.emptyToNull(prefix);
        if(prefix == null){
            setName(name, 0);
            return null;
        }
        throw new IOException("Unknown attribute name '" + prefix + ":" + name + "'");
    }
    public void encode(boolean validate, String uri, String prefix, String name, String value) throws IOException {
        ResourceEntry nameResource = encodeAttributeName(uri, prefix, name);
        EncodeResult encodeResult = super.encodeStyleValue(validate, nameResource, value);
        if(encodeResult.isError()){
            throw new IOException(buildErrorMessage(encodeResult.getError(), value));
        }
    }
    public void encode(String uri, String prefix, String name, String value, boolean validate) throws IOException {
        ResourceEntry attrResource = encodeAttributeName(uri, prefix, name);
        EncodeResult encodeResult = ValueCoder
                .encodeReference(getPackageBlock(), value);
        if(encodeResult != null){
            if(encodeResult.isError()){
                throw new IOException(buildErrorMessage(
                        encodeResult.getError(), value));
            }
            setValue(encodeResult);
            return;
        }
        if(attrResource != null){
            attrResource = attrResource.resolveReference();
        }
        if(attrResource == null || attrResource.isEmpty()){
            encodeResult = ValueCoder.encode(value);
            if(encodeResult != null){
                setValue(encodeResult);
            }else {
                setValueAsString(XmlSanitizer.unEscapeSpecialCharacter(value));
            }
            return;
        }
        AttributeBag attributeBag = AttributeBag.create(attrResource.get());
        encodeResult = attributeBag.encode(value);
        if(encodeResult != null){
            if(encodeResult.valueType == ValueType.STRING){
                setValueAsString(XmlSanitizer.unEscapeSpecialCharacter(value));
                return;
            }
            if(encodeResult.isError()){
                if(validate){
                    throw new IOException(buildErrorMessage(
                            encodeResult.getError(), value));
                }
                logEncodeError(encodeResult, value);
            }else {
                setValue(encodeResult);
                return;
            }
        }
        encodeResult = ValueCoder.encode(value);
        if(encodeResult != null){
            setValue(encodeResult);
            return;
        }
        setValueAsString(XmlSanitizer.unEscapeSpecialCharacter(value));
    }
    private void logEncodeError(EncodeResult error, String value){
        System.out.println(buildErrorMessage(error.getError(), value));
    }
    private String buildErrorMessage(String msg, String value){
        ResXmlElement parent = getParentResXmlElement();
        return msg + ", at line = " + parent.getStartLineNumber() +", <"+ parent.getName(true) + " "
                + getName(true) + "=\"" + value + "\"";
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put(NAME_name, getName());
        jsonObject.put(NAME_id, getNameId());
        jsonObject.put(NAME_namespace_uri, getUri());
        ValueType valueType=getValueType();
        jsonObject.put(NAME_value_type, valueType.name());
        if(valueType==ValueType.STRING){
            jsonObject.put(NAME_data, getValueAsString());
        }else if(valueType==ValueType.BOOLEAN){
            jsonObject.put(NAME_data, getValueAsBoolean());
        }else {
            jsonObject.put(NAME_data, getData());
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        String name = json.optString(NAME_name, "");
        int id =  json.optInt(NAME_id, 0);
        setName(name, id);
        String uri= json.optString(NAME_namespace_uri, null);
        if(uri!=null){
            ResXmlNamespace ns = getParentResXmlElement().getStartNamespaceByUri(uri);
            if(ns==null){
                ns = getParentResXmlElement().getRootElement()
                        .getOrCreateNamespace(uri, "");
            }
            setNamespaceReference(ns.getUriReference());
        }
        ValueType valueType=ValueType.fromName(json.getString(NAME_value_type));
        if(valueType==ValueType.STRING){
            setValueAsString(json.optString(NAME_data, ""));
        }else if(valueType==ValueType.BOOLEAN){
            setValueAsBoolean(json.getBoolean(NAME_data));
        }else {
            setValueType(valueType);
            setData(json.getInt(NAME_data));
        }
    }
    public XMLAttribute decodeToXml() {
        return new XMLAttribute(
                decodeName(true),
                decodeValue());
    }
    @Override
    public String toString(){
        String fullName = getName(true);
        if(fullName!=null ){
            int id= getNameId();
            if(id!=0){
                fullName=fullName+"(@"+ HexUtil.toHex8(id)+")";
            }
            String valStr;
            ValueType valueType=getValueType();
            if(valueType==ValueType.STRING){
                valStr=getValueAsString();
            }else if (valueType==ValueType.BOOLEAN){
                valStr = String.valueOf(getValueAsBoolean());
            }else if (valueType==ValueType.DEC){
                valStr = String.valueOf(getData());
            }else {
                valStr = "["+valueType+"] " + HexUtil.toHex8(getData());
            }
            if(valStr!=null){
                return fullName+"=\""+valStr+"\"";
            }
            return fullName+"["+valueType+"]=\""+ getData()+"\"";
        }
        StringBuilder builder= new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        builder.append(getIndex());
        builder.append("{NamespaceReference=").append(getNamespaceReference());
        builder.append(", NameReference=").append(getNameReference());
        builder.append(", ValueStringReference=").append(getValueStringReference());
        builder.append(", ValueSize=").append(getSize());
        builder.append(", ValueTypeByte=").append(getType() & 0xff);
        builder.append(", Data=").append(getData());
        builder.append("}");
        return builder.toString();
    }



    public static final String NAME_id = "id";
    public static final String NAME_name = "name";
    public static final String NAME_namespace_uri = "namespace_uri";

    private static final int OFFSET_NS = 0;
    private static final int OFFSET_NAME = 4;
    private static final int OFFSET_STRING = 8;

    private static final int OFFSET_SIZE = 12;
}
