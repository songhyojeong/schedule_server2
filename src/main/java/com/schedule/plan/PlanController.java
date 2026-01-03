 package com.schedule.plan;

 import java.time.LocalDate;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlanController {

	private final PlanService planService;
	private final PlanRepository planRepository;


	//스케줄 저장
	@PostMapping("/saveschedule")
	public ResponseEntity<?> save(@RequestBody PlanDTO planDto,
								  @RequestHeader(value = "Authorization") String token) {

		try {
            PlanEntity savedPlan = planService.savePlan(planDto, token);
            return ResponseEntity.ok(savedPlan);
        } catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
		} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("스케줄 저장 실패: " + e.getMessage());
        }
	} //saveSchedule
	
	//특정 날짜의 일정 제목 목록 조회
	@GetMapping("/title")
	public ResponseEntity<?> titleOfDay(@RequestParam("date")
										@DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
										@RequestHeader("Authorization") String token) {


		try {
			return ResponseEntity.ok(planService.getTitlesOfDay(date, token));
		}
		catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
		}
	} //titleOfDay
	
	//날짜 범위 일정 조회
	@GetMapping("/range")
	public ResponseEntity<?> planOfRange(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
										 @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
										 @RequestHeader("Authorization") String token) {
	
		try {
			return ResponseEntity.ok(planService.getPlansOfRange(start, end, token));
		}catch(RuntimeException e){
			 return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
		}
	
	} //range
	
	//일정 상세 조회
	@GetMapping("/{s_id}")
	public ResponseEntity<?> detailPlan(@PathVariable("s_id") int s_id){
		
		Optional<PlanEntity> detail = planRepository.findById(s_id);
		if(detail.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("s_id를 찾지 못했습니다.");
		}
		
		PlanEntity planEntity = detail.get();
		return ResponseEntity.ok(planEntity);
	}//detailPlan
	
	//일정 수정
	@PatchMapping("/updateplan/{s_id}")
	public ResponseEntity<?> updatePlan(@RequestHeader(value = "Authorization") String token,
										  @PathVariable("s_id") int s_id,
										  @RequestBody PlanDTO planDto){
		
		try {
			var updated = planService.updatePlan(token,s_id,planDto);
			return ResponseEntity.ok(updated); 
        } catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
		} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("스케줄 업데이트 실패: " + e.getMessage());
        }
	}//updateDetail
	
	//일정 삭제
	@DeleteMapping("/deleteplan/{s_id}")
	public ResponseEntity<?> deletePlan(@RequestHeader(value = "Authorization") String token,
										@PathVariable("s_id") int s_id) {
		
		try {
			planService.deletePlan(token,s_id);
			return ResponseEntity.ok("삭제 완료");
		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰이 유효하지 않습니다.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("스케줄 삭제 실패"+e.getMessage());
		}
	}//deletePlan
	

}
