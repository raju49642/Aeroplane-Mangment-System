package com.ams.repository;

import com.ams.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUserName(String userName);

    boolean existsByUserName(String userName);

    boolean existsByUserNameIgnoreCase(String userName);

    boolean existsByEmailIdIgnoreCase(String emailId);

    boolean existsByRole(String role);

    List<User> findByRoleAndAdminStatus(String role, String adminStatus);
}
