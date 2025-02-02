package com.reandroid.xml;

import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class XMLDocumentTest {

    @Test
    public void documentParseSerializeTest() throws IOException, XmlPullParserException {
        String xmlString1 = "<?xml version='1.0' encoding='utf-8' ?>" +
                "\n<doc-element>" +
                "\n  <attributes name1=\"value1\" number=\"123456\" scape=\"&lt;tag&gt;\" empty=\"\" />" +
                "\n  <open-close></open-close>" +
                "\n  <self-close />" +
                "\n  <new-lines>\n\n\n\n</new-lines>" +
                "\n  <spaces>           </spaces>" +
                "\n  <new-line-spaces>\n           </new-line-spaces>" +
                "\n  <new-line-text>\n     text1 \ntext2\n      </new-line-text>" +
                "\n  <tab-char>\t</tab-char>" +
                "\n  <inner-text>text1<inner>text2</inner>\ntext3\ttext4</inner-text>" +
                "\n  <scape-lt-gt>&lt;x&gt;scape lt gt</scape-lt-gt>" +
                "\n  <emoji-encoded>&#128077;</emoji-encoded>" +
                "\n</doc-element>";
        XMLDocument document = XMLDocument.load(xmlString1);
        String xmlString2 = document.toXmlString(false);
        Assert.assertEquals(xmlString1, xmlString2);

        XMLElement docElement = document.getDocumentElement();
        Assert.assertNotNull("Null document element", docElement);
        Assert.assertSame("Different document element",
                docElement, document.getElement("doc-element"));
        XMLElement attributes = docElement.getElement("attributes");
        Assert.assertNotNull("attributes element", attributes);

        Assert.assertEquals("attribute count", 4, attributes.getAttributeCount());
        Assert.assertEquals("attribute name1", "value1",
                attributes.getAttributeValue("name1"));
        Assert.assertEquals("attribute number", "123456",
                attributes.getAttributeValue("number"));
        Assert.assertEquals("attribute scape", "<tag>",
                attributes.getAttributeValue("scape"));
        Assert.assertEquals("attribute empty", "",
                attributes.getAttributeValue("empty"));
    }
}
