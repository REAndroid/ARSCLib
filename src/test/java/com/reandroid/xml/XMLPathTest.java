package com.reandroid.xml;

import com.reandroid.app.AndroidManifest;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
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
                5, applicationAttributeList.size());

        List<XMLElement> applicationElementList = XMLPath
                .compile("/manifest/application/*")
                .list(document);
        Assert.assertEquals("applicationElementList",
                10, applicationElementList.size());

        List<XMLAttribute> applicationElementsNameAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute("name")
                .list(document);
        Assert.assertEquals("applicationElementsNameAttributeList",
                10, applicationElementsNameAttributeList.size());

        List<XMLAttribute> applicationElementsLabelAttributeList = XMLPath
                .compile("/manifest/application")
                .element(XMLPath.ANY_NAME)
                .attribute("label")
                .list(document);
        Assert.assertEquals("applicationElementsLabelAttributeList",
                1, applicationElementsLabelAttributeList.size());

        XMLPath altPath = XMLPath
                .compile("/manifest|ddd/application").alternate("package")
                .element("activity").alternate("meta-data").alternate("service");
        XMLPath altPath2 = XMLPath.compile(altPath.getPath());

        List<XMLElement> applicationElementList2 = altPath2
                .list(document);
        Assert.assertEquals("applicationElementList2",
                6, applicationElementList2.size());

    }
    @Test
    public void testWithResXmlDocumentComplexPattern() {
        XMLDocument document = createTestXMLDocument();

        List<XMLAttribute> anyAttributeWithNameList = XMLPath
                .compile("/**;name")
                .list(document);
        Assert.assertEquals("anyAttributeWithNameList",
                32, anyAttributeWithNameList.size());

        List<XMLElement> categoryList = XMLPath
                .compile("/**/category")
                .list(document);
        Assert.assertEquals("categoryList",
                5, categoryList.size());

        List<XMLAttribute> anyNameAttributeUnderApplication = XMLPath
                .compile("/**/application/*/**;name")
                .list(document);
        Assert.assertEquals("anyNameAttributeUnderApplication",
                19, anyNameAttributeUnderApplication.size());

        XMLPath xmlPath = XMLPath.compile("/manifest/application/activity;name")
                .value("com.test.TestActivity1");
        List<XMLAttribute> activityNameList = xmlPath.list(document);
        Assert.assertEquals("activityNameList",
                1, activityNameList.size());

        XMLPath xmlPath2 = XMLPath.compile("/manifest/application/activity;name=\"com.test.TestActivity1\"");
        List<XMLAttribute> activityNameList2 = xmlPath2.list(document);
        Assert.assertEquals("activityNameList2",
                1, activityNameList2.size());

        XMLPath xmlPath3 = XMLPath.compile("/manifest/application/activity;name")
                .value("com.test.TestActivity1")
                .alternateValue("com.test.TestActivity2");
        List<XMLAttribute> activityNameList3 = xmlPath3.list(document);
        Assert.assertEquals("activityNameList3",
                2, activityNameList3.size());

        XMLPath xmlPath4 = XMLPath.compile("/manifest/application/activity;exported=true");
        List<XMLAttribute> activityNameList4 = xmlPath4.list(document);
        Assert.assertEquals("activityNameList4",
                1, activityNameList4.size());
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
        String xml = "<?xml version='1.0' encoding='utf-8' ?>\n" +
                "<manifest android:versionCode=\"100\"\n" +
                "          android:versionName=\"1.0.0\"\n" +
                "          android:compileSdkVersion=\"35\"\n" +
                "          android:compileSdkVersionCodename=\"15\"\n" +
                "          package=\"com.test\"\n" +
                "          platformBuildVersionCode=\"35\"\n" +
                "          platformBuildVersionName=\"15\" xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                "  <uses-sdk android:minSdkVersion=\"26\"\n" +
                "            android:targetSdkVersion=\"35\" />\n" +
                "  <permission android:name=\"com.test.permission.PERM1\" />\n" +
                "  <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
                "  <uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />\n" +
                "  <uses-permission android:name=\"android.permission.READ_EXTERNAL_STORAGE\" />\n" +
                "  <uses-permission android:name=\"android.permission.CAMERA\"\n" +
                "                   android:required=\"false\" />\n" +
                "  <uses-feature android:name=\"android.hardware.wifi\"/>\n" +
                "  <queries>\n" +
                "    <package android:name=\"com.package1\" />\n" +
                "    <package android:name=\"com.package2\" />\n" +
                "    <intent>\n" +
                "      <action android:name=\"android.intent.action.GET_CONTENT\" />\n" +
                "      <category android:name=\"android.intent.category.OPENABLE\" />\n" +
                "      <data android:mimeType=\"*/*\" />\n" +
                "    </intent>\n" +
                "    <intent>\n" +
                "      <action android:name=\"android.intent.action.VIEW\" />\n" +
                "      <category android:name=\"android.intent.category.BROWSABLE\" />\n" +
                "      <data android:scheme=\"https\" />\n" +
                "    </intent>\n" +
                "  </queries>\n" +
                "  <application android:theme=\"@style/Theme.Default\"\n" +
                "               android:label=\"@string/app_name\"\n" +
                "               android:icon=\"@mipmap/icon\"\n" +
                "               android:name=\"com.test.TestApp\"\n" +
                "               android:allowBackup=\"false\">\n" +
                "    <meta-data android:name=\"com.meta-data1\"\n" +
                "               android:value=\"true\" />\n" +
                "    <meta-data android:name=\"com.meta-data2\"\n" +
                "               android:value=\"value2\" />\n" +
                "    <activity android:theme=\"@style/Theme.Translucent.NoTitleBar\"\n" +
                "              android:name=\"com.test.TestActivity1\"\n" +
                "              android:label=\"Label TestActivity1\"\n" +
                "              android:exported=\"true\">\n" +
                "      <intent-filter>\n" +
                "        <action android:name=\"android.intent.action.VIEW\" />\n" +
                "        <category android:name=\"android.intent.category.BROWSABLE\" />\n" +
                "        <category android:name=\"android.intent.category.DEFAULT\" />\n" +
                "        <data android:scheme=\"schemeValue1\"\n" +
                "              android:host=\"hostValue1\" />\n" +
                "        <data android:scheme=\"schemeValue2\"\n" +
                "              android:host=\"hostValue2\" />\n" +
                "      </intent-filter>\n" +
                "    </activity>\n" +
                "    <activity android:theme=\"@style/Theme.Translucent.NoTitleBar\"\n" +
                "              android:name=\"com.test.TestActivity2\"\n" +
                "              android:screenOrientation=\"portrait\"/>\n" +
                "    <activity-alias android:name=\"com.test.TestActivityAlias3\"\n" +
                "                    android:exported=\"true\"\n" +
                "                    android:targetActivity=\"com.test.TestActivity2\">\n" +
                "      <intent-filter>\n" +
                "        <action android:name=\"android.intent.action.MAIN\" />\n" +
                "        <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                "      </intent-filter>\n" +
                "      <meta-data android:name=\"meta-data-in-activity-alias\"\n" +
                "                 android:resource=\"@xml/res1\" />\n" +
                "    </activity-alias>\n" +
                "    <receiver android:name=\"com.test.TestReceiver1\"\n" +
                "              android:exported=\"false\" />\n" +
                "    <receiver android:name=\"com.test.TestReceiver1\"\n" +
                "              android:exported=\"false\">\n" +
                "      <intent-filter>\n" +
                "        <action android:name=\"android.appwidget.action.APPWIDGET_UPDATE\" />\n" +
                "      </intent-filter>\n" +
                "      <meta-data android:name=\"meta-data-in-receiver1\"\n" +
                "                 android:resource=\"value2\" />\n" +
                "    </receiver>\n" +
                "    <service android:name=\"com.test.TestService1\"\n" +
                "             android:exported=\"false\">\n" +
                "      <meta-data android:name=\"meta-data-in-service1\"\n" +
                "                 android:value=\"value1\" />\n" +
                "    </service>\n" +
                "    <provider android:name=\"com.test.TestProvider1\"\n" +
                "              android:exported=\"false\"\n" +
                "              android:authorities=\"com.test.authorities1\"\n" +
                "              android:initOrder=\"99\" />\n" +
                "    <meta-data android:name=\"com.android.dynamic.apk.fused.modules\"\n" +
                "               android:value=\"config.xxhdpi,base\" />\n" +
                "  </application>\n" +
                "</manifest>";

        try {
            return XMLDocument.load(xml);
        } catch (XmlPullParserException|IOException e) {
            throw new RuntimeException(e);
        }
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
