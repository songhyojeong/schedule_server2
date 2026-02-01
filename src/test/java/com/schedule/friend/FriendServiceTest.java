package com.schedule.friend;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.schedule.friend.entity.FriendEntity;
import com.schedule.common.AuthUtil;
import com.schedule.friend.FriendRepository;
import com.schedule.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private FriendService friendService;

    private String validToken;
    private String userEmail;
    private String targetEmail;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-token";
        userEmail = "user1@test.com";
        targetEmail = "user2@test.com";
        
        when(authUtil.extractEmail(anyString())).thenReturn(userEmail);
    }

    @Test
    @DisplayName("친구 추가 성공")
    void 친구_추가_성공() {
        // given
        when(userRepository.existsByEmail(targetEmail)).thenReturn(true);
        when(friendRepository.existsPair(anyString(), anyString())).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> friendService.addfriend(validToken, targetEmail));

        // 저장 메서드가 1번 호출됐는지 검증
        verify(friendRepository, times(1)).save(any(FriendEntity.class));
    }

    @Test
    @DisplayName("친구 추가 실패 - 본인 추가 불가")
    void 친구_추가_실패_본인추가불가() {
        // given
        // (setUp에서 authUtil 설정 완료)

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> friendService.addfriend(validToken, userEmail)
        );

        assertEquals("본인은 친구로 추가할 수 없습니다.", exception.getMessage());

        // 저장 메서드가 호출되지 않았는지 검증
        verify(friendRepository, never()).save(any(FriendEntity.class));
    }

    @Test
    @DisplayName("친구 추가 실패 - 이미 친구")
    void 친구_추가_실패_이미친구() {
        // given
        when(userRepository.existsByEmail(targetEmail)).thenReturn(true);
        when(friendRepository.existsPair(anyString(), anyString())).thenReturn(true);

        // when & then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> friendService.addfriend(validToken, targetEmail)
        );

        assertEquals("이미 친구입니다.", exception.getMessage());

        // 저장 메서드가 호출되지 않았는지 검증
        verify(friendRepository, never()).save(any(FriendEntity.class));
    }
}