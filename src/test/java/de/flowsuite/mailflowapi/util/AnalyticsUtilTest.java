package de.flowsuite.mailflowapi.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.flowsuite.mailflowapi.common.constant.Timeframe;
import de.flowsuite.mailflowapi.common.util.AnalyticsUtil;
import de.flowsuite.mailflowapi.common.util.Util;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Date;

class AnalyticsUtilTest {

    @Test
    void resolveStartDate_givenFromDate_returnsFromDate() {
        Date from = new Date();
        ZonedDateTime result = AnalyticsUtil.resolveStartDate(from, Timeframe.DAILY);

        assertEquals(ZonedDateTime.ofInstant(from.toInstant(), Util.BERLIN_ZONE), result);
    }

    @Test
    void resolveStartDate_nullFrom_dailyTimeframe() {
        ZonedDateTime now =
                ZonedDateTime.now(Util.BERLIN_ZONE)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        ZonedDateTime result = AnalyticsUtil.resolveStartDate(null, Timeframe.DAILY);

        assertTrue(result.isBefore(now));
        assertEquals(now.minusDays(7).toLocalDate(), result.toLocalDate());
    }

    @Test
    void resolveStartDate_nullFrom_weeklyTimeframe() {
        ZonedDateTime now =
                ZonedDateTime.now(Util.BERLIN_ZONE)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        ZonedDateTime result = AnalyticsUtil.resolveStartDate(null, Timeframe.WEEKLY);

        assertEquals(now.minusWeeks(4).toLocalDate(), result.toLocalDate());
    }

    @Test
    void resolveStartDate_nullFrom_monthlyTimeframe() {
        ZonedDateTime now =
                ZonedDateTime.now(Util.BERLIN_ZONE)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        ZonedDateTime result = AnalyticsUtil.resolveStartDate(null, Timeframe.MONTHLY);

        assertEquals(now.minusMonths(3).toLocalDate(), result.toLocalDate());
    }

    @Test
    void resolveStartDate_nullFrom_yearlyTimeframe() {
        ZonedDateTime now =
                ZonedDateTime.now(Util.BERLIN_ZONE)
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);
        ZonedDateTime result = AnalyticsUtil.resolveStartDate(null, Timeframe.YEARLY);

        assertEquals(now.minusYears(1).toLocalDate(), result.toLocalDate());
    }

    @Test
    void resolveEndDate_givenToDate_returnsToDate() {
        Date to = new Date();
        ZonedDateTime result = AnalyticsUtil.resolveEndDate(to);

        assertEquals(ZonedDateTime.ofInstant(to.toInstant(), Util.BERLIN_ZONE), result);
    }

    @Test
    void getTruncUnitForTimeframe_daily() {
        assertEquals("day", AnalyticsUtil.getTruncUnitForTimeframe(Timeframe.DAILY));
    }

    @Test
    void getTruncUnitForTimeframe_weekly() {
        assertEquals("week", AnalyticsUtil.getTruncUnitForTimeframe(Timeframe.WEEKLY));
    }

    @Test
    void getTruncUnitForTimeframe_monthly() {
        assertEquals("month", AnalyticsUtil.getTruncUnitForTimeframe(Timeframe.MONTHLY));
    }

    @Test
    void getTruncUnitForTimeframe_yearly() {
        assertEquals("year", AnalyticsUtil.getTruncUnitForTimeframe(Timeframe.YEARLY));
    }
}
