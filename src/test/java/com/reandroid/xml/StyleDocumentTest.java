package com.reandroid.xml;

import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.item.TableString;
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
        tableString.getXml();
        Assert.assertNotNull(tableString);
        styleDocument = StyleDocument.parseStyledString(STYLED_XML_STRING_1);
        String xml = styleDocument.getXml(true);
        Assert.assertEquals(STYLED_XML_STRING_1, xml);
        String styled = styleDocument.getStyledString();
        Assert.assertEquals(STYLED_STRING_1, styled);
        String html = styleDocument.getHtml();
        Assert.assertEquals(STYLED_HTML_STRING_1, html);
    }
    private static final String STYLED_XML_STRING_1 = "<br>\n   </br><font color=\"#E7E7E7\" size=\"bold\">%1dThe quick </font>brown<font color=\"#FF0000\"> fox &lt;jumps </font>over<a/> the lazy dog<b/>";
    private static final String STYLED_STRING_1 = "\n" +
            "   %1dThe quick brown fox <jumps over the lazy dog";
    private static final String STYLED_HTML_STRING_1 = "<br>\n" +
            "   </br><font;color=#E7E7E7;size=bold>%1dThe quick </font>brown<font;color=#FF0000> fox <jumps </font>over<a/> the lazy dog<b/>";
}
