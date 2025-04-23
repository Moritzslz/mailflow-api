package de.flowsuite.mailflowapi.messagelog;

import static de.flowsuite.mailflowapi.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflowapi.common.entity.MessageLogEntry;
import de.flowsuite.mailflowapi.common.exception.EntityNotFoundException;
import de.flowsuite.mailflowapi.common.exception.IdConflictException;
import de.flowsuite.mailflowapi.common.exception.IdorException;
import de.flowsuite.mailflowapi.common.util.AesUtil;
import de.flowsuite.mailflowapi.common.util.AuthorisationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
class MessageLogService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogService.class);
    private final MessageLogRepository messageLogRepository;

    MessageLogService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    MessageLogEntry createMessageLogEntry(
            long customerId, long userId, MessageLogEntry messageLogEntry, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!messageLogEntry.getCustomerId().equals(customerId)
                || !messageLogEntry.getUserId().equals(userId)) {
            throw new IdConflictException();
        }

        String emailAddress = messageLogEntry.getFromEmailAddress();
        if (emailAddress != null) {
            messageLogEntry.setFromEmailAddress(AesUtil.encrypt(emailAddress.toLowerCase()));
        }

        return messageLogRepository.save(messageLogEntry);
    }

    List<MessageLogEntry> listMessageLogEntriesByCustomer(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        return messageLogRepository.findByCustomerId(customerId);
    }

    List<MessageLogEntry> listMessageLogEntriesByUser(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);
        return messageLogRepository.findByCustomerIdAndUserId(customerId, userId);
    }

    MessageLogEntry getMessageLogEntry(long customerId, long userId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        MessageLogEntry messageLogEntry =
                messageLogRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                MessageLogEntry.class.getSimpleName()));

        if (messageLogEntry.getCustomerId() != customerId
                || messageLogEntry.getUserId() != userId) {
            throw new IdorException();
        }

        return messageLogEntry;
    }

    MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalyticsForCustomer(
            long customerId, Date from, Date to, MessageLogResource.Timeframe timeframe, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        ZonedDateTime startDate = null;
        ZonedDateTime endDate;

        if (from != null) {
            startDate = ZonedDateTime.ofInstant(from.toInstant(), BERLIN_ZONE);
        }

        if (to != null) {
            endDate = ZonedDateTime.ofInstant(to.toInstant(), BERLIN_ZONE);
        } else {
            endDate = ZonedDateTime.now(BERLIN_ZONE);
        }

        if (from != null && to != null && to.before(from)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        if (timeframe == null) {
            timeframe = MessageLogResource.Timeframe.DAILY;
        }

        List<Object[]> queryResult = new ArrayList<>();

        switch (timeframe) {
            case DAILY -> {
                if (startDate == null) {
                    startDate =
                            ZonedDateTime.now(BERLIN_ZONE)
                                    .minusDays(7)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0);
                }

                queryResult =
                        messageLogRepository.findCategoryCountsGroupedByDayAndCustomerId(
                                customerId, startDate, endDate);
            }
            case WEEKLY -> {
                if (startDate == null) {
                    startDate =
                            ZonedDateTime.now(BERLIN_ZONE)
                                    .minusWeeks(4)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0);
                }

                queryResult =
                        messageLogRepository.findCategoryCountsGroupedByWeekAndCustomerId(
                                customerId, startDate, endDate);
            }

            case MONTHLY -> {
                if (startDate == null) {
                    startDate =
                            ZonedDateTime.now(BERLIN_ZONE)
                                    .minusMonths(3)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0);
                }

                queryResult =
                        messageLogRepository.findCategoryCountsGroupedByMonthAndCustomerId(
                                customerId, startDate, endDate);
            }

            case YEARLY -> {
                if (from == null) {
                    startDate =
                            ZonedDateTime.now(BERLIN_ZONE)
                                    .minusYears(1)
                                    .withHour(0)
                                    .withMinute(0)
                                    .withSecond(0)
                                    .withNano(0);
                }

                queryResult =
                        messageLogRepository.findCategoryCountsGroupedByYearAndCustomerId(
                                customerId, startDate, endDate);
            }
        }

        LOG.debug("Query result size: {}", queryResult.size());

        Map<String, Map<String, Long>> grouped = new LinkedHashMap<>();

        for (Object[] row : queryResult) {
            String period = row[0].toString();
            String extractedCategory = row[1].toString();
            Long count = (Long) row[2];
            LOG.debug("Period: {}, category: {}, count: {}", period, extractedCategory, count);
            grouped.computeIfAbsent(period, k -> new HashMap<>()).put(extractedCategory, count);
        }

        double avgProcessingTimeInSeconds =
                Math.round(
                        messageLogRepository.findAverageProcessingTimeByCustomerId(
                                customerId, startDate, endDate));
        double responseRate =
                messageLogRepository.getResponseRateBetween(customerId, startDate, endDate);

        return new MessageLogResource.MessageLogAnalyticsResponse(
                avgProcessingTimeInSeconds, responseRate, grouped);
    }

    MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalyticsForUser(
            long customerId,
            long userId,
            Date from,
            Date to,
            MessageLogResource.Timeframe timeframe,
            Jwt jwt) {
        return null;
    }
}
