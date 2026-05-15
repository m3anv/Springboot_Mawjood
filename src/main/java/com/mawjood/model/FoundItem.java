package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An item found and logged by an {@link Office} into their private inventory.
 *
 * Found items are never visible to regular users. The office uses them as the
 * other side of a potential match against a {@link LostReport}. When a match is
 * confirmed and the item returned, the status advances to RETURNED or CLOSED.
 *
 * Category and color use the same uppercase code system as {@link LostReport}
 * so the {@link com.mawjood.service.matching.StrictMatchingStrategy} can
 * compare them directly regardless of the language the form was filled in.
 */
@Entity
@Table(name = "found_items")
public class FoundItem {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Relationships ─────────────────────────────────────────────────────────

    /** The office that added this item to inventory. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    /** Denormalised location copied from the office for simpler queries. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    /** The office staff member who created this inventory record. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    // ── Item details ──────────────────────────────────────────────────────────

    @Column(nullable = false, length = 120)
    private String itemName;

    /** Uppercase category code (e.g. "PHONE"). */
    @Column(nullable = false, length = 100)
    private String category;

    /** Uppercase color code (e.g. "BLACK"). */
    @Column(length = 50)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "found_date", nullable = false)
    private LocalDate foundDate;

    @Column(name = "found_area", nullable = false, length = 160)
    private String foundArea;

    /** Optional shelf / locker reference so staff can physically locate the item. */
    @Column(name = "storage_reference", length = 100)
    private String storageReference;

    // ── Status & Audit ────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FoundItemStatus status = FoundItemStatus.IN_INVENTORY;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Office getOffice() { return office; }
    public void setOffice(Office office) { this.office = office; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getFoundDate() { return foundDate; }
    public void setFoundDate(LocalDate foundDate) { this.foundDate = foundDate; }
    public String getFoundArea() { return foundArea; }
    public void setFoundArea(String foundArea) { this.foundArea = foundArea; }
    public String getStorageReference() { return storageReference; }
    public void setStorageReference(String storageReference) { this.storageReference = storageReference; }
    public FoundItemStatus getStatus() { return status; }
    public void setStatus(FoundItemStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
