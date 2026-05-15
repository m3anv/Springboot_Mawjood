package com.mawjood.model;

/**
 * Lifecycle states for a {@link CaseMatch}.
 *
 * Mirrors the report lifecycle but is specific to the match between one
 * {@link LostReport} and one {@link FoundItem}.
 *
 * The {@link #canTransitionTo} guard prevents illegal state changes.
 */
public enum MatchStatus {
    /** Match created, pending office review. */
    PENDING,
    /** Office is actively reviewing this match. */
    UNDER_REVIEW,
    /** Match confirmed — item and owner verified. */
    MATCHED,
    /** Match was dismissed; item does not belong to this reporter. */
    REJECTED,
    /** Item has been returned to the owner. */
    RETURNED,
    /** Case fully closed. */
    CLOSED;

    /**
     * Returns true if the transition from the current state to {@code next}
     * is permitted.
     */
    public boolean canTransitionTo(MatchStatus next) {
        if (this == CLOSED) return false;
        if (this == RETURNED) return next == CLOSED;
        return this != next;
    }
}
