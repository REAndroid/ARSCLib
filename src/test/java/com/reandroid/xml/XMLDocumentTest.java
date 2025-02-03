package com.reandroid.xml;

import com.reandroid.utils.collection.CollectionUtil;
import org.junit.Assert;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class XMLDocumentTest {

    @Test
    public void xmlDocumentParseSerializeTest() throws IOException, XmlPullParserException {
        String xmlString1 =
                "<?xml version='1.0' encoding='utf-8' ?>" +
                "\n<!DOCTYPE doc-type>" +
                "\n<!--Comment text1-->" +
                "\n<doc-element>" +
                "\n  <!--Comment text2-->" +
                "\n  <attributes name1=\"value1\" number=\"123456\" escape=\"&lt;tag&gt;\" empty=\"\" />" +
                "\n  <open-close></open-close>" +
                "\n  <self-close />" +
                "\n  <new-lines>\n\n\n\n</new-lines>" +
                "\n  <spaces>           </spaces>" +
                "\n  <new-line-spaces>\n           </new-line-spaces>" +
                "\n  <new-line-text>\n     text1 \ntext2\n      </new-line-text>" +
                "\n  <tab-char>\t</tab-char>" +
                "\n  <inner-text>text1<inner>text2</inner>\ntext3\ttext4</inner-text>" +
                "\n  <escape-lt-gt>&lt;x&gt;scape lt gt</escape-lt-gt>" +
                "\n  <emoji-encoded>&#128077;</emoji-encoded>" +
                "\n  <nested>" +
                "\n    <level1>" +
                "\n      <level2 attribute=\"value\">Content</level2>" +
                "\n    </level1>" +
                "\n  </nested>" +
                "\n  <mixed-content>Text <b>bold</b> text</mixed-content>" +
                "\n  <CDATA><![CDATA[This is some <data>inside CDATA</data>]]></CDATA>" +
                "\n  <processing-instruction><?instruction value?></processing-instruction>" +
                "\n<bad-indent1 />" +
                "\n                <bad-indent2 />" +
                "\n\t\t\t<bad-indent3 />" +
                "\n\t  \t <bad-indent4 />" +
                "\n<bad-indent5 /><bad-indent5 /><bad-indent5 /><bad-indent5 /><bad-indent5 />" +
                "\n</doc-element>";
        XMLDocument document = XMLDocument.load(xmlString1);
        String xmlString2 = document.toXmlString(false);
        Assert.assertEquals(xmlString1, xmlString2);

        Assert.assertNotNull("Null DOCTYPE node", document.getDocType());
        Assert.assertEquals("doc-type", document.getDocType().getName());

        XMLComment comment1 = CollectionUtil.getFirst(document.iterator(XMLComment.class));
        Assert.assertNotNull("Null comment node", comment1);
        Assert.assertEquals("Comment text1", comment1.getText());

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
        Assert.assertEquals("attribute escape", "<tag>",
                attributes.getAttributeValue("escape"));
        Assert.assertEquals("attribute empty", "",
                attributes.getAttributeValue("empty"));
    }

    @Test
    public void htmlDocumentParseSerializeTest() throws IOException, XmlPullParserException {
        String htmlString1 =
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" " +
                        "\"http://www.w3.org/TR/html4/loose.dtd\">" +
                "\n<html>" +
                "\n<head>" +
                "\n    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "\n    <title>Test HTML</title>" +
                "\n    <style>" +
                "\n        body { font-family: Arial, sans-serif; }" +
                "\n    </style>" +
                "\n    <!-- Comment in head -->" +
                "\n</head>" +
                "\n<body>" +
                "\n    <h1>Main Title</h1>" +
                "\n    <ul>" +
                "\n        <li><a href=\"#home\">Home</a></li>" +
                "\n        <li><a href=\"#about\">About</a></li>" +
                "\n    </ul>" +
                "\n    <h2>Article Title</h2>" +
                "\n    <p>This is a paragraph with <strong>strong text</strong> and <em>emphasized text</em>.</p>" +
                "\n    <p>Another paragraph with a <a href=\"https://example.com\">link</a> and a list:</p>" +
                "\n    <ul>" +
                "\n        <li>Item 1</li>" +
                "\n        <li>Item 2</li>" +
                "\n        <li>Item 3</li>" +
                "\n    </ul>" +
                "\n    <p>Text with a <span style=\"color: red;\">span element</span> for styling.</p>" +
                "\n    <br>" +
                "\n    <hr>" +
                "\n    <img src=\"image.jpg\" alt=\"Description\">" +
                "\n    <input type=\"text\" placeholder=\"Enter text\">" +
                "\n    <p>&#169; 2023 Test HTML. All rights reserved.</p>" +
                "\n    <p>Contact: <a href=\"mailto:test@example.com\">test@example.com</a></p>" +
                "\n    <script>" +
                "\n        console.log('Test script running');" +
                "\n    </script>" +
                "\n    <!-- Comment in body -->" +
                "\n</body>" +
                "\n</html>";

        XMLDocument document = XMLDocument.load(htmlString1);
        String htmlString2 = document.toXmlString(false);
        Assert.assertEquals(htmlString1, htmlString2);
    }
}
