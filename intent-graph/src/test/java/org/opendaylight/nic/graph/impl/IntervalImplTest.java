/*
 * Copyright (c) 2016 Yrineu Rodrigues and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.nic.graph.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

/**
 * Created by yrineu on 10/08/16.
 */
@RunWith(PowerMockRunner.class)
public class IntervalImplTest {

    @Test
    public void testCreateIntervalImplWithStartGreaterThanEnd() {
        final IntervalImpl interval = IntervalImpl.getInstance(4, 2);
        final boolean isNull = interval.isNull();

        Assert.assertEquals(Integer.MIN_VALUE, interval.start());
        Assert.assertTrue(isNull);
    }

    @Test
    public void testCreateIntervalImplWithJustOneParameterShouldReturnTheSameValueForStartAndEnd() {
        final int expected = 5;
        final IntervalImpl interval = IntervalImpl.getInstance(expected);

        Assert.assertEquals(expected, interval.start());
        Assert.assertEquals(expected, interval.end());
    }

    @Test
    public void testNotEqualsShouldReturnFalseWhenCreateTwoInstancesWithSameValue() {
        final boolean shouldBeNotEqual = IntervalImpl.getInstance(5).notEquals(IntervalImpl.getInstance(2));
        final boolean shouldBeEqual = IntervalImpl.getInstance(5).notEquals(IntervalImpl.getInstance(5));

        Assert.assertTrue(shouldBeNotEqual);
        Assert.assertFalse(shouldBeEqual);

        final boolean shouldBeTrue = IntervalImpl.getInstance(5, 3).notEquals(IntervalImpl.getInstance(2, 6));
        final boolean shouldBeFalse = IntervalImpl.getInstance(5, 3).notEquals(IntervalImpl.getInstance(5, 3));

        Assert.assertTrue(shouldBeTrue);
        Assert.assertFalse(shouldBeFalse);
    }

    @Test
    public void testGreaterThanShouldReturnTrueWhenStartOrEndAreGreaterThanAnotherIntervalInstance() {
        final boolean shouldBeGreaterThan = IntervalImpl.getInstance(5).greaterThan(IntervalImpl.getInstance(2));
        final boolean shouldNotBeGreaterThanToo = IntervalImpl.getInstance(5).greaterThan(IntervalImpl.getInstance(7));

        Assert.assertFalse(shouldBeGreaterThan);
        Assert.assertFalse(shouldNotBeGreaterThanToo);

        final boolean shouldBeTrue = IntervalImpl.getInstance(2, 6).greaterThan(IntervalImpl.getInstance(4, 5));
        final boolean shouldBeFalse = IntervalImpl.getInstance(2, 1).notEquals(IntervalImpl.getInstance(5, 3));

        Assert.assertTrue(shouldBeTrue);
        Assert.assertFalse(shouldBeFalse);

        final boolean isFalse = IntervalImpl.getInstance(7, 6).greaterThan(IntervalImpl.getInstance(4, 5));

        Assert.assertFalse(isFalse);
    }

    @Test
    public void testLessThanShouldReturnTrueWhenStartOrEndAreLessThanAnotherIntervalInstance() {
        final boolean shouldBeGreaterThan = IntervalImpl.getInstance(5).lessThan(IntervalImpl.getInstance(2));
        final boolean shouldBeLessThan = IntervalImpl.getInstance(5).lessThan(IntervalImpl.getInstance(7));

        Assert.assertFalse(shouldBeGreaterThan);
        Assert.assertFalse(shouldBeLessThan);

        final boolean shouldBeTrue = IntervalImpl.getInstance(4, 5).lessThan(IntervalImpl.getInstance(2, 6));
        final boolean shouldBeFalse = IntervalImpl.getInstance(2, 1).lessThan(IntervalImpl.getInstance(5, 3));

        Assert.assertTrue(shouldBeTrue);
        Assert.assertFalse(shouldBeFalse);

        final boolean isFalse = IntervalImpl.getInstance(7, 6).lessThan(IntervalImpl.getInstance(4, 5));

        Assert.assertFalse(isFalse);
    }

