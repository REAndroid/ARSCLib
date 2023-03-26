/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)
package org.xmlpull.v1;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

public interface XmlPullParser{
    String NO_NAMESPACE = "";
    int START_DOCUMENT = 0;
    int END_DOCUMENT = 1;
    int START_TAG = 2;
    int END_TAG = 3;
    int TEXT = 4;
    int CDSECT = 5;
    int ENTITY_REF = 6;
    int IGNORABLE_WHITESPACE = 7;
    int PROCESSING_INSTRUCTION = 8;
    int COMMENT = 9;
    int DOCDECL = 10;
    String [] TYPES = {
            "START_DOCUMENT",
            "END_DOCUMENT",
            "START_TAG",
            "END_TAG",
            "TEXT",
            "CDSECT",
            "ENTITY_REF",
            "IGNORABLE_WHITESPACE",
            "PROCESSING_INSTRUCTION",
            "COMMENT",
            "DOCDECL"
    };
    String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
    String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";
    String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl";
    String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation";

    void setFeature(String name, boolean state) throws XmlPullParserException;
    boolean getFeature(String name);
    void setProperty(String name, Object value) throws XmlPullParserException;
    Object getProperty(String name);
    void setInput(Reader in) throws XmlPullParserException;
    void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException;
    String getInputEncoding();
    void defineEntityReplacementText( String entityName, String replacementText ) throws XmlPullParserException;
    int getNamespaceCount(int depth) throws XmlPullParserException;
    String getNamespacePrefix(int pos) throws XmlPullParserException;
    String getNamespaceUri(int pos) throws XmlPullParserException;
    String getNamespace (String prefix);
    int getDepth();
    String getPositionDescription ();
    int getLineNumber();
    int getColumnNumber();
    boolean isWhitespace() throws XmlPullParserException;
    String getText ();
    char[] getTextCharacters(int [] holderForStartAndLength);
    String getNamespace ();
    String getName();
    String getPrefix();
    boolean isEmptyElementTag() throws XmlPullParserException;
    int getAttributeCount();
    String getAttributeNamespace (int index);
    String getAttributeName (int index);
    String getAttributePrefix(int index);
    String getAttributeType(int index);
    boolean isAttributeDefault(int index);
    String getAttributeValue(int index);
    String getAttributeValue(String namespace, String name);
    int getEventType() throws XmlPullParserException;
    int next() throws XmlPullParserException, IOException;
    int nextToken() throws XmlPullParserException, IOException;
    void require(int type, String namespace, String name) throws XmlPullParserException, IOException;
    String nextText() throws XmlPullParserException, IOException;
    int nextTag() throws XmlPullParserException, IOException;

}
