package de.flowsuite.mailflowapi.customer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.flowsuite.mailflowapi.BaseServiceTest;
import de.flowsuite.mailflowcommon.constant.Authorities;
import de.flowsuite.mailflowcommon.entity.Customer;
import de.flowsuite.mailflowcommon.entity.User;
import de.flowsuite.mailflowcommon.exception.*;
import de.flowsuite.mailflowcommon.util.AuthorisationUtil;

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
                    "https://example.com/cta");

    private Customer buildTestCustomer() {
        return Customer.builder()
                .id(testUser.getCustomerId())
                .company(createCustomerRequest.company())
                .street(createCustomerRequest.street())
                .houseNumber(createCustomerRequest.houseNumber())
                .postalCode(createCustomerRequest.postalCode())
                .city(createCustomerRequest.city())
                .billingEmailAddress(createCustomerRequest.billingEmailAddress())
                .openaiApiKey(createCustomerRequest.openaiApiKey())
                .sourceOfContact(createCustomerRequest.sourceOfContact())
                .websiteUrl(createCustomerRequest.websiteUrl())
                .privacyPolicyUrl(createCustomerRequest.privacyPolicyUrl())
                .ctaUrl(createCustomerRequest.ctaUrl())
                .registrationToken("registrationToken")
                .build();
    }

    @Override
    protected void mockJwtForUser(User user) {
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_SCOPE))
                .thenReturn(Authorities.USER.getAuthority());
        when(jwtMock.getClaim(AuthorisationUtil.CLAIM_CUSTOMER_ID))
                .thenReturn(user.getCustomerId());
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
        mockJwtForUser(testUser);
        when(customerRepository.findById(testCustomer.getId()))
                .thenReturn(Optional.of(testCustomer));

        Customer customer = customerService.getCustomer(testCustomer.getId(), jwtMock);

        verify(customerRepository).findById(testCustomer.getId());

        assertEquals(testCustomer, customer);
    }

    @Test
    void testGetCustomer_notFound() {
        mockJwtForUser(testUser);
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> customerService.getCustomer(testCustomer.getId(), jwtMock));
    }

    @Test
    void testGetCustomer_idor() {
        mockJwtForUser(testUser);
        assertThrows(
                IdorException.class,
                () -> customerService.getCustomer(testCustomer.getId() + 1, jwtMock));

        verify(customerRepository, never()).findById(testCustomer.getId());
    }

    @Test
    void testUpdateCustomer_success() {
        mockJwtForUser(testUser);
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
        mockJwtForUser(testUser);

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
        mockJwtForUser(testUser);
        when(customerRepository.findById(testCustomer.getId())).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> customerService.updateCustomer(testCustomer.getId(), testCustomer, jwtMock));
    }

    @Test
    void testUpdateCustomer_openaiKeyConflict() {
        mockJwtForUser(testUser);

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
}
