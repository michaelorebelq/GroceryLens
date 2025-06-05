package com.example.grocerylens;

import org.junit.Test;

import static org.junit.Assert.*;
//(original written tests)
public class ProgressTest {

    @Test
    public void testProgressPercentage() {
        int result = Progress.getProgressPercentage(25, 50);
        assertEquals(50, result);
    }

    @Test
    public void testProgressStopsAt100() {
        int result = Progress.getProgressPercentage(80, 50); // 160%
        assertEquals(100, result);
    }

    @Test
    public void testUserBadgeLevels() {
        assertEquals(1, Progress.getUserBadgeLevel(5));
        assertEquals(2, Progress.getUserBadgeLevel(10));
        assertEquals(3, Progress.getUserBadgeLevel(25));
        assertEquals(4, Progress.getUserBadgeLevel(50));
        assertEquals(5, Progress.getUserBadgeLevel(100));
    }
}
