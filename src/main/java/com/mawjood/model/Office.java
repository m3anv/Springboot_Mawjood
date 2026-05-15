package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a Lost & Found Office that manages a specific {@link Location}.
 *
 * Each office has one or more OFFICE-role {@link User} accounts assigned to it.
 * All {@link LostReport} records for a location are visible only to the office
 * that owns that location. Found inventory and matches are also scoped per office.
 */
@Entity
@Table(name = "offices")
public class Office {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Office details ────────────────────────────────────────────────────────

    @Column(nullable = false, length = 150)
    private String name;

    /** The physical location this office is responsible for. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "contact_email", length = 120)
    private String contactEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
