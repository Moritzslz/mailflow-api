package de.flowsuite.mailflowapi.messagelog;

import static de.flowsuite.mailflowcommon.util.Util.BERLIN_ZONE;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.BaseServiceTest;
import de.flowsuite.mailflowcommon.entity.MessageLogEntry;
import de.flowsuite.mailflowcommon.entity.User;
import de.flowsuite.mailflowcommon.exception.IdConflictException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

@ExtendWith(MockitoExtension.class)
class MessageLogTest extends BaseServiceTest {

    @Mock private MessageLogRepository messageLogRepository;

    @InjectMocks private MessageLogService messageLogService;

    private final User testUser = buildTestUser();
    private final MessageLogResource.CreateMessageLogEntryRequest createMessageLogEntryRequest =
            buildCreateCustomerRequest(testUser.getId(), testUser.getCustomerId());
    private MessageLogEntry testMessageLogEntry;

    private MessageLogResource.CreateMessageLogEntryRequest buildCreateCustomerRequest(
            long userId, long customerId) {
        int processingTimeInSeconds = 30;
        ZonedDateTime processAt = ZonedDateTime.now(BERLIN_ZONE);
        ZonedDateTime receivedAt = processAt.minusSeconds(processingTimeInSeconds);

        return new MessageLogResource.CreateMessageLogEntryRequest(
                userId,
                customerId,
                true,
                "Category",
                "Language",
                "test@example.de",
                "Subject",
                receivedAt,
                processAt,
                processingTimeInSeconds,
                "LLM",
                1500,
                1000,
                2500);
    }

    private MessageLogEntry buildTestMessageLogEntry() {
        return MessageLogEntry.builder()
                .id(1L)
                .userId(createMessageLogEntryRequest.userId())
                .customerId(createMessageLogEntryRequest.customerId())
                .isReplied(createMessageLogEntryRequest.isReplied())
                .category(createMessageLogEntryRequest.category())
                .language(createMessageLogEntryRequest.language())
                .fromEmailAddress(ENCRYPTED_VALUE)
                .subject(createMessageLogEntryRequest.subject())
                .receivedAt(createMessageLogEntryRequest.receivedAt())
                .processedAt(createMessageLogEntryRequest.processedAt())
                .processingTimeInSeconds(createMessageLogEntryRequest.processingTimeInSeconds())
                .llmUsed(createMessageLogEntryRequest.llmUsed())
                .inputTokens(createMessageLogEntryRequest.inputTokens())
                .outputTokens(createMessageLogEntryRequest.outputTokens())
                .totalTokens(createMessageLogEntryRequest.totalTokens())
                .token("token")
                .tokenExpiresAt(ZonedDateTime.now(BERLIN_ZONE).plusDays(7))
                .build();
    }

    @BeforeEach
    void setup() {
        testMessageLogEntry = buildTestMessageLogEntry();
    }

    @Test
    void testCreateMessageLogEntry_success() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(messageLogRepository.existsByToken(anyString())).thenReturn(false);

        messageLogService.createMessageLogEntry(
                testUser.getCustomerId(), testUser.getId(), createMessageLogEntryRequest, jwtMock);

        ArgumentCaptor<MessageLogEntry> messageLogEntryCaptor =
                ArgumentCaptor.forClass(MessageLogEntry.class);
        verify(messageLogRepository).save(messageLogEntryCaptor.capture());
        MessageLogEntry savedMessageLogEntry = messageLogEntryCaptor.getValue();

        assertNotNull(savedMessageLogEntry);
        assertNotNull(savedMessageLogEntry.getToken());
        assertTrue(
                savedMessageLogEntry
                        .getTokenExpiresAt()
                        .isAfter(
                                ZonedDateTime.now()
                                        .plusHours(MessageLogService.TOKEN_TTL_DAYS)
                                        .minusMinutes(5)));
        assertEquals(
                createMessageLogEntryRequest.customerId(), savedMessageLogEntry.getCustomerId());
        assertEquals(createMessageLogEntryRequest.userId(), savedMessageLogEntry.getUserId());
        assertEquals(createMessageLogEntryRequest.isReplied(), savedMessageLogEntry.isReplied());
        assertEquals(createMessageLogEntryRequest.category(), savedMessageLogEntry.getCategory());
        assertEquals(createMessageLogEntryRequest.category(), savedMessageLogEntry.getCategory());
        assertEquals(createMessageLogEntryRequest.language(), savedMessageLogEntry.getLanguage());
        assertEquals(ENCRYPTED_VALUE, savedMessageLogEntry.getFromEmailAddress());
        assertEquals(createMessageLogEntryRequest.subject(), savedMessageLogEntry.getSubject());
        assertEquals(
                createMessageLogEntryRequest.receivedAt(), savedMessageLogEntry.getReceivedAt());
        assertEquals(
                createMessageLogEntryRequest.processedAt(), savedMessageLogEntry.getProcessedAt());
        assertEquals(
                createMessageLogEntryRequest.processingTimeInSeconds(),
                savedMessageLogEntry.getProcessingTimeInSeconds());
        assertEquals(createMessageLogEntryRequest.llmUsed(), savedMessageLogEntry.getLlmUsed());
        assertEquals(
                createMessageLogEntryRequest.inputTokens(), savedMessageLogEntry.getInputTokens());
        assertEquals(
                createMessageLogEntryRequest.outputTokens(),
                savedMessageLogEntry.getOutputTokens());
        assertEquals(
                createMessageLogEntryRequest.totalTokens(), savedMessageLogEntry.getTotalTokens());
    }

    @Test
    void testCreateMessageLogEntry_idConflict() {
        mockJwtWithUserAndCustomerClaims(testUser);

        MessageLogResource.CreateMessageLogEntryRequest request1 =
                buildCreateCustomerRequest(testUser.getId() + 1, testUser.getCustomerId());
        MessageLogResource.CreateMessageLogEntryRequest request2 =
                buildCreateCustomerRequest(testUser.getId(), testUser.getCustomerId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId(), testUser.getId(), request1, jwtMock));

        assertThrows(
                IdConflictException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId(), testUser.getId(), request2, jwtMock));

        verify(messageLogRepository, never()).save(any(MessageLogEntry.class));
    }

    @Test
    void testCreateMessageLogEntry_idor() {
        mockJwtWithUserAndCustomerClaims(testUser);

        MessageLogResource.CreateMessageLogEntryRequest request1 =
                buildCreateCustomerRequest(testUser.getId() + 1, testUser.getCustomerId());
        MessageLogResource.CreateMessageLogEntryRequest request2 =
                buildCreateCustomerRequest(testUser.getId(), testUser.getCustomerId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId(), testUser.getId(), request1, jwtMock));

        assertThrows(
                IdConflictException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId(), testUser.getId(), request2, jwtMock));

        verify(messageLogRepository, never()).save(any(MessageLogEntry.class));
    }

    @Test
    void testListMessageLogEntriesByCustomer() {
        mockJwtWithUserAndCustomerClaims(testUser);
    }

    @Test
    void testListMessageLogEntriesByUser() {
        mockJwtWithUserAndCustomerClaims(testUser);
    }
}
