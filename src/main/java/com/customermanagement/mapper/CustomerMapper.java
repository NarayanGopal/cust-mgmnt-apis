package com.customermanagement.mapper;

import com.customermanagement.dto.CustomerDto;
import com.customermanagement.model.Customer;
import com.customermanagement.service.MembershipTierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    
    private final MembershipTierService membershipTierService;
    
    @Autowired
    public CustomerMapper(MembershipTierService membershipTierService) {
        this.membershipTierService = membershipTierService;
    }
    
    public CustomerDto toDto(Customer customer) {
        if (customer == null) {
            return null;
        }
        
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setAnnualSpend(customer.getAnnualSpend());
        dto.setLastPurchaseDate(customer.getLastPurchaseDate());
        
        // Calculate membership tier on-the-fly based on business rules
        dto.setMembershipTier(membershipTierService.calculateMembershipTier(customer));
        
        return dto;
    }
    
    public Customer toEntity(CustomerDto dto) {
        if (dto == null) {
            return null;
        }
        
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setAnnualSpend(dto.getAnnualSpend());
        customer.setLastPurchaseDate(dto.getLastPurchaseDate());
        
        return customer;
    }
    
    public void updateEntityFromDto(CustomerDto dto, Customer customer) {
        if (dto == null || customer == null) {
            return;
        }
        
        customer.setName(dto.getName());
        customer.setEmail(dto.getEmail());
        customer.setAnnualSpend(dto.getAnnualSpend());
        customer.setLastPurchaseDate(dto.getLastPurchaseDate());
    }
}