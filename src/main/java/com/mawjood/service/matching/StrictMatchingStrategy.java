package com.mawjood.service.matching;

import com.mawjood.model.FoundItem;
import com.mawjood.model.LostReport;
import com.mawjood.service.LookupService;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

/**
 * The default matching algorithm that compares a lost report against a found item.
 *
 * A match requires ALL three conditions to be true:
 * <ol>
 *   <li><b>Category</b> – both items must have the exact same normalised category
 *       code (e.g. both "PHONE").</li>
 *   <li><b>Color</b> – either side may be UNKNOWN/blank (we treat that as a
 *       wildcard), or both colors must match exactly.</li>
 *   <li><b>Date</b> – the found date must be within {@value #MAX_DAYS_WINDOW} days
 *       of the lost date (in either direction). Missing dates are skipped.</li>
 * </ol>
 *
 * Category and color are compared after normalisation (uppercase, trimmed) so
 * Arabic and English input produce the same code and can be matched together.
 */
@Component
public class StrictMatchingStrategy implements MatchingStrategy {

    private final LookupService lookupService;

    public StrictMatchingStrategy(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    /** Maximum number of days between the lost date and found date for a match. */
    private static final int MAX_DAYS_WINDOW = 60;

    // ── Match Evaluation ──────────────────────────────────────────────────────

    @Override
    public boolean isMatch(LostReport lost, FoundItem found) {
        boolean category = sameNormalized(lost.getCategory(), found.getCategory());
        boolean color = isUnknown(lost.getColor()) || isUnknown(found.getColor())
                || sameNormalized(lost.getColor(), found.getColor());
        boolean date = lost.getLostDate() == null || found.getFoundDate() == null
                || Math.abs(ChronoUnit.DAYS.between(lost.getLostDate(), found.getFoundDate())) <= MAX_DAYS_WINDOW;
        return category && color && date;
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    /** Returns true if both values normalise to the same non-blank code. */
    private boolean sameNormalized(String a, String b) {
        String na = lookupService.normalize(a);
        return !na.isBlank() && na.equals(lookupService.normalize(b));
    }

    /** Returns true if the value is blank or the "UNKNOWN" placeholder code.
     *  An unknown color is treated as a wildcard in the match. */
    private boolean isUnknown(String value) {
        String normalized = lookupService.normalize(value);
        return normalized.isBlank() || normalized.equals("UNKNOWN");
    }
}
