package com.reandroid.xml;

import com.reandroid.app.AndroidManifest;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XMLPathTest {

    @Test
    public void testNewElement() {
        XMLPath xmlPath = XMLPath.newElement("manifest");
        Assert.assertNotNull(xmlPath);
        Assert.assertEquals("/manifest", xmlPath.getPath());
        Assert.assertNull("parent", xmlPath.getParent());
        XMLPath.InvalidPathException exception = null;
        try {
            XMLPath.newElement("/manifest");
        } catch (XMLPath.InvalidPathException e) {
            exception = e;
        }
        Assert.assertNotNull("InvalidPathException", exception);
    }

    @Test
    public void testChildPath() {
        XMLPath xmlPath = XMLPath
                .newElement("manifest")
                .element("application");
        Assert.assertNotNull(xmlPath);
        Assert.assertEquals("/manifest/application", xmlPath.getPath());
        Assert.assertNotNull("parent", xmlPath.getParent());
        Assert.assertEquals("/manifest", xmlPath.getParent().getPath());

        xmlPath = xmlPath.attribute("name");

        Assert.assertEquals("/manifest/application;name", xmlPath.getPath());
        XMLPath.InvalidPathException exception = null;
        try {
            xmlPath.element("element");
        } catch (XMLPath.InvalidPathException e) {
            exception = e;
        }
        Assert.assertNotNull("InvalidPathException", exception);

        xmlPath = xmlPath.changeName("label");
        Assert.assertEquals("/manifest/application;label", xmlPath.getPath());

        XMLPath xmlPath2 = XMLPath.compile(xmlPath.getPath());
        Assert.assertEquals("Compiled path", xmlPath, xmlPath2);
    }

    @Test
    public void testWithXMLDocument() {
        XMLDocument document = createTestXMLDocument();

        XMLElement manifest = XMLPath
                .compile("/manifest")
                .findFirst(document);
        Assert.assertNotNull(manifest);
        Assert.assertEquals("manifest", manifest.getName());

        XMLElement application = XMLPath
                .compile("/manifest/application")
                .findFirst(document);
        Assert.assertNotNull(application);
        Assert.assertEquals("application", application.getName());

        XMLElement activity = XMLPath
                .compile("/manifest/application/activity")
                .findFirst(document);
        Assert.assertNotNull(activity);
        Assert.assertEquals("activity", activity.getName());

        XMLAttribute activityName = XMLPath
                .compile("/manifest/application/activity;name")
                .findFirst(document);
        Assert.assertNotNull(activityName);
        Assert.assertEquals("name", activityName.getName());

        List<XMLAttribute> applicationAttributeList = XMLPath
                .compile("/manifest/application")
                .attribute(XMLPath.ANY_NAME)
                .list(document);
        Assert.assertEquals("applicationAttributeList",
                2, applicationAttributeList.size());

        List<XMLElement> applicationElementList = XMLPath
                .compile("/manifest/application/*")
                .list(document);
        Assert.assertEquals("applicationElementList",
                3, applicationElementList.size());

        List<XMLAttribute> applicationElementsNameAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute("name")
                .list(document);
        Assert.assertEquals("applicationElementsNameAttributeList",
                3, applicationElementsNameAttributeList.size());

        List<XMLAttribute> applicationElementsLabelAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute("label")
                .list(document);
        Assert.assertEquals("applicationElementsLabelAttributeList",
                1, applicationElementsLabelAttributeList.size());

    }
    @Test
    public void testWithResXmlDocument() {
        ResXmlDocument document = createTestResXmlDocument();

        ResXmlElement manifest = XMLPath
                .compile("/manifest").findFirst(document);
        Assert.assertNotNull(manifest);
        Assert.assertEquals("manifest", manifest.getName());

        ResXmlElement application = XMLPath
                .compile("/manifest/application")
                .findFirst(document);
        Assert.assertNotNull(application);
        Assert.assertEquals("application", application.getName());

        ResXmlElement activity = XMLPath
                .compile("/manifest/application/activity")
                .findFirst(document);
        Assert.assertNotNull(activity);
        Assert.assertEquals("activity", activity.getName());

        ResXmlAttribute activityName = XMLPath
                .compile("/manifest/application/activity")
                .attribute(AndroidManifest.ID_name)
                .findFirst(document);
        Assert.assertNotNull(activityName);
        Assert.assertEquals("name", activityName.getName());

        List<ResXmlAttribute> applicationAttributeList = XMLPath
                .compile("/manifest/application")
                .attribute(XMLPath.ANY_NAME)
                .list(document);
        Assert.assertEquals("applicationAttributeList",
                2, applicationAttributeList.size());

        List<ResXmlElement> applicationElementList = XMLPath
                .compile("/manifest/application/*")
                .list(document);
        Assert.assertEquals("applicationElementList",
                3, applicationElementList.size());

        List<ResXmlAttribute> applicationElementsNameAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute(AndroidManifest.ID_name)
                .list(document);
        Assert.assertEquals("applicationElementsNameAttributeList",
                3, applicationElementsNameAttributeList.size());

        List<ResXmlAttribute> applicationElementsLabelAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute(AndroidManifest.ID_label)
                .list(document);
        Assert.assertEquals("applicationElementsLabelAttributeList",
                1, applicationElementsLabelAttributeList.size());

        List<ResXmlAttribute> applicationElementsLabelAttributeList2 = XMLPath
                .compile("/manifest/application/*;@0x1010001")
                .list(document);
        Assert.assertEquals("applicationElementsLabelAttributeList2",
                1, applicationElementsLabelAttributeList2.size());
    }

    private static XMLDocument createTestXMLDocument() {
        XMLDocument document = new XMLDocument("manifest");
        document.setEncoding("utf-8");
        XMLElement manifest = document.getDocumentElement();
        XMLElement application = manifest.newElement("application");

        application.addAttribute("name", "com.TestApp");
        application.addAttribute("label", "My App");

        XMLElement activity = application.newElement("activity");
        activity.addAttribute("name", "com.TestActivity");
        activity.addAttribute("label", "Test Activity");

        XMLElement service = application.newElement("service");
        service.addAttribute("name", "com.TestService");

        XMLElement metaData = application.newElement("meta-data");
        metaData.addAttribute("name", "com-meta-data");

        return document;
    }

    private static ResXmlDocument createTestResXmlDocument() {
        ResXmlDocument document = new ResXmlDocument();
        document.setEncoding("utf-8");
        ResXmlElement manifest = document.getDocumentElement();
        manifest.setName("manifest");
        ResXmlElement application = manifest.newElement("application");

        application.getOrCreateAndroidAttribute(AndroidManifest.NAME_name, AndroidManifest.ID_name)
                .setValueAsString("com.TestApp");
        application.getOrCreateAndroidAttribute(AndroidManifest.NAME_label, AndroidManifest.ID_label)
                .setValueAsString("My App");

        ResXmlElement activity = application.newElement("activity");
        activity.getOrCreateAndroidAttribute(AndroidManifest.NAME_name, AndroidManifest.ID_name)
                .setValueAsString("com.TestActivity");
        activity.getOrCreateAndroidAttribute(AndroidManifest.NAME_label, AndroidManifest.ID_label)
                .setValueAsString("Test Activity");

        ResXmlElement service = application.newElement("service");
        service.getOrCreateAndroidAttribute(AndroidManifest.NAME_name, AndroidManifest.ID_name)
                .setValueAsString("com.TestService");

        ResXmlElement metaData = application.newElement("meta-data");
        metaData.getOrCreateAndroidAttribute(AndroidManifest.NAME_name, AndroidManifest.ID_name)
                .setValueAsString("com-meta-data");

        return document;
    }
}
