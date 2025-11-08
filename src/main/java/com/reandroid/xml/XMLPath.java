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

import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.*;
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
    public static final String ANY_ELEMENT_PATH = ObjectsUtil.of("**");

    private final XMLPath parent;
    private final String name;
    private final int nameId;
    private final int type;

    XMLPath(XMLPath parent, String name, int nameId, int type) throws InvalidPathException {
        this.parent = parent;
        this.name = name;
        this.nameId = nameId;
        this.type = type;
        if (parent != null && parent.type() == TYPE_ATTRIBUTE) {
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
        return parse(this, childPath);
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
        return parse(this, name);
    }
    public XMLPath alternate(String name) throws InvalidPathException {
        return CombinedPath.combined(this,
                new XMLPath(getParent(), name, 0, type()));
    }
    public XMLPath alternate(int id) throws InvalidPathException {
        return CombinedPath.combined(this,
                new XMLPath(getParent(), encodeId(id), id, type()));
    }
    protected boolean matchesName(int depth, NamedNode namedNode) {
        if (namedNode == null) {
            return false;
        }
        XMLPath xmlPath = getPath(depth);
        return xmlPath != null && xmlPath.matchesName(namedNode);
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
    public boolean isAnyName() {
        return ANY_NAME.equals(getName());
    }
    public boolean containsAnyNae() {
        XMLPath parent = this;
        while (parent != null) {
            if (parent.isAnyName()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    public boolean isAnyElementPath() {
        return type() == TYPE_ELEMENT && ANY_ELEMENT_PATH.equals(getName());
    }
    public boolean containsAnyElementPath() {
        XMLPath parent = this;
        while (parent != null) {
            if (parent.isAnyElementPath()) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    @Override
    public boolean test(NamedNode namedNode) {
        if (namedNode == null) {
            return false;
        }
        if (isAnyElementPath()) {
            XMLPath parent = getParent();
            if (parent == null) {
                return true;
            }
            NamedNode parentNode = namedNode;
            while (parentNode != null) {
                if (parent.test(parentNode)) {
                    return true;
                }
                parentNode = getParentNode(parentNode);
            }
            return false;
        }
        if (!matchesName(namedNode)) {
            return false;
        }
        XMLPath parent = getParent();
        if (parent != null) {
            return parent.test(getParentNode(namedNode));
        }
        return true;
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
    public<T extends NamedNode> T findFirst(Element<?> element) {
        return CollectionUtil.getFirst(find(element));
    }
    public<T extends NamedNode> Iterator<T> find(Document<?> document) {
        if (document == null) {
            return EmptyIterator.of();
        }
        return find(document.getDocumentElement());
    }
    public<T extends NamedNode> Iterator<T> find(Element<?> element) {
        if (element == null) {
            return EmptyIterator.of();
        }
        if (type() == TYPE_ATTRIBUTE && getParent() == null) {
            return findAll(element.getAttributes());
        }
        return findAll(SingleIterator.of(element));
    }
    public<E extends NamedNode, T extends NamedNode> Iterator<T> findAll(Iterator<E> iterator) {
        return ObjectsUtil.cast(search(ObjectsUtil.cast(iterator)));
    }
    private Iterator<NamedNode> search(Iterator<NamedNode> iterator) {
        if (containsAnyElementPath()) {
            iterator = new IterableIterator<NamedNode, NamedNode>(iterator) {
                @Override
                public Iterator<NamedNode> iterator(NamedNode node) {
                    if (node instanceof Element) {
                        return RecursiveIterator.of(node, n ->
                                        ((Element<?>) n).iterator(Element.class));
                    }
                    return SingleIterator.of(node);
                }
            };
            if (type() == TYPE_ATTRIBUTE) {
                iterator = new IterableIterator<NamedNode, NamedNode>(iterator) {
                    @Override
                    public Iterator<NamedNode> iterator(NamedNode node) {
                        if (node instanceof Element) {
                            return ObjectsUtil.cast(((Element<?>) node).getAttributes());
                        }
                        return SingleIterator.of(node);
                    }
                };
            }
            return FilterIterator.of(iterator, this);
        }
        return this.search(iterator, depth(), 0);
    }
    private Iterator<NamedNode> search(Iterator<NamedNode> iterator, int depth, int depthEnd) {
        iterator = FilterIterator.of(iterator, namedNode -> matchesName(depth, namedNode));
        if (depth == depthEnd || !iterator.hasNext()) {
            return iterator;
        }
        int nextDepth = depth - 1;
        boolean attribute = nextDepth == depthEnd && this.type() == TYPE_ATTRIBUTE;
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
        return this.search(iterator, nextDepth, depthEnd);
    }
    protected boolean equalsName(XMLPath xmlPath) {
        if (xmlPath == null) {
            return false;
        }
        if (xmlPath == this) {
            return true;
        }
        if (!(this instanceof CombinedPath) && (xmlPath instanceof CombinedPath)) {
            return xmlPath.equalsName(this);
        }
        if (this.type() != xmlPath.type()) {
            return false;
        }
        int id = this.getNameId();
        if (id != 0) {
            return id == xmlPath.getNameId();
        }
        return this.getName().equals(xmlPath.getName());
    }
    protected int compareName(XMLPath path) {
        int id1 = getNameId();
        int id2 = path.getNameId();
        if (id1 != 0) {
            if (id2 == 0) {
                return -1;
            }
            return CompareUtil.compare(id1, id2);
        }
        if (id2 != 0) {
            return 1;
        }
        return this.getName().compareTo(path.getName());
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
        hash = hash * 31 + ObjectsUtil.hash(getParent(), getName());
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
    static String encodeId(int id) {
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
        return parse(null, path);
    }
    public static XMLPath compile(XMLPath parent, String path) throws InvalidPathException {
        return parse(parent, path);
    }
    public static XMLPath newElement(String name) throws InvalidPathException {
        validateSimpleName(name);
        return new XMLPath(null, name, 0, TYPE_ELEMENT);
    }
    private static XMLPath parse(XMLPath parent, String name) throws InvalidPathException {
        int type = TYPE_ATTRIBUTE;
        int i = name.lastIndexOf(';');
        if (i < 0) {
            i = name.lastIndexOf('/');
            type = TYPE_ELEMENT;
        }
        if (i < 0) {
            throw new InvalidPathException("Name should start with / or ;");
        }
        String simpleName = name.substring(i + 1);
        if (i > 0) {
            parent = parse(parent, name.substring(0, i));
        }
        if (parent != null && parent.type() == TYPE_ATTRIBUTE) {
            throw new InvalidPathException("Attribute can not have child");
        }
        if (simpleName.indexOf('|') > 0) {
            String[] elementNames = StringsUtil.split(simpleName, '|');
            int length = elementNames.length;
            XMLPath[] elements = new XMLPath[length];
            for (int j = 0; j < length; j++) {
                String element = elementNames[j];
                elements[j] = new XMLPath(parent, element, decodeNameId(element), type);
            }
            return CombinedPath.combined(elements);
        }
        return new XMLPath(parent, simpleName, decodeNameId(simpleName), type);
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

    static class CombinedPath extends XMLPath {

        private final XMLPath[] elements;

        CombinedPath(XMLPath parent, XMLPath[] elements) throws InvalidPathException {
            super(parent, combineNames(elements), 0, elements[0].type());
            this.elements = elements;
        }

        public XMLPath[] elements() {
            return elements;
        }

        @Override
        protected boolean matchesName(NamedNode namedNode) {
            if (namedNode == null) {
                return false;
            }
            for (XMLPath xmlPath : elements()) {
                if (xmlPath.matchesName(namedNode)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        protected boolean matchesName(int depth, NamedNode namedNode) {
            if (namedNode == null) {
                return false;
            }
            for (XMLPath xmlPath : elements()) {
                if (xmlPath.matchesName(depth, namedNode)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected boolean equalsName(XMLPath xmlPath) {
            if (super.equalsName(xmlPath)) {
                return true;
            }
            for (XMLPath path : elements()) {
                if (path.equalsName(xmlPath)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean test(NamedNode namedNode) {
            for (XMLPath path : elements()) {
                if (path.test(namedNode)) {
                    return true;
                }
            }
            return false;
        }

        public static XMLPath combined(XMLPath ... paths) {
            validatePaths(paths);
            if (paths.length == 1) {
                return paths[0];
            }
            int depth = paths[0].depth();
            XMLPath result = null;
            for (int i = depth; i >= 0; i--) {
                result = new CombinedPath(result, getAtDepth(paths, i));
            }
            return result;
        }
        private static XMLPath[] getAtDepth(XMLPath[] paths, int depth) {
            int length = paths.length;
            List<XMLPath> results = new ArrayCollection<>(length);
            for (int i = 0; i < length; i++) {
                addIfAbsent(results, paths[i].getPath(depth));
            }
            results.sort(XMLPath::compareName);
            length = results.size();
            return results.toArray(new XMLPath[length]);
        }
        private static void addIfAbsent(List<XMLPath> pathList, XMLPath xmlPath) {
            if (xmlPath instanceof CombinedPath) {
                XMLPath[] elements = ((CombinedPath) xmlPath).elements();
                for (XMLPath element : elements) {
                    addIfAbsent(pathList, element);
                }
            } else if (xmlPath != null) {
                int size = pathList.size();
                for (int i = 0; i < size; i++) {
                    if (pathList.get(i).equalsName(xmlPath)) {
                        return;
                    }
                }
                pathList.add(xmlPath);
            }
        }
        private static String combineNames(XMLPath[] paths) {
            StringBuilder builder = new StringBuilder();
            int length = paths.length;
            for (int i = 0; i < length; i++) {
                if (i != 0) {
                    builder.append('|');
                }
                XMLPath path = paths[i];
                int id = path.getNameId();
                if (id != 0) {
                    builder.append(XMLPath.encodeId(id));
                } else {
                    builder.append(path.getName());
                }
            }
            return builder.toString();
        }
        private static void validatePaths(XMLPath ... paths) throws InvalidPathException {
            if (paths == null || paths.length == 0) {
                throw new InvalidPathException("Paths can not be empty");
            }
            int length = paths.length;
            if (length == 1) {
                return;
            }
            XMLPath xmlPath = paths[0];
            int depth = xmlPath.depth();
            int type = xmlPath.type();
            for (int i = 1; i < length; i++) {
                xmlPath = paths[i];
                if (xmlPath.depth() != depth) {
                    throw new InvalidPathException("Can not combine different depth paths: "
                            + depth + " vs " + xmlPath.depth());
                }
                if (xmlPath.type() != type) {
                    throw new InvalidPathException("Can not combine different type paths: "
                            + type + " vs " + xmlPath.type());
                }
            }
        }
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
