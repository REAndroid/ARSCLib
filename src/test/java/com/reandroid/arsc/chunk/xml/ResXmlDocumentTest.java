package com.reandroid.arsc.chunk.xml;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.xml.XMLFactory;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;

public class ResXmlDocumentTest {
    @Test
    public void testAddAttribute(){
        ResXmlDocument document = new ResXmlDocument();
        ResXmlElement root = document.getOrCreateElement("manifest");
        ResXmlAttribute attribute = root.getOrCreateAttribute(
                        null,
                        null,
                        "package", 0);
        attribute.setValueAsString("com.example.package");
        Assert.assertNotNull(root.searchAttributeByName("package"));
    }
    @Test
    public void testRemoveAttribute(){
        ResXmlDocument document = new ResXmlDocument();
        ResXmlElement root = document.getOrCreateElement("manifest");
        ResXmlAttribute attribute = root.getOrCreateAttribute(
                null,
                null,
                "package", 0);
        attribute.setValueAsString("com.example.package");
        root.removeAttribute(attribute);
        Assert.assertNull(root.searchAttributeByName("package"));
    }
    @Test
    public void testEncodeDecodeXml() throws XmlPullParserException, IOException {
        ResXmlDocument document = new ResXmlDocument();
        document.setPackageBlock(createDummy().pickOne());

        XmlPullParser parser = XMLFactory.newPullParser(XML_STRING);
        document.parse(parser);

        ResXmlElement root = document.getResXmlElement();
        Assert.assertNotNull(root);
        Assert.assertEquals(3, root.countElements());
        Assert.assertEquals(1, root.getNamespaceCount());
        Assert.assertEquals(8, root.getAttributeCount());

        ResXmlAttribute attribute = root.searchAttributeByName("style");
        Assert.assertNotNull(attribute);

        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        document.serialize(serializer);
        writer.close();

        String org = XML_STRING;
        String decoded = writer.toString();
        org = org.replaceAll("\\s+", "");
        decoded = decoded.replaceAll("\\s+", "");
        Assert.assertEquals(org, decoded);
    }
    private static TableBlock createDummy() throws IOException {
        TableBlock tableBlock = new TableBlock();
        tableBlock.newPackage(0x7f, "com.example.package");
        tableBlock.refresh();
        tableBlock.addFramework(AndroidFrameworks.getLatest().getTableBlock());
        return tableBlock;
    }
    private static final String XML_STRING = "<?xml version='1.0' encoding='utf-8' ?>\n" +
            "<manifest android:versionCode=\"1\"\n" +
            "          android:versionName=\"1.0\"\n" +
            "          android:compileSdkVersion=\"32\"\n" +
            "          android:compileSdkVersionCodename=\"12\"\n" +
            "          package=\"com.example.package\"\n" +
            "          platformBuildVersionCode=\"32\"\n" +
            "          platformBuildVersionName=\"12\"\n" +
            "          style=\"@android:style/Widget\" xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
            "  <uses-sdk android:minSdkVersion=\"21\"\n" +
            "            android:targetSdkVersion=\"32\" />\n" +
            "  <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"\n" +
            "                   android:maxSdkVersion=\"28\" />\n" +
            "  <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" />\n" +
            "</manifest>";
}
