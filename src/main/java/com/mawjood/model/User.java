package com.mawjood.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents a platform user.
 *
 * Three roles exist: USER (citizen submitting lost reports), OFFICE (staff at a
 * Lost & Found office who manage inventory and matches), and ADMIN (platform
 * administrator with full visibility).
 *
 * OFFICE users are linked to an {@link Office} via the office_id foreign key.
 * USER and ADMIN accounts have a null office reference.
 */
@Entity
@Table(name = "users")
public class User {

    // ── Primary Key ──────────────────────────────────────────────────────────

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Identity fields ───────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String name;

    /** Must be unique across all accounts; used as the login credential. */
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    // ── Role & Office link ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;

    /** Optional fields kept for future organisation-level features. */
    @Column(name = "organization_name", length = 150)
    private String organizationName;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    /** Non-null only for OFFICE role users; determines which office they manage. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id")
    private Office office;

    // ── Audit ─────────────────────────────────────────────────────────────────

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Office getOffice() { return office; }
    public void setOffice(Office office) { this.office = office; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ── Convenience helpers (not persisted) ───────────────────────────────────

    /** Returns true if this user has the ADMIN role. */
    @Transient
    public boolean isAdmin() { return role == Role.ADMIN; }

    /** Returns true if this user has the OFFICE role. */
    @Transient
    public boolean isOffice() { return role == Role.OFFICE; }
}
