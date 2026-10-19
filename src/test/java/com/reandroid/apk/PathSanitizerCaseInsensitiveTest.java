package com.reandroid.apk;

import com.reandroid.archive.InputSource;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PathSanitizerCaseInsensitiveTest {

    private static InputSource source(final String name) {
        return new InputSource(name) {
            @Override
            public InputStream openStream() {
                return new ByteArrayInputStream(
                    name.getBytes(java.nio.charset.StandardCharsets.UTF_8)
                );
            }
        };
    }

    private static int countCollisions(List<InputSource> sources) {
        Set<String> names = new HashSet<>();
        int collisions = 0;
        for (InputSource source : sources) {
            String key = source.getAlias()
                .toLowerCase(Locale.ROOT);
            if (!names.add(key)) {
                collisions++;
            }
        }
        return collisions;
    }

    @Test
    public void testCaseInsensitivePaths() throws Exception {
        List<InputSource> sources = new ArrayList<>();

        sources.add(source("res/Zy.xml"));
        sources.add(source("res/zy.xml"));
        sources.add(source("res/-n.xml"));
        sources.add(source("res/-N.xml"));
        sources.add(source("res/1b.xml"));
        sources.add(source("res/1B.xml"));
        sources.add(source("res/unique.xml"));

        int originalCount = sources.size();

        Assert.assertEquals(3, countCollisions(sources));

        PathSanitizer sanitizer = new PathSanitizer(sources);
        sanitizer.setCaseInsensitive(true);
        sanitizer.sanitize();

        Assert.assertEquals(originalCount, sources.size());
        Assert.assertEquals(0, countCollisions(sources));

        for (InputSource source : sources) {
            Assert.assertNotNull(source.getAlias());
            Assert.assertFalse(source.getAlias().isEmpty());
            Assert.assertEquals(
                source.getName(),
                new String(
                    source.openStream().readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8
                )
            );
        }

        System.out.println("Case-insensitive regression test passed");
    }
}
