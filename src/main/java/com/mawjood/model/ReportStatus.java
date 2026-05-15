package com.mawjood.model;

/**
 * Lifecycle states for a {@link LostReport}.
 *
 * The valid progression is roughly:
 * SUBMITTED → UNDER_REVIEW → POSSIBLE_MATCH → MATCHED → RETURNED → CLOSED
 *
 * The {@link #canTransitionTo} method enforces allowed transitions so that
 * the office cannot accidentally skip or regress states.
 */
public enum ReportStatus {
    /** Report received, not yet reviewed by the office. */
    SUBMITTED,
    /** Office is actively reviewing the case. */
    UNDER_REVIEW,
    /** At least one possible match was found in inventory. */
    POSSIBLE_MATCH,
    /** The office confirmed an item matches this report. */
    MATCHED,
    /** The item has been physically returned to the owner. */
    RETURNED,
    /** Case closed (item not found or owner declined). */
    CLOSED;

    /**
     * Returns true if transitioning from the current state to {@code next} is
     * allowed. A closed report cannot be re-opened, and a returned item can
     * only move to CLOSED.
     */
    public boolean canTransitionTo(ReportStatus next) {
        if (this == CLOSED) return false;
        if (this == RETURNED) return next == CLOSED;
        return this != next;
    }
}
