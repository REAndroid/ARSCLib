package com.reandroid.lib.arsc.decoder.xml;

import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.chunk.xml.ResXmlText;
import com.reandroid.lib.arsc.decoder.ResDecoder;
import com.reandroid.lib.arsc.decoder.ResourceNameStore;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.ValueType;

import java.util.Collection;
import java.util.List;

public class ResXmlDecoder extends ResDecoder<ResXmlBlock, String> {
    private byte mCurrentPackageId;
    public ResXmlDecoder(ResourceNameStore store){
        super(store);
    }
    private String decodeToString(ResXmlBlock resXmlBlock){
        StringBuilder builder=new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        builder.append("\n");
        String body=decodeToString(resXmlBlock.getResXmlElement());
        builder.append(body);
        return builder.toString();
    }
    private String decodeToString(ResXmlElement resXmlElement){
        if(resXmlElement==null){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        for(int i=0;i<resXmlElement.getDepth();i++){
            builder.append(' ');
        }
        builder.append('<');
        String tagName=resXmlElement.getTagName();
        builder.append(tagName);
        for(ResXmlAttribute attribute:resXmlElement.listResXmlAttributes()){
            builder.append(' ');
            String name=decodeAttributeFullName(attribute);
            builder.append(name);
            builder.append("=\"");
            String value=decodeAttributeValue(attribute);
            builder.append(value);
            builder.append("\"");
        }
        boolean useEndTag=false;
        ResXmlText resXmlText=resXmlElement.getResXmlText();
        String text=null;
        if(resXmlText!=null){
            useEndTag=true;
            text=resXmlText.getText();
        }
        List<ResXmlElement> childElements=resXmlElement.listElements();
        if(!useEndTag){
            useEndTag=childElements.size()>0;
        }
        if(!useEndTag){
            builder.append("/>");
            return builder.toString();
        }
        builder.append(">");
        if(text!=null){
            builder.append(text);
        }
        for(ResXmlElement child:childElements){
            builder.append("\n");
            String txtChild=decodeToString(child);
            builder.append(txtChild);
        }
        if(childElements.size()>0){
            builder.append("\n");
            for(int i=0;i<resXmlElement.getDepth();i++){
                builder.append(' ');
            }
        }
        builder.append("</");
        builder.append(tagName);
        builder.append(">");
        return builder.toString();
    }
    private String decodeAttributeValue(ResXmlAttribute attribute){
        ValueType valueType=attribute.getValueType();
        if(valueType==ValueType.FIRST_INT || valueType==ValueType.INT_HEX){
            int nameId=attribute.getNameResourceID();
            String val=decodeAttribute(nameId, attribute.getRawValue());
            if(val!=null){
                return val;
            }
        }
        if(valueType==ValueType.REFERENCE){
            int id=attribute.getRawValue();
            byte pkgId= (byte) ((id>>24)&0xFF);
            return getResourceFullName(id, pkgId!=getCurrentPackageId());
        }
        if(valueType==ValueType.STRING){
            return attribute.getValueString();
        }
        String val= ValueDecoder.decode(valueType, attribute.getRawValue());
        if(val!=null){
            return val;
        }
        return attribute.getRawValue()+"["+valueType+"]";
    }
    private String decodeAttributeFullName(ResXmlAttribute attribute){
        StringBuilder builder=new StringBuilder();
        String prefix=getRealAttributeNamePrefix(attribute);
        String name=getRealAttributeName(attribute);
        if(prefix!=null&&!name.contains(":")){
            builder.append(prefix);
            builder.append(':');
        }
        builder.append(name);
        return builder.toString();
    }
    private String getRealAttributeNamePrefix(ResXmlAttribute attribute){
        /* TODO readjust wrong/stripped attribute name prefix prefix; */
        return attribute.getNamePrefix();
    }
    private String getRealAttributeName(ResXmlAttribute attribute){
        int nameId=attribute.getNameResourceID();
        ResourceNameStore store=getResourceNameStore();
        byte pkgId= (byte) ((nameId>>24)&0xFF);
        String name=store.getResourceName(nameId, getCurrentPackageId()!=pkgId);
        if(name!=null){
            return name;
        }
        return attribute.getName();
    }
    private byte getCurrentPackageId(){
        return mCurrentPackageId;
    }
    @Override
    public String decode(byte currentPackageId, ResXmlBlock resXmlBlock) {
        this.mCurrentPackageId=currentPackageId;
        return decodeToString(resXmlBlock);
    }
}
