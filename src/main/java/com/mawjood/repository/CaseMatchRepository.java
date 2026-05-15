package com.mawjood.repository;

import com.mawjood.model.CaseMatch;
import com.mawjood.model.FoundItem;
import com.mawjood.model.LostReport;
import com.mawjood.model.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for {@link CaseMatch} records.
 */
public interface CaseMatchRepository extends JpaRepository<CaseMatch, Long> {

    /** Returns all matches managed by an office, newest first. */
    List<CaseMatch> findByOfficeOrderByCreatedAtDesc(Office office);

    /** Returns all matches linked to a specific lost report, newest first. */
    List<CaseMatch> findByLostReportOrderByCreatedAtDesc(LostReport lostReport);

    /**
     * Checks if a match between a specific report and found item already exists.
     * Used to prevent duplicate match records when auto-detection runs more than once.
     */
    Optional<CaseMatch> findByLostReportIdAndFoundItemId(Long lostReportId, Long foundItemId);

    /** Total number of matches for an office; shown on the office dashboard. */
    long countByOffice(Office office);

    /** Removes all matches linked to a lost report; called before deleting that report. */
    void deleteByLostReport(LostReport lostReport);

    /** Removes all matches linked to a found item; called before deleting that item. */
    void deleteByFoundItem(FoundItem foundItem);
}
