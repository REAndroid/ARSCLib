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
package com.reandroid.apk.xmldecoder;

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.xml.XMLFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class XMLEntryDecoderSerializer extends XMLEntryDecoder<XmlSerializer> implements Closeable {
    private final EntryWriterSerializer entryWriterSerializer;
    private Closeable mClosable;
    private boolean mStart;

    public XMLEntryDecoderSerializer(XmlSerializer serializer) {
        super();
        this.entryWriterSerializer = new EntryWriterSerializer(serializer);
    }
    public XMLEntryDecoderSerializer() {
        this(XMLFactory.newSerializer());
    }

    public int decode(File resDirectory, SpecTypePair specTypePair) throws IOException {
        return decodeUniqueConfigs(resDirectory, specTypePair);
    }
    private int decodeUniqueConfigs(File resDirectory, SpecTypePair specTypePair) throws IOException {
        int total = 0;
        Iterator<TypeBlock> itr = specTypePair.iteratorNonEmpty();
        while (itr.hasNext()){
            TypeBlock typeBlock = itr.next();
            File outXml = toOutXmlFile(resDirectory, typeBlock);
            total += decode(outXml, typeBlock);
        }
        return total;
    }
    public int decode(File outXmlFile, TypeBlock typeBlock) throws IOException {
        setOutput(outXmlFile);
        int count = super.decode(entryWriterSerializer, typeBlock);
        close();
        deleteIfZero(count, outXmlFile);
        return count;
    }
    public int decode(TypeBlock typeBlock) throws IOException {
        return super.decode(entryWriterSerializer, typeBlock);
    }
    public void setOutput(File file) throws IOException {
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        setOutput(new FileOutputStream(file));
    }
    public void setOutput(OutputStream outputStream) throws IOException {
        close();
        getXmlSerializer().setOutput(outputStream, StandardCharsets.UTF_8.name());
        this.mClosable = outputStream;
        start();
    }
    public void setOutput(Writer writer) throws IOException {
        close();
        getXmlSerializer().setOutput(writer);
        this.mClosable = writer;
        start();
    }

    private void start() throws IOException {
        if(!mStart){
            XmlSerializer xmlSerializer = getXmlSerializer();
            xmlSerializer.startDocument("utf-8", null);
            XmlHelper.setIndent(xmlSerializer, true);
            xmlSerializer.startTag(null, XmlHelper.RESOURCES_TAG);
            mStart = true;
        }
    }
    private void end() throws IOException {
        if(mStart){
            XmlSerializer xmlSerializer = getXmlSerializer();
            XmlHelper.setIndent(xmlSerializer, true);
            xmlSerializer.endTag(null, XmlHelper.RESOURCES_TAG);
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            mStart = false;
        }
    }
    private XmlSerializer getXmlSerializer(){
        return entryWriterSerializer.getXmlSerializer();
    }

    @Override
    public void close() throws IOException {
        Closeable closeable = this.mClosable;
        end();
        if(closeable != null){
            closeable.close();
        }
        this.mClosable = null;
    }

}
