package com.mawjood.dto;

import com.mawjood.model.ReportStatus;

import java.time.LocalDate;

/**
 * A read-only data transfer object for displaying a lost report to the citizen.
 *
 * Controllers use this DTO instead of the raw {@link com.mawjood.model.LostReport}
 * entity when rendering the user-facing dashboard and report-details pages. This
 * ensures that sensitive fields (e.g. the linked User entity) are never
 * accidentally exposed through the template.
 */
public class LostReportDTO {

    // ── Fields ────────────────────────────────────────────────────────────────

    private final Long id;
    private final String itemName;
    private final String category;
    private final String color;
    private final String description;
    private final LocalDate lostDate;
    private final String lostArea;
    private final ReportStatus status;
    /** Display name of the location the report was sent to. */
    private final String locationName;

    // ── Constructor ───────────────────────────────────────────────────────────

    public LostReportDTO(Long id, String itemName, String category, String color,
                         String description, LocalDate lostDate, String lostArea,
                         ReportStatus status, String locationName) {
        this.id = id;
        this.itemName = itemName;
        this.category = category;
        this.color = color;
        this.description = description;
        this.lostDate = lostDate;
        this.lostArea = lostArea;
        this.status = status;
        this.locationName = locationName;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Long getId() { return id; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getColor() { return color; }
    public String getDescription() { return description; }
    public LocalDate getLostDate() { return lostDate; }
    public String getLostArea() { return lostArea; }
    public ReportStatus getStatus() { return status; }
    public String getLocationName() { return locationName; }
}
