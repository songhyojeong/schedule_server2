package com.schedule.friend;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.friend.dto.TargetEmailRequest;
import com.schedule.friend.dto.UserSummary;

import lombok.RequiredArgsConstructor;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FriendController {

	private final FriendService friendService;



	//친구 검색
	@GetMapping("/searchfriend")
	public ResponseEntity<List<UserSummary>> searchFriend(
			@RequestHeader("Authorization") String token,
			@RequestParam("q") String q){
		
		return ResponseEntity.ok(friendService.searchCandidates(token, q));
	}//searchfriend

	//친구 추가
	@PostMapping("/addfriend")
	public ResponseEntity<?> add(
			@RequestBody TargetEmailRequest targetEmailRequest,
			@RequestHeader(value="Authorization") String token) {
		
		friendService.addfriend(token, targetEmailRequest.getTargetEmail());
		return ResponseEntity.ok("친구추가 완료");
	}//add

	//친구 목록
	@GetMapping("/listfriend")
	public ResponseEntity<List<UserSummary>> list(
			@RequestHeader("Authorization") String token) {

		return ResponseEntity.ok(friendService.listFriends(token));
	}//list

	//친구 삭제
	@DeleteMapping("/deletefriend")
	public ResponseEntity<?> remove(
		    @RequestHeader("Authorization") String token,
		    @RequestBody TargetEmailRequest targetEmailRequest) {

		friendService.removeFriend(token, targetEmailRequest.getTargetEmail());
		return ResponseEntity.ok("친구 삭제 완료");
	}//remove
	
	//친구 스케줄 불러오기
	@GetMapping("/friend/calendar")
	public ResponseEntity<?> friendCalendar(
	        @RequestHeader("Authorization") String token,
	        @RequestParam("friendEmail") String friendEmail,
	        @RequestParam("year") int year,
	        @RequestParam("month") int month
	    ) {
	        try {
	            return ResponseEntity.ok(friendService.getMonthly(token, friendEmail, year, month));
	        } catch (ResponseStatusException e) {
	            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("조회 실패: " + e.getMessage());
	        }
	    }//friendCalendar
	
}
