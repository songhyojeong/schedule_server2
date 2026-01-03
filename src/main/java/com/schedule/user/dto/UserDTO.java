package com.schedule.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

	private String email;
	private String social_id;
	private String pw;
	private String nickname;
	private String token;

}
