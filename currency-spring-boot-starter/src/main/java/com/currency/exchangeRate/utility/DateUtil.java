package com.currency.exchangeRate.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DateUtil {
    public static LocalDate getLastBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.minusDays(1);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.minusDays(2);
        }
        return date;
    }
}
