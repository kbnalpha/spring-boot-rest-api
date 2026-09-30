package com.ehspro.repository;
import com.ehspro.entity.*;
import org.springframework.data.jpa.repository.*;
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> { java.util.Optional<UserAccount> findByUsername(String username);
    java.util.Optional<UserAccount> findByEmployeeId(Long employeeId);
    java.util.Optional<UserAccount> findByUsernameIgnoreCase(String username);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from UserAccount a where a.id = :id")
    java.util.Optional<UserAccount> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
