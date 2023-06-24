package com.reandroid.apk.xmlencoder;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.CrcOutputStream;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.util.IOUtil;
import com.reandroid.xml.source.XMLParserSource;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStream;

public class XMLParseEncodeSource extends ByteInputSource {
    private final PackageBlock packageBlock;
    private final XMLParserSource parserSource;
    private ResXmlDocument mResXmlDocument;
    private APKLogger mLogger;

    public XMLParseEncodeSource(PackageBlock packageBlock, XMLParserSource parserSource) {
        super(new byte[0], parserSource.getPath());
        this.packageBlock = packageBlock;
        this.parserSource = parserSource;
    }
    @Override
    public long getLength() throws IOException {
        return getResXmlDocument().countBytes();
    }
    @Override
    public long getCrc() throws IOException{
        ResXmlDocument resXmlDocument = getResXmlDocument();
        CrcOutputStream outputStream=new CrcOutputStream();
        resXmlDocument.writeBytes(outputStream);
        return outputStream.getCrcValue();
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getResXmlDocument().writeBytes(outputStream);
    }
    @Override
    public byte[] getBytes() {
        try {
            return getResXmlDocument().getBytes();
        } catch (IOException ignored) {
        }
        //should not reach here
        return new byte[0];
    }
    @Override
    public void disposeInputSource(){
        mResXmlDocument = null;
    }

    public PackageBlock getPackageBlock() {
        return packageBlock;
    }
    public XMLParserSource getParserSource() {
        return parserSource;
    }
    public ResXmlDocument getResXmlDocument() throws IOException{
        if(mResXmlDocument == null){
            try {
                mResXmlDocument = encode();
            } catch (XmlPullParserException ex) {
                throw new IOException(ex.getMessage());
            }
        }
        return mResXmlDocument;
    }
    private ResXmlDocument encode() throws XmlPullParserException, IOException {
        logVerbose("Encoding: " + getParserSource().getPath());
        XmlPullParser parser = getParserSource().getParser();
        ResXmlDocument resXmlDocument = new ResXmlDocument();
        resXmlDocument.setPackageBlock(getPackageBlock());
        resXmlDocument.parse(parser);
        IOUtil.close(parser);
        return resXmlDocument;
    }
    public void setApkLogger(APKLogger logger){
        this.mLogger = logger;
    }
    private void logVerbose(String msg){
        APKLogger logger = this.mLogger;
        if(logger != null){
            logger.logVerbose(msg);
        }
    }
}
