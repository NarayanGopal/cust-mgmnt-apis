package com.customermanagement.dto;

import com.customermanagement.model.MembershipTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public class CustomerDto {
    
    private UUID id;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    private BigDecimal annualSpend;
    
    private ZonedDateTime lastPurchaseDate;
    
    private MembershipTier membershipTier;
    
    // Constructors
    public CustomerDto() {}
    
    public CustomerDto(String name, String email, BigDecimal annualSpend, ZonedDateTime lastPurchaseDate) {
        this.name = name;
        this.email = email;
        this.annualSpend = annualSpend;
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public BigDecimal getAnnualSpend() {
        return annualSpend;
    }
    
    public void setAnnualSpend(BigDecimal annualSpend) {
        this.annualSpend = annualSpend;
    }
    
    public ZonedDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(ZonedDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    public MembershipTier getMembershipTier() {
        return membershipTier;
    }
    
    public void setMembershipTier(MembershipTier membershipTier) {
        this.membershipTier = membershipTier;
    }
}