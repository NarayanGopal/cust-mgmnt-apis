package com.customermanagement.service;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.exception.CustomerNotFoundException;
import com.customermanagement.exception.DuplicateEmailException;
import com.customermanagement.mapper.CustomerMapper;
import com.customermanagement.model.Customer;
import com.customermanagement.model.MembershipTier;
import com.customermanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final MembershipTierService membershipTierService;
    
    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper, MembershipTierService membershipTierService) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.membershipTierService = membershipTierService;
    }
    
    public List<CustomerDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public CustomerDto getCustomerById(UUID id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return customerMapper.toDto(customer);
    }
    
    public CustomerDto getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with email: " + email));
        return customerMapper.toDto(customer);
    }
    
    public List<CustomerDto> searchCustomersByName(String name) {
        return customerRepository.findByNameContaining(name)
                .stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public List<CustomerDto> getCustomersByMembershipTier(MembershipTier tier) {
        return customerRepository.findAll()
                .stream()
                .filter(customer -> membershipTierService.calculateMembershipTier(customer) == tier)
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public CustomerDto createCustomer(CustomerDto customerDto) {
        if (customerRepository.existsByEmail(customerDto.getEmail())) {
            throw new DuplicateEmailException("Customer with email " + customerDto.getEmail() + " already exists");
        }
        
        Customer customer = customerMapper.toEntity(customerDto);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDto(savedCustomer);
    }
    
    public CustomerDto updateCustomer(UUID id, CustomerDto customerDto) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        
        // Check for duplicate email (excluding current customer)
        if (customerRepository.existsByEmailAndIdNot(customerDto.getEmail(), id)) {
            throw new DuplicateEmailException("Customer with email " + customerDto.getEmail() + " already exists");
        }
        
        customerMapper.updateEntityFromDto(customerDto, existingCustomer);
        Customer savedCustomer = customerRepository.save(existingCustomer);
        return customerMapper.toDto(savedCustomer);
    }
    
    public void deleteCustomer(UUID id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }
}