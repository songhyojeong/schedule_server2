package com.schedule.plan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.schedule.common.AuthUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {
	private final PlanRepository planRepository;
	private final AuthUtil authUtil;


	//새로운 스케줄 저장
	public PlanDTO savePlan(PlanDTO planDto,String token){
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("일정 등록 시도: email={}, title={}", email, planDto.getTitle());
		
	   planDto.setEmail(email);
	   

	   if(planDto.getStart_date() == null) {
		   planDto.setStart_date(LocalDateTime.now());
	   }
	   if(planDto.getEnd_date() == null) {
		   planDto.setEnd_date(LocalDateTime.now());
	   }
	   if(planDto.getStart_time() == null) {
		   planDto.setStart_time(LocalTime.now());
	   }
	   if(planDto.getEnd_time() == null) {
		   planDto.setEnd_time(LocalTime.now());
	   }

	   PlanEntity saved = planRepository.save(new PlanEntity(planDto));
	   log.info("일정 등록 완료: s_id={}, email={}", saved.getS_id(), email);
	   
	   return new PlanDTO(saved);
	}//saveplan

	//특정 날짜의 일정 제목 목록 조회(하루의 시작/끝 시간을 계산해서 쿼리 범위 설정)
	public List<String> getTitlesOfDay(LocalDate day , String token){

		String email = authUtil.extractEmail(token);
		log.info("특정 날짜 일정 조회: email={}, date={}", email, day);
		
		LocalDateTime dayStart = day.atStartOfDay();
		LocalDateTime dayEnd = day.atTime(LocalTime.MAX);

		List<String> titles = planRepository.findByPlanOfDay(email, dayStart, dayEnd)
	            .stream()
	            .map(PlanEntity::getTitle)
	            .toList();
	    
	    log.info("특정 날짜 일정 조회 완료: email={}, 조회 건수={}", email, titles.size());
	    return titles;
	}//getTitlesOfDay

	//날짜 범위 일정 조회
	public List<PlanDTO> getPlansOfRange(LocalDate start,LocalDate end,String token){

		String email = authUtil.extractEmail(token);
		log.info("날짜 범위 일정 조회: email={}, start={}, end={}", email, start, end);
		
		LocalDateTime rangeStart = start.atStartOfDay();
		LocalDateTime rangeEnd = end.atTime(23, 59, 59);

		 List<PlanDTO> plans = planRepository.findPlansOverlapping(email, rangeStart, rangeEnd)
		            .stream()
		            .map(PlanDTO::new)
		            .toList();
		    
		    log.info("날짜 범위 일정 조회 완료: email={}, 조회 건수={}", email, plans.size());
		    return plans;
	}//getPlansOfRange
	
	//일정 상세 조회
	public PlanDTO getPlanDetail(int s_id, String token) {
		
		String me = authUtil.extractEmail(token);
		log.info("일정 상세 조회: s_id={}, email={}", s_id, me);
		
		PlanEntity plan = planRepository.findById(s_id)
				.orElseThrow(() -> {
	                log.warn("일정 조회 실패 - 존재하지 않는 일정: s_id={}", s_id);
	                return new IllegalArgumentException("존재하지 않는 일정입니다");
	            });
		
		if(!plan.getEmail().equalsIgnoreCase(me)) {
			log.warn("일정 조회 실패 - 권한 없음: s_id={}, 요청자={}", s_id, me);
			throw new IllegalStateException("본인의 일정만 조회할 수 있습니다.");
		}
		
		return new PlanDTO(plan);
	}//getPlanDetail

	//일정 수정
	@Transactional
	public PlanDTO updatePlan(String token,int s_id,PlanDTO planDto) {

		String me = authUtil.extractEmail(token);
		log.info("일정 수정 시도: s_id={}, email={}", s_id, me);

		PlanEntity planEntity = planRepository.findById(s_id)
				.orElseThrow(() -> {
					log.warn("일정 수정 실패 - 존재하지 않는 일정: s_id={}", s_id);
					return new IllegalArgumentException("존재하지 않는 일정입니다.");
					});

				if(!planEntity.getEmail().equalsIgnoreCase(me)) {
					log.warn("일정 수정 실패 - 권한 없음: s_id={}, 요청자={}, 소유자={}", s_id, me, planEntity.getEmail());
					throw new IllegalStateException("본인의 일정만 수정할 수 있습니다");
				}

				if (planDto.getTitle() != null) {
					planEntity.setTitle(planDto.getTitle());
				}
			    if (planDto.getContent() != null) {
					planEntity.setContent(planDto.getContent());
				}
			    if (planDto.getStart_date() != null) {
					planEntity.setStart_date(planDto.getStart_date());
				}
			    if (planDto.getEnd_date() != null) {
					planEntity.setEnd_date(planDto.getEnd_date());
				}
			    if (planDto.getStart_time() != null) {
					planEntity.setStart_time(planDto.getStart_time());
				}
			    if (planDto.getEnd_time() != null) {
					planEntity.setEnd_time(planDto.getEnd_time());
				}
		
			    log.info("일정 수정 완료: s_id={}", s_id);
			    return new PlanDTO(planEntity);

	}//updatedetail

	//일정 삭제
	@Transactional
	public void deletePlan(String token,int s_id) {

		String me = authUtil.extractEmail(token);
		log.info("일정 삭제 시도: s_id={}, email={}", s_id, me);

		PlanEntity planEntity = planRepository.findById(s_id)
				.orElseThrow(() -> {
	                log.warn("일정 삭제 실패 - 존재하지 않는 일정: s_id={}", s_id);
	                return new IllegalArgumentException("존재하지 않는 일정입니다");
	            });

		if (!planEntity.getEmail().equalsIgnoreCase(me)) {
			log.warn("일정 삭제 실패 - 권한 없음: s_id={}, 요청자={}, 소유자={}", s_id, me, planEntity.getEmail());
	        throw new IllegalStateException("본인의 일정만 삭제할 수 있습니다.");
	    }

		planRepository.delete(planEntity);
		log.info("일정 삭제 완료: s_id={}", s_id);

	}//deleteplan


}
