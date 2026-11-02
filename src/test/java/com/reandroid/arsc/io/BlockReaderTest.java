package com.reandroid.arsc.io;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BlockReaderTest {

    @Test
    public void positionTest() throws IOException {
        int length = 0x80;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }
        BlockReader reader = new BlockReader(bytes);
        Assert.assertEquals(0, reader.position());
        Assert.assertEquals(length, reader.available());
        Assert.assertEquals(0, reader.read());
        Assert.assertEquals(1, reader.read());
        Assert.assertEquals(2, reader.position());
        reader.position(2);
        Assert.assertEquals(2, reader.position());
        reader.position(0);
        Assert.assertEquals(0, reader.position());
        reader.offset(4);
        Assert.assertEquals(4, reader.position());
        reader.offset(1);
        Assert.assertEquals(5, reader.position());
        reader.offset(-3);
        Assert.assertEquals(2, reader.position());
        reader.position(0);
        Assert.assertEquals(0, reader.position());
        for (int i = 0; i < length; i++) {
            reader.position(i);
            Assert.assertEquals(i, reader.position());
            Assert.assertEquals(i, reader.read());
        }
        reader.position(length);
        Assert.assertEquals(length, reader.position());
    }

    @Test
    public void readTest() throws IOException {
        int length = 0x60;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }
        length = length - 2;
        BlockReader reader = new BlockReader(new ByteArrayInputStream(bytes), length);
        Assert.assertEquals("Reader buffer length",
                length, reader.buffer().length);

        Assert.assertTrue(reader.isAvailable());

        int readLength = 3;
        byte[] readBytes = new byte[readLength];
        for (int i = 0; i <= length - readLength; i += readLength) {

            Assert.assertEquals("Read length",
                    readLength, reader.read(readBytes, 0, readLength));

            for (int j = 0; j < readLength; j++) {
                int value = i + j;
                Assert.assertEquals("Read bytes at: " + value,
                        value, readBytes[j] & 0xff);
            }
        }
        reader.position(0);
        for (int i = 0; i <= length - readLength; i += readLength) {

            Assert.assertEquals("Read length",
                    readLength, reader.readFully(readBytes));

            for (int j = 0; j < readLength; j++) {
                int value = i + j;
                Assert.assertEquals("Read fully bytes at: " + value,
                        value, readBytes[j] & 0xff);
            }
        }
        Assert.assertTrue(reader.available() < readLength);
        reader.position(reader.length() - readLength + 1);
        Assert.assertTrue(reader.read(readBytes, 0, readLength) < readLength);
        Assert.assertFalse(reader.isAvailable());

        reader.position(reader.length() - readBytes.length + 1);
        IOException exception;
        try {
            reader.readFully(readBytes);
            exception = null;
        } catch (IOException e) {
            exception = e;
        }
        Assert.assertNotNull("Exception for readFully", exception);
    }

    @Test
    public void createSubReaderTest() throws IOException {
        int length = 0x80;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }
        BlockReader reader = new BlockReader(new ByteArrayInputStream(bytes), length);
        Assert.assertEquals("Reader length",
                length, reader.length());
        int third = length / 3;
        reader.position(third);
        BlockReader subReader = reader.create(third);
        Assert.assertTrue(subReader.isAvailable());

        Assert.assertEquals("Sub reader position",
                0, subReader.position());
        Assert.assertEquals("Sub reader available",
                third, subReader.available());

        int readLength = 3;
        byte[] readBytes = new byte[readLength];
        while (subReader.available() >= readLength) {

            int i = subReader.position();

            Assert.assertEquals("Sub read length",
                    readLength, subReader.read(readBytes, 0, readLength));

            for (int j = 0; j < readLength; j++) {
                int value = third + i + j;
                Assert.assertEquals("Sub read bytes at: " + value,
                        value, readBytes[j] & 0xff);
            }
        }
        Assert.assertEquals("Reader position",
                third, reader.position());
        subReader.close();
        Assert.assertFalse(subReader.isAvailable());
        Assert.assertTrue(reader.isAvailable());
    }

    @Test
    public void peekNumbersTest() throws IOException {
        int length = 0x80;
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) i;
        }

        BlockReader reader = new BlockReader(bytes);

        reader.position(0);
        while (reader.available() >= 1) {
            int position = reader.position();
            int peek = reader.peekUByte();
            Assert.assertEquals(position, reader.position());
            byte[] valueBytes = new byte[1];
            reader.readFully(valueBytes);
            int value = valueBytes[0] & 0xff;
            Assert.assertEquals("peekUByte at: " + position, value, peek);
        }

        reader.position(0);
        while (reader.available() >= 2) {
            int position = reader.position();
            int peek = reader.peekUShort();
            Assert.assertEquals(position, reader.position());
            byte[] valueBytes = new byte[2];
            reader.readFully(valueBytes);
            int value = valueBytes[0] & 0xff | (valueBytes[1] & 0xff) << 8;
            Assert.assertEquals("peekUShort at: " + position, value, peek);
        }

        reader.position(0);
        while (reader.available() >= 4) {
            int position = reader.position();
            int peek = reader.peekInteger();
            Assert.assertEquals(position, reader.position());
            byte[] valueBytes = new byte[4];
            reader.readFully(valueBytes);
            int value = valueBytes[0] & 0xff | (valueBytes[1] & 0xff) << 8
                    | (valueBytes[2] & 0xff) << 16 | (valueBytes[3] & 0xff) << 24;
            Assert.assertEquals("peekInteger at: " + position, value, peek);
        }
    }
}
