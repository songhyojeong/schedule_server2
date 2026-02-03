package com.schedule.friend;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schedule.friend.dto.TargetEmailRequest;
import com.schedule.friend.dto.UserSummary;
import com.schedule.plan.PlanDTO;
import com.schedule.user.dto.ResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FriendController {

	private final FriendService friendService;



	//친구 검색
	@GetMapping("/searchfriend")
	public ResponseEntity<ResponseDTO<List<UserSummary>>> searchFriend(
			@RequestHeader("Authorization") String token,
			@RequestParam("q") String q){

		List<UserSummary> results = friendService.searchCandidates(token, q);
        return ResponseEntity.ok(ResponseDTO.setSuccessData("검색 성공", results));
	}//searchfriend

	//친구 추가
	@PostMapping("/addfriend")
	public ResponseEntity<ResponseDTO<?>> add(
			@Valid @RequestBody TargetEmailRequest targetEmailRequest,
			@RequestHeader(value="Authorization") String token) {

		friendService.addfriend(token, targetEmailRequest.getTargetEmail());
		return ResponseEntity.ok(ResponseDTO.setSuccess("친구추가 완료"));
	}//add

	//친구 목록
	@GetMapping("/listfriend")
	public ResponseEntity<ResponseDTO<List<UserSummary>>> list(
			@RequestHeader("Authorization") String token) {

		List<UserSummary> friends = friendService.listFriends(token);
        return ResponseEntity.ok(ResponseDTO.setSuccessData("조회 성공", friends));
	}//list

	//친구 삭제
	@DeleteMapping("/deletefriend")
	public ResponseEntity<ResponseDTO<?>> remove(
		    @RequestHeader("Authorization") String token,
		    @RequestBody TargetEmailRequest targetEmailRequest) {

		friendService.removeFriend(token, targetEmailRequest.getTargetEmail());
		return ResponseEntity.ok(ResponseDTO.setSuccess("친구 삭제 완료"));
	}//remove

	//친구 스케줄 불러오기
	@GetMapping("/friend/calendar")
	public ResponseEntity<ResponseDTO<List<PlanDTO>>> friendCalendar(
	        @RequestHeader("Authorization") String token,
	        @RequestParam("friendEmail") String friendEmail,
	        @RequestParam("year") int year,
	        @RequestParam("month") int month
	    ) {

		List<PlanDTO> plans = friendService.getMonthly(token, friendEmail, year, month);
		return ResponseEntity.ok(ResponseDTO.setSuccessData("조회 성공", plans));
	    }//friendCalendar

}
