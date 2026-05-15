package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An in-app notification delivered to a specific {@link User}.
 *
 * Currently used only for ADMIN users — they receive a notification whenever
 * a match is created or confirmed by any office. The {@code readStatus} flag
 * lets the UI show an unread count badge.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** The user who should receive this notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Content & State ───────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /** False by default; set to true once the user has viewed the notification. */
    @Column(nullable = false)
    private boolean readStatus = false;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isReadStatus() { return readStatus; }
    public void setReadStatus(boolean readStatus) { this.readStatus = readStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
