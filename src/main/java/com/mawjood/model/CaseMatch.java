package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Links a {@link LostReport} to a {@link FoundItem} as a potential match.
 *
 * Matches are created either automatically by the matching algorithm when a new
 * found item is added to inventory, or manually by office staff on the case
 * details page. The office then progresses the match through {@link MatchStatus}
 * states until it is RETURNED or CLOSED.
 *
 * A (lostReportId, foundItemId) pair is unique — the repository prevents
 * duplicates via {@code findByLostReportIdAndFoundItemId}.
 */
@Entity
@Table(name = "case_matches")
public class CaseMatch {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** The citizen's lost item report involved in this match. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lost_report_id", nullable = false)
    private LostReport lostReport;

    /** The office inventory item that may correspond to the lost report. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "found_item_id", nullable = false)
    private FoundItem foundItem;

    /** The office managing this match. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    // ── Status & Notes ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status = MatchStatus.PENDING;

    /** Free-text notes the office can add when updating the match status. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LostReport getLostReport() { return lostReport; }
    public void setLostReport(LostReport lostReport) { this.lostReport = lostReport; }
    public FoundItem getFoundItem() { return foundItem; }
    public void setFoundItem(FoundItem foundItem) { this.foundItem = foundItem; }
    public Office getOffice() { return office; }
    public void setOffice(Office office) { this.office = office; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
