package com.reandroid.xml;

import org.junit.Assert;
import org.junit.Test;

public class SpanAttributesDecoderTest {
    @Test
    public void testDecoding() {
        StyleElement element = new StyleElement();
        element.setName("tag");
        testDecoding(element, "name1=;name2=value2");
        testDecoding(element, "=name1=;name2=value2");
        testDecoding(element, "name1");
        testDecoding(element, "href=https://example.com/?bb=cc;qq=4;id=xyz;style=color:black; width:100%; nowrap;");
    }
    public void testDecoding(StyleElement element, String attrs) {
        SpanAttributesDecoder decoder = new SpanAttributesDecoder(element, attrs);
        if (decoder.decode()) {
            Assert.assertEquals(attrs, element.getSpanAttributes());
        } else {
            Assert.assertEquals(attrs, element.getSpanAttributes().substring(1));
        }
    }
}
