package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.utils.collection.CollectionUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class KeyPairTest {

    @Test
    public void testParse() throws IOException {
        SmaliReader reader = SmaliReader.of(
                "Lcom/test/package1/ = Lcom/test/package2/\n" +
                        "Lcom/test/package/Clazz1;=Lcom/test/package/Class2;\n" +
                        "Lcom/test/package/Clazz1;->field1:I = Lcom/test/package/Clazz1;->field2:I\n" +
                        "Lcom/test/package/Clazz1;->method1()V=Lcom/test/package/Clazz1;->method2()V"
        );
        KeyPair<?, ?> pair = KeyPair.read(reader);
        Assert.assertNotNull("KeyPair", pair);
        Assert.assertNotNull("First", pair.getFirst());
        Assert.assertNotNull("Second", pair.getSecond());

        pair = KeyPair.read(reader);
        Assert.assertNotNull("KeyPair", pair);
        Assert.assertNotNull("First", pair.getFirst());
        Assert.assertNotNull("Second", pair.getSecond());

        pair = KeyPair.read(reader);
        Assert.assertNotNull("KeyPair", pair);
        Assert.assertNotNull("First", pair.getFirst());
        Assert.assertNotNull("Second", pair.getSecond());

        pair = KeyPair.read(reader);
        Assert.assertNotNull("KeyPair", pair);
        Assert.assertNotNull("First", pair.getFirst());
        Assert.assertNotNull("Second", pair.getSecond());
    }

}
