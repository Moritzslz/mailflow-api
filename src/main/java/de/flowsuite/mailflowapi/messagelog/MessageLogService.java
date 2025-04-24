package de.flowsuite.mailflowapi.messagelog;

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

        if (timeframe == null) {
            timeframe = MessageLogResource.Timeframe.DAILY;
        }

        ZonedDateTime startDate = MessageLogUtil.resolveStartDate(from, timeframe);
        ZonedDateTime endDate = MessageLogUtil.resolveEndDate(to);

        MessageLogUtil.validateDateRange(startDate, endDate);

        String truncUnit = MessageLogUtil.getTruncUnitForTimeframe(timeframe);

        List<Object[]> categoryCountRows =
                messageLogRepository.aggregateCategoryCountsByCustomer(
                        truncUnit, customerId, startDate, endDate);

        Map<String, Map<String, Long>> categoryCountsByPeriod =
                MessageLogUtil.groupCategoryCountsByPeriod(categoryCountRows);

        Object[] analyticsRow =
                messageLogRepository
                        .aggregateAvgProcessingTimeAndResponseRateByCustomer(
                                customerId, startDate, endDate)
                        .get(0);

        double averageProcessingTimeInSeconds = (Double) analyticsRow[0];
        double responseRate = (Double) analyticsRow[1];

        return new MessageLogResource.MessageLogAnalyticsResponse(
                averageProcessingTimeInSeconds, responseRate, categoryCountsByPeriod);
    }

    MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalyticsForUser(
            long customerId,
            long userId,
            Date from,
            Date to,
            MessageLogResource.Timeframe timeframe,
            Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (timeframe == null) {
            timeframe = MessageLogResource.Timeframe.DAILY;
        }

        ZonedDateTime startDate = MessageLogUtil.resolveStartDate(from, timeframe);
        ZonedDateTime endDate = MessageLogUtil.resolveEndDate(to);

        MessageLogUtil.validateDateRange(startDate, endDate);

        String truncUnit = MessageLogUtil.getTruncUnitForTimeframe(timeframe);

        List<Object[]> categoryCountRows =
                messageLogRepository.aggregateCategoryCountsByUser(
                        truncUnit, userId, startDate, endDate);

        Map<String, Map<String, Long>> categoryCountsByPeriod =
                MessageLogUtil.groupCategoryCountsByPeriod(categoryCountRows);

        Object[] analyticsRow =
                messageLogRepository
                        .aggregateAvgProcessingTimeAndResponseRateByUser(
                                userId, startDate, endDate)
                        .get(0);

        double averageProcessingTimeInSeconds = (Double) analyticsRow[0];
        double responseRate = (Double) analyticsRow[1];

        return new MessageLogResource.MessageLogAnalyticsResponse(
                averageProcessingTimeInSeconds, responseRate, categoryCountsByPeriod);
    }
}
