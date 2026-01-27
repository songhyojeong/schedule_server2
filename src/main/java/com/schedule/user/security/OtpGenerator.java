package com.schedule.user.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class OtpGenerator {
	private final SecureRandom random = new SecureRandom();

	public String sixDigits() {
		return String.format("%06d", random.nextInt(1_000_000));
	}
}
