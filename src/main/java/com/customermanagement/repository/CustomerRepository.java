package com.customermanagement.repository;

import com.customermanagement.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:name%")
    List<Customer> findByNameContaining(@Param("name") String name);
    
    boolean existsByEmail(String email);
    
    boolean existsByEmailAndIdNot(String email, UUID id);
}