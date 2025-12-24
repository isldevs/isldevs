package com.base.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTests {

    @Test
    void nameOfMonth_shouldReturnEnglishFullName() {
        String monthName = DateUtils.nameOfMonth(1, TextStyle.FULL, Locale.ENGLISH);
        assertEquals("January", monthName);
    }

    @Test
    void numOfMonths_localDate() {
        LocalDate start = LocalDate.of(2024, Month.JANUARY, 10);
        LocalDate end = LocalDate.of(2024, Month.MARCH, 5);
        int months = DateUtils.numOfMonths(start, end);
        assertEquals(3, months);
    }

    @Test
    void numOfMonths_localDateTime() {
        LocalDateTime start = LocalDateTime.of(2024, Month.MAY, 10, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, Month.JULY, 1, 8, 0);
        int months = DateUtils.numOfMonths(start, end);
        assertEquals(3, months);
    }

    @Test
    void numOfMonths_date() {
        LocalDate startDate = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(2024, Month.DECEMBER, 31);
        Date start = DateUtils.toDate(startDate);
        Date end = DateUtils.toDate(endDate);
        int months = DateUtils.numOfMonths(start, end);
        assertEquals(12, months);
    }

    @Test
    void toLocalDate_fromDate() {
        LocalDate original = LocalDate.of(2024, 6, 10);
        Date date = DateUtils.toDate(original);
        LocalDate result = DateUtils.toLocalDate(date);
        assertEquals(original, result);
    }

    @Test
    void toDate_fromLocalDateTime() {
        LocalDateTime now = LocalDateTime.of(2024, 6, 10, 12, 0);
        Date result = DateUtils.toDate(now);
        assertNotNull(result);
    }

    @Test
    void dayMonthYear_fromLocalDate() {
        LocalDate date = LocalDate.of(2024, 8, 20);
        assertEquals(20, DateUtils.day(date));
        assertEquals(8, DateUtils.month(date));
        assertEquals(2024, DateUtils.year(date));
    }

    @Test
    void dayMonthYear_fromDate() {
        LocalDate date = LocalDate.of(2022, 3, 15);
        Date d = DateUtils.toDate(date);
        assertEquals(15, DateUtils.day(d));
        assertEquals(3, DateUtils.month(d));
        assertEquals(2022, DateUtils.year(d));
    }

    @Test
    void getPart_localDate() {
        LocalDate date = LocalDate.of(2024, 7, 5);
        assertEquals(5, DateUtils.getPart(date, DateUtils.DatePart.DAY));
        assertEquals(7, DateUtils.getPart(date, DateUtils.DatePart.MONTH));
        assertEquals(2024, DateUtils.getPart(date, DateUtils.DatePart.YEAR));
    }
}
