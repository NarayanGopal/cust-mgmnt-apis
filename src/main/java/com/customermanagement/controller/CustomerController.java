package com.customermanagement.controller;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.model.MembershipTier;
import com.customermanagement.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer Management", description = "APIs for managing customer data")
public class CustomerController {
    
    private final CustomerService customerService;
    
    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    
    @GetMapping
    @Operation(summary = "Get customers", description = "Retrieve customers by name or email query parameters, or all customers if no parameters")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    public ResponseEntity<List<CustomerDto>> getCustomers(
            @Parameter(description = "Customer name to search for") @RequestParam(required = false) String name,
            @Parameter(description = "Customer email to search for") @RequestParam(required = false) String email) {
        
        if (name != null) {
            List<CustomerDto> customers = customerService.searchCustomersByName(name);
            return ResponseEntity.ok(customers);
        } else if (email != null) {
            CustomerDto customer = customerService.getCustomerByEmail(email);
            return ResponseEntity.ok(List.of(customer));
        } else {
            List<CustomerDto> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers);
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved customer"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer UUID") @PathVariable UUID id) {
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }
    
    @GetMapping("/tier/{tier}")
    @Operation(summary = "Get customers by membership tier", description = "Retrieve customers by their calculated membership tier")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    public ResponseEntity<List<CustomerDto>> getCustomersByMembershipTier(
            @Parameter(description = "Membership tier (SILVER, GOLD, PLATINUM)") @PathVariable MembershipTier tier) {
        List<CustomerDto> customers = customerService.getCustomersByMembershipTier(tier);
        return ResponseEntity.ok(customers);
    }
    
    @PostMapping
    @Operation(summary = "Create new customer", description = "Create a new customer record. ID will be auto-generated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Customer with email already exists")
    })
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerDto customerDto) {
        // Ensure ID is not set for creation
        customerDto.setId(null);
        CustomerDto createdCustomer = customerService.createCustomer(customerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update an existing customer record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists for another customer")
    })
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID id,
            @Valid @RequestBody CustomerDto customerDto) {
        CustomerDto updatedCustomer = customerService.updateCustomer(id, customerDto);
        return ResponseEntity.ok(updatedCustomer);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Delete a customer record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}