package com.vapor.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ReviewStatus 单元测试。
 */
class ReviewStatusTest {

    @Test
    void testReviewStatus_Values() {
        assertEquals("PENDING", ReviewStatus.PENDING.name());
        assertEquals("APPROVED", ReviewStatus.APPROVED.name());
        assertEquals("REJECTED", ReviewStatus.REJECTED.name());
    }

    @Test
    void testValueOf_ValidString() {
        assertEquals(ReviewStatus.PENDING, ReviewStatus.valueOf("PENDING"));
        assertEquals(ReviewStatus.APPROVED, ReviewStatus.valueOf("APPROVED"));
        assertEquals(ReviewStatus.REJECTED, ReviewStatus.valueOf("REJECTED"));
    }

    @Test
    void testValueOf_InvalidString() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReviewStatus.valueOf("INVALID");
        });
    }
}
