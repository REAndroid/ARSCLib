 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  *
  *
  * This class contains code from "apksigner" and I couldn't find the
  * original repo/author to credit here.
  */
package com.reandroid.archive;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

 public class ZipSerializer {
    public static final int ZIP_ENTRY_VERSION = 20;
    public static final int ZIP_ENTRY_USES_DATA_DESCR = 0x0008;

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

    private final List<InputSource> mSourceList;
    private final List<XEntry> mEntries = new ArrayList<>();
    private final ZipAlign zipAlign;
    private WriteProgress writeProgress;
    private WriteInterceptor writeInterceptor;
    public ZipSerializer(List<InputSource> sourceList, ZipAlign zipAlign){
        this.mSourceList=sourceList;
        this.zipAlign = zipAlign;
    }

    public void setWriteInterceptor(WriteInterceptor writeInterceptor) {
        this.writeInterceptor = writeInterceptor;
    }
    public void setWriteProgress(WriteProgress writeProgress){
        this.writeProgress=writeProgress;
    }
    public long writeZip(File outZip) throws IOException{
        File dir=outZip.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        File tmp=toTmpFile(outZip);
        OutputStream outputStream=new BufferedOutputStream(new FileOutputStream(tmp), 32 * 1024);
        long length= writeZip(outputStream);
        outputStream.close();
        outZip.delete();
        tmp.renameTo(outZip);
        return length;
    }
    private File toTmpFile(File file){
        File dir=file.getParentFile();
        String name=file.getName()+".tmp";
        return new File(dir, name);
    }
    public long writeZip(OutputStream outputStream) throws IOException{
        WriteProgress progress=writeProgress;
        FilterOutputStreamEx zipOutputStream=new FilterOutputStreamEx(outputStream);
        for(InputSource inputSource:mSourceList){
            inputSource = interceptWrite(inputSource);
            if(inputSource==null){
                continue;
            }
            if(progress!=null){
                progress.onCompressFile(inputSource.getAlias(), inputSource.getMethod(), zipOutputStream.totalWritten);
            }
            write(zipOutputStream, inputSource);
            inputSource.disposeInputSource();
        }
        buildCentralDirectory(zipOutputStream);
        zipOutputStream.close();
        return zipOutputStream.totalWritten;
    }

    private void writeDescriptor(FilterOutputStreamEx mOutputStream, ZipEntry entry) throws IOException{
        if (entry.getCompressedSize() != -1) {
            mOutputStream.writeInt(entry.getCrc());
            mOutputStream.writeInt(entry.getCompressedSize());
            mOutputStream.writeInt(entry.getSize());
        } else {
            mOutputStream.write(new byte[12]);
        }
    }

    private void write(FilterOutputStreamEx mOutputStream, InputSource inputSource) throws IOException{
        final ZipEntry entry=createZipEntry(inputSource);

        int flags = 1 << 11;
        if (entry.getMethod() != ZipEntry.STORED) {
            flags |= ZIP_ENTRY_USES_DATA_DESCR;
        }

        final long outputEntryHeaderOffset = mOutputStream.totalWritten;
        final long outputEntryDataOffset = ZipDeserializer.dataOffset(outputEntryHeaderOffset, entry);
        int padding = 0;
        if (zipAlign != null) {
            padding = zipAlign.alignEntry(entry, outputEntryDataOffset);
        }
        if (entry.getExtra() == null) {
            entry.setExtra(new byte[0]);
        }

        final XEntry xentry = new XEntry(entry, outputEntryHeaderOffset, flags, padding);
        mEntries.add(xentry);

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

        writeDescriptor(mOutputStream, entry);

        mOutputStream.writeShort(entry.getName().getBytes(StandardCharsets.UTF_8).length);
        mOutputStream.writeShort(entry.getExtra().length);
        mOutputStream.write(entry.getName().getBytes(StandardCharsets.UTF_8));
        mOutputStream.write(entry.getExtra());

        if (entry.getMethod() == ZipEntry.STORED) {
            inputSource.write(mOutputStream);
        } else {
            InputSource.WriteCompressedResult result = inputSource.writeCompressed(mOutputStream);
            entry.setCompressedSize(result.compressedSize);
            entry.setSize(result.uncompressedSize);
            entry.setCrc(result.crc);
        }

        if ((flags & ZIP_ENTRY_USES_DATA_DESCR) != 0) {
            mOutputStream.writeInt(ZipEntry.EXTSIG);
            writeDescriptor(mOutputStream, entry);
        }
    }
    private ZipEntry createZipEntry(InputSource inputSource) throws IOException {
        String name=inputSource.getAlias();
        ZipEntry zipEntry=new ZipEntry(name);

        int method = inputSource.getMethod();
        zipEntry.setMethod(method);
        if (method == ZipEntry.STORED) {
            long length = inputSource.getLength();
            zipEntry.setCrc(inputSource.getCrc());
            zipEntry.setSize(length);
            zipEntry.setCompressedSize(length);
        }
        return zipEntry;
    }
     private void buildCentralDirectory(FilterOutputStreamEx mOutputStream) throws IOException {
         final long centralDirOffset = mOutputStream.totalWritten;
         final int entryCount = mEntries.size();
         for (int i = 0; i < entryCount;  i++) {
             XEntry xentry = mEntries.get(i);
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

     private InputSource interceptWrite(InputSource inputSource){
        WriteInterceptor interceptor=writeInterceptor;
        if(interceptor!=null){
            return interceptor.onWriteArchive(inputSource);
        }
        return inputSource;
    }
}
