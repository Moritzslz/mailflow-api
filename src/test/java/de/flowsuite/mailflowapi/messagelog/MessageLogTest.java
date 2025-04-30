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
import de.flowsuite.mailflowcommon.exception.EntityNotFoundException;
import de.flowsuite.mailflowcommon.exception.IdConflictException;
import de.flowsuite.mailflowcommon.exception.IdorException;

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

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                createMessageLogEntryRequest,
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.createMessageLogEntry(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                createMessageLogEntryRequest,
                                jwtMock));

        verify(messageLogRepository, never()).save(any(MessageLogEntry.class));
    }

    @Test
    void testListMessageLogEntriesByCustomer() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(messageLogRepository.findByCustomerId(testUser.getCustomerId()))
                .thenReturn(List.of(testMessageLogEntry));

        List<MessageLogEntry> messageLogEntries =
                messageLogService.listMessageLogEntriesByCustomer(
                        testUser.getCustomerId(), jwtMock);

        assertEquals(1, messageLogEntries.size());
        assertEquals(testMessageLogEntry, messageLogEntries.get(0));
    }

    @Test
    void testListMessageLogEntriesByCustomer_idor() {
        mockJwtWithCustomerClaimsOnly(testUser);

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.listMessageLogEntriesByCustomer(
                                testUser.getCustomerId() + 1, jwtMock));

        verify(messageLogRepository, never()).findByCustomerId(anyLong());
    }

    @Test
    void testListMessageLogEntriesByUser() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(messageLogRepository.findByUserId(testUser.getId()))
                .thenReturn(List.of(testMessageLogEntry));

        List<MessageLogEntry> messageLogEntries =
                messageLogService.listMessageLogEntriesByUser(
                        testUser.getCustomerId(), testUser.getId(), jwtMock);

        assertEquals(1, messageLogEntries.size());
        assertEquals(testMessageLogEntry, messageLogEntries.get(0));
    }

    @Test
    void testListMessageLogEntriesByUser_idor() {
        mockJwtWithUserAndCustomerClaims(testUser);

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.listMessageLogEntriesByUser(
                                testUser.getCustomerId() + 1, testUser.getId(), jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.listMessageLogEntriesByUser(
                                testUser.getCustomerId(), testUser.getId() + 1, jwtMock));

        verify(messageLogRepository, never()).findByUserId(anyLong());
    }

    @Test
    void testGetMessageLogEntry_success() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(messageLogRepository.findById(testMessageLogEntry.getId()))
                .thenReturn(Optional.of(testMessageLogEntry));

        MessageLogEntry messageLogEntry =
                messageLogService.getMessageLogEntry(
                        testUser.getCustomerId(),
                        testUser.getId(),
                        testMessageLogEntry.getId(),
                        jwtMock);

        verify(messageLogRepository).findById(testMessageLogEntry.getId());

        assertEquals(testMessageLogEntry, messageLogEntry);
    }

    @Test
    void testGetMessageLogEntry_notFound() {
        mockJwtWithUserAndCustomerClaims(testUser);
        when(messageLogRepository.findById(testMessageLogEntry.getId()))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () ->
                        messageLogService.getMessageLogEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testMessageLogEntry.getId(),
                                jwtMock));
    }

    @Test
    void testGetMessageLogEntry_idor() {
        mockJwtWithUserAndCustomerClaims(testUser);

        MessageLogEntry testMessageLogEntryIdor1 = buildTestMessageLogEntry();
        testMessageLogEntryIdor1.setId(testMessageLogEntry.getId() + 1);
        testMessageLogEntryIdor1.setCustomerId(testUser.getCustomerId() + 1);

        MessageLogEntry testMessageLogEntryIdor2 = buildTestMessageLogEntry();
        testMessageLogEntryIdor1.setId(testMessageLogEntry.getId() + 2);
        testMessageLogEntryIdor2.setUserId(testUser.getId() + 1);

        when(messageLogRepository.findById(testMessageLogEntryIdor1.getId()))
                .thenReturn(Optional.of(testMessageLogEntryIdor1));

        when(messageLogRepository.findById(testMessageLogEntryIdor2.getId()))
                .thenReturn(Optional.of(testMessageLogEntryIdor2));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.getMessageLogEntry(
                                testUser.getCustomerId() + 1,
                                testUser.getId(),
                                testMessageLogEntry.getId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.getMessageLogEntry(
                                testUser.getCustomerId(),
                                testUser.getId() + 1,
                                testMessageLogEntry.getId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.getMessageLogEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testMessageLogEntryIdor1.getId(),
                                jwtMock));

        assertThrows(
                IdorException.class,
                () ->
                        messageLogService.getMessageLogEntry(
                                testUser.getCustomerId(),
                                testUser.getId(),
                                testMessageLogEntryIdor2.getId(),
                                jwtMock));
    }
}
