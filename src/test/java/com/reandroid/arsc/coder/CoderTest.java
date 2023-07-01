package com.reandroid.arsc.coder;

import org.junit.Assert;
import org.junit.Test;

public class CoderTest {
    @Test
    public void testCoder(){
        for(String decoded : DECODED_VALUES){
            EncodeResult encodeResult = ValueCoder.encode(decoded);
            Assert.assertNotNull("Fail to encode: " + decoded, encodeResult);
            String decoded2 = ValueCoder.decode(encodeResult.valueType, encodeResult.value);
            Assert.assertEquals(decoded, decoded2);
        }
    }
    private static final String[] DECODED_VALUES = new String[]{
            "10.1dp",
            "-10.1dp",
            "100.0sp",
            "200.0pt",
            "-100.0mm",
            "400.0in",
            "100.0%p",
            "50.0%",
            "-50.0%",
            "#ffaa",
            "#ffaabb",
            "#ffaabbcc",
            "123456",
            "-123456",
            "123.45",
            "-123.45",
            "1.45E19",
            "1.45E-19",
            "0xdef01234",
            "true",
            "false",
            "@null",
            "@empty"
    };
}
