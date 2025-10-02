package com.customermanagement.config;

import com.customermanagement.model.Customer;
import com.customermanagement.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Component
@Profile("!test") // Don't run during tests
public class DataInitializer implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Autowired
    public DataInitializer(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            // Create sample customers with different tier scenarios
            
            // Silver tier customer (annual spend < $1000)
            Customer silverCustomer = new Customer(
                "Alice Johnson",
                "alice.johnson@example.com",
                BigDecimal.valueOf(800),
                ZonedDateTime.now().minusDays(30)
            );
            
            // Gold tier customer (annual spend >= $1000, purchased within 12 months)
            Customer goldCustomer = new Customer(
                    "Bob Smith", 
                    "bob.smith@example.com", 
                    BigDecimal.valueOf(2500), 
                    ZonedDateTime.now().minusMonths(8)
            );
            
            // Platinum tier customer (annual spend >= $10000, purchased within 6 months)
            Customer platinumCustomer = new Customer(
                    "Carol Williams", 
                    "carol.williams@example.com", 
                    BigDecimal.valueOf(15000),
                    ZonedDateTime.now().minusMonths(3)
            );
            
            // Customer with high spend but no recent purchase (should be Silver)
            Customer expiredCustomer = new Customer(
                    "David Brown", 
                    "david.brown@example.com", 
                    BigDecimal.valueOf(12000),
                    ZonedDateTime.now().minusMonths(18)
            );
            
            // Customer with medium spend but old purchase (should be Silver)
            Customer oldPurchaseCustomer = new Customer(
                    "Eva Davis", 
                    "eva.davis@example.com", 
                    BigDecimal.valueOf(3000),
                    ZonedDateTime.now().minusMonths(15)
            );
            
            // Customer with no purchase history
            Customer noPurchaseCustomer = new Customer(
                    "Frank Miller", 
                    "frank.miller@example.com", 
                    BigDecimal.valueOf(5000), 
                    null
            );
            
            customerRepository.save(silverCustomer);
            customerRepository.save(goldCustomer);
            customerRepository.save(platinumCustomer);
            customerRepository.save(expiredCustomer);
            customerRepository.save(oldPurchaseCustomer);
            customerRepository.save(noPurchaseCustomer);
            
            System.out.println("Sample customer data initialized!");
        }
    }
}