package com.schedule.user;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.schedule.user.dto.UserDTO;
import com.schedule.user.entity.UserEntity;
import com.schedule.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    private UserDTO userDto;
    
    @BeforeEach
    void setUp() {
        // 각 테스트 전에 실행 - 테스트용 데이터 준비
        userDto = UserDTO.builder()
                .email("test@test.com")
                .pw("password123")
                .nickname("테스트유저")
                .build();
    }
    
    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        // given (준비)
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // when (실행) & then (검증)
        assertDoesNotThrow(() -> userService.signup(userDto));
        
        // 저장 메서드가 1번 호출됐는지 검증
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void 회원가입_실패_이메일중복() {
        // given
        when(userRepository.existsById(anyString())).thenReturn(true);
        
        // when & then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> userService.signup(userDto)
        );
        
        assertEquals("중복된 이메일", exception.getMessage());
        
        // 저장 메서드가 호출되지 않았는지 검증
        verify(userRepository, never()).save(any(UserEntity.class));
    }
    
    @Test
    @DisplayName("회원가입 실패 - 닉네임 중복")
    void 회원가입_실패_닉네임중복() {
        // given
        when(userRepository.existsById(anyString())).thenReturn(false);
        when(userRepository.existsByNickname(anyString())).thenReturn(true);
        
        // when & then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> userService.signup(userDto)
        );
        
        assertEquals("중복된 닉네임", exception.getMessage());
        
        // 저장 메서드가 호출되지 않았는지 검증
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
