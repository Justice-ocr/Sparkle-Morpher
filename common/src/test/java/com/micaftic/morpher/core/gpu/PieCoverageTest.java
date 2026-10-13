package com.micaftic.morpher.core.gpu;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Regression checks for the bounded renderer replacing coverage rasterization. */
class PieCoverageTest {
    @Test void eighthCircleUsesUpstreamBudget() {
        assertEquals(24, Pie.segmentCount(0, Pie.tau / 8));
    }
    @Test void circleAndOversizedSpanAreBounded() {
        assertEquals(192, Pie.segmentCount(0, Pie.tau));
        assertEquals(192, Pie.segmentCount(0, Pie.tau * 100));
    }
    @Test void emptyAndInvalidSpansDoNotDraw() {
        assertEquals(0, Pie.segmentCount(1, 1));
        assertEquals(0, Pie.segmentCount(Float.NaN, 1));
        assertEquals(0, Pie.segmentCount(0, Float.POSITIVE_INFINITY));
    }
    @Test void clockwiseHasSameBudget() {
        assertEquals(Pie.segmentCount(0, Pie.tau / 8), Pie.segmentCount(Pie.tau / 8, 0));
    }
}
