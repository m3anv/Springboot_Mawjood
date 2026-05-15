package com.mawjood.repository;

import com.mawjood.model.CaseUpdate;
import com.mawjood.model.LostReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link CaseUpdate} records (the timeline entries on a
 * lost report).
 */
public interface CaseUpdateRepository extends JpaRepository<CaseUpdate, Long> {

    /** Returns all updates (visible and hidden) for a report, newest first.
     *  Used by the office case-details page. */
    List<CaseUpdate> findByLostReportOrderByCreatedAtDesc(LostReport lostReport);

    /**
     * Returns only updates with the given visibility flag, newest first.
     * Pass {@code true} to fetch the citizen-visible timeline on the dashboard.
     */
    List<CaseUpdate> findByLostReportAndVisibleToUserOrderByCreatedAtDesc(LostReport lostReport, boolean visibleToUser);

    /** Removes all updates for a report; called before deleting that report. */
    void deleteByLostReport(LostReport lostReport);
}
