package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a physical location (e.g. a university campus, airport, or mall)
 * that has an associated Lost & Found {@link Office}.
 *
 * When a user submits a lost report they choose a Location; the report is then
 * automatically routed to the Office that manages that location.
 */
@Entity
@Table(name = "locations")
public class Location {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Location details ──────────────────────────────────────────────────────

    @Column(nullable = false, length = 150)
    private String name;

    /** Optional type label (e.g. "University", "Airport"). */
    @Column(length = 80)
    private String type;

    @Column(length = 180)
    private String address;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
