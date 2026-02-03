package com.schedule.common;

import org.springframework.stereotype.Component;

import com.schedule.user.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUtil {
	
	private final TokenProvider tokenProvider;
	
	public String extractEmail(String authHeader) {
		if (authHeader == null || authHeader.isBlank()) {
		    throw new IllegalArgumentException("토큰이 없습니다.");
		}
		
		String token = authHeader.startsWith("Bearer ")
				? authHeader.substring(7)
						:authHeader;
		
		String email = tokenProvider.validateJwt(token);
		
		if (email == null) {
			throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
		}
		
		return email;
	}

}
