package com.customermanagement.service;

import com.customermanagement.model.Customer;
import com.customermanagement.model.MembershipTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Service
public class MembershipTierService {
    
    /**
     * Calculate membership tier based on business rules:
     * - Silver: Annual spend < $1000
     * - Gold: Annual spend >= $1000 and purchased within the last 12 months and not qualified for Platinum
     * - Platinum: Annual spend >= $10000 and purchased within the last 6 months
     */
    public MembershipTier calculateMembershipTier(Customer customer) {
        BigDecimal annualSpend = customer.getAnnualSpend();
        ZonedDateTime lastPurchaseDate = customer.getLastPurchaseDate();
        
        // Default to Silver if annualSpend is null or less than $1000
        if (annualSpend == null || annualSpend.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return MembershipTier.SILVER;
        }

        ZonedDateTime now = ZonedDateTime.now();
        
        // Check for Platinum tier first (highest tier)
        if (annualSpend.compareTo(BigDecimal.valueOf(10000)) >= 0) {
            // Must have purchased within the last 6 months for Platinum
            if (lastPurchaseDate != null && !lastPurchaseDate.isBefore(now.minusMonths(6))) {
                return MembershipTier.PLATINUM;
            }
            // If annual spend >= $10,000 but no recent purchase within 6 months, check Gold eligibility
            if (lastPurchaseDate != null && !lastPurchaseDate.isBefore(now.minusMonths(12))) {
                return MembershipTier.GOLD;
            }
            // If no purchase within 12 months, default to Silver
            return MembershipTier.SILVER;
        }
        
        // Check for Gold tier (annual spend >= $1000 and < $10000)
        if (annualSpend.compareTo(BigDecimal.valueOf(1000)) >= 0) {
            // Must have purchased within the last 12 months for Gold
            if (lastPurchaseDate != null && !lastPurchaseDate.isBefore(now.minusMonths(12))) {
                return MembershipTier.GOLD;
            }
            // If no purchase within 12 months, default to Silver
            return MembershipTier.SILVER;
        }
        
        // Default case (should not reach here based on initial check, but included for completeness)
        return MembershipTier.SILVER;
    }
}