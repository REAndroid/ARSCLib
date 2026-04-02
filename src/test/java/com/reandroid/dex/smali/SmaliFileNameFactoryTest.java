package com.reandroid.dex.smali;

import com.reandroid.utils.io.FileUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class SmaliFileNameFactoryTest {
    private final Charset UTF8 = Charset.forName("UTF-8");

    @Test
    public void test1ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<100; i++) {
            sb.append((char)i);
        }

        String result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 5);
        Assert.assertEquals(95, result.getBytes(UTF8).length);
        Assert.assertEquals(95, result.length());
    }

    @Test
    public void test2ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x80; i<0x80+100; i++) {
            sb.append((char)i);
        }

        // remove a total of 3 2-byte characters, and then add back in the 1-byte '#'
        String result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 4);
        Assert.assertEquals(200, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(195, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());

        // remove a total of 3 2-byte characters, and then add back in the 1-byte '#'
        result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 5);
        Assert.assertEquals(200, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(195, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());
    }

    @Test
    public void test3ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x800; i<0x800+100; i++) {
            sb.append((char)i);
        }

        // remove a total of 3 3-byte characters, and then add back in the 1-byte '#'
        String result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 6);
        Assert.assertEquals(300, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(292, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());

        // remove a total of 3 3-byte characters, and then add back in the 1-byte '#'
        result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 7);
        Assert.assertEquals(300, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(292, result.getBytes(UTF8).length);
        Assert.assertEquals(98, result.length());
    }

    @Test
    public void test4ByteEncodings() {
        StringBuilder sb = new StringBuilder();
        for (int i=0x10000; i<0x10000+100; i++) {
            sb.appendCodePoint(i);
        }

        // we remove 3 codepoints == 6 characters == 12 bytes, and then add back in the 1-byte '#'
        String result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 8);
        Assert.assertEquals(400, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(389, result.getBytes(UTF8).length);
        Assert.assertEquals(195, result.length());

        // we remove 2 codepoints == 4 characters == 8 bytes, and then add back in the 1-byte '#'
        result = SmaliFileNameFactory.shortenPathComponent(sb.toString(), 7);
        Assert.assertEquals(400, sb.toString().getBytes(UTF8).length);
        Assert.assertEquals(393, result.getBytes(UTF8).length);
        Assert.assertEquals(197, result.length());
    }

    @Test
    public void testMultipleLongNames() throws IOException {
        String filenameFragment = repeat("a", 512);

        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali");

        // put the differentiating character in the middle, where it will get stripped out by the filename shortening
        // logic
        File file1 = factory.getUniqueFilenameForClass("La/a/" + filenameFragment  + "1" + filenameFragment + ";");
        checkFilename(tempDir, file1, "a", "a", repeat("a", 124) + "#" + repeat("a", 118) + ".smali");

        File file2 = factory.getUniqueFilenameForClass("La/a/" + filenameFragment + "2" + filenameFragment + ";");
        checkFilename(tempDir, file2, "a", "a", repeat("a", 124) + "#" + repeat("a", 118) + ".1.smali");

        Assert.assertNotEquals(file1.getAbsolutePath(), file2.getAbsolutePath());
        FileUtil.deleteDirectory(tempDir);
    }

    @Test
    public void testBasicFunctionality() throws IOException {
        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali");

        File file = factory.getUniqueFilenameForClass("La/b/c/d;");
        checkFilename(tempDir, file, "a", "b", "c", "d.smali");

        file = factory.getUniqueFilenameForClass("La/b/c/e;");
        checkFilename(tempDir, file, "a", "b", "c", "e.smali");

        file = factory.getUniqueFilenameForClass("La/b/d/d;");
        checkFilename(tempDir, file, "a", "b", "d", "d.smali");

        file = factory.getUniqueFilenameForClass("La/b;");
        checkFilename(tempDir, file, "a", "b.smali");

        file = factory.getUniqueFilenameForClass("Lb;");
        checkFilename(tempDir, file, "b.smali");
        FileUtil.deleteDirectory(tempDir);
    }

    @Test
    public void testCaseInsensitiveFilesystem() throws IOException {
        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali", false, false);

        File file = factory.getUniqueFilenameForClass("La/b/c;");
        checkFilename(tempDir, file, "a", "b", "c.smali");

        file = factory.getUniqueFilenameForClass("La/b/C;");
        checkFilename(tempDir, file, "a", "b", "C.1.smali");

        file = factory.getUniqueFilenameForClass("La/B/c;");
        checkFilename(tempDir, file, "a", "B.1", "c.smali");
        FileUtil.deleteDirectory(tempDir);
    }

    @Test
    public void testWindowsReservedFilenames() throws IOException {
        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali", false, true);

        File file = factory.getUniqueFilenameForClass("La/con/c;");
        checkFilename(tempDir, file, "a", "con#", "c.smali");

        file = factory.getUniqueFilenameForClass("La/Con/c;");
        checkFilename(tempDir, file, "a", "Con#.1", "c.smali");

        file = factory.getUniqueFilenameForClass("La/b/PRN;");
        checkFilename(tempDir, file, "a", "b", "PRN#.smali");

        file = factory.getUniqueFilenameForClass("La/b/prN;");
        checkFilename(tempDir, file, "a", "b", "prN#.1.smali");

        file = factory.getUniqueFilenameForClass("La/b/com0;");
        checkFilename(tempDir, file, "a", "b", "com0.smali");

        for (String reservedName: new String[] {"con", "prn", "aux", "nul", "com1", "com9", "lpt1", "lpt9"}) {
            file = factory.getUniqueFilenameForClass("L" + reservedName + ";");
            checkFilename(tempDir, file, reservedName +"#.smali");
        }
        FileUtil.deleteDirectory(tempDir);
    }

    @Test
    public void testIgnoringWindowsReservedFilenames() throws IOException {
        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali", true, false);

        File file = factory.getUniqueFilenameForClass("La/con/c;");
        checkFilename(tempDir, file, "a", "con", "c.smali");


        file = factory.getUniqueFilenameForClass("La/b/PRN;");
        checkFilename(tempDir, file, "a", "b", "PRN.smali");


        file = factory.getUniqueFilenameForClass("La/b/com0;");
        checkFilename(tempDir, file, "a", "b", "com0.smali");

        for (String reservedName: new String[] {"con", "prn", "aux", "nul", "com1", "com9", "lpt1", "lpt9"}) {
            file = factory.getUniqueFilenameForClass("L" + reservedName + ";");
            checkFilename(tempDir, file, reservedName +".smali");
        }
        FileUtil.deleteDirectory(tempDir);
    }

    @Test
    public void testUnicodeCollisionOnMac() throws IOException {
        if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
            // The test is only applicable when run on a mac system
            return;
        }

        File tempDir = createTempDir().getCanonicalFile();
        SmaliFileNameFactory factory = new SmaliFileNameFactory(tempDir, ".smali", true, false);

        File file = factory.getUniqueFilenameForClass("Lε;");
        checkFilename(tempDir, file, "ε.smali");

        file = factory.getUniqueFilenameForClass("Lϵ;");
        checkFilename(tempDir, file, "ϵ.1.smali");

        file = factory.getUniqueFilenameForClass("Lε/ε;");
        checkFilename(tempDir, file, "ε", "ε.smali");

        file = factory.getUniqueFilenameForClass("Lε/ϵ;");
        checkFilename(tempDir, file, "ε", "ϵ.1.smali");

        file = factory.getUniqueFilenameForClass("Lϵ/ϵ;");
        checkFilename(tempDir, file, "ϵ.1", "ϵ.smali");

        file = factory.getUniqueFilenameForClass("Lϵ/ε;");
        checkFilename(tempDir, file, "ϵ.1", "ε.1.smali");
        FileUtil.deleteDirectory(tempDir);
    }

    private void checkFilename(File base, File file, String... elements) {
        for (int i = elements.length - 1; i >= 0; i--) {
            Assert.assertEquals(elements[i], file.getName());
            file = file.getParentFile();
        }
        Assert.assertEquals(base.getAbsolutePath(), file.getAbsolutePath());
    }
    private static String repeat(String s, int times) {
        StringBuilder builder = new StringBuilder(s.length() * times);
        for (int i = 0; i < times; i++) {
            builder.append(s);
        }
        return builder.toString();
    }
    private static File createTempDir() {
        File tempDir = FileUtil.getTempDir("SmaliFileNameFactoryTest");
        FileUtil.deleteDirectory(tempDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }
}
