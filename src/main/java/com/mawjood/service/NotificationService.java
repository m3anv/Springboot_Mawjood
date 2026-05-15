package com.mawjood.service;

import com.mawjood.model.Notification;
import com.mawjood.model.User;
import com.mawjood.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creates and retrieves in-app notifications for users.
 *
 * Currently notifications are only sent to ADMIN users, triggered whenever
 * a match is created or its status changes to MATCHED or RETURNED. The
 * notification count is displayed as a badge in the navigation bar.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    /**
     * Creates and persists a new notification for the given user.
     *
     * @param user    the recipient
     * @param message the notification text to display
     */
    public void create(User user, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    /** Returns all notifications for a user ordered by newest first. */
    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /** Returns the number of unread notifications; used for the nav badge count. */
    public long unreadCount(User user) {
        return notificationRepository.countByUserAndReadStatusFalse(user);
    }
}
