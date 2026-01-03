package com.schedule.user.entity;


import com.schedule.user.dto.UserDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Data
@Getter @Setter
@Table( name = "user",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "email"),
				@UniqueConstraint(columnNames = "nickname")
		})

public class UserEntity {

	@Id
	private String email;

	@Column(length=50)
	private String pw;

	@Column(name = "nickname" ,length=50, unique = true)
	private String nickname;

	@Column(length=50)
	private String token;

	public UserEntity(UserDTO userDto) {
		this.email=userDto.getEmail();
		this.pw=userDto.getPw();
		this.nickname=userDto.getNickname();
		this.token=userDto.getToken();
	}

}
