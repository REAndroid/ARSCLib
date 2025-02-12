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

import com.reandroid.arsc.item.ShortItem;
import com.reandroid.utils.ObjectsUtil;

public class ResXmlAttributePosition extends ShortItem {

    private final int type;
    private ResXmlAttribute mAttribute;

    public ResXmlAttributePosition(int type) {
        super();
        this.type = type;
    }

    public int getPosition() {
        return get() - 1;
    }
    public void setPosition(int position) {
        writePosition(position);
        pullAttribute(true);
    }

    public ResXmlAttribute getAttribute() {
        return mAttribute;
    }
    public void setAttribute(ResXmlAttribute attribute) {
        this.mAttribute = attribute;
        int position;
        if (attribute != null) {
            position = attribute.getIndex();
        } else {
            position = -1;
        }
        writePosition(position);
    }
    public void computePosition() {
        ResXmlAttribute attribute = null;
        ResXmlAttributeArray attributeArray = getAttributeArray();
        if (attributeArray != null) {
            attribute = computePosition(attributeArray);
        }
        setAttribute(attribute);
    }
    private ResXmlAttribute computePosition(ResXmlAttributeArray attributeArray) {
        int type = type();
        if (type == TYPE_ID) {
            return attributeArray.searchAttributeByResourceId(ResXmlAttribute.ATTRIBUTE_RESOURCE_ID_id);
        }
        if (type == TYPE_CLASS) {
            return attributeArray.searchAttributeByName(ResXmlAttribute.ATTRIBUTE_NAME_CLASS);
        }
        if (type == TYPE_STYLE) {
            return attributeArray.searchAttributeByName(ResXmlAttribute.ATTRIBUTE_NAME_STYLE);
        }
        throw new RuntimeException("Unknown position type: " + type);
    }

    public void clear() {
        this.mAttribute = null;
        writePosition(-1);
    }

    public void refresh() {
        ResXmlAttribute attribute = this.mAttribute;
        if (isInvalidAttribute(attribute)) {
            clear();
        } else {
            writePosition(attribute.getIndex());
        }
    }

    void linkAttribute() {
        pullAttribute(false);
    }

    private void writePosition(int position) {
        if (position < -1 || position > 65534) {
            throw new IndexOutOfBoundsException("Attribute position " + position
                    + " out of range, must be between -1 to 65534");
        }
        set(position + 1);
    }
    private void pullAttribute(boolean validate) {
        int position = getPosition();
        ResXmlAttribute attribute = null;
        if (position >= 0) {
            ResXmlAttributeArray attributeArray = getAttributeArray();
            if (attributeArray != null) {
                attribute = attributeArray.get(position);
            }
            if (validate && attribute == null) {
                if (attributeArray == null) {
                    throw new IllegalArgumentException("Unable to find attributes array, " +
                            "could be removed or uninitialized element");
                }
                throw new IndexOutOfBoundsException("Position " + position +
                        " out of range, for " + attributeArray.size());
            }
        }
        this.mAttribute = attribute;
    }
    private ResXmlAttributeArray getAttributeArray() {
        ResXmlStartElement startElement = getParentInstance(ResXmlStartElement.class);
        if (startElement != null) {
            return startElement.getResXmlAttributeArray();
        }
        return ObjectsUtil.getNull();
    }

    void fixName() {
        int type = type();
        if (type == TYPE_CLASS) {
            fixName(ResXmlAttribute.ATTRIBUTE_NAME_CLASS);
        } else if (type == TYPE_STYLE) {
            fixName(ResXmlAttribute.ATTRIBUTE_NAME_STYLE);
        }
    }
    private void fixName(String name) {
        ResXmlAttribute attribute = this.mAttribute;
        if (attribute != null && !name.equals(attribute.getName())) {
            attribute.setName(name);
        }
    }

    public int type() {
        return type;
    }
    @Override
    public String toString() {
        return getPosition() + " (" + this.mAttribute + ")";
    }

    private static boolean isInvalidAttribute(ResXmlAttribute attribute) {
        return attribute == null || attribute.isNull() ||
                attribute.isUndefined() || attribute.getParent() == null;
    }
    public static int getPositionType(ResXmlAttribute attribute) {
        int id = attribute.getNameId();
        if (id != 0) {
            if (id == ResXmlAttribute.ATTRIBUTE_RESOURCE_ID_id) {
                return TYPE_ID;
            }
        } else {
            if (ResXmlAttribute.ATTRIBUTE_NAME_CLASS.equals(attribute.getName())) {
                return TYPE_CLASS;
            }
            if (ResXmlAttribute.ATTRIBUTE_NAME_STYLE.equals(attribute.getName())) {
                return TYPE_STYLE;
            }
        }
        return -1;
    }

    public static final int TYPE_ID = ObjectsUtil.of(0);
    public static final int TYPE_CLASS = ObjectsUtil.of(0);
    public static final int TYPE_STYLE = ObjectsUtil.of(0);

}
