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
package com.reandroid.xml;

import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.xml.base.Attribute;
import com.reandroid.xml.base.Document;
import com.reandroid.xml.base.Element;
import com.reandroid.xml.base.NamedNode;
import com.reandroid.xml.base.Node;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class XMLPath implements Predicate<NamedNode> {

    private static final int TYPE_UNKNOWN = ObjectsUtil.of(-1);
    public static final int TYPE_ELEMENT = ObjectsUtil.of(0);
    public static final int TYPE_ATTRIBUTE = ObjectsUtil.of(1);
    public static final String ANY_NAME = ObjectsUtil.of("*");

    private final XMLPath parent;
    private final String name;
    private final int nameId;
    private final int type;

    private XMLPath(XMLPath parent, String name, int nameId, int type) throws InvalidPathException {
        this.parent = parent;
        this.name = name;
        this.nameId = nameId;
        this.type = type;
        if (parent != null && parent.type() == TYPE_ATTRIBUTE) {
            throw new InvalidPathException("Attribute can not have child");
        }
        if (parent == null && type == TYPE_ATTRIBUTE) {
            throw new InvalidPathException("Null parent for attribute");
        }
    }
    private XMLPath(XMLPath parent, String name) throws InvalidPathException {
        int type = TYPE_ATTRIBUTE;
        int i = name.lastIndexOf(';');
        if (i < 0) {
            i = name.lastIndexOf('/');
            type = TYPE_ELEMENT;
        }
        if (i < 0) {
            throw new InvalidPathException("Name should start with / or ;");
        }
        this.type = type;
        String simpleName = name.substring(i + 1);
        this.name = simpleName;
        this.nameId = decodeNameId(simpleName);
        XMLPath thisPrent;
        if (i > 0) {
            thisPrent = new XMLPath(parent, name.substring(0, i));
        } else {
            thisPrent = parent;
        }
        this.parent = thisPrent;
        if (thisPrent != null && thisPrent.type == TYPE_ATTRIBUTE) {
            throw new InvalidPathException("Attribute can not have child");
        }
    }
    private XMLPath(NamedNode node) {
        this.name = node.getName();
        int type = TYPE_UNKNOWN;
        int nameId = 0;
        Element<?> parentElement = null;
        if (node instanceof Attribute) {
            type = TYPE_ATTRIBUTE;
            Attribute attribute = (Attribute) node;
            parentElement = attribute.getParentNode();
            nameId = attribute.getNameId();
        } else if (node instanceof Element) {
            type = TYPE_ELEMENT;
            Node parentNode = ((Element<?>) node).getParentNode();
            if (parentNode instanceof Element) {
                parentElement = (Element<?>) parentNode;
            }
        }
        XMLPath parentPath = null;
        if (parentElement != null) {
            parentPath = new XMLPath(parentElement);
        }
        this.parent = parentPath;
        this.type = type;
        this.nameId = nameId;
    }

    public XMLPath getParent() {
        return parent;
    }
    public String getName() {
        return name;
    }
    public int getNameId() {
        return nameId;
    }
    private XMLPath getPath(int depth) {
        XMLPath path = this;
        int i = 0;
        while (i != depth) {
            path = path.getParent();
            i ++;
        }
        return path;
    }
    private int depth() {
        XMLPath path = getParent();
        int i = 0;
        while (path != null) {
            path = path.getParent();
            i ++;
        }
        return i;
    }
    public int type() {
        return type;
    }
    public String getPath() {
        XMLPath path = this;
        StringBuilder builder = new StringBuilder();
        while (path != null) {
            int nameId = path.getNameId();
            if (nameId != 0) {
                builder.insert(0, encodeId(nameId));
            } else {
                builder.insert(0, path.getName());
            }
            builder.insert(0, getSeparator(path.type()));
            path = path.getParent();
        }
        return builder.toString();
    }
    public XMLPath clearNameId() throws InvalidPathException {
        return changeNameId(0);
    }
    public XMLPath changeNameId(int id) throws InvalidPathException {
        if (id == getNameId()) {
            return this;
        }
        return new XMLPath(getParent(), getName(), id, type());
    }
    public XMLPath changeName(String name) throws InvalidPathException {
        if (getName().equals(name)) {
            return this;
        }
        validateSimpleName(name);
        return new XMLPath(getParent(), name, getNameId(), type());
    }
    public XMLPath parse(String childPath) throws InvalidPathException {
        return new XMLPath(this, childPath);
    }
    public XMLPath attribute(String name) throws InvalidPathException {
        validateSimpleName(name);
        return new XMLPath(this, name, 0, TYPE_ATTRIBUTE);
    }
    public XMLPath attribute(int id) throws InvalidPathException {
        return new XMLPath(this, encodeId(id), id, TYPE_ATTRIBUTE);
    }
    public XMLPath element(String name) throws InvalidPathException {
        if (type() == TYPE_ATTRIBUTE) {
            throw new InvalidPathException("Attribute can not have child element");
        }
        if (name == null || name.length() == 0) {
            throw new InvalidPathException("Name can not be empty");
        }
        char c = name.charAt(0);
        if (c == ';') {
            throw new InvalidPathException("Invalid element name: " + name);
        }
        if (c != '/') {
            name = "/" + name;
        }
        return new XMLPath(this, name);
    }
    protected boolean matchesName(NamedNode namedNode) {
        if (namedNode == null || type() != typeOf(namedNode)) {
            return false;
        }
        String name = this.getName();
        if (ANY_NAME.equals(name)) {
            return true;
        }
        int nameId1 = this.getNameId();
        if (nameId1 != 0) {
            if (namedNode instanceof Attribute) {
                return nameId1 == ((Attribute) namedNode).getNameId();
            }
        }
        return name.equals(namedNode.getName());
    }
    @Override
    public boolean test(NamedNode namedNode) {
        int depth = depth();
        NamedNode parentNode = namedNode;
        for (int i = 0; i <= depth; i++) {
            if (!getPath(i).matchesName(parentNode)) {
                return false;
            }
            parentNode = getParentNode(parentNode);
        }
        return parentNode == null;
    }
    public boolean contains(Document<?> document) {
        return findFirst(document) != null;
    }
    public<T extends NamedNode> List<T> list(Document<?> document) {
        return CollectionUtil.toList(find(document));
    }
    public<T extends NamedNode> T findFirst(Document<?> document) {
        return CollectionUtil.getFirst(find(document));
    }
    public<T extends NamedNode> Iterator<T> find(Document<?> document) {
        if (document == null) {
            return EmptyIterator.of();
        }
        return ObjectsUtil.cast(find(SingleIterator.of(document.getDocumentElement()), depth()));
    }
    private Iterator<NamedNode> find(Iterator<NamedNode> iterator, int depth) {
        XMLPath path = getPath(depth);
        iterator = FilterIterator.of(iterator, path::matchesName);
        if (depth == 0 || !iterator.hasNext()) {
            return iterator;
        }
        int nextDepth = depth - 1;
        boolean attribute = nextDepth == 0 && this.type() == TYPE_ATTRIBUTE;
        iterator = new IterableIterator<NamedNode, NamedNode>(iterator) {
            @Override
            public Iterator<NamedNode> iterator(NamedNode namedNode) {
                Element<?> element = (Element<?>) namedNode;
                if (attribute) {
                    return ObjectsUtil.cast(element.getAttributes());
                }
                return ObjectsUtil.cast(element.iterator(Element.class));
            }
        };
        return this.find(iterator, nextDepth);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof XMLPath)) {
            return false;
        }
        XMLPath xmlPath = (XMLPath) obj;
        return this.getNameId() == xmlPath.getNameId() &&
                this.type() == xmlPath.type() &&
                ObjectsUtil.equals(this.getParent(), xmlPath.getParent()) &&
                ObjectsUtil.equals(this.getName(), xmlPath.getName());
    }

    @Override
    public int hashCode() {
        int hash = 31 + getNameId();
        hash = hash * 31 + type();
        hash = hash * 31 + ObjectsUtil.hash(parent, name);
        return hash;
    }

    @Override
    public String toString() {
        return getPath();
    }

    private static NamedNode getParentNode(NamedNode namedNode) {
        if (namedNode instanceof Element) {
            Node node = ((Element<?>) namedNode).getParentNode();
            if (node instanceof Element) {
                return (NamedNode) node;
            }
            return null;
        }
        if (namedNode instanceof Attribute) {
            return  ((Attribute) namedNode).getParentNode();
        }
        return null;
    }
    private static int decodeNameId(String simpleName) {
        if (simpleName.charAt(0) != '@') {
            return 0;
        }
        simpleName = simpleName.substring(1);
        try {
            long l = Long.decode(simpleName);
            return (int) l;
        } catch (NumberFormatException e) {
            throw new InvalidPathException("Invalid name id: " + simpleName, e);
        }
    }
    private static String encodeId(int id) {
        long l = id & 0xffffffffL;
        return "@0x" + Long.toHexString(l);
    }
    private static int typeOf(NamedNode namedNode) {
        if (namedNode instanceof Attribute) {
            return TYPE_ATTRIBUTE;
        }
        if (namedNode instanceof Element) {
            return TYPE_ELEMENT;
        }
        return TYPE_UNKNOWN;
    }
    private static String getSeparator(int type) {
        if (type == TYPE_ATTRIBUTE) {
            return ";";
        }
        return "/";
    }
    public static XMLPath of(NamedNode node) {
        return new XMLPath(node);
    }
    public static XMLPath compile(String path) throws InvalidPathException {
        return new XMLPath(null, path);
    }
    public static XMLPath newElement(String name) throws InvalidPathException {
        validateSimpleName(name);
        return new XMLPath(null, name, 0, TYPE_ELEMENT);
    }
    private static void validateSimpleName(String name) throws InvalidPathException {
        if (name == null) {
            throw new InvalidPathException("Null name");
        }
        if (name.length() == 0) {
            throw new InvalidPathException("Empty name");
        }
        if (containsIllegalChars(name)) {
            throw new InvalidPathException("Name contains illegal characters: " + name);
        }
    }
    private static boolean containsIllegalChars(String simpleName) {
        return simpleName.indexOf('/') >= 0 || simpleName.indexOf(';') >= 0;
    }

    public static class InvalidPathException extends RuntimeException {
        public InvalidPathException(String message) {
            super(message);
        }
        public InvalidPathException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
