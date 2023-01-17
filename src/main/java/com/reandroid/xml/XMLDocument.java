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

import com.reandroid.xml.parser.XMLDocumentParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;

public class XMLDocument extends XMLNode{
    private XMLElement mDocumentElement;
    private Object mTag;
    private String mName;
    private String mConfigName;
    private float mIndentScale;
    private XmlHeaderElement mHeaderElement;
    private Object mLastElementSorter;
    public XMLDocument(String elementName){
        this();
        XMLElement docElem=new XMLElement(elementName);
        setDocumentElement(docElem);
    }
    public XMLDocument(){
        mIndentScale=0.5f;
        mHeaderElement = new XmlHeaderElement();
    }
    public void setHeaderElement(XmlHeaderElement headerElement){
        this.mHeaderElement=headerElement;
    }
    public void hideComments(boolean hide){
        hideComments(true, hide);
    }
    public void hideComments(boolean recursive, boolean hide){
        if(mDocumentElement==null){
            return;
        }
        mDocumentElement.hideComments(recursive, hide);
    }
    public XmlHeaderElement getHeaderElement(){
        return mHeaderElement;
    }

    public void sortDocumentElement(Comparator<XMLElement> comparator){
        if(mDocumentElement==null||comparator==null){
            return;
        }
        if(mLastElementSorter !=null){
            if(mLastElementSorter.getClass().equals(comparator.getClass())){
                return;
            }
        }
        mLastElementSorter=comparator;
        mDocumentElement.sortChildes(comparator);
    }
    public void setIndentScalePercent(int val){
        int percent;
        if(val>100){
            percent=100;
        }else if(val<0){
            percent=0;
        }else {
            percent=val;
        }
        mIndentScale=percent/100.0f;
        XMLElement docElem=getDocumentElement();
        if(docElem!=null){
            docElem.setIndentScale(mIndentScale);
        }
    }
    public String getName(){
        return mName;
    }
    public String getConfigName(){
        return mConfigName;
    }
    public void setName(String name){
        mName=name;
    }
    public void setConfigName(String configName){
        mConfigName=configName;
    }
    public Object getTag(){
        return mTag;
    }
    public void setTag(Object obj){
        mTag=obj;
    }
    public XMLElement createElement(String tag) {
        XMLElement docEl=getDocumentElement();
        if(docEl==null){
            docEl=new XMLElement(tag);
            setDocumentElement(docEl);
            return docEl;
        }
        XMLElement baseElement=docEl.createElement(tag);
        return baseElement;
    }
    public XMLElement getDocumentElement(){
        return mDocumentElement;
    }
    public void setDocumentElement(XMLElement baseElement){
        mDocumentElement=baseElement;
        if(baseElement!=null){
            baseElement.setIndentScale(mIndentScale);
        }
    }
    private String getElementString(boolean newLineAttributes){
        XMLElement baseElement=getDocumentElement();
        if(baseElement==null){
            return null;
        }
        return baseElement.toString();
    }
    private boolean appendDocumentElement(Writer writer, boolean newLineAttributes) throws IOException {
        if(mDocumentElement==null){
            return false;
        }
        return mDocumentElement.write(writer, newLineAttributes);
    }
    private boolean appendDocumentAttribute(Writer writer) throws IOException {
        XmlHeaderElement headerElement=getHeaderElement();
        if(headerElement==null){
            return false;
        }
        return headerElement.write(writer, false);
    }
    public boolean saveAndroidResource(File file) throws IOException{
        if(file==null){
            throw new IOException("File is null");
        }
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream out=new FileOutputStream(file,false);
        return saveAndroidResource(out);
    }
    public boolean saveAndroidValuesResource(File file) throws IOException{
        if(file==null){
            throw new IOException("File is null");
        }
        File dir=file.getParentFile();
        if(!dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream out=new FileOutputStream(file,false);
        return saveAndroidValuesResource(out);
    }
    public boolean saveAndroidResource(OutputStream out) throws IOException{
        setIndent(1);
        hideComments(true);
        return save(out, true);
    }
    public boolean saveAndroidValuesResource(OutputStream out) throws IOException{
        setIndent(1);
        //hideComments(true);
        return save(out, false);
    }

    public boolean save(OutputStream out, boolean newLineAttributes) throws IOException{
        OutputStreamWriter writer=new OutputStreamWriter(out, StandardCharsets.UTF_8);
        boolean result= write(writer, newLineAttributes);
        writer.flush();
        writer.close();
        return result;
    }
    public boolean save(File file, boolean newLineAttributes) throws IOException{
        File dir=file.getParentFile();
        if(dir!=null&&!dir.exists()){
            dir.mkdirs();
        }
        setIndent(1);
        FileWriter writer=new FileWriter(file,false);
        boolean result= write(writer, newLineAttributes);
        writer.flush();
        writer.close();
        return result;
    }
    @Override
    public boolean write(Writer writer, boolean newLineAttributes) throws IOException{
        boolean has_header=appendDocumentAttribute(writer);
        if(has_header){
            writer.write(XMLUtil.NEW_LINE);
        }
        return appendDocumentElement(writer, newLineAttributes);
    }
    @Override
    public String toText(int indent, boolean newLineAttributes){
        StringWriter writer=new StringWriter();
        setIndent(indent);
        try {
            write(writer, newLineAttributes);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }

    @Override
    public String toString(){
        StringWriter strWriter=new StringWriter();
        ElementWriter writer=new ElementWriter(strWriter, XMLElement.DEBUG_TO_STRING);
        try {
            write(writer, false);
        } catch (IOException e) {
        }
        strWriter.flush();
        return strWriter.toString();
    }
    public static XMLDocument load(String text) throws XMLException {
        XMLDocumentParser parser=new XMLDocumentParser(text);
        return parser.parse();
    }
    public static XMLDocument load(InputStream in) throws XMLException {
        if(in==null){
            throw new XMLException("InputStream=null");
        }
        XMLDocumentParser parser=new XMLDocumentParser(in);
        return parser.parse();
    }
    public static XMLDocument load(File file) throws XMLException {
        XMLDocumentParser parser=new XMLDocumentParser(file);
        XMLDocument resDocument=parser.parse();
        if(resDocument!=null){
            if(resDocument.getTag()==null){
                resDocument.setTag(file);
            }
        }
        return resDocument;
    }

    public void setIndent(int indent){
        XMLElement docEle=getDocumentElement();
        if(docEle==null){
            return;
        }
        docEle.setIndent(indent);
    }

}
