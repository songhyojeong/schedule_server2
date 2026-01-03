package com.schedule.user.dto;

import jakarta.validation.constraints.NotBlank;  // 여기서 jakarta.validation을 사용
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class LoginDTO {

	@NotBlank
	private String email;
	@NotBlank
	private String pw;

}
