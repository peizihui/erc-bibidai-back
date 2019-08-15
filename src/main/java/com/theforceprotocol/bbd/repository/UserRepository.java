package com.theforceprotocol.bbd.repository;

import com.theforceprotocol.bbd.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCountryCodeAndPhone(Integer countryCode, String phone);
}
