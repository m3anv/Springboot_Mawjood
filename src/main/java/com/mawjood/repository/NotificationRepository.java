package com.mawjood.repository;

import com.mawjood.model.Notification;
import com.mawjood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link Notification} records.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Returns all notifications for a user, newest first; used to populate the
     *  admin notification list. */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /** Returns the count of unread notifications; used to display the badge count
     *  in the navigation bar. */
    long countByUserAndReadStatusFalse(User user);
}
