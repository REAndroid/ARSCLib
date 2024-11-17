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

import android.content.res.XmlResourceParser;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.WrappedBlock;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.InstanceIterator;
import com.reandroid.xml.XMLNode;
import com.reandroid.xml.base.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public abstract class ResXmlNode extends WrappedBlock implements
        Node, JSONConvert<JSONObject> {

    ResXmlNode(Block chunk) {
        super(chunk);
    }

    Block getChunk() {
        return getBaseBlock();
    }
    @Override
    public ResXmlNode getParentNode() {
        return getParentInstance(ResXmlNode.class);
    }
    public Iterator<ResXmlNode> getParentNodes() {
        return new ParentNodeIterator(getParentNode());
    }
    <T extends ResXmlNode> Iterator<T> visitParentNodes(Class<T> instance, Class<? extends ResXmlNode> upperBound) {
        return InstanceIterator.of(new ParentNodeIterator(this, upperBound), instance);
    }

    public ResXmlNode getPrevious() {
        ResXmlNode parent = getParentNode();
        if (parent instanceof ResXmlNodeTree) {
            ResXmlNodeTree nodeTree = (ResXmlNodeTree)parent;
            return nodeTree.get(getIndex() - 1);
        }
        return null;
    }
    public ResXmlNode getNext() {
        ResXmlNode parent = getParentNode();
        if (parent instanceof ResXmlNodeTree) {
            ResXmlNodeTree nodeTree = (ResXmlNodeTree)parent;
            return nodeTree.get(getIndex() + 1);
        }
        return null;
    }
    abstract void onPreRemove();
    public boolean removeSelf() {
        throw new RuntimeException("Method not implemented");
    }
    abstract void linkStringReferences();
    public int getDepth() {
        return CollectionUtil.count(getParentNodes());
    }

    @Override
    public int getLineNumber() {
        return getStartLineNumber();
    }
    public int getStartLineNumber() {
        return 0;
    }
    public int getEndLineNumber() {
        return 0;
    }

    abstract Iterator<ResXmlEvent> getParserEvents();

    public void autoSetLineNumber() {
        autoSetLineNumber(1);
    }
    abstract int autoSetLineNumber(int start);

    abstract String nodeTypeName();
    @Override
    public abstract JSONObject toJson();
    @Override
    public abstract void fromJson(JSONObject json);


    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serialize(serializer, true);
    }
    public abstract void serialize(XmlSerializer serializer, boolean decode) throws IOException;
    void serializeComment(XmlSerializer serializer, String comment) throws IOException {
        if (comment != null) {
            serializer.comment(comment);
        }
    }
    @Override
    public abstract void parse(XmlPullParser parser) throws IOException, XmlPullParserException;
    public XmlPullParser getParser() {
        return new ResXmlEventParser(getParserEvents());
    }
    public XmlResourceParser getResourceParser() {
        return new ResXmlPullParser(getParserEvents());
    }
    public abstract XMLNode toXml(boolean decode);

    public boolean isDocument() {
        return false;
    }
    public boolean isElement() {
        return false;
    }
    public boolean isText() {
        return false;
    }
    public boolean isUnknown() {
        return false;
    }
    ResXmlNode newSimilarTo(ResXmlNode otherNode) {
        throw new RuntimeException("Method not implemented");
    }

    public abstract void mergeWithName(ResourceMergeOption mergeOption, ResXmlNode xmlNode);
    public abstract void merge(ResXmlNode xmlNode);

    static void setIndent(XmlSerializer serializer, boolean state) {
        try {
            serializer.setFeature(FEATURE_INDENT_OUTPUT, state);
        } catch (Throwable ignored){
        }
    }

    static boolean isTextEvent(int event){
        return event == XmlPullParser.TEXT
                || event == XmlPullParser.ENTITY_REF
                || event == XmlPullParser.IGNORABLE_WHITESPACE;
    }

    private static final String FEATURE_INDENT_OUTPUT = "http://xmlpull.org/v1/doc/features.html#indent-output";


    static class ParentNodeIterator implements Iterator<ResXmlNode> {

        private ResXmlNode parentNode;
        private final Class<? extends ResXmlNode> upperBoundClass;

        public ParentNodeIterator(ResXmlNode parentNode, Class<? extends ResXmlNode> upperBoundClass) {
            if (parentNode != null && upperBoundClass != null) {
                if (upperBoundClass.isInstance(parentNode)) {
                    parentNode = null;
                }
            }
            this.parentNode = parentNode;
            this.upperBoundClass = upperBoundClass;
        }
        public ParentNodeIterator(ResXmlNode parentNode) {
            this(parentNode, null);
        }

        @Override
        public boolean hasNext() {
            return this.parentNode != null;
        }
        @Override
        public ResXmlNode next() {
            ResXmlNode resXmlNode = this.parentNode;
            ResXmlNode parent = resXmlNode.getParentNode();
            Class<? extends ResXmlNode> upperBoundClass = this.upperBoundClass;
            if (parent != null && upperBoundClass != null) {
                if (upperBoundClass.isInstance(parent)) {
                    parent = null;
                }
            }
            this.parentNode = parent;
            return resXmlNode;
        }
    }



    public static final String JSON_node_type = ObjectsUtil.of("node_type");
    public static final String JSON_node_type_document = ObjectsUtil.of("document");
    public static final String JSON_node_type_element = ObjectsUtil.of("element");
    public static final String JSON_node_type_text = ObjectsUtil.of("text");
    public static final String JSON_node_type_unknown = ObjectsUtil.of("unknown");

    public static final String JSON_name = ObjectsUtil.of("name");
    public static final String JSON_id = ObjectsUtil.of("id");
    public static final String JSON_comment = ObjectsUtil.of("comment");
    public static final String JSON_namespaces = ObjectsUtil.of("namespaces");
    public static final String JSON_uri = ObjectsUtil.of("uri");
    public static final String JSON_prefix = ObjectsUtil.of("prefix");
    public static final String JSON_line = ObjectsUtil.of("line");
    public static final String JSON_line_end = ObjectsUtil.of("line_end");
    public static final String JSON_attributes = ObjectsUtil.of("attributes");
    public static final String JSON_nodes = ObjectsUtil.of("nodes");
    public static final String JSON_value = ObjectsUtil.of("value");
}
