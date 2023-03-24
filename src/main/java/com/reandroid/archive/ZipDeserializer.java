package com.reandroid.archive;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;

public class ZipDeserializer {
    private final FileChannel channel;
    private FilterInputStreamEx inputStream;

    public ZipDeserializer(File file) throws IOException {
        this.channel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
    }

    public static long dataOffset(long headerOffset, ZipEntry entry) {
        final byte[] extra = entry.getExtra();
        return headerOffset + ZipEntry.LOCHDR + (extra != null ? extra.length : 0) + entry.getName().getBytes(StandardCharsets.UTF_8).length;
    }

    public void closeChannel() throws IOException {
        channel.close();
    }

    public List<InputSource> listInputSources() throws IOException {
        inputStream = new FilterInputStreamEx(Channels.newInputStream(channel));
        ArrayList<InputSource> list = new ArrayList<>();
        channel.position(channel.size() - ZipEntry.ENDHDR);

        int commentSize = 0;
        while (inputStream.readInt() != ZipEntry.ENDSIG) {
            channel.position(channel.position() - 5);
            if (commentSize > 65535) {
                throw new IOException("Could not find central directory");
            }
            commentSize++;
        }

        skip(ZipEntry.ENDTOT - 4);
        int numRecords = inputStream.readShort(); // number of entries
        skip(4); // size of central directory
        long centralDirectoryOffset = inputStream.readInt(); // central directory offset.

        channel.position(centralDirectoryOffset);
        int sort = 0;
        for (int i = 0; i < numRecords; i++) {
            ZipEntrySource inputSource = readCentralDirRecord();
            if (inputSource != null) {
                inputSource.setSort(sort);
                list.add(inputSource);
                sort += 1;
            }
        }
        return list;
    }

    public Map<String, InputSource> mapInputSources() throws IOException {
        final LinkedHashMap<String, InputSource> map = new LinkedHashMap<>();
        for (InputSource inputSource : listInputSources()) {
            map.put(inputSource.getName(), inputSource);
        }
        return map;
    }

    private void validateSignature(long actual, long expected, String recordType) throws IOException {
        if (actual != expected) {
            throw new IOException("Invalid " + recordType + " signature.");
        }
    }

    private ZipEntrySource readCentralDirRecord() throws IOException {
        validateSignature(inputStream.readInt(), ZipEntry.CENSIG, "central directory");
        skip(6); // versions and general purpose bit flag.
        int method = inputStream.readShort(); // compression method
        skip(4); // modification date/time
        long crc = inputStream.readInt(); // crc-32
        long compressedSize = inputStream.readInt(); // compressed size
        long uncompressedSize = inputStream.readInt(); // uncompressed size
        int nameLength = inputStream.readShort(); // name length
        int extraLength = inputStream.readShort(); // extra field length (central directory)
        int commentLength = inputStream.readShort(); // comment length
        skip(8); // attributes and disk number.
        long headerOffset = inputStream.readInt(); // location of the entry
        String name = new String(inputStream.readBytes(nameLength)); // name
        byte[] extra = readLocalExtraField(headerOffset, nameLength); // extra (from local entry)
        skip(extraLength); // skip the extra field of the central directory.
        String comment = new String(inputStream.readBytes(commentLength)); // comment

        ZipEntry entry = new ZipEntry(name);
        entry.setExtra(extra);
        entry.setComment(comment);
        entry.setMethod(method);
        entry.setCompressedSize(compressedSize);
        entry.setSize(uncompressedSize);
        entry.setCrc(crc);
        if (entry.isDirectory()) {
            return null;
        }

        return new ZipEntrySource(entry, headerOffset, channel);
    }

    private byte[] readLocalExtraField(long headerOffset, int nameLength) throws IOException {
        long previousPosition = channel.position();
        byte[] extra;
        try {
            channel.position(headerOffset);
            validateSignature(inputStream.readInt(), ZipEntry.LOCSIG, "local file");

            // skip to and read the length of the extra field.
            skip(ZipEntry.LOCEXT - 4);
            final int extraLength = inputStream.readShort();

            skip(nameLength);
            extra = inputStream.readBytes(extraLength);
        } finally {
            channel.position(previousPosition);
        }
        return extra;
    }
    private void skip(long n) throws IOException {
        channel.position(channel.position() + n);
    }

    private static class FilterInputStreamEx extends FilterInputStream {
        public FilterInputStreamEx(InputStream inputStream) {
            super(inputStream);
        }

        public long readInt() throws IOException {
            final byte[] bytes = readBytes(4);
            return ((long) (0xff & bytes[3]) << 24) | ((0xff & bytes[2]) << 16) |
                    ((0xff & bytes[1]) << 8) | (0xff & bytes[0]);
        }

        public int readShort() throws IOException {
            final byte[] bytes = readBytes(2);
            return (((0xff & bytes[1]) << 8) | (bytes[0] & 0xff));
        }

        public byte[] readBytes(int n) throws IOException {
            final byte[] bytes = new byte[n];
            read(bytes);
            return bytes;
        }
    }
}
