package org.agri.agritrade.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agri.agritrade.dto.response.NotificationDTO;
import org.agri.agritrade.dto.response.ResponseStructure;
import org.agri.agritrade.entity.Notification;
import org.agri.agritrade.entity.User;
import org.agri.agritrade.repository.NotificationRepository;
import org.agri.agritrade.repository.UserRepository;
import org.agri.agritrade.service.NotificationServicePort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements NotificationServicePort {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendNotification(Long userId, String title, String message, String type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);

        NotificationDTO dto = toDTO(saved);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
        log.info("Notification sent to user {}: {}", userId, title);
    }

    @Override
    public ResponseStructure<List<NotificationDTO>> getUserNotifications(Long userId) {
        List<NotificationDTO> notifications = notificationRepository
                .findByUser_IdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
        return new ResponseStructure<>(HttpStatus.OK.value(), "Notifications retrieved", notifications);
    }

    @Override
    public ResponseStructure<Long> getUnreadCount(Long userId) {
        long count = notificationRepository.countByUser_IdAndReadFalse(userId);
        return new ResponseStructure<>(HttpStatus.OK.value(), "Unread count", count);
    }

    @Override
    @Transactional
    public ResponseStructure<Void> markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return new ResponseStructure<>(HttpStatus.OK.value(), "Marked as read", null);
    }

    @Override
    @Transactional
    public ResponseStructure<Void> markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream().filter(n -> !n.isRead()).toList();
        unread.forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        return new ResponseStructure<>(HttpStatus.OK.value(), "All marked as read", null);
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .userId(n.getUser().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
