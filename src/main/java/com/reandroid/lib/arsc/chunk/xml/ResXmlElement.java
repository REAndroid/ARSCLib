package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.BlockList;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.container.SingleBlockContainer;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResXmlElement extends FixedBlockContainer implements JSONConvert<JSONObject> {
    private final BlockList<ResXmlStartNamespace> mStartNamespaceList;
    private final SingleBlockContainer<ResXmlStartElement> mStartElementContainer;
    private final BlockList<ResXmlElement> mBody;
    private final SingleBlockContainer<ResXmlText> mResXmlTextContainer;
    private final SingleBlockContainer<ResXmlEndElement> mEndElementContainer;
    private final BlockList<ResXmlEndNamespace> mEndNamespaceList;
    private int mDepth;
    public ResXmlElement() {
        super(6);
        this.mStartNamespaceList =new BlockList<>();
        this.mStartElementContainer=new SingleBlockContainer<>();
        this.mBody=new BlockList<>();
        this.mResXmlTextContainer=new SingleBlockContainer<>();
        this.mEndElementContainer=new SingleBlockContainer<>();
        this.mEndNamespaceList =new BlockList<>();
        addChild(0, mStartNamespaceList);
        addChild(1, mStartElementContainer);
        addChild(2, mBody);
        addChild(3, mResXmlTextContainer);
        addChild(4, mEndElementContainer);
        addChild(5, mEndNamespaceList);
    }
    @Override
    protected void onPreRefreshRefresh(){
        ResXmlStartElement start = getStartElement();
        if(start==null){
            return;
        }
        start.getResXmlAttributeArray().sortAttributes();
    }
    public ResXmlElement createChildElement(){
        return createChildElement(null);
    }
    public ResXmlElement createChildElement(String tag){
        int lineNo=getStartElement().getLineNumber()+1;
        ResXmlElement resXmlElement=new ResXmlElement();
        resXmlElement.newStartElement(lineNo);

        addElement(resXmlElement);

        if(tag!=null){
            resXmlElement.setTag(tag);
        }
        return resXmlElement;
    }
    public ResXmlAttribute createAndroidAttribute(String name, int resourceId){
        ResXmlAttribute attribute=createAttribute(name, resourceId);
        ResXmlStartNamespace ns = getOrCreateNamespace(NS_ANDROID_URI, NS_ANDROID_PREFIX);
        attribute.setNamespaceReference(ns.getUriReference());
        return attribute;
    }
    public ResXmlAttribute createAttribute(String name, int resourceId){
        ResXmlAttribute attribute=new ResXmlAttribute();
        addAttribute(attribute);
        attribute.setName(name, resourceId);
        return attribute;
    }
    public void addAttribute(ResXmlAttribute attribute){
        getStartElement().getResXmlAttributeArray().add(attribute);
    }
    public ResXmlElement getElementByTagName(String name){
        if(name==null){
            return null;
        }
        for(ResXmlElement child:listElements()){
            if(name.equals(child.getTag())||name.equals(child.getTagName())){
                return child;
            }
        }
        return null;
    }
    public List<ResXmlElement> searchElementsByTagName(String name){
        List<ResXmlElement> results=new ArrayList<>();
        if(name==null){
            return results;
        }
        for(ResXmlElement child:listElements()){
            if(name.equals(child.getTag())||name.equals(child.getTagName())){
                results.add(child);
            }
        }
        return results;
    }
    public ResXmlAttribute searchAttributeByName(String name){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeByName(name);
        }
        return null;
    }
    public ResXmlAttribute searchAttributeById(int resourceId){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.searchAttributeById(resourceId);
        }
        return null;
    }
    public void setTag(String tag){
        ResXmlStringPool pool = getStringPool();
        if(pool==null){
            return;
        }
        ensureStartEndElement();
        ResXmlStartElement start=getStartElement();
        String prefix=null;
        String name=tag;
        int i=tag.lastIndexOf(':');
        if(i>=0){
            prefix=tag.substring(0,i);
            i++;
            name=tag.substring(i);
        }
        start.setName(name);
    }
    public String getTagName(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getTagName();
        }
        return null;
    }
    public String getTag(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getName();
        }
        return null;
    }
    public String getTagUri(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getUri();
        }
        return null;
    }
    public String getTagPrefix(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getPrefix();
        }
        return null;
    }
    public Collection<ResXmlAttribute> listResXmlAttributes(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.listResXmlAttributes();
        }
        return new ArrayList<>();
    }
    public ResXmlStringPool getStringPool(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getStringPool();
            }
            if(parent instanceof ResXmlElement){
                return ((ResXmlElement)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    public ResXmlIDMap getResXmlIDMap(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getResXmlIDMap();
            }
            parent=parent.getParent();
        }
        return null;
    }

    public int getDepth(){
        return mDepth;
    }
    private void setDepth(int depth){
        mDepth=depth;
    }
    public void addElement(ResXmlElement element){
        mBody.add(element);
    }
    public int countElements(){
        return mBody.size();
    }
    public List<ResXmlElement> listElements(){
        return mBody.getChildes();
    }
    public ResXmlElement getParentResXmlElement(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof ResXmlElement){
                return (ResXmlElement)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public ResXmlStartNamespace getStartNamespaceByUriRef(int uriRef){
        if(uriRef<0){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(uriRef==ns.getUriReference()){
                return ns;
            }
        }
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUriRef(uriRef);
        }
        return null;
    }
    public ResXmlStartNamespace getOrCreateNamespace(String uri, String prefix){
        ResXmlStartNamespace exist=getStartNamespaceByUri(uri);
        if(exist!=null){
            return exist;
        }
        ResXmlStartNamespace startNamespace=new ResXmlStartNamespace();
        ResXmlEndNamespace endNamespace=new ResXmlEndNamespace();
        startNamespace.setEnd(endNamespace);

        addStartNamespace(startNamespace);
        addEndNamespace(endNamespace);

        startNamespace.setUri(uri);
        startNamespace.setPrefix(prefix);

        return startNamespace;
    }
    public ResXmlStartNamespace getStartNamespaceByUri(String uri){
        if(uri==null){
            return null;
        }
        for(ResXmlStartNamespace ns:mStartNamespaceList.getChildes()){
            if(uri.equals(ns.getUri())){
                return ns;
            }
        }
        ResXmlElement xmlElement=getParentResXmlElement();
        if(xmlElement!=null){
            return xmlElement.getStartNamespaceByUri(uri);
        }
        return null;
    }


    public List<ResXmlStartNamespace> getStartNamespaceList(){
        return mStartNamespaceList.getChildes();
    }
    public void addStartNamespace(ResXmlStartNamespace item){
        mStartNamespaceList.add(item);
    }

    public List<ResXmlEndNamespace> getEndNamespaceList(){
        return mEndNamespaceList.getChildes();
    }
    public void addEndNamespace(ResXmlEndNamespace item){
        mEndNamespaceList.add(item);
    }

    private ResXmlStartElement newStartElement(int lineNo){
        ResXmlStartElement startElement=new ResXmlStartElement();
        setStartElement(startElement);

        ResXmlEndElement endElement=new ResXmlEndElement();
        startElement.setResXmlEndElement(endElement);

        setEndElement(endElement);
        endElement.setResXmlStartElement(startElement);

        startElement.setLineNumber(lineNo);
        endElement.setLineNumber(lineNo);

        return startElement;
    }

    public ResXmlStartElement getStartElement(){
        return mStartElementContainer.getItem();
    }
    public void setStartElement(ResXmlStartElement item){
        mStartElementContainer.setItem(item);
    }

    public ResXmlEndElement getEndElement(){
        return mEndElementContainer.getItem();
    }
    public void setEndElement(ResXmlEndElement item){
        mEndElementContainer.setItem(item);
    }

    public ResXmlText getResXmlText(){
        return mResXmlTextContainer.getItem();
    }
    public void setResXmlText(ResXmlText xmlText){
        mResXmlTextContainer.setItem(xmlText);
    }
    public void setResXmlText(String text){
        if(text==null){
            mResXmlTextContainer.setItem(null);
        }else {
            ResXmlText xmlText=mResXmlTextContainer.getItem();
            if(xmlText==null){
                xmlText=new ResXmlText();
                mResXmlTextContainer.setItem(xmlText);
                ResXmlStartElement start = getStartElement();
                xmlText.setLineNumber(start.getLineNumber());
            }
            xmlText.setText(text);
        }
    }

    private boolean isBalanced(){
        return isElementBalanced() && isNamespaceBalanced();
    }
    private boolean isNamespaceBalanced(){
        return (mStartNamespaceList.size()==mEndNamespaceList.size());
    }
    private boolean isElementBalanced(){
        return (hasStartElement() && hasEndElement());
    }
    private boolean hasStartElement(){
        return mStartElementContainer.hasItem();
    }
    private boolean hasEndElement(){
        return mEndElementContainer.hasItem();
    }

    private void linkStartEnd(){
        linkStartEndElement();
        linkStartEndNameSpaces();
    }
    private void linkStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start==null || end==null){
            return;
        }
        start.setResXmlEndElement(end);
        end.setResXmlStartElement(start);
    }
    private void ensureStartEndElement(){
        ResXmlStartElement start=getStartElement();
        ResXmlEndElement end=getEndElement();
        if(start!=null && end!=null){
            return;
        }
        if(start==null){
            start=new ResXmlStartElement();
            setStartElement(start);
        }
        if(end==null){
            end=new ResXmlEndElement();
            setEndElement(end);
        }
        linkStartEndElement();
    }
    private void linkStartEndNameSpaces(){
        if(!isNamespaceBalanced()){
            return;
        }
        int max=mStartNamespaceList.size();
        for(int i=0;i<max;i++){
            ResXmlStartNamespace start=mStartNamespaceList.get(i);
            ResXmlEndNamespace end=mEndNamespaceList.get(max-i-1);
            start.setEnd(end);
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        if(isBalanced()){
            return;
        }
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==null){
            unknownChunk(reader, headerBlock);
            return;
        }
        if(chunkType==ChunkType.XML_START_ELEMENT){
            onStartElement(reader);
        }else if(chunkType==ChunkType.XML_END_ELEMENT){
            onEndElement(reader);
        }else if(chunkType==ChunkType.XML_START_NAMESPACE){
            onStartNamespace(reader);
        }else if(chunkType==ChunkType.XML_END_NAMESPACE){
            onEndNamespace(reader);
        }else if(chunkType==ChunkType.XML_CDATA){
            onXmlText(reader);
        }else{
            unexpectedChunk(reader, headerBlock);
        }
        if(!isBalanced()){
            if(!reader.isAvailable()){
                unBalancedFinish(reader);
            }else {
                readBytes(reader);
                return;
            }
        }
        linkStartEnd();
        onFinishedRead(reader, headerBlock);
    }
    private void onFinishedRead(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        int avail=reader.available();
        if(avail>0 && getDepth()==0){
            onFinishedUnexpected(reader);
            return;
        }
        onFinishedSuccess(reader, headerBlock);
    }
    private void onFinishedSuccess(BlockReader reader, HeaderBlock headerBlock) throws IOException{

    }
    private void onFinishedUnexpected(BlockReader reader) throws IOException{
        StringBuilder builder=new StringBuilder();
        builder.append("Unexpected finish reading: reader=").append(reader.toString());
        HeaderBlock header = reader.readHeaderBlock();
        if(header!=null){
            builder.append(", next header=");
            builder.append(header.toString());
        }
        throw new IOException(builder.toString());
    }
    private void onStartElement(BlockReader reader) throws IOException{
        if(hasStartElement()){
            ResXmlElement childElement=new ResXmlElement();
            addElement(childElement);
            childElement.setDepth(getDepth()+1);
            childElement.readBytes(reader);
        }else{
            ResXmlStartElement startElement=new ResXmlStartElement();
            setStartElement(startElement);
            startElement.readBytes(reader);
        }
    }
    private void onEndElement(BlockReader reader) throws IOException{
        if(hasEndElement()){
            multipleEndElement(reader);
            return;
        }
        ResXmlEndElement endElement=new ResXmlEndElement();
        setEndElement(endElement);
        endElement.readBytes(reader);
    }
    private void onStartNamespace(BlockReader reader) throws IOException{
        ResXmlStartNamespace startNamespace=new ResXmlStartNamespace();
        addStartNamespace(startNamespace);
        startNamespace.readBytes(reader);
    }
    private void onEndNamespace(BlockReader reader) throws IOException{
        ResXmlEndNamespace endNamespace=new ResXmlEndNamespace();
        addEndNamespace(endNamespace);
        endNamespace.readBytes(reader);
    }
    private void onXmlText(BlockReader reader) throws IOException{
        ResXmlText xmlText=new ResXmlText();
        setResXmlText(xmlText);
        xmlText.readBytes(reader);
    }

    private void unknownChunk(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unknown chunk: "+headerBlock.toString());
    }
    private void multipleEndElement(BlockReader reader) throws IOException{
        throw new IOException("Multiple end element: "+reader.toString());
    }
    private void unexpectedChunk(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected chunk: "+headerBlock.toString());
    }
    private void unBalancedFinish(BlockReader reader) throws IOException{
        if(!isNamespaceBalanced()){
            throw new IOException("Unbalanced namespace: start="
                    +mStartNamespaceList.size()+", end="+mEndNamespaceList.size());
        }

        if(!isElementBalanced()){
            // Should not happen unless corrupted file, auto corrected above
            StringBuilder builder=new StringBuilder();
            builder.append("Unbalanced element: start=");
            ResXmlStartElement startElement=getStartElement();
            if(startElement!=null){
                builder.append(startElement);
            }else {
                builder.append("null");
            }
            builder.append(", end=");
            ResXmlEndElement endElement=getEndElement();
            if(endElement!=null){
                builder.append(endElement);
            }else {
                builder.append("null");
            }
            throw new IOException(builder.toString());
        }
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        ResXmlStartElement start = getStartElement();
        jsonObject.put(NAME_line, start.getLineNumber());
        int i=0;
        JSONArray nsList=new JSONArray();
        for(ResXmlStartNamespace namespace:getStartNamespaceList()){
            JSONObject ns=new JSONObject();
            ns.put(NAME_namespace_uri, namespace.getUri());
            ns.put(NAME_namespace_prefix, namespace.getPrefix());
            nsList.put(i, ns);
            i++;
        }
        if(i>0){
            jsonObject.put(NAME_namespaces, nsList);
        }
        jsonObject.put(NAME_name, start.getName());
        String comment=start.getComment();
        if(comment!=null){
            jsonObject.put(NAME_comment, comment);
        }
        ResXmlText xmlText=getResXmlText();
        if(xmlText!=null){
            String text=xmlText.getText();
            if(text!=null){
                jsonObject.put(NAME_text, text);
            }
        }
        String uri=start.getUri();
        if(uri!=null){
            jsonObject.put(NAME_namespace_uri, uri);
        }
        JSONArray attrArray=start.getResXmlAttributeArray().toJson();
        jsonObject.put(NAME_attributes, attrArray);
        i=0;
        JSONArray childes=new JSONArray();
        for(ResXmlElement element:listElements()){
            childes.put(i, element.toJson());
            i++;
        }
        if(i>0){
            jsonObject.put(NAME_childes, childes);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        ResXmlStartElement start = getStartElement();
        int lineNo=json.optInt(NAME_line, 1);
        if(start==null){
            start = newStartElement(lineNo);
        }else {
            start.setLineNumber(lineNo);
        }
        JSONArray nsArray = json.optJSONArray(NAME_namespaces);
        if(nsArray!=null){
            int length=nsArray.length();
            for(int i=0;i<length;i++){
                JSONObject nsObject=nsArray.getJSONObject(i);
                String uri=nsObject.getString(NAME_namespace_uri);
                String prefix=nsObject.getString(NAME_namespace_prefix);
                getOrCreateNamespace(uri,prefix);
            }
        }
        setTag(json.getString(NAME_name));
        start.setComment(json.optString(NAME_comment, null));
        String text= json.optString(NAME_text, null);
        if(text!=null){
            setResXmlText(text);
        }
        String uri = json.optString(NAME_namespace_uri, null);
        if(uri!=null){
            ResXmlStartNamespace ns = getStartNamespaceByUri(uri);
            if(ns==null){
                throw new IllegalArgumentException("Undefined uri: "
                        +uri+", must be defined in array: "+NAME_namespaces);
            }
            start.setNamespaceReference(ns.getUriReference());
        }
        JSONArray attributes = json.optJSONArray(NAME_attributes);
        if(attributes!=null){
            start.getResXmlAttributeArray().fromJson(attributes);
        }
        JSONArray childArray= json.optJSONArray(NAME_childes);
        if(childArray!=null){
            int length=childArray.length();
            for(int i=0;i<length;i++){
                JSONObject childObject=childArray.getJSONObject(i);
                ResXmlElement child = createChildElement();
                child.fromJson(childObject);
            }
        }
    }
    @Override
    public String toString(){
        ResXmlStartElement start = getStartElement();
        if(start!=null){
            ResXmlText text=getResXmlText();
            StringBuilder builder=new StringBuilder();
            builder.append("<");
            builder.append(start.toString());
            if(text!=null){
                builder.append(">");
                builder.append(text.toString());
                builder.append("</");
                builder.append(start.getTagName());
                builder.append(">");
            }else {
                builder.append("/>");
            }
            return builder.toString();
        }
        return "NULL";
    }
    static ResXmlElement newResXmlElement(String tag){
        ResXmlElement resXmlElement=new ResXmlElement();
        ResXmlStartElement startElement=new ResXmlStartElement();
        resXmlElement.setStartElement(startElement);
        ResXmlEndElement endElement=new ResXmlEndElement();
        resXmlElement.setEndElement(endElement);
        resXmlElement.setTag(tag);
        return resXmlElement;
    }

    public static final String NS_ANDROID_URI = "http://schemas.android.com/apk/res/android";
    public static final String NS_ANDROID_PREFIX = "android";

    static final String NAME_name = "name";
    static final String NAME_comment = "comment";
    static final String NAME_text = "text";
    static final String NAME_namespaces = "namespaces";
    static final String NAME_namespace_uri = "namespace_uri";
    static final String NAME_namespace_prefix = "namespace_prefix";
    private static final String NAME_line = "line";
    static final String NAME_attributes = "attributes";
    static final String NAME_childes = "childes";
}
