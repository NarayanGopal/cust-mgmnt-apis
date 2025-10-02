package com.customermanagement.controller;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.exception.CustomerNotFoundException;
import com.customermanagement.exception.DuplicateEmailException;
import com.customermanagement.model.MembershipTier;
import com.customermanagement.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    private ObjectMapper objectMapper;
    private CustomerDto testCustomerDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testId = UUID.randomUUID();
        testCustomerDto = new CustomerDto();
        testCustomerDto.setId(testId);
        testCustomerDto.setName("John Doe");
        testCustomerDto.setEmail("john.doe@example.com");
        testCustomerDto.setAnnualSpend(new BigDecimal("1500.00"));
        testCustomerDto.setLastPurchaseDate(ZonedDateTime.now());
        testCustomerDto.setMembershipTier(MembershipTier.GOLD);
    }

    @Test
    void getAllCustomers_ShouldReturnAllCustomers() throws Exception {
        // Given
        List<CustomerDto> customers = Arrays.asList(testCustomerDto, createAnotherCustomer());
        when(customerService.getAllCustomers()).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(testCustomerDto.getId().toString())))
                .andExpect(jsonPath("$[0].name", is(testCustomerDto.getName())))
                .andExpect(jsonPath("$[0].email", is(testCustomerDto.getEmail())))
                .andExpect(jsonPath("$[0].membershipTier", is(testCustomerDto.getMembershipTier().toString())));

        verify(customerService).getAllCustomers();
    }

    @Test
    void getCustomersByName_ShouldReturnFilteredCustomers() throws Exception {
        // Given
        String searchName = "John";
        List<CustomerDto> customers = Collections.singletonList(testCustomerDto);
        when(customerService.searchCustomersByName(searchName)).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers")
                        .param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", containsString(searchName)));

        verify(customerService).searchCustomersByName(searchName);
    }

    @Test
    void getCustomersByEmail_ShouldReturnSingleCustomer() throws Exception {
        // Given
        String email = "john.doe@example.com";
        when(customerService.getCustomerByEmail(email)).thenReturn(testCustomerDto);

        // When & Then
        mockMvc.perform(get("/api/customers")
                        .param("email", email))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(email)));

        verify(customerService).getCustomerByEmail(email);
    }

    @Test
    void getCustomerById_ShouldReturnCustomer_WhenCustomerExists() throws Exception {
        // Given
        when(customerService.getCustomerById(testId)).thenReturn(testCustomerDto);

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testId.toString())))
                .andExpect(jsonPath("$.name", is(testCustomerDto.getName())))
                .andExpect(jsonPath("$.email", is(testCustomerDto.getEmail())));

        verify(customerService).getCustomerById(testId);
    }

    @Test
    void getCustomerById_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(customerService.getCustomerById(nonExistentId))
                .thenThrow(new CustomerNotFoundException("Customer not found with id: " + nonExistentId));

        // When & Then
        mockMvc.perform(get("/api/customers/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(customerService).getCustomerById(nonExistentId);
    }

    @Test
    void getCustomersByMembershipTier_ShouldReturnCustomersWithSpecificTier() throws Exception {
        // Given
        MembershipTier tier = MembershipTier.GOLD;
        List<CustomerDto> customers = Collections.singletonList(testCustomerDto);
        when(customerService.getCustomersByMembershipTier(tier)).thenReturn(customers);

        // When & Then
        mockMvc.perform(get("/api/customers/tier/{tier}", tier.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].membershipTier", is(tier.toString())));

        verify(customerService).getCustomersByMembershipTier(tier);
    }

    @Test
    void createCustomer_ShouldReturnCreatedCustomer_WhenValidInput() throws Exception {
        // Given
        CustomerDto inputDto = new CustomerDto();
        inputDto.setName("Jane Smith");
        inputDto.setEmail("jane.smith@example.com");
        inputDto.setAnnualSpend(new BigDecimal("2000.00"));
        inputDto.setLastPurchaseDate(ZonedDateTime.now());

        CustomerDto createdDto = new CustomerDto();
        createdDto.setId(UUID.randomUUID());
        createdDto.setName(inputDto.getName());
        createdDto.setEmail(inputDto.getEmail());
        createdDto.setAnnualSpend(inputDto.getAnnualSpend());
        createdDto.setLastPurchaseDate(inputDto.getLastPurchaseDate());
        createdDto.setMembershipTier(MembershipTier.GOLD);

        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(createdDto);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is(inputDto.getName())))
                .andExpect(jsonPath("$.email", is(inputDto.getEmail())))
                .andExpect(jsonPath("$.membershipTier", is(MembershipTier.GOLD.toString())));

        verify(customerService).createCustomer(any(CustomerDto.class));
    }

    @Test
    void createCustomer_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        // Given
        CustomerDto invalidDto = new CustomerDto();
        invalidDto.setName(""); // Invalid: blank name
        invalidDto.setEmail("invalid-email"); // Invalid: invalid email format

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).createCustomer(any());
    }

    @Test
    void createCustomer_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Given
        CustomerDto inputDto = new CustomerDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("existing@example.com");
        inputDto.setAnnualSpend(new BigDecimal("1000.00"));

        when(customerService.createCustomer(any(CustomerDto.class)))
                .thenThrow(new DuplicateEmailException("Customer with email already exists"));

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isConflict());

        verify(customerService).createCustomer(any(CustomerDto.class));
    }

    @Test
    void updateCustomer_ShouldReturnUpdatedCustomer_WhenValidInput() throws Exception {
        // Given
        CustomerDto updatedDto = new CustomerDto();
        updatedDto.setId(testId);
        updatedDto.setName("Updated Name");
        updatedDto.setEmail("updated@example.com");
        updatedDto.setAnnualSpend(new BigDecimal("3000.00"));
        updatedDto.setLastPurchaseDate(ZonedDateTime.now());
        updatedDto.setMembershipTier(MembershipTier.PLATINUM);

        when(customerService.updateCustomer(eq(testId), any(CustomerDto.class))).thenReturn(updatedDto);

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testId.toString())))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.membershipTier", is(MembershipTier.PLATINUM.toString())));

        verify(customerService).updateCustomer(eq(testId), any(CustomerDto.class));
    }

    @Test
    void updateCustomer_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        CustomerDto updateDto = new CustomerDto();
        updateDto.setName("Updated Name");
        updateDto.setEmail("updated@example.com");

        when(customerService.updateCustomer(eq(nonExistentId), any(CustomerDto.class)))
                .thenThrow(new CustomerNotFoundException("Customer not found"));

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(customerService).updateCustomer(eq(nonExistentId), any(CustomerDto.class));
    }

    @Test
    void updateCustomer_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        // Given
        CustomerDto invalidDto = new CustomerDto();
        invalidDto.setName(""); // Invalid: blank name
        invalidDto.setEmail("invalid-email"); // Invalid: invalid email format

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(customerService, never()).updateCustomer(any(), any());
    }

    @Test
    void updateCustomer_ShouldReturnConflict_WhenEmailAlreadyExistsForAnotherCustomer() throws Exception {
        // Given
        CustomerDto updateDto = new CustomerDto();
        updateDto.setName("John Doe");
        updateDto.setEmail("existing@example.com");
        updateDto.setAnnualSpend(new BigDecimal("1000.00"));

        when(customerService.updateCustomer(eq(testId), any(CustomerDto.class)))
                .thenThrow(new DuplicateEmailException("Email already exists for another customer"));

        // When & Then
        mockMvc.perform(put("/api/customers/{id}", testId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isConflict());

        verify(customerService).updateCustomer(eq(testId), any(CustomerDto.class));
    }

    @Test
    void deleteCustomer_ShouldReturnNoContent_WhenCustomerExists() throws Exception {
        // Given
        doNothing().when(customerService).deleteCustomer(testId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", testId))
                .andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(testId);
    }

    @Test
    void deleteCustomer_ShouldReturnNotFound_WhenCustomerDoesNotExist() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new CustomerNotFoundException("Customer not found"))
                .when(customerService).deleteCustomer(nonExistentId);

        // When & Then
        mockMvc.perform(delete("/api/customers/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(customerService).deleteCustomer(nonExistentId);
    }

    @Test
    void createCustomer_ShouldIgnoreProvidedId() throws Exception {
        // Given
        CustomerDto inputDto = new CustomerDto();
        inputDto.setId(UUID.randomUUID()); // This should be ignored
        inputDto.setName("Jane Smith");
        inputDto.setEmail("jane.smith@example.com");
        inputDto.setAnnualSpend(new BigDecimal("2000.00"));

        CustomerDto createdDto = new CustomerDto();
        UUID newId = UUID.randomUUID();
        createdDto.setId(newId);
        createdDto.setName(inputDto.getName());
        createdDto.setEmail(inputDto.getEmail());
        createdDto.setAnnualSpend(inputDto.getAnnualSpend());
        createdDto.setMembershipTier(MembershipTier.GOLD);

        when(customerService.createCustomer(any(CustomerDto.class))).thenReturn(createdDto);

        // When & Then
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(newId.toString())))
                .andExpect(jsonPath("$.name", is(inputDto.getName())));

        // Verify that the service was called with a DTO that has null ID
        verify(customerService).createCustomer(argThat(dto -> dto.getId() == null));
    }

    @Test
    void getCustomers_ShouldReturnEmptyList_WhenNoCustomersExist() throws Exception {
        // Given
        when(customerService.getAllCustomers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService).getAllCustomers();
    }

    @Test
    void getCustomersByName_ShouldReturnEmptyList_WhenNoMatchingCustomers() throws Exception {
        // Given
        String searchName = "NonExistent";
        when(customerService.searchCustomersByName(searchName)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers")
                        .param("name", searchName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService).searchCustomersByName(searchName);
    }

    @Test
    void getCustomersByMembershipTier_ShouldReturnEmptyList_WhenNoCustomersWithTier() throws Exception {
        // Given
        MembershipTier tier = MembershipTier.PLATINUM;
        when(customerService.getCustomersByMembershipTier(tier)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/customers/tier/{tier}", tier.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(customerService).getCustomersByMembershipTier(tier);
    }

    private CustomerDto createAnotherCustomer() {
        CustomerDto customer = new CustomerDto();
        customer.setId(UUID.randomUUID());
        customer.setName("Jane Smith");
        customer.setEmail("jane.smith@example.com");
        customer.setAnnualSpend(new BigDecimal("2500.00"));
        customer.setLastPurchaseDate(ZonedDateTime.now().minusDays(10));
        customer.setMembershipTier(MembershipTier.PLATINUM);
        return customer;
    }
}