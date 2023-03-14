/*
 This class is copied from "apksigner" and I couldn't find the
 original repo/author to credit here.
 */

package com.reandroid.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;


public class ZipAlign {
    private static final int ZIP_ENTRY_HEADER_LEN = 30;
    private static final int ZIP_ENTRY_VERSION = 20;
    private static final int ZIP_ENTRY_USES_DATA_DESCR = 0x0008;
    private static final int ZIP_ENTRY_DATA_DESCRIPTOR_LEN = 16;
    private static final int ALIGNMENT_4 = 4;
    private static final int ALIGNMENT_PAGE = 4096;

    private static class XEntry {
        public final ZipEntry entry;
        public final long headerOffset;
        public final int flags;
        public final int padding;
        
        public XEntry(ZipEntry entry, long headerOffset, int flags, int padding) {
            this.entry = entry;
            this.headerOffset = headerOffset;
            this.flags = flags;
            this.padding = padding;
        }
    }

    
    private static class FilterOutputStreamEx extends FilterOutputStream {
        private long totalWritten = 0;        
        public FilterOutputStreamEx(OutputStream out) {
            super(out);
        }
        @Override
        public void write(byte[] b) throws IOException {
            out.write(b);
            totalWritten += b.length;
        }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            totalWritten += len;
        }
        @Override
        public void write(int b) throws IOException {
            out.write(b);
            totalWritten += 1;
        }
        @Override
        public void close() throws IOException {
            super.close();
        }
        public void writeInt(long v) throws IOException {
            write((int) (v & 0xff));
            write((int) ((v >>> 8) & 0xff));
            write((int) ((v >>> 16) & 0xff));
            write((int) ((v >>> 24) & 0xff));
        }
        public void writeShort(int v) throws IOException {
            write((v) & 0xff);
            write((v >>> 8) & 0xff);
        }
    }

    private File mInputFile;
    private int mAlignment;
    private File mOutputFile;
    private ZipFile mZipFile;
    private RandomAccessFile mRafInput;
    private FilterOutputStreamEx mOutputStream;
    private final List<XEntry> mXEntries = new ArrayList<>();
    private long mInputFileOffset = 0;
    private int mTotalPadding = 0;

    public void zipAlign(File input, File output) throws IOException {
        zipAlign(input, output, ALIGNMENT_4);
    }
    public void zipAlign(File input, File output, int alignment) throws IOException {
        mInputFile = input;
        mAlignment = alignment;
        mOutputFile = output;
        openFiles();
        copyAllEntries();
        buildCentralDirectory();
        closeFiles();
    }
    private void openFiles() throws IOException {
        mZipFile = new ZipFile(mInputFile);
        mRafInput = new RandomAccessFile(mInputFile, "r");
        mOutputStream = new FilterOutputStreamEx(new BufferedOutputStream(new FileOutputStream(mOutputFile), 32 * 1024));
    }
    private void copyAllEntries() throws IOException {
        final int entryCount = mZipFile.size();
        if (entryCount == 0) {
            return;
        }
        final Enumeration<?> entries = mZipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            final String name = entry.getName();

            int flags = entry.getMethod() == ZipEntry.STORED ? 0 : 1 << 3;
            flags |= 1 << 11;

            final long outputEntryHeaderOffset = mOutputStream.totalWritten;

            final int inputEntryHeaderSize = ZIP_ENTRY_HEADER_LEN + (entry.getExtra() != null ? entry.getExtra().length : 0)
                    + name.getBytes(StandardCharsets.UTF_8).length;
            final long inputEntryDataOffset = mInputFileOffset + inputEntryHeaderSize;

            final int padding;

            if (entry.getMethod() != ZipEntry.STORED) {
                padding = 0;
            } else {
                int alignment = mAlignment;
                if (name.startsWith("lib/") && name.endsWith(".so")) {
                    alignment = ALIGNMENT_PAGE;
                }
                long newOffset = inputEntryDataOffset + mTotalPadding;
                padding = (int) ((alignment - (newOffset % alignment)) % alignment);
                mTotalPadding += padding;
            }

            final XEntry xentry = new XEntry(entry, outputEntryHeaderOffset, flags, padding);
            mXEntries.add(xentry);
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
            mOutputStream.writeInt(ZipOutputStream.LOCSIG);
            mOutputStream.writeShort(ZIP_ENTRY_VERSION);
            mOutputStream.writeShort(flags);
            mOutputStream.writeShort(entry.getMethod());

            int modDate;
            int time;
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date(entry.getTime()));
            int year = cal.get(Calendar.YEAR);
            if (year < 1980) {
                modDate = 0x21;
                time = 0;
            } else {
                modDate = cal.get(Calendar.DATE);
                modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                time = cal.get(Calendar.SECOND) >> 1;
                time = (cal.get(Calendar.MINUTE) << 5) | time;
                time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
            }

            mOutputStream.writeShort(time);
            mOutputStream.writeShort(modDate);

            mOutputStream.writeInt(entry.getCrc());
            mOutputStream.writeInt(entry.getCompressedSize());
            mOutputStream.writeInt(entry.getSize());

            mOutputStream.writeShort(entry.getName().getBytes(StandardCharsets.UTF_8).length);
            mOutputStream.writeShort(entry.getExtra().length);
            mOutputStream.write(entry.getName().getBytes(StandardCharsets.UTF_8));
            mOutputStream.write(entry.getExtra(), 0, entry.getExtra().length);

            mInputFileOffset += inputEntryHeaderSize;

            final long sizeToCopy;
            if ((flags & ZIP_ENTRY_USES_DATA_DESCR) != 0) {
                sizeToCopy = (entry.isDirectory() ? 0 : entry.getCompressedSize()) + ZIP_ENTRY_DATA_DESCRIPTOR_LEN;
            } else {
                sizeToCopy = entry.isDirectory() ? 0 : entry.getCompressedSize();
            }

            if (sizeToCopy > 0) {
                mRafInput.seek(mInputFileOffset);

                long totalSizeCopied = 0;
                final byte[] buf = new byte[32 * 1024];
                while (totalSizeCopied < sizeToCopy) {
                    int read = mRafInput.read(buf, 0, (int) Math.min(32 * 1024, sizeToCopy - totalSizeCopied));
                    if (read <= 0) {
                        break;
                    }
                    mOutputStream.write(buf, 0, read);
                    totalSizeCopied += read;
                }
            }

            mInputFileOffset += sizeToCopy;
        }
    }

    private void buildCentralDirectory() throws IOException {
        final long centralDirOffset = mOutputStream.totalWritten;
        final int entryCount = mXEntries.size();
        for (int i = 0; i < entryCount;  i++) {
            XEntry xentry = mXEntries.get(i);
            final ZipEntry entry = xentry.entry;
            int modDate;
            int time;
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date(entry.getTime()));
            int year = cal.get(Calendar.YEAR);
            if (year < 1980) {
                modDate = 0x21;
                time = 0;
            } else {
                modDate = cal.get(Calendar.DATE);
                modDate = (cal.get(Calendar.MONTH) + 1 << 5) | modDate;
                modDate = ((cal.get(Calendar.YEAR) - 1980) << 9) | modDate;
                time = cal.get(Calendar.SECOND) >> 1;
                time = (cal.get(Calendar.MINUTE) << 5) | time;
                time = (cal.get(Calendar.HOUR_OF_DAY) << 11) | time;
            }

            mOutputStream.writeInt(ZipFile.CENSIG); // CEN header signature
            mOutputStream.writeShort(ZIP_ENTRY_VERSION); // version made by
            mOutputStream.writeShort(ZIP_ENTRY_VERSION); // version needed to extract
            mOutputStream.writeShort(xentry.flags); // general purpose bit flag
            mOutputStream.writeShort(entry.getMethod()); // compression method
            mOutputStream.writeShort(time);
            mOutputStream.writeShort(modDate);
            mOutputStream.writeInt(entry.getCrc()); // crc-32
            mOutputStream.writeInt(entry.getCompressedSize()); // compressed size
            mOutputStream.writeInt(entry.getSize()); // uncompressed size
            final byte[] nameBytes = entry.getName().getBytes(StandardCharsets.UTF_8);
            mOutputStream.writeShort(nameBytes.length);
            mOutputStream.writeShort(entry.getExtra() != null ? entry.getExtra().length - xentry.padding : 0);
            final byte[] commentBytes;
            if (entry.getComment() != null) {
                commentBytes = entry.getComment().getBytes(StandardCharsets.UTF_8);
                mOutputStream.writeShort(Math.min(commentBytes.length, 0xffff));
            } else {
                commentBytes = null;
                mOutputStream.writeShort(0);
            }
            mOutputStream.writeShort(0); // starting disk number
            mOutputStream.writeShort(0); // internal file attributes (unused)
            mOutputStream.writeInt(0); // external file attributes (unused)
            mOutputStream.writeInt(xentry.headerOffset); // relative offset of local header
            mOutputStream.write(nameBytes);
            if (entry.getExtra() != null) {
                mOutputStream.write(entry.getExtra(), 0, entry.getExtra().length - xentry.padding);
            }
            if (commentBytes != null) {
                mOutputStream.write(commentBytes, 0, Math.min(commentBytes.length, 0xffff));
            }
        }
        final long centralDirSize = mOutputStream.totalWritten - centralDirOffset;

        mOutputStream.writeInt(ZipFile.ENDSIG); // END record signature
        mOutputStream.writeShort(0); // number of this disk
        mOutputStream.writeShort(0); // central directory start disk
        mOutputStream.writeShort(entryCount); // number of directory entries on disk
        mOutputStream.writeShort(entryCount); // total number of directory entries
        mOutputStream.writeInt(centralDirSize); // length of central directory
        mOutputStream.writeInt(centralDirOffset); // offset of central directory
        mOutputStream.writeShort(0);
        mOutputStream.flush();
    }

    private void closeFiles() throws IOException {
        try {
            mZipFile.close();
        } finally {
            try {
                mRafInput.close();
            } finally {
                mOutputStream.close();
            }
        }

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
        ZipAlign zipAlign=new ZipAlign();
        File dir=outFile.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        zipAlign.zipAlign(inFile, outFile, alignment);
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

