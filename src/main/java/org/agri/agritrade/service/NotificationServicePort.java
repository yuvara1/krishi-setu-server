package org.agri.agritrade.service;

import org.agri.agritrade.dto.NotificationDTO;
import org.agri.agritrade.dto.ResponseStructure;

import java.util.List;

public interface NotificationServicePort {
    void sendNotification(Long userId, String title, String message, String type);
    public ResponseStructure<List<NotificationDTO>> getUserNotifications(Long userId);
    public ResponseStructure<Long> getUnreadCount(Long userId);
    public ResponseStructure<Void> markAsRead(Long notificationId);
    public ResponseStructure<Void> markAllAsRead(Long userId);

}
