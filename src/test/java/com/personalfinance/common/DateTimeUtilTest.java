package com.personalfinance.common;

import com.personalfinance.common.util.DateTimeUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DateTimeUtilTest {

    @Test
    void testStartAndEndOfDay() {
        LocalDate date = LocalDate.of(2024, 11, 18);
        OffsetDateTime start = DateTimeUtil.startOfDay(date);
        OffsetDateTime end = DateTimeUtil.endOfDay(date);

        assertNotNull(start);
        assertNotNull(end);
        assertEquals(2024, start.getYear());
        assertEquals(11, start.getMonthValue());
        assertEquals(18, start.getDayOfMonth());
        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());
    }

    @Test
    void testParseDateTimeString() {
        String sepayDate = "2024-11-18 10:30:00";
        OffsetDateTime parsed = DateTimeUtil.parseDateTimeString(sepayDate);

        assertNotNull(parsed);
        assertEquals(2024, parsed.getYear());
        assertEquals(11, parsed.getMonthValue());
        assertEquals(18, parsed.getDayOfMonth());
    }
}
