package de.flowsuite.mailflow.api.messagelog;

import static de.flowsuite.mailflow.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflow.common.constant.Timeframe;
import de.flowsuite.mailflow.common.dto.CreateMessageLogEntryRequest;
import de.flowsuite.mailflow.common.entity.MessageLogEntry;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;
import de.flowsuite.mailflow.common.exception.IdConflictException;
import de.flowsuite.mailflow.common.exception.IdorException;
import de.flowsuite.mailflow.common.util.AesUtil;
import de.flowsuite.mailflow.common.util.AnalyticsUtil;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;
import de.flowsuite.mailflow.common.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class MessageLogService {

    private static final Logger LOG = LoggerFactory.getLogger(MessageLogService.class);
    public static final int TOKEN_TTL_DAYS = 7;
    private final MessageLogRepository messageLogRepository;

    MessageLogService(MessageLogRepository messageLogRepository) {
        this.messageLogRepository = messageLogRepository;
    }

    public MessageLogEntry getByToken(String token) {
        return messageLogRepository
                .findByToken(token)
                .orElseThrow(
                        () -> new EntityNotFoundException(MessageLogEntry.class.getSimpleName()));
    }

    public int countByCustomerId(long customerId) {
        return messageLogRepository.countByCustomerId(customerId);
    }

    public int countByUserId(long userId) {
        return messageLogRepository.countByUserId(userId);
    }

    private String generateToken() {
        String token;
        do {
            token = Util.generateRandomUrlSafeToken();
        } while (messageLogRepository.existsByToken(token));
        return token;
    }

    private static Map<String, Map<String, Long>> groupCategoryCountsByPeriod(
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

    MessageLogEntry createMessageLogEntry(
            long customerId, long userId, CreateMessageLogEntryRequest request, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        if (!request.customerId().equals(customerId) || !request.userId().equals(userId)) {
            throw new IdConflictException();
        }

        MessageLogEntry messageLogEntry =
                MessageLogEntry.builder()
                        .userId(request.userId())
                        .customerId(request.customerId())
                        .replied(request.replied())
                        .category(request.category())
                        .language(request.language())
                        .subject(request.subject())
                        .receivedAt(request.receivedAt())
                        .processedAt(request.processedAt())
                        .processingTimeInSeconds(request.processingTimeInSeconds())
                        .categorisationLlmUsed(request.categorisationLlmUsed())
                        .categorisationInputTokens(request.categorisationInputTokens())
                        .categorisationOutputTokens(request.categorisationOutputTokens())
                        .categorisationTotalTokens(request.categorisationTotalTokens())
                        .llmUsed(request.llmUsed())
                        .inputTokens(request.inputTokens())
                        .outputTokens(request.outputTokens())
                        .totalTokens(request.totalTokens())
                        .build();

        String emailAddress = request.fromEmailAddress();
        if (emailAddress != null) {
            messageLogEntry.setFromEmailAddress(AesUtil.encrypt(emailAddress.toLowerCase()));
        }

        String token = generateToken();
        ZonedDateTime tokenExpiresAt = ZonedDateTime.now(BERLIN_ZONE).plusDays(TOKEN_TTL_DAYS);

        messageLogEntry.setToken(token);
        messageLogEntry.setTokenExpiresAt(tokenExpiresAt);

        return messageLogRepository.save(messageLogEntry);
    }

    List<MessageLogEntry> listMessageLogEntriesByCustomer(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        return messageLogRepository.findByCustomerId(customerId);
    }

    List<MessageLogEntry> listMessageLogEntriesByUser(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);
        return messageLogRepository.findByUserId(userId);
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

        if (!messageLogEntry.getCustomerId().equals(customerId)
                || !messageLogEntry.getUserId().equals(userId)) {
            throw new IdorException();
        }

        return messageLogEntry;
    }

    MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalyticsForCustomer(
            long customerId, Date from, Date to, Timeframe timeframe, Jwt jwt) {
        return getMessageLogAnalytics(customerId, null, from, to, timeframe, jwt, false);
    }

    MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalyticsForUser(
            long customerId, long userId, Date from, Date to, Timeframe timeframe, Jwt jwt) {
        return getMessageLogAnalytics(customerId, userId, from, to, timeframe, jwt, true);
    }

    private MessageLogResource.MessageLogAnalyticsResponse getMessageLogAnalytics(
            long customerId,
            Long userId,
            Date from,
            Date to,
            Timeframe timeframe,
            Jwt jwt,
            boolean isUser) {

        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        if (isUser) {
            AuthorisationUtil.validateAccessToUser(userId, jwt);
        }

        if (timeframe == null) {
            timeframe = Timeframe.DAILY;
        }

        ZonedDateTime startDate = AnalyticsUtil.resolveStartDate(from, timeframe);
        ZonedDateTime endDate = AnalyticsUtil.resolveEndDate(to);

        AnalyticsUtil.validateDateRange(startDate, endDate);

        String truncUnit = AnalyticsUtil.getTruncUnitForTimeframe(timeframe);

        List<Object[]> categoryCountRows =
                isUser
                        ? messageLogRepository.aggregateCategoryCountsByUser(
                                truncUnit, userId, startDate, endDate)
                        : messageLogRepository.aggregateCategoryCountsByCustomer(
                                truncUnit, customerId, startDate, endDate);

        Map<String, Map<String, Long>> categoryCountsByPeriod =
                groupCategoryCountsByPeriod(categoryCountRows);

        Object[] analyticsRow =
                isUser
                        ? messageLogRepository
                                .aggregateAvgProcessingTimeAndResponseRateByUser(
                                        userId, startDate, endDate)
                                .get(0)
                        : messageLogRepository
                                .aggregateAvgProcessingTimeAndResponseRateByCustomer(
                                        customerId, startDate, endDate)
                                .get(0);

        if (analyticsRow[0] == null) {
            return null;
        }

        double averageProcessingTimeInSeconds =
                (double) Math.round((double) analyticsRow[0] * 100) / 100;
        double responseRate = (double) Math.round((double) analyticsRow[1] * 100) / 100;

        ZonedDateTime startOfDay =
                ZonedDateTime.now(BERLIN_ZONE).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        int processedMessagesToday =
                isUser
                        ? messageLogRepository.countByUserIdAndReceivedAtBetween(
                                userId, startOfDay, endOfDay)
                        : messageLogRepository.countByCustomerIdAndReceivedAtBetween(
                                customerId, startOfDay, endOfDay);

        return new MessageLogResource.MessageLogAnalyticsResponse(
                averageProcessingTimeInSeconds,
                responseRate,
                processedMessagesToday,
                categoryCountsByPeriod);
    }
}
