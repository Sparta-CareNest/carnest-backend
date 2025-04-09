package com.carenest.business.userservice.domain.repository;

import com.carenest.business.userservice.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
