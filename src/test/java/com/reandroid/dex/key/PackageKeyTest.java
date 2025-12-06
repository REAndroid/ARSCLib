package com.reandroid.dex.key;

import com.reandroid.utils.collection.CollectionUtil;
import org.junit.Assert;
import org.junit.Test;

public class PackageKeyTest {

    @Test
    public void testCreate() {
        PackageKey key = PackageKey.of("Lcom/test/package/");
        Assert.assertNotNull(key);
        Assert.assertEquals(3, key.depth());

        Assert.assertTrue(key.equalsPackage("Lcom/test/package/"));
        Assert.assertTrue(key.equalsPackage(TypeKey.create("Lcom/test/package/Clazz;")));
        Assert.assertTrue(key.equalsPackage(TypeKey.create("[Lcom/test/package/Clazz;")));
        Assert.assertTrue(key.equalsPackage(TypeKey.create("[[Lcom/test/package/Clazz;")));

        Assert.assertEquals(key, PackageKey.of(TypeKey.create("Lcom/test/package/Clazz;")));
        Assert.assertEquals(key, PackageKey.of(TypeKey.create("[Lcom/test/package/Clazz;")));

        Assert.assertFalse(key.contains("Lcom/test/package2/"));
        Assert.assertFalse(key.contains("Lcom/test2/"));

        Assert.assertNull("Not suffixed with slash", PackageKey.of("Lcom/test/package"));
        Assert.assertNull("Not prefixed with L", PackageKey.of("com/test/package/"));
        Assert.assertNull("Type name", PackageKey.of("Lcom/test/package/Clazz;"));
        Assert.assertNull("Primitive type", PackageKey.of(TypeKey.create("I")));
        Assert.assertNull("Primitive array type", PackageKey.of(TypeKey.create("[I")));

        PackageKey parent1 = key.getParent();
        Assert.assertTrue("Parent level-1", parent1.equalsPackage("Lcom/test/"));
        Assert.assertTrue("Contains parent-1", key.contains(parent1));

        PackageKey parent2 = parent1.getParent();
        Assert.assertTrue("Parent level-2", parent2.equalsPackage("Lcom/"));
        Assert.assertTrue("Contains parent-2", key.contains(parent2));

        PackageKey parent3 = parent2.getParent();
        Assert.assertTrue("Parent level-3", parent3.equalsPackage("L"));
        Assert.assertTrue("Contains parent-3", key.contains(parent3));
        Assert.assertTrue("Root parent-3", parent3.isRootPackage());

        Assert.assertNull("Parent level-4", parent3.getParent());

        Assert.assertEquals("Parents iterator", 3, CollectionUtil.count(key.getParents()));

        PackageKey renamed = key.changeSimpleName("renamed");
        Assert.assertTrue("Renamed simple name", renamed.equalsPackage("Lcom/test/renamed/"));

        PackageKey child = key.child("child");
        Assert.assertTrue("Child", child.equalsPackage("Lcom/test/package/child/"));
    }

    @Test
    public void testReplace() {
        PackageKey key = PackageKey.of("Lcom/test/package/");

        PackageKey search = PackageKey.of("Lcom/test/package/");
        PackageKey replace = PackageKey.of("Lcom/test/new_pkg_name/");
        PackageKey result = key.replace(search, replace);
        Assert.assertEquals(result, replace);

        result = key.replace(null, replace);
        Assert.assertEquals(result, key);
        result = key.replace(search, null);
        Assert.assertEquals(result, key);
        result = key.replace(null, null);
        Assert.assertEquals(result, key);

        search = PackageKey.of("Lcom/test/");
        replace = PackageKey.of("Lcom/b/");
        result = key.replace(search, replace);
        Assert.assertTrue(result.equalsPackage("Lcom/b/package/"));

        search = PackageKey.of("Lcom/test/");
        replace = PackageKey.of("Lc/test/");
        result = key.replace(search, replace);
        Assert.assertTrue(result.equalsPackage("Lc/test/package/"));

        search = PackageKey.of("Lcom/test/");
        replace = PackageKey.of("Lc/b/");
        result = key.replace(search, replace);
        Assert.assertTrue(result.equalsPackage("Lc/b/package/"));

        search = PackageKey.of("L");
        replace = PackageKey.of("Lc/b/");
        result = key.replace(search, replace);
        Assert.assertTrue(result.equalsPackage("Lc/b/com/test/package/"));

        search = PackageKey.of("Lcom/test/");
        replace = PackageKey.of("L");
        result = key.replace(search, replace);
        Assert.assertTrue(result.equalsPackage("Lpackage/"));
    }
}
