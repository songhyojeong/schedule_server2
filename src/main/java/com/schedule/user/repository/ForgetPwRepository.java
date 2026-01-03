package com.schedule.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedule.user.entity.ForgetPwEntity;

public interface ForgetPwRepository extends JpaRepository<ForgetPwEntity, Long> {
	
	Optional<ForgetPwEntity> findByEmail(String email);
    
	void deleteByEmail(String email);

}
