package com.base.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComparisonUtilsTests {

    @Test
    void isBetween_valueWithinRange_shouldReturnTrue() {
        assertTrue(ComparisonUtils.isBetween(5, 1, 10));
        assertTrue(ComparisonUtils.isBetween(1, 1, 10));
        assertTrue(ComparisonUtils.isBetween(10, 1, 10));
    }

}
