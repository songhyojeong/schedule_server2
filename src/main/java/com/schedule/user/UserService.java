package com.schedule.user;

import java.time.LocalDateTime;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.schedule.user.dto.LoginDTO;
import com.schedule.user.dto.UserDTO;
import com.schedule.user.entity.ForgetPwEntity;
import com.schedule.user.entity.UserEntity;
import com.schedule.user.repository.ForgetPwRepository;
import com.schedule.user.repository.UserRepository;
import com.schedule.user.security.OtpGenerator;
import com.schedule.user.security.TokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final ForgetPwRepository forgetPwRepo;
    private final OtpGenerator optGenerator;
    private final JavaMailSender mailSender;

    private static final int EXPIRE_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int TOKEN_EXPIRY_SECONDS = 3600;


    // 이메일 중복 체크
    public boolean isEmailCheck(String email) {
        return !userRepository.existsByEmail(email.trim().toLowerCase());
    }

    // 닉네임 중복 체크
    public boolean isNicknameCheck(String nickname) {
        return !userRepository.existsByNickname(nickname.trim());
    }


    // 회원가입
    public void signup(UserDTO userDto) {

        String email = userDto.getEmail().trim().toLowerCase();
        String pw = userDto.getPw();
        String nickname = userDto.getNickname().trim();
        
        log.info("회원가입 시도:email={}",email);

        if (userRepository.existsById(email)) {
        	log.warn("회원가입 실패 - 중복된 이메일: {}",email);
            throw new IllegalStateException("중복된 이메일");
        }

        if (userRepository.existsByNickname(nickname)) {
        	log.warn("회원가입 실패 - 중복된 닉네임: {}",nickname);
        	throw new IllegalStateException("중복된 닉네임");
        }

        // 비밀번호 암호화
        userDto.setPw(passwordEncoder.encode(pw));

       UserEntity user = new UserEntity(userDto);
       userRepository.save(user);
       
       log.info("회원가입 성공:email={}",email);
    }


    //로그인
    @Transactional
    public String login(LoginDTO loginDto) {

        String email = loginDto.getEmail();
        String pw = loginDto.getPw();
        
        log.info("로그인 시도:email={}",email);

        UserEntity userEntity = userRepository.findByEmail(email)
        		.orElseThrow(() -> {
        			log.warn("로그인 실패 - 존재하지 않는 이메일: {}",email);	
        			return new IllegalArgumentException("이메일 또는 비밀번호 불일치");
        		});

        if(!passwordEncoder.matches(pw, userEntity.getPw())) {
        	log.warn("로그인 실패 - 비밀번호 불일치: {}",email);
        	throw new IllegalArgumentException("이메일 또는 비밀번호 불일치");
        	}

        log.info("로그인 성공:email={}",email);

        return tokenProvider.createJwt(userEntity.getEmail(),TOKEN_EXPIRY_SECONDS);
    }


    // 비밀번호 재설정 - 메일 보내기
    public void sendResetCode(String to, String code) {

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("비밀번호 재설정 인증코드");
        msg.setText("""
                아래 6자리 코드를 10분 이내에 입력하세요.
                인증코드: %s
                """.formatted(code));

        mailSender.send(msg);
    }


    // 비밀번호 재설정 요청
    @Transactional
    public void requestCode(String email) {

        if (userRepository.existsByEmail(email)) {
            String code = optGenerator.sixDigits();
            String hash = passwordEncoder.encode(code);

            // 기존 요청 삭제
            forgetPwRepo.findByEmail(email).ifPresent(forgetPwRepo::delete);

            ForgetPwEntity fge = new ForgetPwEntity();
            fge.setEmail(email);
            fge.setCodeHash(hash);
            fge.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES));
            fge.setAttempts(0);

            forgetPwRepo.save(fge);
            sendResetCode(email, code);
            log.info("인증코드 발송 완료: email={}", email);
        } else {
            log.info("비밀번호 재설정 요청 - 존재하지 않는 이메일 (보안상 동일 응답): email={}", email);
        }
        
    }
    //공통 코드 검증
    private void validateCode(ForgetPwEntity fge, String code) {
    	if (fge.getExpiresAt().isBefore(LocalDateTime.now())) {
            forgetPwRepo.delete(fge);
            throw new IllegalStateException("코드가 만료되었습니다.");
        }

        if (fge.getAttempts() >= MAX_ATTEMPTS) {
            forgetPwRepo.delete(fge);
            throw new IllegalStateException("시도 횟수를 초과했습니다.");
        }

        fge.setAttempts(fge.getAttempts() + 1);

        if (!passwordEncoder.matches(code, fge.getCodeHash())) {
            throw new IllegalStateException("인증코드가 일치하지 않습니다.");
        }
    }//validateCode

    // 코드 검증
    @Transactional
    public void verify(String email, String code) {

    	 ForgetPwEntity fge = forgetPwRepo.findByEmail(email)
    	            .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 요청입니다."));

    	    validateCode(fge, code);
    }


    // 비밀번호 재설정
    @Transactional
    public void reset(String email, String code, String pw) {
    	
    	log.info("비밀번호 재설정 시도: email={}", email);


    	ForgetPwEntity fge = forgetPwRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 요청입니다."));

        validateCode(fge, code);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setPw(passwordEncoder.encode(pw));
        userRepository.save(user);

        forgetPwRepo.delete(fge);
        
        log.info("비밀번호 재설정 완료: email={}", email);
        }

}
