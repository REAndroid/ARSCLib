package com.reandroid.apk;

import com.reandroid.archive.ByteInputSource;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Issue70CompressionTest {

    @Test
    public void storedEntrySurvivesApkModuleAdd() throws Exception {
        ApkModule module = new ApkModule();

        ByteInputSource source = new ByteInputSource(
                "Byblack regression test".getBytes("UTF-8"),
                "assets/issue70-test.bin"
        );

        source.setMethod(ZipEntry.STORED);
        module.add(source);

        Assert.assertEquals(
                "Method changed immediately after add",
                ZipEntry.STORED,
                module.getZipEntryMap()
                        .getInputSource("assets/issue70-test.bin")
                        .getMethod()
        );

        byte[] apkBytes = module.writeApkBytes();

        boolean found = false;

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(apkBytes))) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if ("assets/issue70-test.bin".equals(entry.getName())) {
                    found = true;

                    Assert.assertEquals(
                            "STORED entry became DEFLATED",
                            ZipEntry.STORED,
                            entry.getMethod()
                    );
                }
                zis.closeEntry();
            }
        }

        Assert.assertTrue("Test entry missing from APK", found);
    }

    @Test
    public void deflatedEntryRemainsDeflated() throws Exception {
        ApkModule module = new ApkModule();

        ByteInputSource source = new ByteInputSource(
                "Deflated regression test".getBytes("UTF-8"),
                "assets/issue70-deflated.bin"
        );

        source.setMethod(ZipEntry.DEFLATED);
        module.add(source);

        byte[] apkBytes = module.writeApkBytes();
        boolean found = false;

        try (ZipInputStream zis = new ZipInputStream(
                new ByteArrayInputStream(apkBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if ("assets/issue70-deflated.bin".equals(entry.getName())) {
                    found = true;
                    Assert.assertEquals(ZipEntry.DEFLATED, entry.getMethod());
                }
                zis.closeEntry();
            }
        }

        Assert.assertTrue("DEFLATED entry missing", found);
    }

    @Test
    public void explicitCompressionChangeIsRespected() {
        ByteInputSource source = new ByteInputSource(
                new byte[]{1, 2, 3},
                "assets/issue70-change.bin"
        );

        source.setMethod(ZipEntry.STORED);
        Assert.assertEquals(ZipEntry.STORED, source.getMethod());

        source.setUncompressed(false);
        Assert.assertEquals(ZipEntry.DEFLATED, source.getMethod());
    }

    @Test
    public void storedEntryAfterRuleRemoval() {
        UncompressedFiles rules = new UncompressedFiles();

        ByteInputSource source = new ByteInputSource(
                new byte[]{1, 2, 3},
                "assets/issue70-rule-change.bin"
        );

        source.setMethod(ZipEntry.STORED);

        // No matching uncompressed rule exists.
        rules.apply(source);

        Assert.assertEquals(
                "Existing STORED state should be preserved",
                ZipEntry.STORED,
                source.getMethod()
        );
    }
}
