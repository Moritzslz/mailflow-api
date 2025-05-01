package de.flowsuite.mailflow.api.responserating;

import static de.flowsuite.mailflow.common.util.Util.BERLIN_ZONE;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflow.api.BaseServiceTest;
import de.flowsuite.mailflow.api.messagelog.MessageLogService;
import de.flowsuite.mailflow.common.entity.MessageLogEntry;
import de.flowsuite.mailflow.common.entity.ResponseRating;
import de.flowsuite.mailflow.common.entity.User;
import de.flowsuite.mailflow.common.exception.EntityAlreadyExistsException;
import de.flowsuite.mailflow.common.exception.EntityNotFoundException;
import de.flowsuite.mailflow.common.exception.IdorException;
import de.flowsuite.mailflow.common.exception.TokenExpiredException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ResponseRatingTest extends BaseServiceTest {

    private static final String TOKEN = "token";

    @Mock ResponseRatingRepository responseRatingRepository;

    @Mock MessageLogService messageLogService;

    @InjectMocks ResponseRatingService responseRatingService;

    private final User testUser = buildTestUser();
    private MessageLogEntry testMessageLogEntry = buildTestMessageLogEntry();

    private ResponseRating testResponseRating;

    private MessageLogEntry buildTestMessageLogEntry() {
        return MessageLogEntry.builder()
                .id(1L)
                .userId(testUser.getId())
                .customerId(testUser.getCustomerId())
                .token(TOKEN)
                .tokenExpiresAt(ZonedDateTime.now(BERLIN_ZONE).plusDays(7))
                .build();
    }

    private ResponseRatingResource.CreateResponseRatingRequest createResponseRatingRequest =
            new ResponseRatingResource.CreateResponseRatingRequest(
                    true, 5, "Answered all my questions!");

    private ResponseRating buildResponseRating() {
        return ResponseRating.builder()
                .messageLogId(testMessageLogEntry.getId())
                .customerId(testMessageLogEntry.getCustomerId())
                .userId(testMessageLogEntry.getUserId())
                .isSatisfied(createResponseRatingRequest.isSatisfied())
                .rating(createResponseRatingRequest.rating())
                .feedback(createResponseRatingRequest.feedback())
                .build();
    }

    @BeforeEach
    void setup() {
        testResponseRating = buildResponseRating();
    }

    @Test
    void createResponseRating_success() {
        when(messageLogService.getByToken(TOKEN)).thenReturn(testMessageLogEntry);
        when(responseRatingRepository.existsByMessageLogId(testMessageLogEntry.getId()))
                .thenReturn(false);

        responseRatingService.createResponseRating(TOKEN, createResponseRatingRequest);

        ArgumentCaptor<ResponseRating> responseRatingCaptor =
                ArgumentCaptor.forClass(ResponseRating.class);
        verify(responseRatingRepository).save(responseRatingCaptor.capture());
        ResponseRating savedResponseRating = responseRatingCaptor.getValue();

        assertNotNull(savedResponseRating);
        assertEquals(testResponseRating, savedResponseRating);
    }

    @Test
    void createResponseRating_alreadyExists() {
        when(messageLogService.getByToken(TOKEN)).thenReturn(testMessageLogEntry);
        when(responseRatingRepository.existsByMessageLogId(testMessageLogEntry.getId()))
                .thenReturn(true);

        assertThrows(
                EntityAlreadyExistsException.class,
                () ->
                        responseRatingService.createResponseRating(
                                TOKEN, createResponseRatingRequest));

        verify(responseRatingRepository, never()).save(any(ResponseRating.class));
    }

    @Test
    void createResponseRating_tokenExpired() {
        when(messageLogService.getByToken(TOKEN)).thenReturn(testMessageLogEntry);

        testMessageLogEntry.setTokenExpiresAt(ZonedDateTime.now().minusMinutes(5));

        assertThrows(
                TokenExpiredException.class,
                () ->
                        responseRatingService.createResponseRating(
                                TOKEN, createResponseRatingRequest));

        verify(responseRatingRepository, never()).save(any(ResponseRating.class));
    }

    @Test
    void getResponseRating_success() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(responseRatingRepository.findById(testResponseRating.getMessageLogId()))
                .thenReturn(Optional.of(testResponseRating));

        ResponseRating responseRating =
                responseRatingService.getResponseRating(
                        testUser.getCustomerId(),
                        testUser.getId(),
                        testMessageLogEntry.getId(),
                        jwtMock);

        verify(responseRatingRepository).findById(testResponseRating.getMessageLogId());

        assertEquals(testResponseRating, responseRating);
    }

    @Test
    void getResponseRating_notFound() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(responseRatingRepository.findById(testResponseRating.getMessageLogId()))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        responseRatingService.getResponseRating(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testMessageLogEntry.getId(),
                                jwtMock));
    }

    @Test
    void getResponseRating_idor() {
        mockJwtWithUserAndCustomerClaims(testUser);

        ResponseRating testResponseRatingIdor1 = buildResponseRating();
        testResponseRatingIdor1.setMessageLogId(testMessageLogEntry.getId() + 1);
        testResponseRatingIdor1.setCustomerId(testUser.getCustomerId() + 1);

        ResponseRating testResponseRatingIdor2 = buildResponseRating();
        testResponseRatingIdor2.setMessageLogId(testMessageLogEntry.getId() + 2);
        testResponseRatingIdor2.setUserId(testUser.getId() + 1);

        when(responseRatingRepository.findById(testResponseRatingIdor1.getMessageLogId()))
                .thenReturn(Optional.of(testResponseRatingIdor1));

        when(responseRatingRepository.findById(testResponseRatingIdor2.getMessageLogId()))
                .thenReturn(Optional.of(testResponseRatingIdor2));

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.getResponseRating(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                testResponseRating.getMessageLogId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.getResponseRating(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                testResponseRating.getMessageLogId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.getResponseRating(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testResponseRatingIdor1.getMessageLogId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.getResponseRating(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testResponseRatingIdor2.getMessageLogId(),
                                jwtMock));
    }

    @Test
    void testListMessageLogEntriesByCustomer() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(responseRatingRepository.findByCustomerId(testUser.getCustomerId()))
                .thenReturn(List.of(testResponseRating));

        List<ResponseRating> responseRatings =
                responseRatingService.listResponseRatingsByCustomer(
                        testUser.getCustomerId(), jwtMock);

        assertEquals(1, responseRatings.size());
        assertEquals(testResponseRating, responseRatings.get(0));
    }

    @Test
    void testListMessageLogEntriesByCustomer_idor() {
        mockJwtWithCustomerClaimsOnly(testUser);

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.listResponseRatingsByCustomer(
                                testUser.getCustomerId() + 1, jwtMock));

        verify(responseRatingRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void testListMessageLogEntriesByUser() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(responseRatingRepository.findByUserId(testUser.getId()))
                .thenReturn(List.of(testResponseRating));

        List<ResponseRating> responseRatings =
                responseRatingService.listResponseRatingsByUser(
                        testUser.getCustomerId(), testUser.getId(), jwtMock);

        assertEquals(1, responseRatings.size());
        assertEquals(testResponseRating, responseRatings.get(0));
    }

    @Test
    void testListMessageLogEntriesByUser_idor() {
        mockJwtWithUserAndCustomerClaims(testUser);

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.listResponseRatingsByUser(
                                testUser.getCustomerId() + 1, testUser.getId(), jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        responseRatingService.listResponseRatingsByUser(
                                testUser.getCustomerId(), testUser.getId() + 1, jwtMock));

        verify(responseRatingRepository, never()).findByUserId(anyLong());
    }
}
