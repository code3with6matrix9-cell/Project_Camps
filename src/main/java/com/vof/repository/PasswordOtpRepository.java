package com.vof.repository;

import com.vof.entity.PasswordOtp;
import com.vof.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordOtpRepository extends JpaRepository<PasswordOtp, Long> {
    Optional<PasswordOtp> findByUser(User user);
    void deleteByUser(User user);
}
