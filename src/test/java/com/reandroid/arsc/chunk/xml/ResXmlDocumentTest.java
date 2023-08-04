package com.reandroid.arsc.chunk.xml;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceLibrary;
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
    public void testXmlNodes(){
        ResXmlDocument document = new ResXmlDocument();
        ResXmlElement root = document.getOrCreateElement("manifest");
        ResXmlElement child = root.createChildElement("child");
        child.setTagNamespace(ResourceLibrary.URI_RES_AUTO, "prefix");

        Assert.assertEquals("prefix:child", child.getName(true));

        child.setTagNamespace(null, null);

        Assert.assertEquals("child", child.getName(true));

        String text = "Xml text node";
        child.addResXmlText(text);

        Assert.assertEquals(1, child.listXmlTextNodes().size());
        Assert.assertEquals(text, child.listXmlTextNodes().get(0).getText());

        child.removeNode(child.listXmlTextNodes().get(0));

        Assert.assertEquals(0, child.listXmlTextNodes().size());
        Assert.assertEquals(0, child.listXmlNodes().size());

        root.createChildElement("child");
        root.createChildElement("child-2");

        Assert.assertEquals(3, root.listElements().size());
        Assert.assertEquals(2, root.listElements("child").size());
        Assert.assertEquals(1, root.listElements("child-2").size());
        Assert.assertEquals(0, root.listElements("child-3").size());
        Assert.assertEquals(3, root.listXmlNodes().size());

        Assert.assertNotNull("Element not found <child>", root.getElementByTagName("child"));
        root.removeNode(root.getElementByTagName("child"));
        Assert.assertEquals(1, root.listElements("child").size());
        root.clearChildes();
        Assert.assertEquals("Child nodes cleared", 0, root.listXmlNodes().size());
    }
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

        attribute = root.getOrCreateAttribute(
                ResourceLibrary.URI_RES_AUTO,
                ResourceLibrary.PREFIX_APP,
                "attr_1", 0x7f010000);
        attribute.setValueAsBoolean(true);

        Assert.assertNotNull(root.searchAttributeByResourceId(0x7f010000));
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

        attribute = root.getOrCreateAttribute(
                ResourceLibrary.URI_RES_AUTO,
                ResourceLibrary.PREFIX_APP,
                "attr_1", 0x7f010000);
        attribute.setValueAsBoolean(true);

        root.removeAttribute(attribute);

        Assert.assertNull(root.searchAttributeByResourceId(0x7f010000));

        attribute = root.getOrCreateAttribute(
                ResourceLibrary.URI_ANDROID,
                ResourceLibrary.PREFIX_ANDROID,
                "attr_1", 0x7f010000);
        attribute.setValueAsBoolean(true);

        attribute = root.getOrCreateAttribute(
                ResourceLibrary.URI_RES_AUTO,
                ResourceLibrary.PREFIX_APP,
                "attr_2", 0x7f010000);
        attribute.setValueAsBoolean(true);

        attribute = root.searchAttributeByResourceId(0x7f010000);

        root.removeAttribute(attribute);
        Assert.assertEquals("Attribute count", 0, root.getAttributeCount());
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
        Assert.assertEquals(2, root.getNamespaceCount());
        Assert.assertEquals(12, root.getAttributeCount());

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
            "<manifest android:configChanges=\"keyboardHidden|orientation|screenSize\"\n" +
            "          android:title=\"60%\"\n" +
            "          android:versionCode=\"1\"\n"+
            "          android:versionName=\"1.0\"\n" +
            "          android:compileSdkVersion=\"32\"\n" +
            "          android:compileSdkVersionCodename=\"12\"\n" +
            "          app:r0x7f0501ab=\"0x00000005\"\n" +
            "          package=\"com.example.package\"\n" +
            "          platformBuildVersionCode=\"32\"\n" +
            "          platformBuildVersionName=\"12\"\n" +
            "          platformBuildVersionName=\"duplicate\"\n" +
            "          style=\"@android:style/Widget\"" +
            "          xmlns:app=\"http://schemas.android.com/apk/res-auto\""+
            "          xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
            "  <uses-sdk android:minSdkVersion=\"21\"\n" +
            "            android:targetSdkVersion=\"32\" />\n" +
            "  <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\"\n" +
            "                   android:maxSdkVersion=\"28\" />\n" +
            "  <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" />\n" +
            "</manifest>";
}
