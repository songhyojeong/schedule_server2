package com.schedule.plan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.schedule.common.AuthUtil;
import com.schedule.plan.PlanDTO;
import com.schedule.plan.PlanEntity;
import com.schedule.plan.PlanRepository;


@ExtendWith(MockitoExtension.class)
public class PlanServiceTest {
	@Mock
    private PlanRepository planRepository;
    
    @Mock
    private AuthUtil authUtil;
    
    @InjectMocks
    private PlanService planService;
    
    private String validToken;
    private String userEmail;
    private PlanDTO planDto;
    private PlanEntity planEntity;
    
    @BeforeEach
    void setUp() {
        // 테스트용 데이터 준비
        validToken = "Bearer valid-token";
        userEmail = "test@test.com";
        
        planDto = PlanDTO.builder()
                .title("회의")
                .content("팀 미팅")
                .start_date(LocalDateTime.of(2025, 1, 20, 14, 0))
                .end_date(LocalDateTime.of(2025, 1, 20, 15, 0))
                .start_time(LocalTime.of(14, 0))
                .end_time(LocalTime.of(15, 0))
                .color(1)
                .build();
        
        planEntity = PlanEntity.builder()
                .s_id(1)
                .email(userEmail)
                .title("회의")
                .content("팀 미팅")
                .start_date(LocalDateTime.of(2025, 1, 20, 14, 0))
                .end_date(LocalDateTime.of(2025, 1, 20, 15, 0))
                .start_time(LocalTime.of(14, 0))
                .end_time(LocalTime.of(15, 0))
                .color(1)
                .build();
    }
    
    @Test
    @DisplayName("일정 등록 성공")
    void 일정_등록_성공() {
        // given
        when(authUtil.extractEmail(anyString())).thenReturn(userEmail);
        when(planRepository.save(any(PlanEntity.class))).thenReturn(planEntity);
        
        // when
        PlanDTO result = planService.savePlan(planDto, validToken);
        
        // then
        assertNotNull(result);
        assertEquals("회의", result.getTitle());
        verify(planRepository, times(1)).save(any(PlanEntity.class));
    }
    
    @Test
    @DisplayName("일정 수정 성공 - 본인 확인")
    void 일정_수정_성공_본인확인() {
        // given
    	when(authUtil.extractEmail(anyString())).thenReturn(userEmail);
        when(planRepository.findById(1)).thenReturn(Optional.of(planEntity));
        
        PlanDTO updateDto = PlanDTO.builder()
                .title("회의 변경")
                .build();
        
        // when
        PlanDTO result = planService.updatePlan(validToken, 1, updateDto);
        
        // then
        assertNotNull(result);
        assertEquals("회의 변경", result.getTitle());
    }
    
    @Test
    @DisplayName("일정 수정 실패 - 권한 없음")
    void 일정_수정_실패_권한없음() {
        // given
        String otherUserEmail = "other@test.com";
        when(authUtil.extractEmail(anyString())).thenReturn(otherUserEmail);
        when(planRepository.findById(1)).thenReturn(Optional.of(planEntity));
        
        PlanDTO updateDto = PlanDTO.builder()
                .title("회의 변경")
                .build();
        
        // when & then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> planService.updatePlan(validToken, 1, updateDto)
        );
        
        assertEquals("본인의 일정만 수정할 수 있습니다", exception.getMessage());
    }
}
