package com.schedule.friend.entity;

import com.schedule.friend.dto.FriendDTO;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name="friends",
		uniqueConstraints = @UniqueConstraint(
        name = "uq_friend_pair",
        columnNames = {"email", "f_email"})
		)

public class FriendEntity {

	@EmbeddedId
    private FriendId friendId;


	public FriendEntity(FriendDTO friendDto) {
		this.friendId = new FriendId(friendDto.getEmail(),friendDto.getF_email());
	}

	// 편의 생성자
    public static FriendEntity of(String email, String fEmail) {
        return FriendEntity.builder()
                .friendId(new FriendId(email, fEmail))
                .build();
    }

    // 기존 코드 호환용 브리지 메서드
    // 바깥에서는 getF_email()/setF_email()을 계속 쓸 수 있게 유지
    public String getEmail() { return friendId != null ? friendId.getEmail() : null; }

    public String getF_email() { return friendId != null ? friendId.getFEmail() : null; }

    public void setEmail(String email) {
        if (friendId == null) {
			friendId = new FriendId();
		}
        friendId.setEmail(email);
    }

    public void setF_email(String f_email) {
        if (friendId == null) {
			friendId = new FriendId();
		}
        friendId.setFEmail(f_email);
    }
}
