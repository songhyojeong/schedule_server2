package com.schedule.friend;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schedule.common.AuthUtil;
import com.schedule.friend.dto.UserSummary;
import com.schedule.friend.entity.FriendEntity;
import com.schedule.plan.PlanDTO;
import com.schedule.plan.PlanRepository;
import com.schedule.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

	private final FriendRepository friendRepository;
	private final UserRepository userRepository;
	private final AuthUtil authUtil;
	private final PlanRepository planRepository;


	//이메일 검색(본인 제외 이미 친구인 사람 제외)
	@Transactional(readOnly = true)
	public List<UserSummary> searchCandidates (String token, String q){
		String me = authUtil.extractEmail(token);
		log.info("친구 검색: 요청자={}, 검색어={}", me, q);


		var myFriends = friendRepository.findFriendEmailsOf(me); //내 친구 이메일 목록
		var users = userRepository.findTop10ByEmailContainingIgnoreCase(q);

		return users.stream()
				.filter(u -> !u.getEmail().equalsIgnoreCase(me))
				.filter(u -> myFriends.stream().noneMatch(f -> f.equalsIgnoreCase(u.getEmail())))
				.map(u -> new UserSummary(u.getEmail(),u.getNickname()))
				.toList();

	}//searchCandidates

	//친구 추가(본인 제외)
	@Transactional
	public void addfriend(String token,String targetEmail) {
		String me = authUtil.extractEmail(token);
		log.info("친구 추가 시도: 요청자={}, 대상={}", me, targetEmail);

		if(me.equalsIgnoreCase(targetEmail)) {
			log.warn("친구 추가 실패 - 본인 추가 시도: email={}", me);
			throw new IllegalArgumentException("본인은 친구로 추가할 수 없습니다.");
		}

		if(!userRepository.existsByEmail(targetEmail)) {
			log.warn("친구 추가 실패 - 존재하지 않는 사용자: 대상={}", targetEmail);
			throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
		}

		// 두 이메일을 정렬하여 friendId 일관성 유지
        String lo = (Comparator.<String>naturalOrder()
                .compare(me.toLowerCase(), targetEmail.toLowerCase()) <= 0) ? me : targetEmail;
        String hi = lo.equals(me) ? targetEmail : me;

        //친구 검증
        if (friendRepository.existsPair(lo, hi)) {
        	log.warn("친구 추가 실패 - 이미 친구: 요청자={}, 대상={}", me, targetEmail);
            throw new IllegalStateException("이미 친구입니다.");
        }

        // 저장 (정렬은 위에서 보정, 트리거 있으면 역순으로 넣어도 보정됨)
        friendRepository.save(FriendEntity.of(lo, hi));
        log.info("친구 추가 완료: 요청자={}, 대상={}", me, targetEmail);

	} //addfriend

	//친구 목록
	@Transactional(readOnly = true)
	public List<UserSummary> listFriends(String token) {
		String me = authUtil.extractEmail(token);
		log.info("친구 목록 조회: email={}", me);

		var emails = friendRepository.findFriendEmailsOf(me);
		var users = userRepository.findAllById(emails);

		return users.stream()
				.map(u -> new UserSummary(u.getEmail(), u.getNickname()))
				.toList();
	} //listFriends

	//친구 삭제
	@Transactional
	public void removeFriend(String token, String targetEmail) {
		String me = authUtil.extractEmail(token);
		log.info("친구 삭제 시도: 요청자={}, 대상={}", me, targetEmail);

		if(me.equalsIgnoreCase(targetEmail)) {
			log.info("친구 삭제 - 본인 삭제 요청 무시: email={}", me);
			return;
		}

		friendRepository.deletePair(me, targetEmail);
		log.info("친구 삭제 완료: 요청자={}, 대상={}", me, targetEmail);
	}//removeFriend


	//두 사용자가 친구인지 판단
	private boolean isFriend(String a,String b) {
		return friendRepository.existsAnyDirection(
				a.trim().toLowerCase(),
				b.trim().toLowerCase()
				);
	}//isFriend

	//친구 월별 일정 조회
	@Transactional(readOnly = true)
	public List<PlanDTO> getMonthly(String token,String friendEmail,int year,int month){
		String me =authUtil.extractEmail(token);
		log.info("친구 일정 조회: 요청자={}, 친구={}, 년월={}-{}", me, friendEmail, year, month);

		//본인 아니면 친구 여부 확인(비친구 403)
		if (!me.equalsIgnoreCase(friendEmail) && !isFriend(me, friendEmail)) {
			throw new IllegalStateException("친구만 열람할 수 있습니다");
		}

		LocalDate first = LocalDate.of(year,month,1);
		LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
		LocalDateTime rangeStart = first.atStartOfDay();
		LocalDateTime rangeEnd = last.atTime(23,59,59);

		return planRepository.findPlansOverlapping(friendEmail, rangeStart,rangeEnd)
				.stream()
				.map(PlanDTO::new)
				.toList();
	}//getMonthly
}
