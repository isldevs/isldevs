/*
 * Copyright 2025 iSLDevs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.base.utils;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * @author YISivlay
 */
public class DateUtils {

    public enum DatePart {
        DAY, MONTH, YEAR
    }

    /**
     * Formats a month number into a localized month name based on the given text style and locale.
     *
     * <p>This method supports internationalization and returns the correct month name
     * for the specified locale (for example, English or Khmer for Cambodia).
     *
     * @param month
     *     the month number (1 = January, 12 = December)
     * @param style
     *     the text style to apply (SHORT, FULL, NARROW)
     * @param locale
     *     the locale used for localization (e.g. en-US, km-KH)
     * @return the formatted month name for the given month and locale
     * @throws java.time.DateTimeException
     *     if the month value is outside the range 1–12
     */
    public static String nameOfMonth(int month,
                                     TextStyle style,
                                     Locale locale) {

        Objects.requireNonNull(style, "style must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        return Month.of(month)
                .getDisplayName(style, locale);
    }

    /**
     * Calculates the number of months between two LocalDates, inclusive.
     */
    public static int numOfMonths(LocalDate start,
                                  LocalDate end) {

        Objects.requireNonNull(start, "start date must not be null");
        Objects.requireNonNull(end, "end date must not be null");

        return (int) ChronoUnit.MONTHS.between(start.withDayOfMonth(1), end.withDayOfMonth(1)) + 1;
    }

    /**
     * Calculates the number of months between two Dates, inclusive.
     */
    public static int numOfMonths(Date startDate,
                                  Date endDate) {

        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        return numOfMonths(toLocalDate(startDate), toLocalDate(endDate));
    }

    /**
     * Calculates the number of months between two LocalDateTimes, inclusive.
     */
    public static int numOfMonths(LocalDateTime start,
                                  LocalDateTime end) {

        Objects.requireNonNull(start, "start datetime must not be null");
        Objects.requireNonNull(end, "end datetime must not be null");

        return numOfMonths(start.toLocalDate(), end.toLocalDate());
    }

    /** Convert java.util.Date to LocalDate using system default zone. */
    public static LocalDate toLocalDate(Date date) {

        Objects.requireNonNull(date, "date must not be null");

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    /** Convert java.util.LocalDateTime to LocalDate using system default zone. */
    public static LocalDate toLocalDate(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        return dateTime.toLocalDate();
    }

    /** Convert java.time.LocalDate to java.util.Date using system default zone. */
    public static Date toDate(LocalDate localDate) {
        Objects.requireNonNull(localDate, "localDate must not be null");
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant());
    }

    /** Convert java.time.LocalDateTime to java.util.Date using system default zone. */
    public static Date toDate(LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, "localDateTime must not be null");
        return Date.from(localDateTime.atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Extracts the specified part (day, month, year) from a date.
     *
     * @param date
     *     the date object (Date, LocalDate, or LocalDateTime)
     * @param part
     *     the part to extract
     * @return integer value of the requested part
     * @throws IllegalArgumentException
     *     if the object type is unsupported
     */
    public static int getPart(Object date,
                              DatePart part) {

        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(part, "part must not be null");

        LocalDate localDate;

        if (date instanceof Date d) {
            localDate = toLocalDate(d);
        } else if (date instanceof LocalDate ld) {
            localDate = ld;
        } else if (date instanceof LocalDateTime ldt) {
            localDate = toLocalDate(ldt);
        } else {
            throw new IllegalArgumentException("Unsupported date type: " + date.getClass()
                    .getName());
        }

        return switch (part) {
            case DAY -> localDate.getDayOfMonth();
            case MONTH -> localDate.getMonthValue();
            case YEAR -> localDate.getYear();
        };
    }

    public static int day(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.getDayOfMonth();
    }

    public static int month(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.getMonthValue();
    }

    public static int year(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.getYear();
    }

    public static int day(Date date) {
        return day(toLocalDate(date));
    }

    public static int month(Date date) {
        return month(toLocalDate(date));
    }

    public static int year(Date date) {
        return year(toLocalDate(date));
    }
}
