package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An audit-trail entry attached to a {@link LostReport}.
 *
 * Every time the office changes the report status or leaves a message, a
 * CaseUpdate is created. Updates with {@code visibleToUser = true} are shown
 * to the citizen on their report details page; internal notes can be hidden
 * by setting {@code visibleToUser = false}.
 *
 * The first update is automatically created when the report is submitted,
 * confirming receipt.
 */
@Entity
@Table(name = "case_updates")
public class CaseUpdate {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** The report this update belongs to. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_report_id", nullable = false)
    private LostReport lostReport;

    /** The office that issued this update; null for system-generated entries. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    /** The specific staff member who triggered this update; null for automated ones. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ── Update content ────────────────────────────────────────────────────────

    /** The report status at the time this update was recorded. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    /** When true the citizen can read this update on their dashboard. */
    @Column(name = "visible_to_user", nullable = false)
    private boolean visibleToUser = true;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LostReport getLostReport() { return lostReport; }
    public void setLostReport(LostReport lostReport) { this.lostReport = lostReport; }
    public Office getOffice() { return office; }
    public void setOffice(Office office) { this.office = office; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isVisibleToUser() { return visibleToUser; }
    public void setVisibleToUser(boolean visibleToUser) { this.visibleToUser = visibleToUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
