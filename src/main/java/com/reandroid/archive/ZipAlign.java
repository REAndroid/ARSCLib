/*
 This class is copied from "apksigner" and I couldn't find the
 original repo/author to credit here.
 */

package com.reandroid.archive;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;

public class ZipAlign {
    private static final int ALIGNMENT_4 = 4;
    private static final int ALIGNMENT_PAGE = 4096;
    private final int mAlignment;

    public ZipAlign(int mAlignment) {
        this.mAlignment = mAlignment;
    }
    public ZipAlign() {
        this(ALIGNMENT_4);
    }

    public void zipAlign(File input, File output) throws IOException {
        ZipDeserializer zipDeserializer = new ZipDeserializer(input);
        try {
            ZipSerializer zipSerializer = new ZipSerializer(zipDeserializer.listInputSources(), this);
            zipSerializer.writeZip(output);
        } finally {
            zipDeserializer.closeChannel();
        }
    }

    public int alignEntry(ZipEntry entry, long dataOffset) {
        String name = entry.getName();
        final int padding;

        if (entry.getMethod() != ZipEntry.STORED) {
            padding = 0;
        } else {
            int alignment = mAlignment;
            if (name.startsWith("lib/") && name.endsWith(".so")) {
                alignment = ALIGNMENT_PAGE;
            }
            padding = (int) ((alignment - (dataOffset % alignment)) % alignment);
        }

        byte[] extra = entry.getExtra();
        if (extra == null) {
            extra = new byte[padding];
        } else {
            byte[] newExtra = new byte[extra.length + padding];
            System.arraycopy(extra, 0, newExtra, 0, extra.length);
            Arrays.fill(newExtra, extra.length, newExtra.length, (byte) 0);
            extra = newExtra;
        }
        entry.setExtra(extra);

        return padding;
    }

    public static void align4(File inFile) throws IOException{
        align(inFile, ALIGNMENT_4);
    }
    public static void align4(File inFile, File outFile) throws IOException{
        align(inFile, outFile, ALIGNMENT_4);
    }
    public static void align(File inFile, int alignment) throws IOException{
        File tmp=toTmpFile(inFile);
        tmp.delete();
        align(inFile, tmp, alignment);
        inFile.delete();
        tmp.renameTo(inFile);
    }
    public static void align(File inFile, File outFile, int alignment) throws IOException{
        ZipAlign zipAlign=new ZipAlign(alignment);
        File dir=outFile.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        zipAlign.zipAlign(inFile, outFile);
    }
    private static File toTmpFile(File file){
        String name=file.getName()+".align.tmp";
        File dir=file.getParentFile();
        if(dir==null){
            return new File(name);
        }
        return new File(dir, name);
    }
}

