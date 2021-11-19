package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.BlockList;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.container.SingleBlockContainer;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResXmlElement extends FixedBlockContainer {
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
    public String getTagName(){
        ResXmlStartElement startElement=getStartElement();
        if(startElement!=null){
            return startElement.getTagName();
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
    private void linkStartEndNameSpaces(){
        if(!isNamespaceBalanced()){
            return;
        }
        int max=mStartNamespaceList.size();
        for(int i=0;i<max;i++){
            ResXmlStartNamespace start=mStartNamespaceList.get(i);
            ResXmlEndNamespace end=mEndNamespaceList.get(max-i-1);
            start.setResXmlEndNamespace(end);
            end.setResXmlStartNamespace(start);
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
            }
            return;
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
        throw new IOException("Unexpected finish reading: "+reader.toString());
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
            throw new IOException("Unbalanced element: hasStart="
                    +hasStartElement()+", hasEnd="+hasEndElement());
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
}
