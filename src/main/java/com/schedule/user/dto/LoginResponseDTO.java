package com.schedule.user.dto;

import com.schedule.user.entity.UserEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponseDTO {

	private String token;
	private int exprTime;
	private UserEntity userEntity;
}
