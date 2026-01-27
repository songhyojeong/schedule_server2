 package com.schedule.plan;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
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

import com.schedule.user.dto.ResponseDTO;

import lombok.RequiredArgsConstructor;




@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PlanController {

	private final PlanService planService;
	private final PlanRepository planRepository;


	//스케줄 저장
	@PostMapping("/saveschedule")
	public ResponseEntity<ResponseDTO<PlanDTO>> save(@RequestBody PlanDTO planDto,
								  @RequestHeader(value = "Authorization") String token) {

		PlanDTO savedPlan = planService.savePlan(planDto, token);
	    return ResponseEntity.ok(ResponseDTO.setSuccessData("일정 등록 성공", savedPlan));

	} //saveSchedule

	//특정 날짜의 일정 제목 목록 조회
	@GetMapping("/title")
	public ResponseEntity<ResponseDTO<List<String>>> titleOfDay(@RequestParam("date")
										@DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate date,
										@RequestHeader("Authorization") String token) {

        List<String> titles = planService.getTitlesOfDay(date, token);
        return ResponseEntity.ok(ResponseDTO.setSuccessData("조회 성공", titles));

	} //titleOfDay

	//날짜 범위 일정 조회
	@GetMapping("/range")
	public ResponseEntity<ResponseDTO<List<PlanDTO>>> planOfRange(@RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
										 @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
										 @RequestHeader("Authorization") String token) {

		 List<PlanDTO> plans = planService.getPlansOfRange(start, end, token);
	     return ResponseEntity.ok(ResponseDTO.setSuccessData("조회 성공", plans));

	} //range

	//일정 상세 조회
	@GetMapping("/{s_id}")
	public ResponseEntity<ResponseDTO<PlanDTO>> detailPlan(@PathVariable("s_id") int s_id){

		Optional<PlanEntity> detail = planRepository.findById(s_id);

		if(detail.isPresent()) {
			PlanDTO planDto = new PlanDTO(detail.get());
			return ResponseEntity.ok(ResponseDTO.setSuccessData("조회 성공", planDto));
		} else {
			return ResponseEntity.status(404)
					.body(ResponseDTO.setFailed("일정을 찾을 수 없습니다."));
		}

	}//detailPlan

	//일정 수정
	@PatchMapping("/updateplan/{s_id}")
	public ResponseEntity<ResponseDTO<PlanDTO>> updatePlan(@RequestHeader(value = "Authorization") String token,
										  @PathVariable("s_id") int s_id,
										  @RequestBody PlanDTO planDto){

		PlanDTO updated = planService.updatePlan(token, s_id, planDto);
        return ResponseEntity.ok(ResponseDTO.setSuccessData("수정 성공", updated));
	}//updateDetail

	//일정 삭제
	@DeleteMapping("/deleteplan/{s_id}")
	public ResponseEntity<ResponseDTO<?>> deletePlan(@RequestHeader(value = "Authorization") String token,
										@PathVariable("s_id") int s_id) {

		planService.deletePlan(token, s_id);
		return ResponseEntity.ok(ResponseDTO.setSuccess("삭제 성공"));
	}//deletePlan


}
