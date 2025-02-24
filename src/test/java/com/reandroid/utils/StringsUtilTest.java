package com.reandroid.utils;

import org.junit.Assert;
import org.junit.Test;

public class StringsUtilTest {
    
    @Test
    public void testReplaceAll_string_string() {
        Assert.assertEquals("ABCXYZFGH",
                StringsUtil.replaceAll("ABCDEFGH", "DE", "XYZ"));

        Assert.assertEquals("XYZCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", "AB", "XYZ"));

        Assert.assertEquals("ABCDEFXYZ",
                StringsUtil.replaceAll("ABCDEFGH", "GH", "XYZ"));

        Assert.assertEquals("XYZBCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", "A", "XYZ"));

        Assert.assertEquals("ABCDEFGXYZ",
                StringsUtil.replaceAll("ABCDEFGH", "H", "XYZ"));

        Assert.assertEquals("XYZCDXYZCD",
                StringsUtil.replaceAll("ABCDABCD", "AB", "XYZ"));

        Assert.assertEquals("XYZDXYZD",
                StringsUtil.replaceAll("ABCDABCD", "ABC", "XYZ"));

        Assert.assertEquals("XYZXYZ",
                StringsUtil.replaceAll("ABCABC", "ABC", "XYZ"));

        Assert.assertEquals("ABCABC",
                StringsUtil.replaceAll("ABCABC", "", "XYZ"));

        Assert.assertEquals("ABCABC",
                StringsUtil.replaceAll("ABCABC", "X", "XYZ"));

        Assert.assertEquals("ABCABC",
                StringsUtil.replaceAll("ABCABC", "A", "A"));

        Assert.assertEquals("BCBC",
                StringsUtil.replaceAll("ABCABC", "A", ""));

        Assert.assertEquals("",
                StringsUtil.replaceAll("AAA", "A", ""));

        Assert.assertEquals("A\\B\\C\\D",
                StringsUtil.replaceAll("A/B/C/D", "/", "\\"));

        Assert.assertEquals("A$B\\CXD[!]+",
                StringsUtil.replaceAll("A$B\\C.*D[!]+", ".*", "X"));

        Assert.assertEquals("A$B\\C.*DX",
                StringsUtil.replaceAll("A$B\\C.*D[!]+", "[!]+", "X"));
    }

    @Test
    public void testReplaceAll_char_string() {
        Assert.assertEquals("XYZBCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", 'A', "XYZ"));

        Assert.assertEquals("ABCXYZEFGH",
                StringsUtil.replaceAll("ABCDEFGH", 'D', "XYZ"));

        Assert.assertEquals("ABCDEFGXYZ",
                StringsUtil.replaceAll("ABCDEFGH", 'H', "XYZ"));

        Assert.assertEquals("ABCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", 'K', "XYZ"));

        Assert.assertEquals("XYZ",
                StringsUtil.replaceAll("A", 'A', "XYZ"));

        Assert.assertEquals("XYZXYZ",
                StringsUtil.replaceAll("AA", 'A', "XYZ"));

        Assert.assertEquals("",
                StringsUtil.replaceAll("", 'A', "XYZ"));

        Assert.assertEquals("BCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", 'A', ""));
    }

    @Test
    public void testReplaceAll_string_char() {
        Assert.assertEquals("XCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", "AB", 'X'));

        Assert.assertEquals("ABCXFGH",
                StringsUtil.replaceAll("ABCDEFGH", "DE", 'X'));

        Assert.assertEquals("ABCDEFX",
                StringsUtil.replaceAll("ABCDEFGH", "GH", 'X'));

        Assert.assertEquals("XCDEFGH",
                StringsUtil.replaceAll("ABCDEFGH", "AB", 'X'));
    }
}
