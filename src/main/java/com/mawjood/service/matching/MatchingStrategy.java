package com.mawjood.service.matching;

import com.mawjood.model.FoundItem;
import com.mawjood.model.LostReport;

/**
 * Strategy interface for determining whether a {@link LostReport} and a
 * {@link FoundItem} are a potential match.
 *
 * Using an interface (the Strategy design pattern) means the matching logic
 * can be swapped out or tested independently without changing any service code.
 * The current implementation is {@link StrictMatchingStrategy}.
 */
public interface MatchingStrategy {

    /**
     * Returns true if the lost report and found item are considered a match.
     *
     * @param lost  the citizen's lost item report
     * @param found an item from the office's found inventory
     */
    boolean isMatch(LostReport lost, FoundItem found);
}
