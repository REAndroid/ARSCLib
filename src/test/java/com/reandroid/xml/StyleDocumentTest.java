package com.reandroid.xml;

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class StyleDocumentTest {

    @Test
    public void testStyledString() throws IOException, XmlPullParserException {
        StyleDocument styleDocument = StyleDocument.parseStyledString("<u>복사하기</u>");
        TableBlock tableBlock = new TableBlock();
        TableString tableString = tableBlock.getTableStringPool().getOrCreate(styleDocument);
        Assert.assertNotNull(tableString);
        styleDocument = StyleDocument.parseStyledString(STYLED_XML_STRING_1);
        String xml = styleDocument.getXml(true);
        Assert.assertEquals(STYLED_XML_STRING_1, xml);
        String styled = styleDocument.getStyledString();
        Assert.assertEquals(STYLED_STRING_1, styled);
        String html = styleDocument.getHtml();
        Assert.assertEquals(STYLED_HTML_STRING_1, html);
    }


    @Test
    public void testStyledAndPlainString() throws IOException, XmlPullParserException {
        String str = "This is <b>bold</b> text.";
        String strAttr = "This is <b color=\"#ff0000\">bold</b> text.";
        TableBlock tableBlock = new TableBlock();
        TableStringPool stringPool = tableBlock.getTableStringPool();

        TableString styledString = stringPool.getOrCreate(StyleDocument.parseStyledString(str));
        Assert.assertNotNull(styledString);
        Assert.assertTrue("styled-hasStyle=false", styledString.hasStyle());
        Assert.assertEquals(str, styledString.getXml());
        Assert.assertNotEquals(str, styledString.get());

        TableString styledStringAttribute = stringPool.getOrCreate(StyleDocument.parseStyledString(strAttr));
        Assert.assertNotNull(styledStringAttribute);
        Assert.assertTrue("styled-attr-hasStyle=false", styledStringAttribute.hasStyle());
        Assert.assertEquals(strAttr, styledStringAttribute.getXml());
        Assert.assertNotEquals(strAttr, styledStringAttribute.get());

        TableString plainString = stringPool.getOrCreate(str);
        Assert.assertNotNull(plainString);
        Assert.assertFalse("plain-hasStyle=true", plainString.hasStyle());
        Assert.assertEquals(str, plainString.getXml());
        Assert.assertEquals(str, plainString.get());
        Assert.assertFalse(styledString.equalsValue(plainString.get()));

        TableString plainStringAttribute = stringPool.getOrCreate(strAttr);
        Assert.assertNotNull(plainStringAttribute);
        Assert.assertFalse("plain-attr-hasStyle=true", plainStringAttribute.hasStyle());

        Assert.assertEquals(str, plainString.getXml());
        Assert.assertEquals(str, plainString.get());
        Assert.assertEquals(styledString.get(), styledStringAttribute.get());
        Assert.assertNotEquals(styledString.getXml(), styledStringAttribute.getXml());
    }

    private static final String STYLED_XML_STRING_1 = "<br>\n   </br><font color=\"#E7E7E7\" size=\"bold\">%1dThe quick </font>brown<font color=\"#FF0000\"> fox &lt;jumps </font>over<a /> the lazy dog<b />";
    private static final String STYLED_STRING_1 = "\n" +
            "   %1dThe quick brown fox <jumps over the lazy dog";
    private static final String STYLED_HTML_STRING_1 = "<br>\n" +
            "   </br><font;color=#E7E7E7;size=bold>%1dThe quick </font>brown<font;color=#FF0000> fox <jumps </font>over<a /> the lazy dog<b />";
}
