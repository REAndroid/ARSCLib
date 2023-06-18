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
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.attribute.AttributeBag;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class ResXmlPullSerializer implements XmlSerializer {
    private PackageBlock mCurrentPackage;
    private ResXmlDocument mDocument;
    private ResXmlElement mCurrentElement;
    private boolean mEndDocument;
    public ResXmlPullSerializer(){

    }
    public PackageBlock getCurrentPackage(){
        return mCurrentPackage;
    }
    public void setCurrentPackage(PackageBlock packageBlock){
        this.mCurrentPackage = packageBlock;
        if(mDocument != null){
            mDocument.setPackageBlock(packageBlock);
        }
    }
    public ResXmlDocument getDocument() {
        return mDocument;
    }

    public void setDocument(ResXmlDocument document) {
        this.mDocument = document;
        if(document == null){
            return;
        }
        PackageBlock packageBlock = document.getPackageBlock();
        if(packageBlock == null){
            document.setPackageBlock(getCurrentPackage());
        }else if(getCurrentPackage() == null){
            setCurrentPackage(packageBlock);
        }
    }

    private ResXmlDocument getCurrentDocument() {
        ResXmlDocument document = this.mDocument;
        if(mEndDocument){
            document = null;
            mCurrentElement = null;
            mEndDocument = false;
        }
        if(document == null){
            document = new ResXmlDocument();
            mCurrentElement = null;
            mDocument = document;
        }
        if(document.getPackageBlock() == null){
            document.setPackageBlock(getCurrentPackage());
        }
        return document;
    }

    private ResXmlElement getCurrentElement(){
        ResXmlElement element = mCurrentElement;
        if(element == null){
            ResXmlDocument document =  getCurrentDocument();
            element = document.getResXmlElement();
            if(element == null){
                element = document.createRootElement(null);
            }
            mCurrentElement = element;
        }
        return element;
    }
    @Override
    public void setFeature(String name, boolean state) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public boolean getFeature(String name) {
        return false;
    }

    @Override
    public void setProperty(String name, Object value) throws IllegalArgumentException, IllegalStateException {

    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setOutput(OutputStream os, String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IllegalArgumentException("Can not set OutputStream");
    }

    @Override
    public void setOutput(Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        throw new IllegalArgumentException("Can not set OutputStream");
    }
    @Override
    public void startDocument(String encoding, Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        if(mCurrentElement != null){
            mEndDocument = true;
        }
    }

    @Override
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        mEndDocument = true;
    }

    @Override
    public void setPrefix(String prefix, String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = getCurrentElement();
        if(element == null){
            // TODO: throw?
            return;
        }
        element.getOrCreateNamespace(namespace, prefix);
    }

    @Override
    public String getPrefix(String namespace, boolean generatePrefix) throws IllegalArgumentException {
        if(namespace == null){
            return null;
        }
        ResXmlElement element = mCurrentElement;
        if(element == null){
            // TODO: throw?
            return null;
        }
        ResXmlNamespace resXmlNamespace = element.getNamespaceByUri(namespace);
        if(resXmlNamespace == null && generatePrefix){
            String prefix;
            if(namespace.equals(ResourceLibrary.URI_ANDROID)){
                prefix = ResourceLibrary.PREFIX_ANDROID;
            }else {
                prefix = ResourceLibrary.PREFIX_APP;
            }
            resXmlNamespace = element.getOrCreateNamespace(namespace, prefix);
        }
        if(resXmlNamespace == null){
            return null;
        }
        return resXmlNamespace.getPrefix();
    }

    @Override
    public int getDepth() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getDepth();
        }
        return 0;
    }

    @Override
    public String getNamespace() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getTagUri();
        }
        return null;
    }
    @Override
    public String getName() {
        ResXmlElement element = mCurrentElement;
        if(element != null){
            return element.getTag();
        }
        return null;
    }

    @Override
    public XmlSerializer startTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = getCurrentElement();
        String prefix = null;
        int i = name.indexOf(':');
        if(i > 0){
            prefix = name.substring(0, i);
            name = name.substring(i + 1);
        }
        if(element.getTag() == null){
            element.setTag(name);
        }else {
            element = element.createChildElement(name);
            mCurrentElement = element;
        }
        element.setTagNamespace(namespace, prefix);
        mCurrentElement = element;
        return this;
    }

    @Override
    public XmlSerializer attribute(String namespace, String name, String value) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = mCurrentElement;
        String prefix = null;
        int i = name.indexOf(':');
        if(i > 0){
            prefix = name.substring(0, i);
            name = name.substring(i + 1);
        }
        if(prefix == null){
            ResXmlNamespace resXmlNamespace = element.getStartNamespaceByUri(namespace);
            if(resXmlNamespace != null){
                prefix = resXmlNamespace.getPrefix();
            }
        }
        ResXmlAttribute resXmlAttribute = element.newAttribute();
        encode(resXmlAttribute, namespace, prefix, name, value);
        return this;
    }

    @Override
    public XmlSerializer endTag(String namespace, String name) throws IOException, IllegalArgumentException, IllegalStateException {
        mCurrentElement.calculatePositions();
        mCurrentElement = mCurrentElement.getParentResXmlElement();
        return this;
    }

    @Override
    public XmlSerializer text(String text) throws IOException, IllegalArgumentException, IllegalStateException {
        ResXmlElement element = mCurrentElement;
        element.addResXmlText(text);
        return this;
    }

    @Override
    public XmlSerializer text(char[] buf, int start, int len) throws IOException, IllegalArgumentException, IllegalStateException {
        return text(new String(buf, start, len));
    }

    @Override
    public void cdsect(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void entityRef(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void processingInstruction(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void comment(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void docdecl(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void ignorableWhitespace(String text) throws IOException, IllegalArgumentException, IllegalStateException {

    }

    @Override
    public void flush() throws IOException {

    }

    private void encode(ResXmlAttribute attribute, String uri, String prefix, String name, String value){
        attribute.setNamespace(uri, prefix);
        Entry attrEntry = null;
        if(prefix != null){
            ResourceEntry resourceEntry = getAttributeName(prefix, name);
            if(resourceEntry == null){
                throw new ResourceEncodeException("Unknown attribute '" + prefix + ":" + name);
            }
            attribute.setName(resourceEntry.getName(), resourceEntry.getResourceId());
            attribute.setNamespace(uri, prefix);
            attrEntry = resourceEntry.get();
        }else {
            attribute.setName(name, 0);
        }
        EncodeResult encodeResult = encodeReference(value);
        if(encodeResult != null){
            attribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
            return;
        }
        if(attrEntry != null){
            AttributeBag attributeBag = AttributeBag.create(attrEntry.getResValueMapArray());
            if(attributeBag != null){
                encodeResult = attributeBag.encodeEnumOrFlagValue(value);
                if(encodeResult!=null){
                    attribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
                    return;
                }
            }
        }
        if(attrEntry != null){
            AttributeDataFormat[] formats = attrEntry.getResTableMapEntry().getAttributeTypeFormats();
            encodeResult = ValueCoder.encode(value, formats);
        }
        if(encodeResult != null){
            attribute.setTypeAndData(encodeResult.valueType, encodeResult.value);
            return;
        }
        attribute.setValueAsString(XmlSanitizer.unEscapeUnQuote(value));
    }
    private ResourceEntry getAttributeName(String prefix, String name){
        TableBlock tableBlock = getTableBlock();
        ResourceEntry resourceEntry = tableBlock.getResource(prefix, "attr", name);
        if(resourceEntry == null){
            resourceEntry = tableBlock.getResource(null, "attr", name);
        }
        return resourceEntry;
    }
    private EncodeResult encodeReference(String text){
        EncodeResult encodeResult = ValueCoder.encodeUnknownResourceId(text);
        if(encodeResult != null){
            return encodeResult;
        }
        ReferenceString referenceString = ReferenceString.parseReference(text);
        if(referenceString == null){
            return null;
        }
        encodeResult = referenceString.encode(getTableBlock());
        if(encodeResult == null){
            throw new ResourceEncodeException("Unknown reference: " + text);
        }
        return encodeResult;
    }
    private TableBlock getTableBlock(){
        PackageBlock packageBlock = getCurrentPackage();
        return packageBlock.getTableBlock();
    }

    public static class ResourceEncodeException extends IllegalArgumentException{
        public ResourceEncodeException(String msg){
            super(msg);
        }
    }
}
