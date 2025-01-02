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

import com.reandroid.utils.collection.*;
import com.reandroid.xml.base.NodeTree;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;


public abstract class XMLNodeTree extends XMLNode implements
        NodeTree<XMLNode>, Iterable<XMLNode>, SizedSupplier<XMLNode> {

    private ArrayCollection<XMLNode> mNodeList;

    private int lastTrimSize;

    public XMLNodeTree(){
        super();
        this.mNodeList = EMPTY;
    }

    public XMLNode getLast(){
        int size = size();
        if(size == 0){
            return null;
        }
        return mNodeList.get(size - 1);
    }
    @Override
    public void clear(){
        if(size() == 0){
            return;
        }
        synchronized (this){
            mNodeList.clear();
            mNodeList.trimToSize();
            lastTrimSize = 0;
        }
    }
    public Iterator<XMLNode> iterator(org.apache.commons.collections4.Predicate<? super XMLNode> filter){
        return new IndexIterator<>(this, filter);
    }
    @Override
    public Iterator<XMLNode> iterator(){
        return new IndexIterator<>(this);
    }
    public Iterator<XMLNode> recursiveNodes(){
        return RecursiveIterator.of(this, XMLNode::iterator);
    }
    @Override
    public int size(){
        return mNodeList.size();
    }
    @Override
    public XMLNode get(int index) {
        synchronized (this) {
            return mNodeList.get(index);
        }
    }
    public void addAll(Iterable<? extends XMLNode> iterable){
        for (XMLNode xmlNode : iterable) {
            add(xmlNode);
        }
    }
    public boolean add(XMLNode xmlNode) {
        if(xmlNode == null || xmlNode == this){
            return false;
        }
        synchronized (this){
            if(mNodeList == EMPTY){
                mNodeList = new ArrayCollection<>();
            }
            if (mNodeList.containsExact(xmlNode)) {
                throw new IllegalArgumentException("Duplicate node: " + xmlNode);
            }
            boolean added = mNodeList.add(xmlNode);
            xmlNode.setParentNode(this);
            if(mNodeList.size() - lastTrimSize > TRIM_INTERVAL){
                mNodeList.trimToSize();
                lastTrimSize = mNodeList.size();
            }
            return added;
        }
    }
    public void add(int i, XMLNode xmlNode) {
        if(xmlNode == null || xmlNode == this){
            return;
        }
        synchronized (this){
            if(mNodeList == EMPTY){
                mNodeList = new ArrayCollection<>();
            }
            if (mNodeList.containsExact(xmlNode)) {
                throw new IllegalArgumentException("Duplicate node: " + xmlNode);
            }
            mNodeList.add(i, xmlNode);
            xmlNode.setParentNode(this);
            if(mNodeList.size() - lastTrimSize > TRIM_INTERVAL){
                mNodeList.trimToSize();
                lastTrimSize = mNodeList.size();
            }
        }
    }
    public int indexOf(XMLNode node){
        return mNodeList.indexOf(node);
    }
    public boolean remove(XMLNode xmlNode){
        synchronized (this){
            if(xmlNode != null && mNodeList.remove(xmlNode)){
                xmlNode.setParentNode(null);
                return true;
            }
            return false;
        }
    }
    public XMLNode remove(int i){
        synchronized (this){
            XMLNode xmlNode = mNodeList.remove(i);
            if(xmlNode != null){
                xmlNode.setParentNode(null);
            }
            return xmlNode;
        }
    }
    /**
     * Use removeIf
     * */
    @Deprecated
    public boolean remove(org.apache.commons.collections4.Predicate<? super XMLNode> filter) {
        throw new RuntimeException("Depreciated method");
    }
    @Override
    public boolean removeIf(org.apache.commons.collections4.Predicate<? super XMLNode> filter) {
        synchronized (this){
            return mNodeList.removeIf(filter);
        }
    }
    @Override
    public boolean sort(Comparator<? super XMLNode> comparator) {
        synchronized (this){
            return mNodeList.sortItems(comparator);
        }
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        startSerialize(serializer);
        serializeChildes(serializer);
        endSerialize(serializer);
    }
    abstract void startSerialize(XmlSerializer serializer) throws IOException;
    private void serializeChildes(XmlSerializer serializer) throws IOException {
        Iterator<XMLNode> itr = iterator();
        while (itr.hasNext()){
            itr.next().serialize(serializer);
        }
    }
    abstract void endSerialize(XmlSerializer serializer) throws IOException;

    public abstract XMLElement newElement();
    public abstract XMLText newText();
    public XMLText newText(String text) {
        XMLText xmlText = newText();
        xmlText.setText(text);
        return xmlText;
    }
    public XMLComment newComment(){
        XMLComment comment = new XMLComment();
        add(comment);
        return comment;
    }
    public XMLNamespace newNamespace(String uri, String prefix) {
        return new XMLNamespace(uri, prefix);
    }

    private static final int TRIM_INTERVAL = 1000;
    private static final ArrayCollection<XMLNode> EMPTY = ArrayCollection.empty();
}
