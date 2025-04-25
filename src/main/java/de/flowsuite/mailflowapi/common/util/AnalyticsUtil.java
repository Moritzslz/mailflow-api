package de.flowsuite.mailflowapi.common.util;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflowapi.common.constant.Timeframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.*;

public class AnalyticsUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsUtil.class);

    public static ZonedDateTime resolveStartDate(Date from, Timeframe timeframe) {
        if (from != null) return ZonedDateTime.ofInstant(from.toInstant(), BERLIN_ZONE);

        ZonedDateTime now =
                ZonedDateTime.now(BERLIN_ZONE).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return switch (timeframe) {
            case DAILY -> now.minusDays(7);
            case WEEKLY -> now.minusWeeks(4);
            case MONTHLY -> now.minusMonths(3);
            case YEARLY -> now.minusYears(1);
        };
    }

    public static ZonedDateTime resolveEndDate(Date to) {
        return (to != null)
                ? ZonedDateTime.ofInstant(to.toInstant(), BERLIN_ZONE)
                : ZonedDateTime.now(BERLIN_ZONE);
    }

    public static void validateDateRange(ZonedDateTime start, ZonedDateTime end) {
        if (end.isBefore(start))
            throw new IllegalArgumentException("End date must be after start date.");
    }

    public static String getTruncUnitForTimeframe(Timeframe timeframe) {
        return switch (timeframe) {
            case DAILY -> "day";
            case WEEKLY -> "week";
            case MONTHLY -> "month";
            case YEARLY -> "year";
        };
    }

    public static Map<String, Map<String, Long>> groupCategoryCountsByPeriod(
            List<Object[]> categoryCountRows) {
        Map<String, Map<String, Long>> categoryCountsByPeriod = new LinkedHashMap<>();
        for (Object[] row : categoryCountRows) {
            String period = row[0].toString();
            String category = row[1].toString();
            Long count = (Long) row[2];
            categoryCountsByPeriod
                    .computeIfAbsent(period, k -> new HashMap<>())
                    .put(category, count);
        }
        return categoryCountsByPeriod;
    }
}
