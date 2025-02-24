package com.reandroid.arsc.pool;

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.ReferenceItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.utils.HexUtil;
import com.reandroid.xml.StyleDocument;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Iterator;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TableStringPoolTest {

    @Test
    public void stringPoolTest() {
        createStringsTest();
        encodingTest();
        sortStringsTest();
        duplicateStringsTest();
        styledStringsTest();
    }

    private void createStringsTest() {
        TableStringPool stringPool = newStringPool();
        Assert.assertNotNull("Null table string", stringPool);

        TableString item_a = stringPool.getOrCreate("item-0");
        item_a.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_b = stringPool.getOrCreate("item-1");
        item_b.addReference(StringPoolTestUtil.newFakeReference());

        Assert.assertEquals("Pool size", 2, stringPool.size());

        TableString item_a_1 = stringPool.getOrCreate("item-0");
        Assert.assertSame("Different pool string objects", item_a, item_a_1);
    }
    private void encodingTest() {
        TableStringPool stringPool = newStringPool();

        stringPool.setUtf8(true);

        TableString item_a = stringPool.getOrCreate("item-0");
        item_a.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_b = stringPool.getOrCreate("item-1");
        item_b.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_c = stringPool.getOrCreate("item-2");
        item_c.addReference(StringPoolTestUtil.newFakeReference());

        Assert.assertEquals("item-0", item_a.get());

        Assert.assertEquals("06066974656d2d3100", HexUtil.toHexString(item_b.getBytes()));

        stringPool.setUtf8(false);

        Assert.assertEquals("06006900740065006d002d0031000000", HexUtil.toHexString(item_b.getBytes()));
    }
    private void sortStringsTest() {
        TableStringPool stringPool = newStringPool();

        TableString item_2 = stringPool.getOrCreate("item-2");
        item_2.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1 = stringPool.getOrCreate("item-1");
        item_1.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_0 = stringPool.getOrCreate("item-0");
        item_0.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_3 = stringPool.getOrCreate("item-3");
        item_3.addReference(StringPoolTestUtil.newFakeReference());

        stringPool.sort();

        Assert.assertSame("Out of order \"item-0\"", item_0, stringPool.get(0));
        Assert.assertSame("Out of order \"item-1\"", item_1, stringPool.get(1));
        Assert.assertSame("Out of order \"item-2\"", item_2, stringPool.get(2));
        Assert.assertSame("Out of order \"item-3\"", item_3, stringPool.get(3));
    }

    private void duplicateStringsTest() {
        TableStringPool stringPool = newStringPool();

        TableString item_0 = stringPool.getOrCreate("item-0");
        item_0.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1 = stringPool.getOrCreate("item-1");
        item_1.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1_a = stringPool.getOrCreate("item-1-a");
        item_1_a.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1_b = stringPool.getOrCreate("item-1-b");
        item_1_b.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1_c = stringPool.getOrCreate("item-1-c");
        item_1_c.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_2 = stringPool.getOrCreate("item-2");
        item_2.addReference(StringPoolTestUtil.newFakeReference());


        item_1.set("item-1");
        item_1_a.set("item-1");
        item_1_b.set("item-1");
        item_1_c.set("item-1");


        Assert.assertEquals("Pool size with duplicates", 6, stringPool.size());

        TableString get_or_create = stringPool.getOrCreate("item-1");
        Assert.assertNotNull(get_or_create);

        Assert.assertEquals("Pool size after get or create", 6, stringPool.size());

        Iterator<TableString> iterator = stringPool.getAll("item-1");
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count ++;
        }

        Assert.assertEquals("Strings with value \"item-1\"", 4, count);

        stringPool.compressDuplicates();

        Assert.assertEquals("Unused items count", 3, stringPool.countUnused());
        List<TableString> unusedList = stringPool.listUnused();
        Assert.assertEquals("Unused items list size", 3, unusedList.size());
        for (TableString unused : unusedList) {
            Assert.assertEquals("Unused string value", "item-1", unused.get());
            Assert.assertFalse("Unused has reference", unused.hasReference());
        }

        TableString used_item_1 = stringPool.getOrCreate("item-1");
        Assert.assertNotNull(used_item_1);

        Assert.assertEquals("References count for \"item-1\"", 4, used_item_1.getReferencesSize());

        stringPool.removeUnusedStrings();

        Assert.assertEquals("Pool size after remove unused strings", 3, stringPool.size());

        Iterator<ReferenceItem> item_1_references = used_item_1.getReferences();
        while (item_1_references.hasNext()) {
            ReferenceItem referenceItem = item_1_references.next();
            Assert.assertEquals("Reference index", used_item_1.getIndex(), referenceItem.get());
        }
    }

    private void styledStringsTest() {
        TableStringPool stringPool = newStringPool();

        TableString item_2 = stringPool.getOrCreate("item-2");
        item_2.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_1_plain = stringPool.getOrCreate("item-1");
        item_1_plain.addReference(StringPoolTestUtil.newFakeReference());

        String xml_1 = "<tag attr_0=\"value_0\">item_1</tag>";
        StyleDocument styleDocument_1 = StyleDocument.create(xml_1);
        Assert.assertNotNull("Failed to parse styled xml_1", styleDocument_1);
        TableString item_1_styled = stringPool.getOrCreate(styleDocument_1);
        item_1_styled.addReference(StringPoolTestUtil.newFakeReference());

        Assert.assertEquals(xml_1, item_1_styled.getXml());

        String xml_2 = "<br /><tag>item_2</tag>";
        StyleDocument styleDocument_2 = StyleDocument.create(xml_2);
        Assert.assertNotNull("Failed to parse styled xml_1", styleDocument_2);
        TableString item_2_styled = stringPool.getOrCreate(styleDocument_2);
        item_2_styled.addReference(StringPoolTestUtil.newFakeReference());

        Assert.assertEquals(xml_2, item_2_styled.getXml());

        TableString item_0 = stringPool.getOrCreate("item-0");
        item_0.addReference(StringPoolTestUtil.newFakeReference());

        TableString item_3 = stringPool.getOrCreate("item-3");
        item_3.addReference(StringPoolTestUtil.newFakeReference());

        stringPool.sort();

        Assert.assertSame("Styled string out of order \"item-1\"", item_1_styled, stringPool.get(0));
    }
    public TableStringPool newStringPool() {
        return newTableBlock().getStringPool();
    }
    public TableBlock newTableBlock() {
        TableBlock tableBlock = new TableBlock();
        tableBlock.getOrCreatePackage(0x7f, "com.test");
        return tableBlock;
    }
}
