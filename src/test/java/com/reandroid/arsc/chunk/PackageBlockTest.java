package com.reandroid.arsc.chunk;

import com.reandroid.TestUtils;
import com.reandroid.apk.framework.InternalFrameworks;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.model.FrameworkTable;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.AttributeType;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResValueMap;
import org.junit.Assert;
import org.junit.Test;

public class PackageBlockTest {

    @Test
    public void testNameAndId() {
        TableBlock tableBlock = new TableBlock();

        PackageBlock packageBlock = tableBlock.newPackage(
                TestUtils.MAIN_PACKAGE_ID, TestUtils.MAIN_PACKAGE_NAME);
        Assert.assertEquals("Packages count", 1, tableBlock.size());

        Assert.assertEquals("Package id", TestUtils.MAIN_PACKAGE_ID, packageBlock.getId());
        Assert.assertEquals("Package name", TestUtils.MAIN_PACKAGE_NAME, packageBlock.getName());
        Assert.assertNotNull(tableBlock.get(0));

        Assert.assertNotNull(tableBlock.getPackageBlockById(TestUtils.MAIN_PACKAGE_ID));
        Assert.assertTrue(tableBlock.getPackages(TestUtils.MAIN_PACKAGE_NAME).hasNext());
    }

    @Test
    public void testAttributeAndNamespaceOnMultiPackage() {
        TableBlock tableBlock = new TableBlock();

        PackageBlock packageMain = tableBlock.newPackage(
                TestUtils.MAIN_PACKAGE_ID, TestUtils.MAIN_PACKAGE_NAME);
        Entry idEntry = packageMain.getOrCreate("", "id", "id_main");
        idEntry.setDataAsBoolean(true);

        Entry attr1 = createNameAttribute(tableBlock.newPackage(
                TestUtils.MAIN_PACKAGE_ID + 1, TestUtils.MAIN_PACKAGE_NAME + ".app1"));
        Entry attr2 = createNameAttribute(tableBlock.newPackage(
                TestUtils.MAIN_PACKAGE_ID + 2, TestUtils.MAIN_PACKAGE_NAME + ".app2"));
        Entry attr3 = createNameAttribute(tableBlock.newPackage(
                TestUtils.MAIN_PACKAGE_ID + 3, TestUtils.MAIN_PACKAGE_NAME + ".app3"));

        FrameworkTable frameworkTable = InternalFrameworks.INSTANCE.getLatest().getTableBlock();
        ResourceEntry attrAndroidText = frameworkTable.getResource(
                "android", "attr", "text");

        ResXmlDocument document = new ResXmlDocument();
        ResXmlElement element = document.getDocumentElement();
        element.setName("TextView");

        ResXmlAttribute attribute = element.newAttribute();
        attribute.setName("",
                frameworkTable.getResource("android", "attr", "id").getResourceId());
        attribute.setDataAsResourceId(idEntry.getResourceId());

        attribute = element.newAttribute();
        attribute.setName("", attrAndroidText.getResourceId());
        attribute.setDataAsString("text-android-value");

        attribute = element.newAttribute();
        attribute.setName("", attr1.getResourceId());
        attribute.setDataAsString("text-app1-value");

        attribute = element.newAttribute();
        attribute.setName("", attr2.getResourceId());
        attribute.setDataAsString("text-app2-value");

        attribute = element.newAttribute();
        attribute.setName("", attr3.getResourceId());
        attribute.setDataAsString("text-app3-value");

        tableBlock.addFramework(frameworkTable);
        document.setPackageBlock(packageMain);

        document.fixNamespaces();
        document.fixAttributeNames();
        document.refreshFull();
        String expected = "<?xml version='1.0' encoding='utf-8' ?>\n" +
                "<TextView android:id=\"@id/id_main\"\n" +
                "          android:text=\"text-android-value\"\n" +
                "          app1:text=\"text-app1-value\"\n" +
                "          app2:text=\"text-app2-value\"\n" +
                "          app3:text=\"text-app3-value\"" +
                " xmlns:android=\"http://schemas.android.com/apk/res/android\"" +
                " xmlns:app1=\"http://schemas.android.com/apk/res/app1\"" +
                " xmlns:app2=\"http://schemas.android.com/apk/res/app2\"" +
                " xmlns:app3=\"http://schemas.android.com/apk/res/app3\" />";

        Assert.assertEquals(
                TestUtils.squeezeWhiteSpaces(expected),
                TestUtils.squeezeWhiteSpaces(document.toXmlString()));
    }
    private Entry createNameAttribute(PackageBlock packageBlock) {
        Entry attr = packageBlock.getOrCreate("", "attr", "text");
        attr.ensureComplex(true);
        attr.getResTableMapEntry().getHeader().setPublic(true);

        ResValueMapArray array = attr.getResValueMapArray();

        ResValueMap valueMap = array.createNext();
        valueMap.setAttributeType(AttributeType.FORMATS);

        valueMap.addAttributeTypeFormats(
                AttributeDataFormat.REFERENCE,
                AttributeDataFormat.STRING);

        array.refresh();
        return attr;
    }

    @Test
    public void testIsResourceId() {
        Assert.assertFalse(PackageBlock.isResourceId(0x7f00ffff));
        Assert.assertFalse(PackageBlock.isResourceId(0x00ff0000));
        Assert.assertFalse(PackageBlock.isResourceId(0x00ffffff));
        Assert.assertFalse(PackageBlock.isResourceId(0x0000ffff));
        Assert.assertFalse(PackageBlock.isResourceId(0xff000000));
        Assert.assertFalse(PackageBlock.isResourceId(0x0));
        Assert.assertTrue(PackageBlock.isResourceId(0x0101ffff));
        Assert.assertTrue(PackageBlock.isResourceId(0x01010000));
    }

    @Test
    public void testIsPackageId() {
        Assert.assertFalse(PackageBlock.isPackageId(0x00));
        Assert.assertFalse(PackageBlock.isPackageId(0xfff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xfffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffffff));
        Assert.assertFalse(PackageBlock.isPackageId(0xffffffff));
        Assert.assertTrue(PackageBlock.isPackageId(0x01));
        Assert.assertTrue(PackageBlock.isPackageId(0x11));
        Assert.assertTrue(PackageBlock.isPackageId(0xff));
    }

}
