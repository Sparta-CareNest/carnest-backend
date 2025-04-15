package com.carenest.business.notificationservice.application.service;

import com.carenest.business.notificationservice.application.dto.request.NotificationCreateRequestDto;
import com.carenest.business.notificationservice.application.dto.response.UserInfoResponseDto;
import com.carenest.business.notificationservice.domain.model.Notification;
import com.carenest.business.notificationservice.domain.model.NotificationType;
import com.carenest.business.notificationservice.domain.repository.NotificationRepository;
import com.carenest.business.notificationservice.infrastructure.client.UserClient;
import com.carenest.business.notificationservice.infrastructure.config.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createNotificationWithType_shouldSaveNotificationAndSendMessage() throws IOException {
        // given
        UUID userId = UUID.randomUUID();
        NotificationCreateRequestDto requestDto = new NotificationCreateRequestDto(userId, null);
        NotificationType type = NotificationType.RESERVATION_CREATED;

        when(userClient.validateUser(userId)).thenReturn(true);
        //when(userClient.getUserInfo(userId)).thenReturn(makeUserInfo(userId));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        notificationService.createNotificationWithType(requestDto, type);

        // then
        verify(userClient).validateUser(userId);
        //verify(userClient).getUserInfo(userId);
        verify(notificationRepository).save(any(Notification.class));
        verify(webSocketHandler).sendNotification(eq(userId), contains("알림"));
    }

    private UserInfoResponseDto makeUserInfo(UUID userId) {
        UserInfoResponseDto dto = new UserInfoResponseDto();
        dto.setUserId(userId);
        dto.setNickname("은선");
        dto.setEmail("eunseon@test.com");
        return dto;
    }
}
