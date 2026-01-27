package com.schedule.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="password_reset_code")
@Getter @Setter
public class ForgetPwEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique=true, length=50)
    private String   email;

	@Column(name="code_hash", nullable = false,length=100)
    private String codeHash;

	@Column(name="expires_at", nullable = false)
    private LocalDateTime   expiresAt;

	@Column(nullable = false)
    private int  attempts = 0;

	@Column(name="created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

}
