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
import com.reandroid.xml.XMLElement;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;

import static com.reandroid.arsc.chunk.xml.ResXmlAttribute.*;

public class ResXmlAttributeArray extends CountedBlockList<ResXmlAttribute>
        implements JSONConvert<JSONArray> {

    public ResXmlAttributeArray(IntegerReference countReference) {
        super(CREATOR, countReference);
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean sort(Comparator<? super ResXmlAttribute> comparator) {
        if (isEmpty()) {
            return false;
        }
        ResXmlStartElement startElement = getStartElement();

        IntegerReference idPosition = startElement.getIdAttributePosition();
        IntegerReference classPosition = startElement.getClassAttributePosition();
        IntegerReference stylePosition = startElement.getStyleAttributePosition();

        ResXmlAttribute idAttribute = get(idPosition.get() - 1);
        ResXmlAttribute classAttribute = get(classPosition.get() - 1);
        ResXmlAttribute styleAttribute = get(stylePosition.get() - 1);

        fixClassAttribute(classAttribute);
        fixStyleAttribute(styleAttribute);

        boolean sorted = super.sort(comparator);

        idPosition.set(idAttribute == null ? 0 : idAttribute.getIndex() + 1);
        classPosition.set(classAttribute == null ? 0 : classAttribute.getIndex() + 1);
        stylePosition.set(styleAttribute == null ? 0 : styleAttribute.getIndex() + 1);

        return sorted;
    }

    private void fixClassAttribute(ResXmlAttribute classAttribute) {
        if (classAttribute != null) {
            if(!ATTRIBUTE_NAME_CLASS.equals(classAttribute.getName())){
                classAttribute.setName(ATTRIBUTE_NAME_CLASS, 0);
            }
        }
    }
    private void fixStyleAttribute(ResXmlAttribute styleAttribute) {
        if (styleAttribute != null) {
            if(!ATTRIBUTE_NAME_STYLE.equals(styleAttribute.getName())){
                styleAttribute.setName(ATTRIBUTE_NAME_STYLE, 0);
            }
        }
    }

    private void computePositionalAttributes() {

        ResXmlAttribute idAttribute = searchAttributeByResourceId(ATTRIBUTE_RESOURCE_ID_id);
        ResXmlAttribute classAttribute = searchAttributeByName(ATTRIBUTE_NAME_CLASS);
        ResXmlAttribute styleAttribute = searchAttributeByName(ATTRIBUTE_NAME_STYLE);

        ResXmlStartElement startElement = getStartElement();

        IntegerReference idPosition = startElement.getIdAttributePosition();
        IntegerReference classPosition = startElement.getClassAttributePosition();
        IntegerReference stylePosition = startElement.getStyleAttributePosition();

        idPosition.set(idAttribute == null ? 0 : idAttribute.getIndex() + 1);
        classPosition.set(classAttribute == null ? 0 : classAttribute.getIndex() + 1);
        stylePosition.set(styleAttribute == null ? 0 : styleAttribute.getIndex() + 1);
    }

    private void detachPositionalAttribute(ResXmlAttribute attribute) {
        ResXmlStartElement startElement = getStartElement();

        int index = attribute.getIndex();

        IntegerReference idPosition = startElement.getIdAttributePosition();
        IntegerReference classPosition = startElement.getClassAttributePosition();
        IntegerReference stylePosition = startElement.getStyleAttributePosition();

        if (index == idPosition.get() - 1) {
            idPosition.set(0);
        } else if (index == classPosition.get() - 1) {
            classPosition.set(0);
        } else if (index == stylePosition.get() - 1) {
            stylePosition.set(0);
        }
    }

    private void mergePositionalAttribute(ResXmlAttributeArray comingArray, ResXmlAttribute comingAttribute, ResXmlAttribute attribute) {
        ResXmlStartElement comingElement = comingArray.getStartElement();

        int comingIndex = comingAttribute.getIndex();

        IntegerReference idPosition = comingElement.getIdAttributePosition();
        IntegerReference classPosition = comingElement.getClassAttributePosition();
        IntegerReference stylePosition = comingElement.getStyleAttributePosition();

        ResXmlStartElement thisElement = getStartElement();

        if (comingIndex == idPosition.get() - 1) {
            thisElement.getIdAttributePosition().set(attribute.getIndex() + 1);
        } else if (comingIndex == classPosition.get() - 1) {
            thisElement.getClassAttributePosition().set(attribute.getIndex() + 1);
        } else if (comingIndex == stylePosition.get() - 1) {
            thisElement.getStyleAttributePosition().set(attribute.getIndex() + 1);
        }
    }

    public boolean removeUndefinedAttributes() {
        return removeIf(ResXmlAttribute::isUndefined);
    }

    void computePositionsAndSort() {
        computePositionalAttributes();
        sort();
    }
    public void sort() {
        sort(CompareUtil.getComparableComparator());
    }
    private int getOffset() {
        ResXmlStartElement element = getStartElement();
        return element.getHeaderBlock().getHeaderSize()
                + element.getAttributeStart().get();
    }
    private void setOffset(int value) {
        ResXmlStartElement element = getStartElement();
        value = value - element.getHeaderBlock().countBytes();
        element.getAttributeStart().set(value);
    }
    private int getUnitSize() {
        IntegerReference reference = getStartElement().getAttributeUnitSize();
        int unit = reference.get();
        if (unit == 0) {
            unit = 20;
            reference.set(unit);
            updateUnitSize();
        }
        return unit;
    }
    public void setUnitSize(int value) {
        if (value != getUnitSize()) {
            getStartElement().getAttributeUnitSize().set(value);
            updateUnitSize();
        }
    }
    private void updateUnitSize() {
        int unit = getUnitSize();
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).setAttributesUnitSize(unit);
        }
    }
    private ResXmlStartElement getStartElement() {
        return getParentInstance(ResXmlStartElement.class);
    }

    @Override
    public ResXmlAttribute createNext() {
        ResXmlAttribute attribute =  super.createNext();
        attribute.setAttributesUnitSize(getUnitSize());
        return attribute;
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        refreshOffset();
    }
    private void refreshOffset() {
        ResXmlStartElement element = getStartElement();
        setOffset(element.countUpTo(this));
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        reader.seek(getOffset());
        int size = getCountReference().get();
        setSize(size);
        int unitSize = getUnitSize();
        for (int i = 0; i < size; i++) {
            ResXmlAttribute attribute = get(i);
            attribute.setAttributesUnitSize(unitSize);
            int position = reader.getPosition();
            attribute.readBytes(reader);
            reader.seek(position + unitSize);
        }
    }
    public void clear() {
        clearChildes();
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
        }
        return attribute;
    }
    public ResXmlAttribute getOrCreateAttribute(String name, int resourceId) {
        ResXmlAttribute attribute = searchAttribute(name, resourceId);
        if (attribute == null) {
            attribute = createNext();
            attribute.setName(name, resourceId);
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
    @Override
    public void onPreRemove(ResXmlAttribute attribute) {
        super.onPreRemove(attribute);
        detachPositionalAttribute(attribute);
        attribute.onRemoved();
    }

    public void merge(ResXmlAttributeArray attributeArray) {
        Iterator<ResXmlAttribute> iterator = attributeArray.iterator();
        while (iterator.hasNext()) {
            ResXmlAttribute coming = iterator.next();
            ResXmlAttribute attribute = createNext();
            attribute.merge(coming);
            mergePositionalAttribute(attributeArray, coming, attribute);
        }
        computePositionsAndSort();
    }
    public void mergeWithName(ResourceMergeOption option, ResXmlAttributeArray attributeArray) {
        Iterator<ResXmlAttribute> iterator = attributeArray.iterator();
        while (iterator.hasNext()) {
            ResXmlAttribute coming = iterator.next();
            ResXmlAttribute attribute = createNext();
            attribute.mergeWithName(option, coming);
            mergePositionalAttribute(attributeArray, coming, attribute);
        }
        computePositionsAndSort();
    }

    @Override
    public JSONArray toJson() {
        return toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        clear();
        if (json == null) {
            return;
        }
        int length = json.length();
        setSize(length);
        for (int i = 0; i < length; i++) {
            get(i).fromJson(json.getJSONObject(i));
        }
        computePositionsAndSort();
    }

    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).serialize(serializer, decode);
        }
    }
    public void toXml(XMLElement element, boolean decode) {
        int size = size();
        for (int i = 0; i < size; i++) {
            element.addAttribute(get(i).toXml(decode));
        }
    }

    private static final Creator<ResXmlAttribute> CREATOR = new Creator<ResXmlAttribute>() {
        @Override
        public ResXmlAttribute[] newArrayInstance(int length) {
            if (length == 0) {
                return EMPTY;
            }
            return new ResXmlAttribute[length];
        }

        @Override
        public ResXmlAttribute newInstance() {
            return new ResXmlAttribute();
        }
    };

    private static final ResXmlAttribute[] EMPTY = new ResXmlAttribute[0];
}
