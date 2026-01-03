package com.schedule.plan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.user.security.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanService {
	private final PlanRepository planRepository;
	private final TokenProvider tokenProvider;
	
	//토큰 추출
	private String emailFromAuth(String token) {
		
		if (token == null || token.isBlank()) {
	        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 없음");
	    }
		
		String jwt = token.startsWith("Bearer ") ? token.substring(7) : token; // validateJwt에서 email 추출
	    String email = tokenProvider.validateJwt(jwt);

	    if (email == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰");
		}
        return email;
	}//emailFromAuth
	
	//새로운 스케줄 저장
	public PlanEntity savePlan(PlanDTO planDto,String token){

	   planDto.setEmail(emailFromAuth(token));

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

	   return planRepository.save(new PlanEntity(planDto));
	}//saveplan
	
	//특정 날짜의 일정 제목 목록 조회(하루의 시작/끝 시간을 계산해서 쿼리 범위 설정)
	public List<String> getTitlesOfDay(LocalDate day , String token){
		
		String email = emailFromAuth(token);
		LocalDateTime dayStart = day.atStartOfDay();
		LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
		return planRepository.findByPlanOfDay(email, dayStart, dayEnd)
				.stream().map(PlanEntity::getTitle).toList();
	}//getTitlesOfDay 
	
	//날짜 범위 일정 조회
	public List<PlanEntity> getPlansOfRange(LocalDate start,LocalDate end,String token){
		
		String email = emailFromAuth(token);
		LocalDateTime rangeStart = start.atStartOfDay();
		LocalDateTime rangeEnd = end.atTime(23, 59, 59);
		return planRepository.findPlansOverlapping(email, rangeStart, rangeEnd);
	}//getPlansOfRange 
	
	//일정 수정
	@Transactional
	public PlanDTO updatePlan(String token,int s_id,PlanDTO planDto) {
		
		String me = emailFromAuth(token);
		
		PlanEntity planEntity = planRepository.findById(s_id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 s_id"));
		
				if(!planEntity.getEmail().equalsIgnoreCase(me)) {
					throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인만 수정 가능합니다.");
				}
		
				if (planDto.getTitle() != null) planEntity.setTitle(planDto.getTitle());
			    if (planDto.getContent() != null) planEntity.setContent(planDto.getContent());
			    if (planDto.getStart_date() != null) planEntity.setStart_date(planDto.getStart_date());
			    if (planDto.getEnd_date() != null) planEntity.setEnd_date(planDto.getEnd_date());
			    if (planDto.getStart_time() != null) planEntity.setStart_time(planDto.getStart_time());
			    if (planDto.getEnd_time() != null) planEntity.setEnd_time(planDto.getEnd_time());

		   return new PlanDTO(planEntity);
		
	}//updatedetail 
	
	//일정 삭제
	@Transactional
	public void deletePlan(String token,int s_id) {
		
		String me = emailFromAuth(token);
		
		PlanEntity planEntity = planRepository.findById(s_id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"존재하지 않는 s_id"));
		
		if (!planEntity.getEmail().equalsIgnoreCase(me)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 스케줄만 삭제할 수 있습니다.");
	    }
		
		planRepository.delete(planEntity);
		
	}//deleteplan 
	

}
