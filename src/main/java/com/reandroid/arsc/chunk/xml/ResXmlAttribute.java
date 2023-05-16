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

import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.AttributeValue;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueItem;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.EntryStore;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLAttribute;
import com.reandroid.xml.XMLException;

import java.io.IOException;
import java.util.Objects;

public class ResXmlAttribute extends ValueItem implements AttributeValue, Comparable<ResXmlAttribute>{
    private ReferenceItem mNSReference;
    private ReferenceItem mNameReference;
    private ReferenceItem mNameIdReference;
    private ReferenceItem mValueStringReference;
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

    public String getUri(){
        return getString(getNamespaceReference());
    }
    public String getFullName(){
        String name=getName();
        if(name==null){
            return null;
        }
        String prefix=getNamePrefix();
        if(prefix==null){
            return name;
        }
        return prefix+":"+name;
    }
    public String getName(){
        return getString(getNameReference());
    }
    public String getNamePrefix(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement==null){
            return null;
        }
        ResXmlStartNamespace startNamespace=xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
        if(startNamespace==null){
            return null;
        }
        return startNamespace.getPrefix();
    }
    // WARN! Careful this is not real value
    public String getValueString(){
        return getString(getValueStringReference());
    }
    @Override
    public int getNameResourceID(){
        ResXmlID xmlID = getResXmlID();
        if(xmlID != null){
            return xmlID.get();
        }
        return 0;
    }
    @Override
    public void setNameResourceID(int resourceId){
        ResXmlIDMap xmlIDMap=getResXmlIDMap();
        if(xmlIDMap==null){
            return;
        }
        ResXmlID xmlID = xmlIDMap.getOrCreate(resourceId);
        setNameReference(xmlID.getIndex());
    }
    @Override
    public Entry resolveName(){
        return resolve(getNameResourceID());
    }

    public void setName(String name, int resourceId){
        if(Objects.equals(name, getName()) && resourceId==getNameResourceID()){
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
    }
    private void linkStartNameSpace(){
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement==null){
            return;
        }
        ResXmlStartNamespace startNamespace=xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
        if(startNamespace==null){
            return;
        }
        startNamespace.addAttributeReference(this);
    }
    private void unLinkStartNameSpace(){
        ResXmlElement xmlElement = getParentResXmlElement();
        if(xmlElement == null){
            return;
        }
        ResXmlStartNamespace startNamespace =
                xmlElement.getStartNamespaceByUriRef(getNamespaceReference());
        if(startNamespace == null){
            return;
        }
        startNamespace.removeAttributeReference(this);
    }
    private ResXmlString getOrCreateAttributeName(String name, int resourceId){
        ResXmlStringPool stringPool = getStringPool();
        if(stringPool==null){
            return null;
        }
        return stringPool.getOrCreateAttribute(resourceId, name);
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
        if(ref<0){
            return null;
        }
        StringPool<?> stringPool = getStringPool();
        if(stringPool == null){
            return null;
        }
        StringItem stringItem = stringPool.get(ref);
        if(stringItem == null){
            return null;
        }
        return stringItem.getHtml();
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
    public void setNamespace(String uri, String prefix){
        if(uri == null || prefix == null){
            setNamespaceReference(-1);
            return;
        }
        ResXmlElement parentElement = getParentResXmlElement();
        if(parentElement == null){
            return;
        }
        ResXmlStartNamespace ns = parentElement.getOrCreateNamespace(uri, prefix);
        setNamespaceReference(ns.getUriReference());
    }
    public void setNamespaceReference(int ref){
        if(ref == getNamespaceReference()){
            return;
        }
        unlink(mNSReference);
        putInteger(getBytesInternal(), OFFSET_NS, ref);
        mNSReference = link(OFFSET_NS);
        linkStartNameSpace();
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
        int id1=getNameResourceID();
        int id2=other.getNameResourceID();
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
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject= new JSONObject();
        jsonObject.put(NAME_name, getName());
        jsonObject.put(NAME_id, getNameResourceID());
        jsonObject.put(NAME_namespace_uri, getUri());
        ValueType valueType=getValueType();
        jsonObject.put(NAME_value_type, valueType.name());
        if(valueType==ValueType.STRING){
            jsonObject.put(NAME_data, getValueAsString());
        }else if(valueType==ValueType.INT_BOOLEAN){
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
            ResXmlStartNamespace ns = getParentResXmlElement().getStartNamespaceByUri(uri);
            if(ns==null){
                ns = getParentResXmlElement().getRootResXmlElement()
                        .getOrCreateNamespace(uri, "");
            }
            setNamespaceReference(ns.getUriReference());
        }
        ValueType valueType=ValueType.fromName(json.getString(NAME_value_type));
        if(valueType==ValueType.STRING){
            setValueAsString(json.optString(NAME_data, ""));
        }else if(valueType==ValueType.INT_BOOLEAN){
            setValueAsBoolean(json.getBoolean(NAME_data));
        }else {
            setValueType(valueType);
            setData(json.getInt(NAME_data));
        }
    }
    public XMLAttribute decodeToXml(EntryStore entryStore, int currentPackageId) throws XMLException {
        int resourceId=getNameResourceID();
        String name;
        if(resourceId==0){
            name=getName();
        }else {
            EntryGroup group = entryStore.getEntryGroup(resourceId);
            if(group==null){
                //Lets ignore such error until XML encoder implemented
                //throw new XMLException("Failed to decode attribute name: "
                //HexUtil.toHex8("@0x", resourceId));
                name = HexUtil.toHex8("@0x", resourceId);
            }else {
                name = group.getSpecName();
            }
        }
        String prefix = getNamePrefix();
        if(prefix!=null){
            name=prefix+":"+name;
        }
        ValueType valueType = getValueType();
        int raw = getData();
        String value = ValueDecoder.decode(entryStore, currentPackageId, (AttributeValue) this);
        XMLAttribute attribute = new XMLAttribute(name, value);
        attribute.setNameId(resourceId);
        if(valueType==ValueType.REFERENCE||valueType==ValueType.ATTRIBUTE){
            attribute.setValueId(raw);
        }
        return attribute;
    }
    @Override
    public String toString(){
        String fullName = getFullName();
        if(fullName!=null ){
            int id=getNameResourceID();
            if(id!=0){
                fullName=fullName+"(@"+ HexUtil.toHex8(id)+")";
            }
            String valStr;
            ValueType valueType=getValueType();
            if(valueType==ValueType.STRING){
                valStr=getValueAsString();
            }else if (valueType==ValueType.INT_BOOLEAN){
                valStr = String.valueOf(getValueAsBoolean());
            }else if (valueType==ValueType.INT_DEC){
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
