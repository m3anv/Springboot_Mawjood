package com.mawjood.repository;

import com.mawjood.model.FoundItem;
import com.mawjood.model.FoundItemStatus;
import com.mawjood.model.Location;
import com.mawjood.model.Office;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Database access for {@link FoundItem} records (office inventory).
 */
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {

    /** Returns all items in a specific office's inventory, newest first. */
    List<FoundItem> findByOfficeOrderByCreatedAtDesc(Office office);

    /**
     * Returns items at a location whose status is in the given list, newest first.
     * Used by the matching algorithm to find candidate inventory items that are
     * still active (IN_INVENTORY or UNDER_REVIEW).
     */
    List<FoundItem> findByLocationAndStatusInOrderByCreatedAtDesc(Location location, List<FoundItemStatus> statuses);

    /** Total number of items ever logged by this office; used for dashboard stats. */
    long countByOffice(Office office);

    /** Number of items with a specific status at this office; used for the "returned" counter. */
    long countByOfficeAndStatus(Office office, FoundItemStatus status);
}
