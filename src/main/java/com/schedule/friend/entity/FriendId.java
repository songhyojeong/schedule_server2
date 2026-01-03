package com.schedule.friend.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendId implements Serializable {

	@Column(name = "email", length = 50, nullable = false)
	private String email;

	 @Column(name = "f_email", length = 50, nullable = false)
    private String fEmail;
}
