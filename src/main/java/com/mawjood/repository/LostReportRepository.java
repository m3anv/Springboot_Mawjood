package com.mawjood.repository;

import com.mawjood.model.Location;
import com.mawjood.model.LostReport;
import com.mawjood.model.ReportStatus;
import com.mawjood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link LostReport} records.
 */
public interface LostReportRepository extends JpaRepository<LostReport, Long> {

    /** Returns all reports submitted by a specific user, newest first. */
    List<LostReport> findByUserOrderByCreatedAtDesc(User user);

    /** Returns all reports for a given location (i.e. an office's queue), newest first. */
    List<LostReport> findByLocationOrderByCreatedAtDesc(Location location);

    /** Counts how many reports have been submitted for a location; used for dashboard stats. */
    long countByLocation(Location location);

    /** Counts reports with a specific status at a location; used for the "returned" counter. */
    long countByLocationAndStatus(Location location, ReportStatus status);
}
