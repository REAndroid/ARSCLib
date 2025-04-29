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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

public class ResXmlAttributeArray extends CountedBlockList<ResXmlAttribute>
        implements JSONConvert<JSONArray> {

    public ResXmlAttributeArray(IntegerReference unitSize, IntegerReference countReference) {
        super(new AttributesCreator(unitSize), countReference);
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean sort(Comparator<? super ResXmlAttribute> comparator) {
        if (super.sort(comparator)) {
            getStartElement().refreshAttributePositions();
            return true;
        }
        return false;
    }

    private void computePositionalAttributes() {
        ResXmlStartElement startElement = getStartElement();

        startElement.getIdAttributePosition().computePosition();
        startElement.getClassAttributePosition().computePosition();
        startElement.getStyleAttributePosition().computePosition();
    }
    private ResXmlAttributePosition getPosition(int positionType) {
        if (positionType != -1) {
            ResXmlStartElement startElement = getStartElement();
            if (positionType == ResXmlAttributePosition.TYPE_ID) {
                return startElement.getIdAttributePosition();
            }
            if (positionType == ResXmlAttributePosition.TYPE_CLASS) {
                return startElement.getClassAttributePosition();
            }
            if (positionType == ResXmlAttributePosition.TYPE_STYLE) {
                return startElement.getStyleAttributePosition();
            }
        }
        return ObjectsUtil.getNull();
    }
    private ResXmlAttributePosition getPosition(ResXmlAttribute attribute) {
        ResXmlStartElement startElement = getStartElement();
        ResXmlAttributePosition position = startElement.getIdAttributePosition();
        if (position.getAttribute() == attribute) {
            return position;
        }
        position = startElement.getClassAttributePosition();
        if (position.getAttribute() == attribute) {
            return position;
        }
        position = startElement.getStyleAttributePosition();
        if (position.getAttribute() == attribute) {
            return position;
        }
        return null;
    }
    private void linkPositionalAttribute(ResXmlAttribute attribute) {
        ResXmlAttributePosition position = getPosition(ResXmlAttributePosition.getPositionType(attribute));
        if (position != null) {
            position.setAttribute(attribute);
        }
    }

    public void sort() {
        sort(CompareUtil.getComparableComparator());
    }
    private ResXmlStartElement getStartElement() {
        return getParentInstance(ResXmlStartElement.class);
    }
    private ResXmlElement element() {
        return getParentInstance(ResXmlElement.class);
    }

    @Override
    public ResXmlAttribute createNext() {
        ResXmlAttribute attribute =  super.createNext();
        updateCountReference();
        return attribute;
    }

    @Override
    public void setSize(int size) {
        if (size != size()) {
            getCountReference().set(size);
            super.setSize(size);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int size = getCountReference().get();
        setSize(size);
        if (size != 0) {
            int unitSize = getStartElement().getAttributeUnitSize().get();
            for (int i = 0; i < size; i++) {
                ResXmlAttribute attribute = get(i);
                attribute.setAttributesUnitSize(unitSize);
                int position = reader.getPosition();
                attribute.readBytes(reader);
                reader.seek(position + unitSize);
            }
        }
    }
    public void clear() {
        clearChildes();
        updateCountReference();
    }

    public ResXmlAttribute getOrCreateAndroidAttribute(String name, int resourceId){
        return getOrCreateAttribute(
                Namespace.URI_ANDROID,
                Namespace.PREFIX_ANDROID,
                name,
                resourceId);
    }
    public ResXmlAttribute getOrCreateAttribute(String uri, String prefix, String name, int resourceId) {
        ResXmlAttribute attribute = searchAttribute(name, resourceId);
        if (attribute == null) {
            attribute = createNext();
            attribute.setName(name, resourceId);
            attribute.setNamespace(uri, prefix);
            linkPositionalAttribute(attribute);
        }
        return attribute;
    }
    public ResXmlAttribute getOrCreateAttribute(String name, int resourceId) {
        ResXmlAttribute attribute = searchAttribute(name, resourceId);
        if (attribute == null) {
            attribute = createNext();
            attribute.setName(name, resourceId);
            linkPositionalAttribute(attribute);
        }
        return attribute;
    }
    private ResXmlAttribute searchAttribute(String name, int resourceId){
        if(resourceId == 0){
            return searchAttributeByName(name);
        }
        return searchAttributeByResourceId(resourceId);
    }
    public ResXmlAttribute searchAttributeByName(String name){
        if (name == null) {
            return null;
        }
        ResXmlAttribute withIdAttribute = null;
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlAttribute attribute = get(i);
            if (attribute.equalsName(name)) {
                if(attribute.getNameId() != 0){
                    withIdAttribute = attribute;
                    continue;
                }
                return attribute;
            }
        }
        return withIdAttribute;
    }
    public ResXmlAttribute searchAttributeByResourceId(int resourceId) {
        if (resourceId == 0) {
            return null;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlAttribute attribute = get(i);
            if(resourceId == attribute.getNameId()){
                return attribute;
            }
        }
        return null;
    }
    public ResXmlAttribute searchAttribute(String namespace, String name){
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlAttribute attribute = get(i);
            if (attribute.isEqual(namespace, name)) {
                return attribute;
            }
        }
        return null;
    }
    @Override
    public void onPreRemove(ResXmlAttribute attribute) {
        super.onPreRemove(attribute);
        ResXmlAttributePosition position = getPosition(attribute);
        if (position != null) {
            position.setAttribute(null);
        }
        attribute.onRemoved();
    }

    public void merge(ResXmlAttributeArray attributeArray) {
        Iterator<ResXmlAttribute> iterator = attributeArray.iterator();
        while (iterator.hasNext()) {
            ResXmlAttribute coming = iterator.next();
            ResXmlAttribute attribute = createNext();
            attribute.merge(coming);
            mergePositionalAttribute(attribute, attributeArray, coming);
        }
    }
    public void mergeWithName(ResourceMergeOption option, ResXmlAttributeArray attributeArray) {
        Iterator<ResXmlAttribute> iterator = attributeArray.iterator();
        while (iterator.hasNext()) {
            ResXmlAttribute coming = iterator.next();
            ResXmlAttribute attribute = createNext();
            attribute.mergeWithName(option, coming);
            mergePositionalAttribute(attribute, attributeArray, coming);
        }
    }
    private void mergePositionalAttribute(ResXmlAttribute attribute, ResXmlAttributeArray sourceArray, ResXmlAttribute source) {
        ResXmlAttributePosition sourcePosition = sourceArray.getPosition(source);
        if (sourcePosition != null) {
            this.getPosition(sourcePosition.type()).setAttribute(attribute);
        }
    }

    @Override
    public JSONArray toJson() {
        return toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json != null) {
            int start = size();
            int length = json.length();
            setSize(start + length);
            for (int i = 0; i < length; i++) {
                get(start + i).fromJson(json.getJSONObject(i));
            }
            computePositionalAttributes();
            sort();
        }
    }

    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        if (decode) {
            getStartElement().fixClassStyleAttributeNames();
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).serialize(serializer, decode);
        }
    }

    public void parse(XmlPullParser parser) throws IOException {
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            String prefix = XMLUtil.splitPrefix(name);
            name = XMLUtil.splitName(name);
            String value = parser.getAttributeValue(i);
            if (Namespace.isValidNamespace(value, prefix)) {
                continue;
            }
            if (prefix == null) {
                prefix = StringsUtil.emptyToNull(parser.getAttributePrefix(i));
            }
            String uri;
            if (prefix != null) {
                uri = parser.getAttributeNamespace(i);
                if (StringsUtil.isEmpty(uri)) {
                    ResXmlNamespace ns = element().getNamespaceForPrefix(prefix);
                    if(ns != null){
                        uri = ns.getUri();
                    }
                }
            } else {
                uri = null;
            }
            try {
                createNext().encode(false, uri, prefix, name, value);
            } catch (IOException e) {
                throw new IOException(XMLUtil.getSimplePositionDescription(parser) + "\n" + e.getMessage(), e);
            }
        }
        if (count != 0) {
            computePositionalAttributes();
            sort();
        }
    }
    public void toXml(XMLElement element, boolean decode) {
        if (decode) {
            getStartElement().fixClassStyleAttributeNames();
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            element.addAttribute(get(i).toXml(decode));
        }
    }

    static class AttributesCreator implements Creator<ResXmlAttribute> {

        private final IntegerReference unitSize;

        public AttributesCreator(IntegerReference unitSize) {
            this.unitSize = unitSize;
        }

        @Override
        public ResXmlAttribute newInstance() {
            ResXmlAttribute attribute = new ResXmlAttribute();
            attribute.setAttributesUnitSize(unitSize.get());
            return attribute;
        }
    }
}
