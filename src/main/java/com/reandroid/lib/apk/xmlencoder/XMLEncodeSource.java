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
package com.reandroid.lib.apk.xmlencoder;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.lib.apk.CrcOutputStream;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.xml.XMLException;
import com.reandroid.xml.source.XMLSource;

import java.io.IOException;
import java.io.OutputStream;

public class XMLEncodeSource extends ByteInputSource {
    private final EncodeMaterials encodeMaterials;
    private final XMLSource xmlSource;
    private ResXmlBlock resXmlBlock;
    public XMLEncodeSource(EncodeMaterials encodeMaterials, XMLSource xmlSource){
        super(new byte[0], xmlSource.getPath());
        this.encodeMaterials=encodeMaterials;
        this.xmlSource=xmlSource;
    }
    @Override
    public long getLength() throws IOException{
        return getResXmlBlock().countBytes();
    }
    @Override
    public long getCrc() throws IOException{
        ResXmlBlock resXmlBlock = getResXmlBlock();
        CrcOutputStream outputStream=new CrcOutputStream();
        resXmlBlock.writeBytes(outputStream);
        return outputStream.getCrcValue();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getResXmlBlock().writeBytes(outputStream);
    }
    @Override
    public byte[] getBytes() {
        try {
            return getResXmlBlock().getBytes();
        } catch (IOException ignored) {
        }
        //should not reach here
        return new byte[0];
    }
    public ResXmlBlock getResXmlBlock() throws IOException{
        if(resXmlBlock!=null){
            return resXmlBlock;
        }
        try {
            XMLFileEncoder xmlFileEncoder=new XMLFileEncoder(encodeMaterials);
            xmlFileEncoder.setCurrentPath(xmlSource.getPath());
            encodeMaterials.logVerbose("Encoding xml: "+xmlSource.getPath());
            resXmlBlock = xmlFileEncoder.encode(xmlSource.getXMLDocument());
        } catch (XMLException ex) {
            throw new EncodeException("XMLException on: '"+xmlSource.getPath()
                    +"'\n         '"+ex.getMessage()+"'");
        }
        return resXmlBlock;
    }
    @Override
    public void disposeInputSource(){
        this.xmlSource.disposeXml();
        if(this.resXmlBlock!=null){
            resXmlBlock=null;
        }
    }
}
