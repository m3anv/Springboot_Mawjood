package com.mawjood.repository;

import com.mawjood.model.Location;
import com.mawjood.model.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Database access for {@link Office} records.
 *
 * Used to look up the office responsible for a given location, which is needed
 * during auto-match detection when a new lost report is submitted.
 */
public interface OfficeRepository extends JpaRepository<Office, Long> {

    /** Returns the office assigned to the given location, if one exists. */
    Optional<Office> findByLocation(Location location);
}
