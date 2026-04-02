package com.reandroid.utils.collection;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class StartStopIteratorTest {

    @Test
    public void testStartStop() {
        List<String> list = CollectionUtil.asList(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H"
        );
        Predicate<String> startPredicate = "C"::equals;
        Predicate<String> stopPredicate = "F"::equals;

        Iterator<String> sourceIterator = list.iterator();
        Iterator<String> iterator = StartStopIterator.of(sourceIterator, startPredicate, stopPredicate);
        List<String> C_to_F_list = CollectionUtil.collect(iterator);
        Assert.assertTrue("C_to_F_list sourceIterator", sourceIterator.hasNext());

        Assert.assertEquals("C_to_F_list size", 4, C_to_F_list.size());

        Assert.assertEquals("C_to_F_list(0)", "C", C_to_F_list.get(0));
        Assert.assertEquals("C_to_F_list(1)", "D", C_to_F_list.get(1));
        Assert.assertEquals("C_to_F_list(2)", "E", C_to_F_list.get(2));
        Assert.assertEquals("C_to_F_list(3)", "F", C_to_F_list.get(3));
    }

    @Test
    public void testStart() {
        List<String> list = CollectionUtil.asList(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H"
        );
        Predicate<String> startPredicate = "C"::equals;

        Iterator<String> sourceIterator = list.iterator();
        Iterator<String> iterator = StartStopIterator.start(sourceIterator, startPredicate);
        List<String> C_to_END_list = CollectionUtil.collect(iterator);

        Assert.assertFalse("C_to_END_list sourceIterator", sourceIterator.hasNext());

        Assert.assertEquals("C_to_F_list size", 6, C_to_END_list.size());

        Assert.assertEquals("C_to_END_list(0)", "C", C_to_END_list.get(0));
        Assert.assertEquals("C_to_END_list(1)", "D", C_to_END_list.get(1));
        Assert.assertEquals("C_to_END_list(2)", "E", C_to_END_list.get(2));
        Assert.assertEquals("C_to_END_list(3)", "F", C_to_END_list.get(3));
        Assert.assertEquals("C_to_END_list(4)", "G", C_to_END_list.get(4));
        Assert.assertEquals("C_to_END_list(5)", "H", C_to_END_list.get(5));

    }

    @Test
    public void testStop() {
        List<String> list = CollectionUtil.asList(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H"
        );
        Predicate<String> stopPredicate = "E"::equals;

        Iterator<String> sourceIterator = list.iterator();
        Iterator<String> iterator = StartStopIterator.stop(sourceIterator, stopPredicate);
        List<String> START_to_E_list = CollectionUtil.collect(iterator);

        Assert.assertTrue("START_to_E_list sourceIterator", sourceIterator.hasNext());

        Assert.assertEquals("START_to_E_list size", 5, START_to_E_list.size());

        Assert.assertEquals("START_to_E_list(0)", "A", START_to_E_list.get(0));
        Assert.assertEquals("START_to_E_list(1)", "B", START_to_E_list.get(1));
        Assert.assertEquals("START_to_E_list(2)", "C", START_to_E_list.get(2));
        Assert.assertEquals("START_to_E_list(3)", "D", START_to_E_list.get(3));
        Assert.assertEquals("START_to_E_list(4)", "E", START_to_E_list.get(4));
    }

    @Test
    public void testStopExclude() {
        List<String> list = CollectionUtil.asList(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H"
        );
        Predicate<String> stopPredicate = "E"::equals;

        Iterator<String> sourceIterator = list.iterator();
        Iterator<String> iterator = StartStopIterator.stopExclude(sourceIterator, stopPredicate);
        List<String> START_to_D_list = CollectionUtil.collect(iterator);

        Assert.assertTrue("START_to_D_list sourceIterator", sourceIterator.hasNext());

        Assert.assertEquals("START_to_D_list size", 4, START_to_D_list.size());

        Assert.assertEquals("START_to_D_list(0)", "A", START_to_D_list.get(0));
        Assert.assertEquals("START_to_D_list(1)", "B", START_to_D_list.get(1));
        Assert.assertEquals("START_to_D_list(2)", "C", START_to_D_list.get(2));
        Assert.assertEquals("START_to_D_list(3)", "D", START_to_D_list.get(3));
    }
}
