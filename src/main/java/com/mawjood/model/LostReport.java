package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A lost item report submitted by a {@link User}.
 *
 * When created the status is SUBMITTED. The assigned {@link Office} moves the
 * report through the {@link ReportStatus} lifecycle as they review it, find a
 * possible match, confirm the match, and ultimately mark it as RETURNED or
 * CLOSED.
 *
 * Category and color are stored as uppercase string codes (e.g. "PHONE",
 * "BLACK") so they match consistently across Arabic and English UI input.
 */
@Entity
@Table(name = "lost_reports")
public class LostReport {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** The citizen who submitted this report. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The location where the item was lost; determines which office handles it. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ── Item details ──────────────────────────────────────────────────────────

    @Column(nullable = false, length = 120)
    private String itemName;

    /** Uppercase code from the fixed list in {@link com.mawjood.service.LookupService}. */
    @Column(nullable = false, length = 100)
    private String category;

    /** Uppercase color code; may be null or blank if unknown. */
    @Column(length = 50)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "lost_date", nullable = false)
    private LocalDate lostDate;

    @Column(name = "lost_area", nullable = false, length = 160)
    private String lostArea;

    @Column(name = "contact_info", nullable = false, length = 150)
    private String contactInfo;

    // ── Status & Audit ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status = ReportStatus.SUBMITTED;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getLostDate() { return lostDate; }
    public void setLostDate(LocalDate lostDate) { this.lostDate = lostDate; }
    public String getLostArea() { return lostArea; }
    public void setLostArea(String lostArea) { this.lostArea = lostArea; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
