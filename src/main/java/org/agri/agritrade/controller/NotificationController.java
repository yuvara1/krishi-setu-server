package org.agri.agritrade.controller;

import lombok.RequiredArgsConstructor;
import org.agri.agritrade.dto.NotificationDTO;
import org.agri.agritrade.dto.ResponseStructure;
import org.agri.agritrade.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseStructure<List<NotificationDTO>>> getUserNotifications(@PathVariable Long userId) {
        ResponseStructure<List<NotificationDTO>> response = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ResponseStructure<Long>> getUnreadCount(@PathVariable Long userId) {
        ResponseStructure<Long> response = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ResponseStructure<Void>> markAsRead(@PathVariable Long id) {
        ResponseStructure<Void> response = notificationService.markAsRead(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ResponseStructure<Void>> markAllAsRead(@PathVariable Long userId) {
        ResponseStructure<Void> response = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(response);
    }
}
