package org.agri.agritrade.repository;

import org.agri.agritrade.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);
    long countByUser_IdAndReadFalse(Long userId);
}
