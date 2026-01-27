package com.schedule.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schedule.user.dto.ForgetPwDTO.ForgotRequest;
import com.schedule.user.dto.ForgetPwDTO.ResetRequest;
import com.schedule.user.dto.ForgetPwDTO.VerifyRequest;
import com.schedule.user.dto.LoginDTO;
import com.schedule.user.dto.ResponseDTO;
import com.schedule.user.dto.UserDTO;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

	private final UserService userService;

	//회원가입
	@PostMapping("/signup")
	public ResponseEntity<ResponseDTO<?>> signup(@RequestBody UserDTO requestBody) {
	    userService.signup(requestBody);
	    return ResponseEntity.ok(ResponseDTO.setSuccess("회원가입 성공"));

	}//signup

	//이메일 중복확인
	@GetMapping("/checkEmail")
	public ResponseEntity<ResponseDTO<?>> checkEmail(@RequestParam("value") String email) {

	    if (email == null || email.isBlank()) {
	        return ResponseEntity.badRequest().body(ResponseDTO.setFailed("이메일이 비어있습니다."));
	    }
	    boolean available = userService.isEmailCheck(email);

	    if (available) {
	    	return ResponseEntity.ok(ResponseDTO.setSuccess("사용 가능한 이메일입니다."));
	    } else {
	    	return ResponseEntity.badRequest().body(ResponseDTO.setFailed("이미 사용중인 이메일입니다."));
	    }

	} //checkEmail

	//닉네임 중복확인
	@GetMapping("/checkNickname")
	public ResponseEntity<ResponseDTO<?>> checkNickname(@RequestParam("value") String nickname) {

	    if (nickname == null || nickname.isBlank()) {
	        return ResponseEntity.badRequest().body(ResponseDTO.setFailed("닉네임이 비어있습니다."));
	    }
	    boolean available = userService.isNicknameCheck(nickname);

	    if(available) {
	    	return ResponseEntity.ok(ResponseDTO.setSuccess("사용 가능한 닉네임입니다."));
	    } else {
	    	return ResponseEntity.badRequest().body(ResponseDTO.setFailed("이미 사용중인 닉네임입니다."));
	    }

	} //checkNickname

	//로그인
		@PostMapping("/login")
		public ResponseEntity<ResponseDTO<String>> login(@RequestBody LoginDTO requestBody){
			String token = userService.login(requestBody);
			return ResponseEntity.ok(ResponseDTO.setSuccessData("로그인 성공", token));

		}//login

		//비밀번호 재설정 요청
	    @PostMapping("/forgot")
	    public ResponseEntity<ResponseDTO<?>> forgot(@RequestBody ForgotRequest req) {
	        userService.requestCode(req.email());
	        return ResponseEntity.ok(ResponseDTO.setSuccess("해당 이메일로 인증코드를 전송했습니다."));
	    }//forget

	    //코드 검증
	    @PostMapping("/verify")
	    public ResponseEntity<ResponseDTO<?>> verify(@RequestBody VerifyRequest req) {
	        userService.verify(req.email(), req.code());
	        return ResponseEntity.ok(ResponseDTO.setSuccess("인증에 성공했습니다."));
	    }//verify

	    // 3) 재설정(코드 검증 + 비번 변경)
	    @PostMapping("/reset")
	    public ResponseEntity<ResponseDTO<?>> reset(@RequestBody ResetRequest req) {
	        userService.reset(req.email(), req.code(), req.pw());
	        return ResponseEntity.ok(ResponseDTO.setSuccess("비밀번호가 변경되었습니다."));
	    }


}