    @Test
    public void testGreaterThanOrEqual() {
        final boolean shouldBeGreaterThan = IntervalImpl.getInstance(1, 6).greaterThanOrEqual(IntervalImpl.getInstance(2, 4));
        final boolean shouldBeEqual = IntervalImpl.getInstance(5).greaterThanOrEqual(IntervalImpl.getInstance(5));

        Assert.assertTrue(shouldBeGreaterThan);
        Assert.assertTrue(shouldBeEqual);

        final boolean shouldNotBeGreaterThan = IntervalImpl.getInstance(2, 4).greaterThanOrEqual(IntervalImpl.getInstance(1, 6));

        Assert.assertFalse(shouldNotBeGreaterThan);
    }

    @Test
    public void testLessThanOrEqual() {
        final boolean shouldBeLessThan = IntervalImpl.getInstance(2, 4).lessThanOrEqual(IntervalImpl.getInstance(1,6));
        final boolean shouldBeEqual = IntervalImpl.getInstance(5).lessThanOrEqual(IntervalImpl.getInstance(5));

        Assert.assertTrue(shouldBeLessThan);
        Assert.assertTrue(shouldBeEqual);

        final boolean shouldNotBeLessThan = IntervalImpl.getInstance(1, 6).lessThanOrEqual(IntervalImpl.getInstance(2, 4));

        Assert.assertFalse(shouldNotBeLessThan);
    }

    @Test
    public void testAnd() {
        final IntervalImpl expectedIntervalOverlap = IntervalImpl.getInstance(2, 3);

        final IntervalImpl shouldBeIntervalOverlap = IntervalImpl.getInstance(2, 6).and(IntervalImpl.getInstance(1, 3));
        final IntervalImpl shouldBeIntervalNull = IntervalImpl.getInstance(-1, -6).and(IntervalImpl.getInstance(-1,-3));
        final IntervalImpl shouldBeEqual = IntervalImpl.getInstance(5).and(IntervalImpl.getInstance(5));

        Assert.assertEquals(shouldBeIntervalOverlap, expectedIntervalOverlap);
        Assert.assertEquals(shouldBeIntervalNull, IntervalImpl.INTERVAL_NULL);
        Assert.assertEquals(shouldBeEqual, IntervalImpl.getInstance(5));
    }

    @Test
    public void testSubWithOverlapShouldReturnAListContainingJustItSelf() {
        final IntervalImpl intervalWithOverLap = IntervalImpl.getInstance(Integer.MIN_VALUE, Integer.MIN_VALUE);
        final IntervalImpl intervalNull = IntervalImpl.INTERVAL_NULL;
        final List<IntervalImpl> shouldContainsJustItSelf = intervalWithOverLap.sub(intervalNull);

        Assert.assertEquals(shouldContainsJustItSelf.get(0),intervalWithOverLap);
        Assert.assertEquals(shouldContainsJustItSelf.size(), 1);
    }

    @Test
    public void testSubBetweenTwoEqualIntervalsShouldReturnAListWithAIntervalNull() {
        final IntervalImpl intervalOne = IntervalImpl.getInstance(2);
        final IntervalImpl intervalTwo = IntervalImpl.getInstance(2);

        final List<IntervalImpl> shouldContainsNullInterval = intervalOne.sub(intervalTwo);

        Assert.assertEquals(shouldContainsNullInterval.get(0), IntervalImpl.INTERVAL_NULL);
        Assert.assertEquals(shouldContainsNullInterval.size(), 1);
    }

    @Test
    public void testSubBetweenTwoIntervalsWithEqualStartsShouldReturnAListContainingANewValidInterval() {
        final IntervalImpl intervalOne = IntervalImpl.getInstance(1, 7);
        final IntervalImpl intervalTwo = IntervalImpl.getInstance(1, 4);

        final List<IntervalImpl> shouldContainsNewValidInterval = intervalOne.sub(intervalTwo);

        final IntervalImpl result = shouldContainsNewValidInterval.get(0);
        final int expectedEnd = result.end();
        final int initialEnd = intervalOne.end();

        final boolean expectedEndShouldBeEqualToInitial = (initialEnd == expectedEnd);
        final boolean startShouldBeLessThanEnd = (result.start() < result.end());

        Assert.assertTrue(expectedEndShouldBeEqualToInitial);
        Assert.assertTrue(startShouldBeLessThanEnd);
    }
}
