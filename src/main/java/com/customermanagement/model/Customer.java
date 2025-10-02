package com.customermanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false)
    private String name;
    
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "annual_spend", precision = 10, scale = 2)
    private BigDecimal annualSpend;
    
    @Column(name = "last_purchase_date")
    private ZonedDateTime lastPurchaseDate;
    
    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
    
    // Constructors
    public Customer() {}
    
    public Customer(String name, String email, BigDecimal annualSpend, ZonedDateTime lastPurchaseDate) {
        this.name = name;
        this.email = email;
        this.annualSpend = annualSpend;
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}