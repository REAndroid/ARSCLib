package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;

public class ResXmlStartNamespace extends BaseXmlChunk {
    private ResXmlEndNamespace mResXmlEndNamespace;
    public ResXmlStartNamespace() {
        super(ChunkType.XML_START_NAMESPACE, 0);
    }
    public void setResXmlEndNamespace(ResXmlEndNamespace ns){
        mResXmlEndNamespace=ns;
    }

    @Override
    public String getUri(){
        return getString(getUriReference());
    }
    public String getPrefix(){
        return getString(getPrefixReference());
    }
    public int getUriReference(){
        return getStringReference();
    }
    public void setUriReference(int ref){
        setStringReference(ref);
        if(mResXmlEndNamespace!=null){
            mResXmlEndNamespace.setStringReference(ref);
        }
    }
    public int getPrefixReference(){
        return getNamespaceReference();
    }
    public void setPrefixReference(int ref){
        setNamespaceReference(ref);
        if(mResXmlEndNamespace!=null){
            mResXmlEndNamespace.setNamespaceReference(ref);
        }
    }

    public ResXmlEndNamespace getResXmlEndNamespace(){
        return mResXmlEndNamespace;
    }
    @Override
    public String toString(){
        String uri=getUri();
        if(uri==null){
            return super.toString();
        }
        return "xmlns:"+getPrefix()+"=\""+getUri()+"\"";
    }
}
