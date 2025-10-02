package com.customermanagement.service;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.exception.CustomerNotFoundException;
import com.customermanagement.exception.DuplicateEmailException;
import com.customermanagement.model.MembershipTier;
import com.customermanagement.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ComponentScan(basePackages = "com.customermanagement")
@ActiveProfiles("test")
@Transactional
class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerDto goldCustomerDto;
    private CustomerDto silverCustomerDto;
    private CustomerDto platinumCustomerDto;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();

        // Gold tier customer (spend between $1000-$10000, recent purchase)
        goldCustomerDto = new CustomerDto();
        goldCustomerDto.setName("Gold Customer");
        goldCustomerDto.setEmail("gold@example.com");
        goldCustomerDto.setAnnualSpend(BigDecimal.valueOf(5000));
        goldCustomerDto.setLastPurchaseDate(ZonedDateTime.now().minusMonths(6));

        // Silver tier customer (spend < $1000)
        silverCustomerDto = new CustomerDto();
        silverCustomerDto.setName("Silver Customer");
        silverCustomerDto.setEmail("silver@example.com");
        silverCustomerDto.setAnnualSpend(BigDecimal.valueOf(500));
        silverCustomerDto.setLastPurchaseDate(ZonedDateTime.now().minusDays(30));

        // Platinum tier customer (spend >= $10000, recent purchase within 6 months)
        platinumCustomerDto = new CustomerDto();
        platinumCustomerDto.setName("Platinum Customer");
        platinumCustomerDto.setEmail("platinum@example.com");
        platinumCustomerDto.setAnnualSpend(BigDecimal.valueOf(15000));
        platinumCustomerDto.setLastPurchaseDate(ZonedDateTime.now().minusMonths(3));
    }

    @Test
    void createCustomer_ShouldPersistCustomerSuccessfully() {
        // When
        CustomerDto savedCustomer = customerService.createCustomer(goldCustomerDto);

        // Then
        assertNotNull(savedCustomer.getId());
        assertEquals(goldCustomerDto.getName(), savedCustomer.getName());
        assertEquals(goldCustomerDto.getEmail(), savedCustomer.getEmail());
        assertEquals(goldCustomerDto.getAnnualSpend(), savedCustomer.getAnnualSpend());
        assertEquals(MembershipTier.GOLD, savedCustomer.getMembershipTier());

        // Verify it's actually persisted
        assertTrue(customerRepository.existsById(savedCustomer.getId()));
    }

    @Test
    void createCustomer_ShouldThrowDuplicateEmailException_WhenEmailExists() {
        // Given
        customerService.createCustomer(goldCustomerDto);

        CustomerDto duplicateCustomer = new CustomerDto();
        duplicateCustomer.setName("Duplicate Customer");
        duplicateCustomer.setEmail(goldCustomerDto.getEmail()); // Same email
        duplicateCustomer.setAnnualSpend(BigDecimal.valueOf(2000));

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            customerService.createCustomer(duplicateCustomer);
        });
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomersWithCorrectMembershipTiers() {
        // Given
        CustomerDto savedGold = customerService.createCustomer(goldCustomerDto);
        CustomerDto savedSilver = customerService.createCustomer(silverCustomerDto);
        CustomerDto savedPlatinum = customerService.createCustomer(platinumCustomerDto);

        // When
        List<CustomerDto> allCustomers = customerService.getAllCustomers();

        // Then
        assertEquals(3, allCustomers.size());

        CustomerDto goldResult = allCustomers.stream()
                .filter(c -> c.getId().equals(savedGold.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(MembershipTier.GOLD, goldResult.getMembershipTier());

        CustomerDto silverResult = allCustomers.stream()
                .filter(c -> c.getId().equals(savedSilver.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(MembershipTier.SILVER, silverResult.getMembershipTier());

        CustomerDto platinumResult = allCustomers.stream()
                .filter(c -> c.getId().equals(savedPlatinum.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(MembershipTier.PLATINUM, platinumResult.getMembershipTier());
    }

    @Test
    void getCustomerById_ShouldReturnCorrectCustomer() {
        // Given
        CustomerDto savedCustomer = customerService.createCustomer(goldCustomerDto);

        // When
        CustomerDto foundCustomer = customerService.getCustomerById(savedCustomer.getId());

        // Then
        assertEquals(savedCustomer.getId(), foundCustomer.getId());
        assertEquals(savedCustomer.getName(), foundCustomer.getName());
        assertEquals(savedCustomer.getEmail(), foundCustomer.getEmail());
        assertEquals(MembershipTier.GOLD, foundCustomer.getMembershipTier());
    }

    @Test
    void getCustomerById_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(nonExistentId);
        });
    }

    @Test
    void getCustomerByEmail_ShouldReturnCorrectCustomer() {
        // Given
        CustomerDto savedCustomer = customerService.createCustomer(goldCustomerDto);

        // When
        CustomerDto foundCustomer = customerService.getCustomerByEmail(savedCustomer.getEmail());

        // Then
        assertEquals(savedCustomer.getId(), foundCustomer.getId());
        assertEquals(savedCustomer.getName(), foundCustomer.getName());
        assertEquals(savedCustomer.getEmail(), foundCustomer.getEmail());
    }

    @Test
    void searchCustomersByName_ShouldReturnMatchingCustomers() {
        // Given
        customerService.createCustomer(goldCustomerDto);
        customerService.createCustomer(silverCustomerDto);
        customerService.createCustomer(platinumCustomerDto);

        // When
        List<CustomerDto> goldResults = customerService.searchCustomersByName("Gold");
        List<CustomerDto> customerResults = customerService.searchCustomersByName("Customer");

        // Then
        assertEquals(1, goldResults.size());
        assertEquals("Gold Customer", goldResults.get(0).getName());

        assertEquals(3, customerResults.size()); // All have "Customer" in name
    }

    @Test
    void getCustomersByMembershipTier_ShouldReturnCorrectCustomers() {
        // Given
        customerService.createCustomer(goldCustomerDto);
        customerService.createCustomer(silverCustomerDto);
        customerService.createCustomer(platinumCustomerDto);

        // When
        List<CustomerDto> goldCustomers = customerService.getCustomersByMembershipTier(MembershipTier.GOLD);
        List<CustomerDto> silverCustomers = customerService.getCustomersByMembershipTier(MembershipTier.SILVER);
        List<CustomerDto> platinumCustomers = customerService.getCustomersByMembershipTier(MembershipTier.PLATINUM);

        // Then
        assertEquals(1, goldCustomers.size());
        assertEquals("Gold Customer", goldCustomers.get(0).getName());

        assertEquals(1, silverCustomers.size());
        assertEquals("Silver Customer", silverCustomers.get(0).getName());

        assertEquals(1, platinumCustomers.size());
        assertEquals("Platinum Customer", platinumCustomers.get(0).getName());
    }

    @Test
    void updateCustomer_ShouldUpdateCustomerSuccessfully() {
        // Given
        CustomerDto savedCustomer = customerService.createCustomer(goldCustomerDto);
        
        CustomerDto updateDto = new CustomerDto();
        updateDto.setName("Updated Gold Customer");
        updateDto.setEmail("updated.gold@example.com");
        updateDto.setAnnualSpend(BigDecimal.valueOf(7500));
        updateDto.setLastPurchaseDate(ZonedDateTime.now().minusDays(15));

        // When
        CustomerDto updatedCustomer = customerService.updateCustomer(savedCustomer.getId(), updateDto);

        // Then
        assertEquals(savedCustomer.getId(), updatedCustomer.getId());
        assertEquals("Updated Gold Customer", updatedCustomer.getName());
        assertEquals("updated.gold@example.com", updatedCustomer.getEmail());
        assertEquals(BigDecimal.valueOf(7500), updatedCustomer.getAnnualSpend());
        assertEquals(MembershipTier.GOLD, updatedCustomer.getMembershipTier());
    }

    @Test
    void updateCustomer_ShouldThrowDuplicateEmailException_WhenEmailAlreadyExists() {
        // Given
        CustomerDto customer1 = customerService.createCustomer(goldCustomerDto);
        CustomerDto customer2 = customerService.createCustomer(silverCustomerDto);

        CustomerDto updateDto = new CustomerDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail(customer2.getEmail()); // Using email of another customer
        updateDto.setAnnualSpend(BigDecimal.valueOf(3000));

        // When & Then
        assertThrows(DuplicateEmailException.class, () -> {
            customerService.updateCustomer(customer1.getId(), updateDto);
        });
    }

    @Test
    void deleteCustomer_ShouldDeleteCustomerSuccessfully() {
        // Given
        CustomerDto savedCustomer = customerService.createCustomer(goldCustomerDto);
        UUID customerId = savedCustomer.getId();

        // When
        customerService.deleteCustomer(customerId);

        // Then
        assertFalse(customerRepository.existsById(customerId));
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.getCustomerById(customerId);
        });
    }

    @Test
    void deleteCustomer_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        assertThrows(CustomerNotFoundException.class, () -> {
            customerService.deleteCustomer(nonExistentId);
        });
    }

    @Test
    void membershipTierCalculation_ShouldWorkCorrectlyInIntegrationContext() {
        // Create customer with conditions that should result in different tiers based on time
        
        // Customer with high spend but old purchase date - should be Silver
        CustomerDto expiredHighSpendCustomer = new CustomerDto();
        expiredHighSpendCustomer.setName("Expired High Spend Customer");
        expiredHighSpendCustomer.setEmail("expired.high@example.com");
        expiredHighSpendCustomer.setAnnualSpend(BigDecimal.valueOf(15000));
        expiredHighSpendCustomer.setLastPurchaseDate(ZonedDateTime.now().minusMonths(15)); // Old purchase

        CustomerDto savedExpiredCustomer = customerService.createCustomer(expiredHighSpendCustomer);
        
        // Verify the membership tier was calculated correctly
        assertEquals(MembershipTier.SILVER, savedExpiredCustomer.getMembershipTier());
        
        // Customer with medium spend and recent purchase - should be Gold
        CustomerDto recentMediumSpendCustomer = new CustomerDto();
        recentMediumSpendCustomer.setName("Recent Medium Spend Customer");
        recentMediumSpendCustomer.setEmail("recent.medium@example.com");
        recentMediumSpendCustomer.setAnnualSpend(BigDecimal.valueOf(5000));
        recentMediumSpendCustomer.setLastPurchaseDate(ZonedDateTime.now().minusDays(30)); // Recent purchase
        
        CustomerDto savedRecentCustomer = customerService.createCustomer(recentMediumSpendCustomer);
        
        // Verify the membership tier was calculated correctly
        assertEquals(MembershipTier.GOLD, savedRecentCustomer.getMembershipTier());
    }
}