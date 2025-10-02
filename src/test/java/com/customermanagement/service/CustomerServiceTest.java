package com.customermanagement.service;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.exception.CustomerNotFoundException;
import com.customermanagement.exception.DuplicateEmailException;
import com.customermanagement.mapper.CustomerMapper;
import com.customermanagement.model.Customer;
import com.customermanagement.model.MembershipTier;
import com.customermanagement.repository.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private MembershipTierService membershipTierService;

    @InjectMocks
    private CustomerService customerService;

    private Customer testCustomer;
    private CustomerDto testCustomerDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testCustomer = new Customer();
        testCustomer.setId(testId);
        testCustomer.setName("John Doe");
        testCustomer.setEmail("john.doe@example.com");
        testCustomer.setAnnualSpend(BigDecimal.valueOf(1500));
        testCustomer.setLastPurchaseDate(ZonedDateTime.now().minusDays(30));

        testCustomerDto = new CustomerDto();
        testCustomerDto.setId(testId);
        testCustomerDto.setName("John Doe");
        testCustomerDto.setEmail("john.doe@example.com");
        testCustomerDto.setAnnualSpend(BigDecimal.valueOf(1500));
        testCustomerDto.setLastPurchaseDate(ZonedDateTime.now().minusDays(30));
        testCustomerDto.setMembershipTier(MembershipTier.GOLD);
    }

    @Test
    void getAllCustomers_ShouldReturnListOfCustomerDtos() {
        // Given
        List<Customer> customers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(customers);
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        // When
        List<CustomerDto> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomerDto, result.get(0));
        verify(customerRepository).findAll();
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void getAllCustomers_ShouldReturnEmptyListWhenNoCustomers() {
        // Given
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CustomerDto> result = customerService.getAllCustomers();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository).findAll();
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getCustomerById_ShouldReturnCustomerDto_WhenCustomerExists() {
        // Given
        when(customerRepository.findById(testId)).thenReturn(Optional.of(testCustomer));
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        // When
        CustomerDto result = customerService.getCustomerById(testId);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerDto, result);
        verify(customerRepository).findById(testId);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void getCustomerById_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        when(customerRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerService.getCustomerById(testId)
        );

        assertEquals("Customer not found with id: " + testId, exception.getMessage());
        verify(customerRepository).findById(testId);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getCustomerByEmail_ShouldReturnCustomerDto_WhenCustomerExists() {
        // Given
        String email = "john.doe@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(testCustomer));
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        // When
        CustomerDto result = customerService.getCustomerByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(testCustomerDto, result);
        verify(customerRepository).findByEmail(email);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void getCustomerByEmail_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        String email = "nonexistent@example.com";
        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerService.getCustomerByEmail(email)
        );

        assertEquals("Customer not found with email: " + email, exception.getMessage());
        verify(customerRepository).findByEmail(email);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void searchCustomersByName_ShouldReturnMatchingCustomers() {
        // Given
        String searchName = "John";
        List<Customer> matchingCustomers = Arrays.asList(testCustomer);
        when(customerRepository.findByNameContaining(searchName)).thenReturn(matchingCustomers);
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        // When
        List<CustomerDto> result = customerService.searchCustomersByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomerDto, result.get(0));
        verify(customerRepository).findByNameContaining(searchName);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void searchCustomersByName_ShouldReturnEmptyListWhenNoMatches() {
        // Given
        String searchName = "NonExistent";
        when(customerRepository.findByNameContaining(searchName)).thenReturn(Collections.emptyList());

        // When
        List<CustomerDto> result = customerService.searchCustomersByName(searchName);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository).findByNameContaining(searchName);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getCustomersByMembershipTier_ShouldReturnMatchingCustomers() {
        // Given
        MembershipTier tier = MembershipTier.GOLD;
        List<Customer> allCustomers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(allCustomers);
        when(membershipTierService.calculateMembershipTier(testCustomer)).thenReturn(tier);
        when(customerMapper.toDto(testCustomer)).thenReturn(testCustomerDto);

        // When
        List<CustomerDto> result = customerService.getCustomersByMembershipTier(tier);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomerDto, result.get(0));
        verify(customerRepository).findAll();
        verify(membershipTierService).calculateMembershipTier(testCustomer);
        verify(customerMapper).toDto(testCustomer);
    }

    @Test
    void getCustomersByMembershipTier_ShouldReturnEmptyListWhenNoMatches() {
        // Given
        MembershipTier tier = MembershipTier.PLATINUM;
        List<Customer> allCustomers = Arrays.asList(testCustomer);
        when(customerRepository.findAll()).thenReturn(allCustomers);
        when(membershipTierService.calculateMembershipTier(testCustomer)).thenReturn(MembershipTier.GOLD);

        // When
        List<CustomerDto> result = customerService.getCustomersByMembershipTier(tier);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(customerRepository).findAll();
        verify(membershipTierService).calculateMembershipTier(testCustomer);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void createCustomer_ShouldCreateAndReturnCustomerDto_WhenEmailIsUnique() {
        // Given
        CustomerDto newCustomerDto = new CustomerDto();
        newCustomerDto.setName("Jane Smith");
        newCustomerDto.setEmail("jane.smith@example.com");
        newCustomerDto.setAnnualSpend(BigDecimal.valueOf(2000));

        Customer newCustomer = new Customer();
        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());

        CustomerDto savedCustomerDto = new CustomerDto();
        savedCustomerDto.setId(savedCustomer.getId());

        when(customerRepository.existsByEmail(newCustomerDto.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(newCustomerDto)).thenReturn(newCustomer);
        when(customerRepository.save(newCustomer)).thenReturn(savedCustomer);
        when(customerMapper.toDto(savedCustomer)).thenReturn(savedCustomerDto);

        // When
        CustomerDto result = customerService.createCustomer(newCustomerDto);

        // Then
        assertNotNull(result);
        assertEquals(savedCustomerDto, result);
        verify(customerRepository).existsByEmail(newCustomerDto.getEmail());
        verify(customerMapper).toEntity(newCustomerDto);
        verify(customerRepository).save(newCustomer);
        verify(customerMapper).toDto(savedCustomer);
    }

    @Test
    void createCustomer_ShouldThrowDuplicateEmailException_WhenEmailAlreadyExists() {
        // Given
        CustomerDto newCustomerDto = new CustomerDto();
        newCustomerDto.setEmail("existing@example.com");

        when(customerRepository.existsByEmail(newCustomerDto.getEmail())).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> customerService.createCustomer(newCustomerDto)
        );

        assertEquals("Customer with email existing@example.com already exists", exception.getMessage());
        verify(customerRepository).existsByEmail(newCustomerDto.getEmail());
        verifyNoInteractions(customerMapper);
        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_ShouldUpdateAndReturnCustomerDto_WhenValidData() {
        // Given
        CustomerDto updateDto = new CustomerDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");
        updateDto.setAnnualSpend(BigDecimal.valueOf(3000));

        Customer existingCustomer = new Customer();
        existingCustomer.setId(testId);

        Customer updatedCustomer = new Customer();
        updatedCustomer.setId(testId);

        CustomerDto updatedCustomerDto = new CustomerDto();
        updatedCustomerDto.setId(testId);

        when(customerRepository.findById(testId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot(updateDto.getEmail(), testId)).thenReturn(false);
        when(customerRepository.save(existingCustomer)).thenReturn(updatedCustomer);
        when(customerMapper.toDto(updatedCustomer)).thenReturn(updatedCustomerDto);

        // When
        CustomerDto result = customerService.updateCustomer(testId, updateDto);

        // Then
        assertNotNull(result);
        assertEquals(updatedCustomerDto, result);
        verify(customerRepository).findById(testId);
        verify(customerRepository).existsByEmailAndIdNot(updateDto.getEmail(), testId);
        verify(customerMapper).updateEntityFromDto(updateDto, existingCustomer);
        verify(customerRepository).save(existingCustomer);
        verify(customerMapper).toDto(updatedCustomer);
    }

    @Test
    void updateCustomer_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        CustomerDto updateDto = new CustomerDto();
        when(customerRepository.findById(testId)).thenReturn(Optional.empty());

        // When & Then
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerService.updateCustomer(testId, updateDto)
        );

        assertEquals("Customer not found with id: " + testId, exception.getMessage());
        verify(customerRepository).findById(testId);
        verify(customerRepository, never()).existsByEmailAndIdNot(any(), any());
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(customerMapper);
    }

    @Test
    void updateCustomer_ShouldThrowDuplicateEmailException_WhenEmailAlreadyExistsForAnotherCustomer() {
        // Given
        CustomerDto updateDto = new CustomerDto();
        updateDto.setEmail("duplicate@example.com");

        Customer existingCustomer = new Customer();
        existingCustomer.setId(testId);

        when(customerRepository.findById(testId)).thenReturn(Optional.of(existingCustomer));
        when(customerRepository.existsByEmailAndIdNot(updateDto.getEmail(), testId)).thenReturn(true);

        // When & Then
        DuplicateEmailException exception = assertThrows(
            DuplicateEmailException.class,
            () -> customerService.updateCustomer(testId, updateDto)
        );

        assertEquals("Customer with email duplicate@example.com already exists", exception.getMessage());
        verify(customerRepository).findById(testId);
        verify(customerRepository).existsByEmailAndIdNot(updateDto.getEmail(), testId);
        verify(customerRepository, never()).save(any());
        verifyNoInteractions(customerMapper);
    }

    @Test
    void deleteCustomer_ShouldDeleteCustomer_WhenCustomerExists() {
        // Given
        when(customerRepository.existsById(testId)).thenReturn(true);

        // When
        customerService.deleteCustomer(testId);

        // Then
        verify(customerRepository).existsById(testId);
        verify(customerRepository).deleteById(testId);
    }

    @Test
    void deleteCustomer_ShouldThrowCustomerNotFoundException_WhenCustomerDoesNotExist() {
        // Given
        when(customerRepository.existsById(testId)).thenReturn(false);

        // When & Then
        CustomerNotFoundException exception = assertThrows(
            CustomerNotFoundException.class,
            () -> customerService.deleteCustomer(testId)
        );

        assertEquals("Customer not found with id: " + testId, exception.getMessage());
        verify(customerRepository).existsById(testId);
        verify(customerRepository, never()).deleteById(any());
    }
}