package de.flowsuite.mailflow.api.responserating;

import static de.flowsuite.mailflow.api.messagelog.MessageLogService.TOKEN_TTL_DAYS;
import static de.flowsuite.mailflow.common.constant.Message.RESPONSE_RATING_EXPIRED_MSG;
import static de.flowsuite.mailflow.common.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflow.api.messagelog.MessageLogService;
import de.flowsuite.mailflow.common.constant.Timeframe;
import de.flowsuite.mailflow.common.entity.MessageLogEntry;
import de.flowsuite.mailflow.common.entity.ResponseRating;
import de.flowsuite.mailflow.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;
import de.flowsuite.mailflow.common.exception.IdorException;
import de.flowsuite.mailflow.common.exception.TokenExpiredException;
import de.flowsuite.mailflow.common.util.AnalyticsUtil;
import de.flowsuite.mailflow.common.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
class ResponseRatingService {

    private final ResponseRatingRepository responseRatingRepository;
    private final MessageLogService messageLogService;

    ResponseRatingService(
            ResponseRatingRepository responseRatingRepository,
            MessageLogService messageLogService) {
        this.responseRatingRepository = responseRatingRepository;
        this.messageLogService = messageLogService;
    }

    ResponseRating createResponseRating(
            String token, ResponseRatingResource.CreateResponseRatingRequest request) {
        MessageLogEntry messageLogEntry = messageLogService.getByToken(token);

        if (messageLogEntry.getTokenExpiresAt().isBefore(ZonedDateTime.now(BERLIN_ZONE))) {
            throw new TokenExpiredException(
                    String.format(RESPONSE_RATING_EXPIRED_MSG, TOKEN_TTL_DAYS));
        }

        if (responseRatingRepository.existsByMessageLogId(messageLogEntry.getId())) {
            throw new EntityAlreadyExistsException(ResponseRating.class.getSimpleName());
        }

        ResponseRating responseRating =
                ResponseRating.builder()
                        .messageLogId(messageLogEntry.getId())
                        .customerId(messageLogEntry.getCustomerId())
                        .userId(messageLogEntry.getUserId())
                        .satisfied(request.isSatisfied())
                        .rating(request.rating())
                        .feedback(request.feedback())
                        .build();

        return responseRatingRepository.save(responseRating);
    }

    ResponseRating getResponseRating(long customerId, long userId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);

        ResponseRating responseRating =
                responseRatingRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                ResponseRating.class.getSimpleName()));

        if (!responseRating.getCustomerId().equals(customerId)
                || !responseRating.getUserId().equals(userId)) {
            throw new IdorException();
        }

        return responseRating;
    }

    List<ResponseRating> listResponseRatingsByCustomer(long customerId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        return responseRatingRepository.findByCustomerId(customerId);
    }

    List<ResponseRating> listResponseRatingsByUser(long customerId, long userId, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(userId, jwt);
        return responseRatingRepository.findByUserId(userId);
    }

    ResponseRatingResource.ResponseRatingAnalyticsResponse getResponseRatingAnalyticsForCustomer(
            long customerId, Date from, Date to, Timeframe timeframe, Jwt jwt) {
        return getResponseRatingAnalytics(customerId, null, from, to, timeframe, jwt, false);
    }

    ResponseRatingResource.ResponseRatingAnalyticsResponse getResponseRatingAnalyticsForUser(
            long customerId, long userId, Date from, Date to, Timeframe timeframe, Jwt jwt) {
        return getResponseRatingAnalytics(customerId, userId, from, to, timeframe, jwt, true);
    }

    ResponseRatingResource.ResponseRatingAnalyticsResponse getResponseRatingAnalytics(
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

        Object[] analyticsRow =
                isUser
                        ? responseRatingRepository
                                .aggregateCountAndAvgSatisfactionAndAvgRatingByUser(
                                        userId, startDate, endDate)
                                .get(0)
                        : responseRatingRepository
                                .aggregateCountAndAvgSatisfactionAndAvgRatingByCustomer(
                                        customerId, startDate, endDate)
                                .get(0);

        long count = (Long) analyticsRow[0];
        if (count == 0) {
            return null;
        }

        double avgSatisfaction = (double) Math.round((double) analyticsRow[1] * 100) / 100;
        double avgRating = (double) Math.round((double) analyticsRow[2] * 100) / 100;

        int messageLogCount =
                isUser
                        ? messageLogService.countByUserId(userId)
                        : messageLogService.countByCustomerId(customerId);

        double ratingRate = (double) count / messageLogCount;

        return new ResponseRatingResource.ResponseRatingAnalyticsResponse(
                avgSatisfaction, avgRating, ratingRate);
    }
}
