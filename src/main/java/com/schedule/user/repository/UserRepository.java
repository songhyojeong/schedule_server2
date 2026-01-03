package com.schedule.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedule.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String>{

	//로그인 용(email + pw 모두 일치하는 유저 존재 여부)
	public boolean existsByEmailAndPw(String email,String pw);


	//로그인 or 회원 조회 용(email로 유저 찾기)
	Optional<UserEntity> findByEmail(String email);

	//이메일만 중복 확인
	boolean existsByEmail(String email);

	//닉네임 중 확인
	boolean existsByNickname(String nickname);

	//친구 검색
    List<UserEntity> findTop10ByEmailContainingIgnoreCase(String q);

}
