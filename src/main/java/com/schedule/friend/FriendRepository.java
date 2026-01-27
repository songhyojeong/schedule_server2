package com.schedule.friend;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.schedule.friend.entity.FriendEntity;
import com.schedule.friend.entity.FriendId;



@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, FriendId> {

  // 존재 여부
  @Query("""
      select case when count(f) > 0 then true else false end
      from FriendEntity f
      where f.friendId.email = :email
        and f.friendId.fEmail = :fEmail
  """)
  boolean existsPair(@Param("email") String email, @Param("fEmail") String fEmail);

  // 내 친구 이메일 목록 (네이티브: 가장 확실)
  @Query(value =
      "SELECT CASE WHEN email = :me THEN f_email ELSE email END AS friend_email " +
      "FROM friends " +
      "WHERE email = :me OR f_email = :me",
      nativeQuery = true)
  List<String> findFriendEmailsOf(@Param("me") String me);

  // 친구 존재 여부 확인
  @Query("""
		  select (count(f) > 0) from FriendEntity f
		  where (f.friendId.email = :a and f.friendId.fEmail = :b)
		     or (f.friendId.email = :b and f.friendId.fEmail = :a)
		""")
		boolean existsAnyDirection(@Param("a") String a, @Param("b") String b);

  // 친구 삭제 (순서 상관 없이 삭제됨)
  @Modifying
  @Transactional
  @Query("""
      delete from FriendEntity f
      where (f.friendId.email = :a and f.friendId.fEmail = :b)
         or (f.friendId.email = :b and f.friendId.fEmail = :a)
  """)
  void deletePair(@Param("a") String a, @Param("b") String b);


}
