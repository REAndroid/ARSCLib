package com.reandroid.utils.collection;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class LinkedIteratorTest {

    @Test
    public void testFileParents() {
        File file = new File(
                File.separatorChar + "H" +
                        File.separatorChar + "G" +
                        File.separatorChar + "F" +
                        File.separatorChar + "E" +
                        File.separatorChar + "D" +
                        File.separatorChar + "C" +
                        File.separatorChar + "B" +
                        File.separatorChar + "A");

        Iterator<File> iterator = LinkedIterator.of(file, File::getParentFile);
        List<File> fileList = CollectionUtil.collect(iterator);
        Assert.assertEquals("itemList size", 8, fileList.size());

        iterator = LinkedIterator.of(true, file, File::getParentFile);

        List<File> fileListIncludingSelf = CollectionUtil.collect(iterator);
        Assert.assertEquals("fileListIncludingSelf size", 9, fileListIncludingSelf.size());
    }
}
