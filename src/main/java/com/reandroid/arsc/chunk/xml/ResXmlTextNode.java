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
import com.reandroid.json.JSONObject;
import com.reandroid.xml.XMLText;

public class ResXmlTextNode extends ResXmlNode {
    private final ResXmlText resXmlText;
    public ResXmlTextNode(ResXmlText resXmlText) {
        super(1);
        this.resXmlText = resXmlText;
        addChild(0, resXmlText);
    }
    public ResXmlTextNode() {
        this(new ResXmlText());
    }
    public ResXmlText getResXmlText() {
        return resXmlText;
    }
    public int getLineNumber(){
        return getResXmlText().getLineNumber();
    }
    public String getComment() {
        return getResXmlText().getComment();
    }
    @Override
    public int getDepth(){
        ResXmlElement parent = getParentResXmlElement();
        if(parent!=null){
            return parent.getDepth() + 1;
        }
        return 0;
    }
    @Override
    void addEvents(ParserEventList parserEventList){
        String comment = getComment();
        if(comment!=null){
            parserEventList.add(
                    new ParserEvent(ParserEvent.COMMENT, this, comment, false));
        }
        parserEventList.add(new ParserEvent(ParserEvent.TEXT, this));
    }
    public ResXmlElement getParentResXmlElement(){
        return getResXmlText().getParentResXmlElement();
    }

    public void setLineNumber(int lineNumber){
        getResXmlText().setLineNumber(lineNumber);
    }
    public String getText(){
        return getResXmlText().getText();
    }
    public void setText(String text){
        getResXmlText().setText(text);
    }
    public int getTextReference(){
        return getResXmlText().getTextReference();
    }
    public void setTextReference(int ref){
        getResXmlText().setTextReference(ref);
    }
    @Override
    void onRemoved(){
        getResXmlText().onRemoved();
    }
    @Override
    void linkStringReferences(){
        getResXmlText().linkStringReferences();
    }
    @Override
    public String toString(){
        String txt=getText();
        if(txt!=null){
            return txt;
        }
        return super.toString();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_node_type, NAME_text);
        jsonObject.put(NAME_text, getText());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setText(json.optString(NAME_text, null));
    }
    public XMLText decodeToXml() {
        XMLText xmlText=new XMLText(ValueDecoder.escapeSpecialCharacter(getText()));
        xmlText.setLineNumber(getLineNumber());
        return xmlText;
    }

    public static final String NAME_text="text";
}
