package com.schedule.user;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.user.dto.LoginDTO;
import com.schedule.user.dto.ResponseDTO;
import com.schedule.user.dto.UserDTO;
import com.schedule.user.entity.ForgetPwEntity;
import com.schedule.user.entity.UserEntity;
import com.schedule.user.repository.ForgetPwRepository;
import com.schedule.user.repository.UserRepository;
import com.schedule.user.security.OtpGenerator;
import com.schedule.user.security.TokenProvider;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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


    // 이메일 중복 체크
    public boolean isEmailCheck(String email) {
        return !userRepository.existsByEmail(email.trim().toLowerCase());
    }

    // 닉네임 중복 체크
    public boolean isNicknameCheck(String nickname) {
        return !userRepository.existsByNickname(nickname.trim());
    }


    // 회원가입
    public ResponseDTO<?> signup(UserDTO userDto) {

        String email = userDto.getEmail().trim().toLowerCase();
        String pw = userDto.getPw();
        String nickname = userDto.getNickname().trim();

        if (userRepository.existsById(email)) {
            return ResponseDTO.setFailed("중복된 이메일");
        }

        if (userRepository.existsByNickname(nickname)) {
            return ResponseDTO.setFailed("중복된 닉네임");
        }

        // 비밀번호 암호화
        userDto.setPw(passwordEncoder.encode(pw));

        try {
            UserEntity user = new UserEntity(userDto);
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseDTO.setFailed("데이터베이스 처리 중 오류발생");
        }

        return ResponseDTO.setSuccess("회원가입 성공");
    }


    // 로그인
    public ResponseDTO<?> login(LoginDTO loginDto) {

        String email = loginDto.getEmail();
        String pw = loginDto.getPw();

        UserEntity userEntity = userRepository.findByEmail(email).orElse(null);

        if (userEntity == null || !passwordEncoder.matches(pw, userEntity.getPw())) {
            return ResponseDTO.setFailed("이메일 또는 비밀번호 불일치");
        }

        int exprTime = 3600;
        String token = tokenProvider.createJwt(userEntity.getEmail(), exprTime);

        return ResponseDTO.setSuccessData("로그인 성공", token);
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
        }
    }
    //공통 코드 검증
    private void validateCode(ForgetPwEntity fge, String code) {
    	if (fge.getExpiresAt().isBefore(LocalDateTime.now())) {
            forgetPwRepo.delete(fge);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "코드 만료");
        }

        if (fge.getAttempts() >= MAX_ATTEMPTS) {
            forgetPwRepo.delete(fge);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "시도 횟수 초과");
        }

        fge.setAttempts(fge.getAttempts() + 1);

        if (!passwordEncoder.matches(code, fge.getCodeHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "코드가 일치하지 않음");
        }
    }//validateCode

    // 코드 검증
    @Transactional
    public void verify(String email, String code) {

    	 ForgetPwEntity fge = forgetPwRepo.findByEmail(email)
    	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "코드 유효하지 않음"));

    	    validateCode(fge, code);
    }


    // 비밀번호 재설정
    @Transactional
    public void reset(String email, String code, String pw) {

    	ForgetPwEntity fge = forgetPwRepo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "코드 유효하지 않음"));

        validateCode(fge, code);

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없음"));

        user.setPw(passwordEncoder.encode(pw));
        userRepository.save(user);

        forgetPwRepo.delete(fge);    
        }

}
