package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.chunk.ChunkType;

public class ResXmlEndNamespace extends BaseXmlChunk {
    private ResXmlStartNamespace mResXmlStartNamespace;
    public ResXmlEndNamespace() {
        super(ChunkType.XML_END_NAMESPACE, 0);
    }

    public void setResXmlStartNamespace(ResXmlStartNamespace ns){
        mResXmlStartNamespace=ns;
    }
    public ResXmlStartNamespace getResXmlStartNamespace(){
        return mResXmlStartNamespace;
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
        if(mResXmlStartNamespace!=null){
            mResXmlStartNamespace.setStringReference(ref);
        }
    }
    public int getPrefixReference(){
        return getNamespaceReference();
    }
    public void setPrefixReference(int ref){
        setNamespaceReference(ref);
        if(mResXmlStartNamespace!=null){
            mResXmlStartNamespace.setNamespaceReference(ref);
        }
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
