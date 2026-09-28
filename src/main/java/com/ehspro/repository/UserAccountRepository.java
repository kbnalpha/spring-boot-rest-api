package com.ehspro.repository;
import com.ehspro.entity.*;
import org.springframework.data.jpa.repository.*;
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> { java.util.Optional<UserAccount> findByUsername(String username);
    java.util.Optional<UserAccount> findByEmployeeId(Long employeeId); }

