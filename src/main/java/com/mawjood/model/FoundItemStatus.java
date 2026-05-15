package com.mawjood.model;

/**
 * Lifecycle states for a {@link FoundItem} in the office inventory.
 *
 * The {@link #canTransitionTo} guard enforces valid progressions and prevents
 * reopening a closed or returned item.
 */
public enum FoundItemStatus {
    /** Item is in the office inventory, available for matching. */
    IN_INVENTORY,
    /** Office is actively reviewing this item against a report. */
    UNDER_REVIEW,
    /** Item has been matched to a lost report. */
    MATCHED,
    /** Item has been returned to its owner. */
    RETURNED,
    /** Item record is closed (disposed of, unclaimed, etc.). */
    CLOSED;

    /**
     * Returns true if transitioning from the current state to {@code next} is
     * allowed.
     */
    public boolean canTransitionTo(FoundItemStatus next) {
        if (this == CLOSED) return false;
        if (this == RETURNED) return next == CLOSED;
        return this != next;
    }
}
