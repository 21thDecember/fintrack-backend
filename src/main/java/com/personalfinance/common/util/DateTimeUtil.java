package com.personalfinance.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * Tiện ích hỗ trợ xử lý ngày tháng theo chuẩn ISO và UTC
 */
public final class DateTimeUtil {

    public static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");
    public static final ZoneOffset ZONE_OFFSET_VN = ZoneOffset.ofHours(7);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtil() {
    }

    public static OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    public static OffsetDateTime startOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atStartOfDay(ZONE_VN).toOffsetDateTime();
    }

    public static OffsetDateTime endOfDay(LocalDate date) {
        if (date == null) return null;
        return date.atTime(LocalTime.MAX).atZone(ZONE_VN).toOffsetDateTime();
    }

    public static OffsetDateTime startOfWeek(LocalDate date) {
        if (date == null) date = LocalDate.now(ZONE_VN);
        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return startOfDay(monday);
    }

    public static OffsetDateTime endOfWeek(LocalDate date) {
        if (date == null) date = LocalDate.now(ZONE_VN);
        LocalDate sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return endOfDay(sunday);
    }

    public static OffsetDateTime startOfMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        return startOfDay(firstDay);
    }

    public static OffsetDateTime endOfMonth(int year, int month) {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        LocalDate lastDay = firstDay.with(TemporalAdjusters.lastDayOfMonth());
        return endOfDay(lastDay);
    }

    public static OffsetDateTime parseDateTimeString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return nowUtc();
        }
        text = text.trim();
        try {
            return OffsetDateTime.parse(text);
        } catch (Exception ignored) {
        }
        try {
            // E.g., SePay format "2024-11-18 10:30:00"
            return LocalDate.parse(text.substring(0, 10))
                    .atTime(LocalTime.parse(text.substring(11, 19)))
                    .atZone(ZONE_VN)
                    .toOffsetDateTime();
        } catch (Exception ignored) {
        }
        return nowUtc();
    }
}
