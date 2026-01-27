package com.schedule.user.dto;

public class ForgetPwDTO {

	public record ForgotRequest(String email) {}
	public record VerifyRequest(String email, String code) {}
	public record ResetRequest(String email, String code,String pw) {}
}
