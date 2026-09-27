package com.reandroid.apk;

import com.reandroid.TestUtils;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.value.Entry;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ApkBundleMergeTest {

    private static final String PACKAGE = "com.reandroid.merge";

    @Test
    public void testMergeKeepsBaseResourcesAndAddsSplitConfigs() throws IOException {
        ApkModule merged = merge();

        PackageBlock packageBlock = merged.getTableBlock().pickOne();
        Assert.assertEquals("Hello", value(packageBlock, "", "hello"));
        Assert.assertEquals("Hallo", value(packageBlock, "-de", "hello"));
        Assert.assertEquals("Only in base", value(packageBlock, "", "base_only"));
    }

    @Test
    public void testMergeKeepsStoredDexStored() throws IOException {
        ApkModule merged = merge();

        Assert.assertTrue(merged.getUncompressedFiles().isUncompressed("classes.dex"));
        Assert.assertTrue(merged.getUncompressedFiles().isUncompressed("classes2.dex"));
    }

    /** Merges, writes and reloads the result, as callers use it */
    private static ApkModule merge() throws IOException {
        ApkBundle bundle = new ApkBundle();
        bundle.addModule(createBase());
        bundle.addModule(createSplit());
        ApkModule merged = bundle.mergeModules();
        File file = new File(TestUtils.getTempDir(), "bundle_merged.apk");
        merged.writeApk(file);
        merged.close();
        bundle.close();
        return ApkModule.loadApkFile(file);
    }

    private static ApkModule createBase() throws IOException {
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(0x7f, PACKAGE);
        packageBlock.getOrCreate("", "string", "hello").setValueAsString("Hello");
        packageBlock.getOrCreate("", "string", "base_only").setValueAsString("Only in base");
        tableBlock.refreshFull();
        return createModule("base", tableBlock, null);
    }

    private static ApkModule createSplit() throws IOException {
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(0x7f, PACKAGE);
        // Created first, so it gets the id "hello" has in the base
        packageBlock.getOrCreate("-de", "string", "hello").setValueAsString("Hallo");
        tableBlock.refreshFull();
        return createModule("split_config.de", tableBlock, "config.de");
    }

    private static ApkModule createModule(String name, TableBlock tableBlock, String split) {
        ApkModule module = new ApkModule(name, new ZipEntryMap());
        module.setLoadDefaultFramework(false);

        AndroidManifestBlock manifest = new AndroidManifestBlock();
        manifest.setPackageName(PACKAGE);
        if (split != null) {
            manifest.setSplit(split, true);
        }
        manifest.refresh();
        module.add(new ByteInputSource(manifest.getBytes(), AndroidManifestBlock.FILE_NAME));

        // A table the module has not parsed yet, as when loaded from an archive
        module.add(new ByteInputSource(tableBlock.getBytes(), TableBlock.FILE_NAME));

        module.add(new ByteInputSource(new byte[]{'d', 'e', 'x', '\n'}, "classes.dex"));
        module.getUncompressedFiles().addPath("classes.dex");
        return module;
    }

    private static String value(PackageBlock packageBlock, String qualifiers, String name) {
        Entry entry = packageBlock.getEntry(qualifiers, "string", name);
        Assert.assertNotNull(qualifiers + "/" + name, entry);
        return entry.getResValue().getValueAsString();
    }
}
