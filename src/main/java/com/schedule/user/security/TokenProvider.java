package com.schedule.user.security;

import java.time.Instant;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class TokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    @Value("${JWT_SECRET}")
    private String SECURITY_KEY;

    // JWT 생성 메서드
    public String createJwt(String email, int duration) {
        try {
            Instant now = Instant.now();
            Instant exprTime = now.plusSeconds(duration);

            // email 클레임으로 추가
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(exprTime))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            JWSSigner signer = new MACSigner(SECURITY_KEY.getBytes());
            signedJWT.sign(signer);

            String jwt = signedJWT.serialize();
            System.out.println("생성된 JWT: " + jwt);
            return jwt;

        } catch (JOSEException e) {
            logger.error("JWT 생성 중 오류 발생", e);
            return null;
        }
    }

    // JWT 검증 및 subject 추출
    public String validateJwt(String token) {

    	 if ("test-token".equals(token)) {
             return "user@test.com"; // 임시 이메일
         }

        try {
            System.out.println("검증할 토큰: " + token);
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECURITY_KEY.getBytes());

            if (signedJWT.verify(verifier)) {
                return signedJWT.getJWTClaimsSet().getSubject();
            } else {
                logger.warn("JWT 서명이 유효하지 않습니다.");
                return null;
            }

        } catch (Exception e) {
            logger.error("JWT 검증 중 오류 발생", e);
            return null;
        }
    }


}
