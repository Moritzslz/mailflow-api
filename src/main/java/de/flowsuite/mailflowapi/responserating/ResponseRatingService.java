package de.flowsuite.mailflowapi.responserating;

import static de.flowsuite.mailflowapi.messagelog.MessageLogService.TOKEN_TTL_DAYS;
import static de.flowsuite.mailflowcommon.constant.Message.RESPONSE_RATING_EXPIRED_MSG;
import static de.flowsuite.mailflowcommon.util.Util.BERLIN_ZONE;

import de.flowsuite.mailflowapi.messagelog.MessageLogService;
import de.flowsuite.mailflowcommon.constant.Timeframe;
import de.flowsuite.mailflowcommon.entity.MessageLogEntry;
import de.flowsuite.mailflowcommon.entity.ResponseRating;
import de.flowsuite.mailflowcommon.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdorException;
import de.flowsuite.mailflowcommon.exception.TokenExpiredException;
import de.flowsuite.mailflowcommon.util.AnalyticsUtil;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Service
class ResponseRatingService {

    private final ResponseRatingRepository responseRatingRepository;
    private final MessageLogService messageLogService;

    public ResponseRatingService(
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
                        .isSatisfied(request.isSatisfied())
                        .rating(request.rating())
                        .feedback(request.feedback())
                        .build();

        return responseRatingRepository.save(responseRating);
    }

    ResponseRating getResponseRating(long customerId, long id, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        ResponseRating responseRating =
                responseRatingRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                ResponseRating.class.getSimpleName()));

        if (!responseRating.getCustomerId().equals(customerId)) {
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
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);

        if (timeframe == null) {
            timeframe = Timeframe.DAILY;
        }

        ZonedDateTime startDate = AnalyticsUtil.resolveStartDate(from, timeframe);
        ZonedDateTime endDate = AnalyticsUtil.resolveEndDate(to);

        AnalyticsUtil.validateDateRange(startDate, endDate);

        Object[] analyticsRow =
                responseRatingRepository
                        .aggregateCountAndAvgSatisfactionAndAvgRatingByCustomer(
                                customerId, startDate, endDate)
                        .get(0);

        long count = (Long) analyticsRow[0];
        double avgSatisfaction = (double) Math.round((double) analyticsRow[1] * 100) / 100;
        double avgRating = (double) Math.round((double) analyticsRow[2] * 100) / 100;

        int messageLogCount = messageLogService.countByCustomerId(customerId);
        double ratingRate = (double) count / (double) messageLogCount;

        return new ResponseRatingResource.ResponseRatingAnalyticsResponse(
                avgSatisfaction, avgRating, ratingRate);
    }

    ResponseRatingResource.ResponseRatingAnalyticsResponse getResponseRatingAnalyticsForUser(
            long customerId, long userId, Date from, Date to, Timeframe timeframe, Jwt jwt) {
        AuthorisationUtil.validateAccessToCustomer(customerId, jwt);
        AuthorisationUtil.validateAccessToUser(customerId, jwt);

        if (timeframe == null) {
            timeframe = Timeframe.DAILY;
        }

        ZonedDateTime startDate = AnalyticsUtil.resolveStartDate(from, timeframe);
        ZonedDateTime endDate = AnalyticsUtil.resolveEndDate(to);

        AnalyticsUtil.validateDateRange(startDate, endDate);

        Object[] analyticsRow =
                responseRatingRepository
                        .aggregateCountAndAvgSatisfactionAndAvgRatingByUser(
                                userId, startDate, endDate)
                        .get(0);

        long count = (Long) analyticsRow[0];
        double avgSatisfaction = (double) Math.round((double) analyticsRow[1] * 100) / 100;
        double avgRating = (double) Math.round((double) analyticsRow[2] * 100) / 100;

        int messageLogCount = messageLogService.countByUserId(userId);
        double ratingRate = (double) count / (double) messageLogCount;

        return new ResponseRatingResource.ResponseRatingAnalyticsResponse(
                avgSatisfaction, avgRating, ratingRate);
    }
}
