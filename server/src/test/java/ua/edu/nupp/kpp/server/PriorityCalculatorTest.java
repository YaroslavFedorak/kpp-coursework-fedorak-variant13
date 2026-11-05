package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriorityCalculatorTest {

    @Test
    void guestImpactIsNormalizedToF1() {
        assertEquals(80.0, PriorityCalculator.factor1(8));
        assertEquals(0.0, PriorityCalculator.factor1(0));
    }

    @Test
    void severityIsNormalizedToF2() {
        assertEquals(0.0, PriorityCalculator.factor2(1));
        assertEquals(50.0, PriorityCalculator.factor2(3));
        assertEquals(100.0, PriorityCalculator.factor2(5));
    }

    @Test
    void downtimeIsNormalizedToF3() {
        assertEquals(25.0, PriorityCalculator.factor3(12));
        assertEquals(6.25, PriorityCalculator.factor3(3));
    }

    @Test
    void downtimeLongerThan48HoursIsLimitedTo100() {
        assertEquals(100.0, PriorityCalculator.factor3(100));
    }

    @Test
    void integralIndexForDemoRequestIs645() {
        assertEquals(64.5, PriorityCalculator.calculate(80, 75, 25));
    }

    @Test
    void factorAbove100IsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(101, 10, 10));
    }

    @Test
    void severityOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.factor2(6));
    }

    @Test
    void priorityLevelDependsOnIndex() {
        assertEquals("КРИТИЧНИЙ", PriorityCalculator.level(80));
        assertEquals("ВИСОКИЙ", PriorityCalculator.level(64.5));
        assertEquals("СЕРЕДНІЙ", PriorityCalculator.level(30.31));
        assertEquals("НИЗЬКИЙ", PriorityCalculator.level(20.75));
    }
}
