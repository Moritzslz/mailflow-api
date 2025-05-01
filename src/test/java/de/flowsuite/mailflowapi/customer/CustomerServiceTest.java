package de.flowsuite.mailflowapi.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflow.common.entity.Customer;
import de.flowsuite.mailflow.common.entity.User;
import de.flowsuite.mailflow.common.exception.*;
import de.flowsuite.mailflowapi.BaseServiceTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest extends BaseServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CustomerService customerService;

    private final User testUser = buildTestUser();
    private Customer testCustomer;

    private static final CustomerResource.CreateCustomerRequest createCustomerRequest =
            new CustomerResource.CreateCustomerRequest(
                    "Company",
                    "Street",
                    "42",
                    "1337",
                    "City",
                    "billing@example.com",
                    "openaiApiKey",
                    "LinkedIn",
                    "https://example.com",
                    "https://example.com/privacy-policy",
                    "https://example.com/cta",
                    true,
                    "test@ionos.de",
                    "password");

    private Customer buildTestCustomer() {
        return Customer.builder()
                .id(testUser.getCustomerId())
                .company(createCustomerRequest.company())
                .street(createCustomerRequest.street())
                .houseNumber(createCustomerRequest.houseNumber())
                .postalCode(createCustomerRequest.postalCode())
                .city(createCustomerRequest.city())
                .billingEmailAddress(createCustomerRequest.billingEmailAddress())
                .openaiApiKey(ENCRYPTED_VALUE)
                .sourceOfContact(createCustomerRequest.sourceOfContact())
                .websiteUrl(createCustomerRequest.websiteUrl())
                .privacyPolicyUrl(createCustomerRequest.privacyPolicyUrl())
                .ctaUrl(createCustomerRequest.ctaUrl())
                .registrationToken("registrationToken")
                .isTestVersion(false)
                .ionosUsername(createCustomerRequest.ionosUsername())
                .ionosPassword(ENCRYPTED_VALUE)
                .build();
    }

    @BeforeEach
    void setup() {
        testCustomer = buildTestCustomer();
    }

    @Test
    void testCreateCustomer_success() {
        when(customerRepository.existsByBillingEmailAddress(
                        createCustomerRequest.billingEmailAddress()))
                .thenReturn(false);
        when(customerRepository.existsByRegistrationToken(anyString())).thenReturn(false);

        customerService.createCustomer(createCustomerRequest);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        // spotless:off
        assertNotNull(savedCustomer);
        assertNotNull(savedCustomer.getRegistrationToken());
        assertEquals(createCustomerRequest.company(), savedCustomer.getCompany());
        assertEquals(createCustomerRequest.street(), savedCustomer.getStreet());
        assertEquals(createCustomerRequest.houseNumber(), savedCustomer.getHouseNumber());
        assertEquals(createCustomerRequest.postalCode(), savedCustomer.getPostalCode());
        assertEquals(createCustomerRequest.city(), savedCustomer.getCity());
        assertEquals(createCustomerRequest.billingEmailAddress(), savedCustomer.getBillingEmailAddress());
        assertEquals(ENCRYPTED_VALUE, savedCustomer.getOpenaiApiKey());
        assertEquals(createCustomerRequest.sourceOfContact(), savedCustomer.getSourceOfContact());
        assertEquals(createCustomerRequest.websiteUrl(), savedCustomer.getWebsiteUrl());
        assertEquals(createCustomerRequest.privacyPolicyUrl(), savedCustomer.getPrivacyPolicyUrl());
        assertEquals(createCustomerRequest.ctaUrl(), savedCustomer.getCtaUrl());
        assertEquals(createCustomerRequest.isTestVersion(), savedCustomer.isTestVersion());
        assertEquals(createCustomerRequest.ionosUsername(), savedCustomer.getIonosUsername());
        assertEquals(ENCRYPTED_VALUE, savedCustomer.getIonosPassword());
        // spotless:on
    }

    @Test
    void testCreateCustomer_alreadyExists() {
        when(customerRepository.existsByBillingEmailAddress(
                        createCustomerRequest.billingEmailAddress()))
                .thenReturn(true);

        assertThrows(
                EntityAlreadyExistsException.class,
                () -> customerService.createCustomer(createCustomerRequest));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testListCustomers_success() {
        when(customerRepository.findAll()).thenReturn(List.of(testCustomer));

        List<Customer> customers = customerService.listCustomers();

        verify(customerRepository).findAll();

        assertEquals(1, customers.size());
        assertEquals(testCustomer, customers.get(0));
    }

    @Test
    void testGetCustomer_success() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        Customer customer = customerService.getCustomer(testCustomer.getId(), jwtMock);

        verify(customerRepository).findById(testCustomer.getId());

        assertEquals(testCustomer, customer);
    }

    @Test
    void testGetCustomer_isTestVersion_success() {
        mockJwtWithCustomerClaimsOnly(testUser);

        testCustomer.setTestVersion(true);

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        Customer customer = customerService.getCustomer(testCustomer.getId(), jwtMock);

        verify(customerRepository).findById(testCustomer.getId());

        assertEquals(testCustomer, customer);
        assertNotNull(customer.getIonosPassword());
        assertEquals(DECRYPTED_VALUE, customer.getIonosPassword());
    }

    @Test
    void testGetCustomer_notFound() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> customerService.getCustomer(testCustomer.getId(), jwtMock));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testGetCustomer_idor() {
        mockJwtWithCustomerClaimsOnly(testUser);
        assertThrows(
                IdorException.class,
                () -> customerService.getCustomer(testCustomer.getId() + 1, jwtMock));

        assertThrows(
                IdorException.class,
                () -> customerService.getCustomer(testCustomer.getId() + 1, jwtMock));
    }

    @Test
    void testUpdateCustomer_success() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setCompany("Updated Company");

        customerService.updateCustomer(testCustomer.getId(), updatedCustomer, jwtMock);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals(updatedCustomer, savedCustomer);
    }

    @Test
    void testUpdateCustomer_idConflict() {
        mockJwtWithCustomerClaimsOnly(testUser);

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setId(testCustomer.getId() + 1);

        assertThrows(
                IdConflictException.class,
                () ->
                        customerService.updateCustomer(
                                testCustomer.getId(), updatedCustomer, jwtMock));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testUpdateCustomer_notFound() {
        mockJwtWithCustomerClaimsOnly(testUser);
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> customerService.updateCustomer(testCustomer.getId(), testCustomer, jwtMock));

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void testUpdateCustomer_updateConflict() {
        mockJwtWithCustomerClaimsOnly(testUser);

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setOpenaiApiKey("differentEncryptedKey");

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        assertThrows(
                UpdateConflictException.class,
                () ->
                        customerService.updateCustomer(
                                testCustomer.getId(), updatedCustomer, jwtMock));
    }

    @Test
    void testUpdateCustomer_billingEmailAddress_success() {
        mockJwtWithCustomerClaimsOnly(testUser);

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setBillingEmailAddress("invoice@exmaple.com");

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByBillingEmailAddress(
                        updatedCustomer.getBillingEmailAddress()))
                .thenReturn(false);

        customerService.updateCustomer(testCustomer.getId(), updatedCustomer, jwtMock);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals(updatedCustomer, savedCustomer);
    }

    @Test
    void testUpdateCustomer_billingEmailAddress_alreadyExists() {
        mockJwtWithCustomerClaimsOnly(testUser);

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setBillingEmailAddress("invoice@exmaple.com");

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));
        when(customerRepository.existsByBillingEmailAddress(
                        updatedCustomer.getBillingEmailAddress()))
                .thenReturn(true);

        assertThrows(
                EntityAlreadyExistsException.class,
                () ->
                        customerService.updateCustomer(
                                testCustomer.getId(), updatedCustomer, jwtMock));
    }

    @Test
    void testUpdateCustomer_preservesIonosCredentials_ifTestVersion_true() {
        mockJwtWithCustomerClaimsOnly(testUser);

        testCustomer.setTestVersion(true);

        Customer updatedCustomer = buildTestCustomer();
        updatedCustomer.setTestVersion(false);
        updatedCustomer.setIonosUsername(null);
        updatedCustomer.setIonosPassword(null);

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        customerService.updateCustomer(testCustomer.getId(), updatedCustomer, jwtMock);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals(updatedCustomer, savedCustomer);
        assertNotNull(savedCustomer.getIonosUsername());
        assertNotNull(savedCustomer.getIonosPassword());
        assertTrue(savedCustomer.isTestVersion());
        assertEquals(testCustomer.getIonosUsername(), savedCustomer.getIonosUsername());
        assertEquals(ENCRYPTED_VALUE, savedCustomer.getIonosPassword());
    }

    @Test
    void testUpdateCustomer_clearsIonosCredentials_ifTestVersion_false() {
        mockJwtWithCustomerClaimsOnly(testUser);

        testCustomer.setTestVersion(false);

        Customer updatedCustomer = buildTestCustomer();

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        customerService.updateCustomer(testCustomer.getId(), updatedCustomer, jwtMock);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertEquals(updatedCustomer, savedCustomer);
        assertFalse(savedCustomer.isTestVersion());
        assertNull(savedCustomer.getIonosUsername());
        assertNull(savedCustomer.getIonosPassword());
    }

    @Test
    void testUpdateCustomerTestVersion_isTestVersion_true() {
        testCustomer.setTestVersion(true);

        CustomerResource.UpdateCustomerTestVersionRequest request =
                new CustomerResource.UpdateCustomerTestVersionRequest(
                        testCustomer.getId(), true, "updatedTest@example.de", "updatedPassword");

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        customerService.updateCustomerTestVersion(testCustomer.getId(), request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertTrue(savedCustomer.isTestVersion());
        assertNotNull(savedCustomer.getIonosUsername());
        assertEquals(ENCRYPTED_VALUE, savedCustomer.getIonosPassword());
    }

    @Test
    void testUpdateCustomerTestVersion_isTestVersion_false() {
        testCustomer.setTestVersion(true);

        CustomerResource.UpdateCustomerTestVersionRequest request =
                new CustomerResource.UpdateCustomerTestVersionRequest(
                        testCustomer.getId(), false, null, null);

        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        customerService.updateCustomerTestVersion(testCustomer.getId(), request);

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        Customer savedCustomer = customerCaptor.getValue();

        assertFalse(savedCustomer.isTestVersion());
        assertNull(savedCustomer.getIonosUsername());
        assertNull(savedCustomer.getIonosPassword());
    }
}
