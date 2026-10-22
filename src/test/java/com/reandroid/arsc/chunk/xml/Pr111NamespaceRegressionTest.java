package com.reandroid.arsc.chunk.xml;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.apk.ApkModule;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.xml.XMLFactory;
import org.junit.Assert;
import org.junit.Test;

public class Pr111NamespaceRegressionTest {

    private ResXmlDocument parse(String xml) throws Exception {
        TableBlock table = new TableBlock();
        table.addFramework(
                AndroidFrameworks.getLatest().getTableBlock());
        table.refresh();

        ApkModule apk = new ApkModule();
        apk.setTableBlock(table);

        ResXmlDocument doc = new ResXmlDocument();
        doc.setApkFile(apk);
        doc.parse(XMLFactory.newPullParser(xml));
        return doc;
    }

    @Test
    public void standardAndroidPrefix() throws Exception {
        ResXmlDocument doc = parse(
            "<manifest xmlns:android=\"" +
            "http://schemas.android.com/apk/res/android\" " +
            "android:versionCode=\"123\"/>");

        ResXmlAttribute attr = doc.getDocumentElement()
                .searchAttributeByName("versionCode");

        Assert.assertNotNull(attr);
        Assert.assertEquals("android", attr.getPrefix());
        Assert.assertTrue(attr.getNameId() != 0);
        Assert.assertEquals(123, attr.getData());
    }

    @Test
    public void alternateAndroidPrefix() throws Exception {
        ResXmlDocument doc = parse(
            "<manifest xmlns:n0=\"" +
            "http://schemas.android.com/apk/res/android\" " +
            "n0:versionCode=\"456\"/>");

        ResXmlAttribute attr = doc.getDocumentElement()
                .searchAttributeByName("versionCode");

        Assert.assertNotNull(attr);
        Assert.assertEquals(
            "http://schemas.android.com/apk/res/android",
            attr.getUri());
        // Prefix may be normalized internally.
        // Verify namespace URI, resource ID and value instead.
        Assert.assertTrue(attr.getNameId() != 0);
        Assert.assertEquals(456, attr.getData());
    }

    @Test
    public void externalNamespaceMustNotBecomeAndroid()
            throws Exception {
        ResXmlDocument doc = parse(
            "<manifest xmlns:n0=\"https://example.com/custom\" " +
            "n0:versionCode=\"789\"/>");

        ResXmlAttribute attr = doc.getDocumentElement()
                .searchAttributeByName("versionCode");

        Assert.assertNotNull(attr);
        Assert.assertEquals(
            "https://example.com/custom",
            attr.getUri());
        Assert.assertEquals(0, attr.getNameId());
    }
}
