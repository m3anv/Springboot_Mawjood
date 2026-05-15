package com.mawjood.service;

import com.mawjood.model.*;
import com.mawjood.repository.CaseMatchRepository;
import com.mawjood.repository.FoundItemRepository;
import com.mawjood.repository.OfficeRepository;
import com.mawjood.repository.UserRepository;
import com.mawjood.service.matching.MatchingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Facade service that coordinates all office-side operations.
 *
 * This class acts as the single entry point for {@link OfficeController} and
 * brings together inventory management, match detection, match updates, and
 * delegation to {@link LostReportService} for report operations.
 *
 * Using the Facade pattern keeps the controller thin: it only calls this service
 * and never touches repositories directly.
 *
 * Key flows:
 * <ul>
 *   <li>When a found item is added → auto-detect matches against open reports.</li>
 *   <li>When a lost report is submitted → auto-detect matches against inventory.</li>
 *   <li>On application startup → scan all existing inventory for missing matches.</li>
 * </ul>
 */
@Service
public class OfficeWorkflowService {

    private final OfficeRepository officeRepository;
    private final FoundItemRepository foundItemRepository;
    private final CaseMatchRepository caseMatchRepository;
    private final MatchingStrategy matchingStrategy;
    private final LostReportService lostReportService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public OfficeWorkflowService(OfficeRepository officeRepository,
                                 FoundItemRepository foundItemRepository,
                                 CaseMatchRepository caseMatchRepository,
                                 MatchingStrategy matchingStrategy,
                                 LostReportService lostReportService,
                                 NotificationService notificationService,
                                 UserRepository userRepository) {
        this.officeRepository = officeRepository;
        this.foundItemRepository = foundItemRepository;
        this.caseMatchRepository = caseMatchRepository;
        this.matchingStrategy = matchingStrategy;
        this.lostReportService = lostReportService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    // ── Office Resolution ─────────────────────────────────────────────────────

    /**
     * Returns the fully-loaded Office entity for the given user.
     * Returns empty if the user is not an office user or has no office assigned.
     */
    public Optional<Office> officeFor(User user) {
        if (user == null || user.getOffice() == null) return Optional.empty();
        return officeRepository.findById(user.getOffice().getId());
    }

    // ── Found Item Management ─────────────────────────────────────────────────

    /**
     * Creates a new found item record in the office inventory and immediately
     * runs auto-match detection against all open lost reports for that office.
     *
     * @return the saved FoundItem entity
     */
    public FoundItem createFoundItem(Office office, User createdBy, String itemName, String category,
                                     String color, String description, LocalDate foundDate,
                                     String foundArea, String storageReference) {
        FoundItem item = new FoundItem();
        item.setOffice(office);
        item.setLocation(office.getLocation());
        item.setCreatedBy(createdBy);
        item.setItemName(itemName);
        item.setCategory(category);
        item.setColor(color);
        item.setDescription(description);
        item.setFoundDate(foundDate);
        item.setFoundArea(foundArea);
        item.setStorageReference(storageReference);
        FoundItem saved = foundItemRepository.save(item);
        autoDetectMatches(office, saved);
        return saved;
    }

    /**
     * Updates the fields of an existing found item record.
     * Does not re-run match detection — office should review matches manually
     * after editing.
     */
    public FoundItem updateFoundItem(FoundItem item, String itemName, String category, String color,
                                     String description, LocalDate foundDate, String foundArea,
                                     String storageReference) {
        item.setItemName(itemName);
        item.setCategory(category);
        item.setColor(color);
        item.setDescription(description);
        item.setFoundDate(foundDate);
        item.setFoundArea(foundArea);
        item.setStorageReference(storageReference);
        return foundItemRepository.save(item);
    }

    /** Returns all found items in an office's inventory, newest first. */
    public List<FoundItem> inventory(Office office) {
        return foundItemRepository.findByOfficeOrderByCreatedAtDesc(office);
    }

    /** Looks up a single found item by its primary key. */
    public Optional<FoundItem> foundItem(Long id) {
        return foundItemRepository.findById(id);
    }

    /** Returns true if the found item belongs to the given office. */
    public boolean belongsToOffice(FoundItem item, Office office) {
        return item.getOffice().getId().equals(office.getId());
    }

    /**
     * Deletes a found item and all its associated match records.
     * Wrapped in a transaction so both deletions succeed or fail together.
     */
    @Transactional
    public void deleteFoundItem(FoundItem item) {
        caseMatchRepository.deleteByFoundItem(item);
        foundItemRepository.delete(item);
    }

    // ── Auto Match Detection ──────────────────────────────────────────────────

    /**
     * Compares a newly added found item against all open lost reports in the
     * same office. For each report that matches, a CaseMatch is created and the
     * report status is advanced to POSSIBLE_MATCH if it was SUBMITTED or
     * UNDER_REVIEW.
     */
    private void autoDetectMatches(Office office, FoundItem saved) {
        lostReportService.forOffice(office).stream()
                .filter(r -> r.getStatus() == ReportStatus.SUBMITTED
                          || r.getStatus() == ReportStatus.UNDER_REVIEW
                          || r.getStatus() == ReportStatus.POSSIBLE_MATCH)
                .filter(r -> matchingStrategy.isMatch(r, saved))
                .forEach(r -> {
                    createMatch(office, r, saved, null);
                    if (r.getStatus() == ReportStatus.SUBMITTED
                            || r.getStatus() == ReportStatus.UNDER_REVIEW) {
                        lostReportService.updateStatus(r, office, null, ReportStatus.POSSIBLE_MATCH,
                                "A possible match was automatically detected for your lost item.", true);
                    }
                });
    }

    /**
     * Scans all offices' entire inventory for missing match records.
     * Called once at application startup to backfill matches that were created
     * before the auto-detection feature existed.
     */
    public void scanExistingForMatches() {
        officeRepository.findAll()
                .forEach(office -> inventory(office).forEach(item -> autoDetectMatches(office, item)));
    }

    /**
     * Compares a newly submitted lost report against active found inventory at
     * the same location and creates match records for any compatible items.
     * Called by {@link com.mawjood.controller.LostReportController} after saving
     * a new report.
     */
    public void autoDetectMatchesForReport(LostReport report) {
        officeRepository.findByLocation(report.getLocation()).ifPresent(office -> {
            foundItemRepository.findByLocationAndStatusInOrderByCreatedAtDesc(
                    report.getLocation(),
                    List.of(FoundItemStatus.IN_INVENTORY, FoundItemStatus.UNDER_REVIEW)
            ).stream()
                    .filter(item -> matchingStrategy.isMatch(report, item))
                    .forEach(item -> {
                        createMatch(office, report, item, null);
                        if (report.getStatus() == ReportStatus.SUBMITTED
                                || report.getStatus() == ReportStatus.UNDER_REVIEW) {
                            lostReportService.updateStatus(report, office, null, ReportStatus.POSSIBLE_MATCH,
                                    "A possible match was automatically detected for your lost item.", true);
                        }
                    });
        });
    }

    // ── Match Management ──────────────────────────────────────────────────────

    /** Returns all matches for an office, newest first. */
    public List<CaseMatch> matches(Office office) {
        return caseMatchRepository.findByOfficeOrderByCreatedAtDesc(office);
    }

    /** Returns all matches linked to a specific lost report, newest first. */
    public List<CaseMatch> matchesForReport(LostReport report) {
        return caseMatchRepository.findByLostReportOrderByCreatedAtDesc(report);
    }

    /**
     * Creates a match between a lost report and a found item, if one does not
     * already exist. Notifies all ADMIN users when a new match is created.
     *
     * @param notes optional notes from the office staff
     * @return the existing or newly created CaseMatch
     */
    public CaseMatch createMatch(Office office, LostReport report, FoundItem item, String notes) {
        return caseMatchRepository.findByLostReportIdAndFoundItemId(report.getId(), item.getId())
                .orElseGet(() -> {
                    CaseMatch match = new CaseMatch();
                    match.setOffice(office);
                    match.setLostReport(report);
                    match.setFoundItem(item);
                    match.setNotes(notes);
                    CaseMatch saved = caseMatchRepository.save(match);
                    String message = "Possible match found: \"" + report.getItemName()
                            + "\" at " + office.getLocation().getName();
                    userRepository.findByRole(Role.ADMIN)
                            .forEach(admin -> notificationService.create(admin, message));
                    return saved;
                });
    }

    /**
     * Advances a match to a new status and optionally updates its notes.
     * Notifies all ADMIN users when a match reaches MATCHED or RETURNED.
     *
     * @throws IllegalStateException if the transition is not permitted by
     *         {@link MatchStatus#canTransitionTo}
     */
    public CaseMatch updateMatch(CaseMatch match, MatchStatus status, String notes) {
        if (!match.getStatus().canTransitionTo(status)) {
            throw new IllegalStateException(
                    "Cannot transition match from " + match.getStatus() + " to " + status);
        }
        match.setStatus(status);
        if (notes != null) match.setNotes(notes.trim());
        CaseMatch saved = caseMatchRepository.save(match);
        // Notify admins on major milestones.
        if (status == MatchStatus.MATCHED || status == MatchStatus.RETURNED) {
            String item = match.getLostReport().getItemName();
            String location = match.getOffice().getLocation().getName();
            String message = status == MatchStatus.MATCHED
                    ? "Match confirmed: \"" + item + "\" at " + location
                    : "Item returned: \"" + item + "\" at " + location;
            userRepository.findByRole(Role.ADMIN)
                    .forEach(admin -> notificationService.create(admin, message));
        }
        return saved;
    }

    /** Looks up a single match by its primary key. */
    public Optional<CaseMatch> match(Long id) {
        return caseMatchRepository.findById(id);
    }

    /**
     * Returns the subset of found inventory that the matching algorithm
     * considers compatible with a given lost report; used on the case-details
     * page to suggest manual matches to the office.
     */
    public List<FoundItem> possibleMatches(Office office, LostReport report) {
        return foundItemRepository.findByLocationAndStatusInOrderByCreatedAtDesc(
                office.getLocation(),
                List.of(FoundItemStatus.IN_INVENTORY, FoundItemStatus.UNDER_REVIEW)
        ).stream().filter(item -> matchingStrategy.isMatch(report, item)).toList();
    }

    // ── Dashboard Statistics ──────────────────────────────────────────────────

    /** Total number of found items ever added to this office's inventory. */
    public long inventoryCount(Office office) {
        return foundItemRepository.countByOffice(office);
    }

    /** Number of found items that have been returned to their owners. */
    public long returnedCount(Office office) {
        return foundItemRepository.countByOfficeAndStatus(office, FoundItemStatus.RETURNED);
    }

    /** Total number of matches created by this office. */
    public long matchCount(Office office) {
        return caseMatchRepository.countByOffice(office);
    }

    // ── Lost Report Delegates ─────────────────────────────────────────────────
    // These methods forward calls to LostReportService so that OfficeController
    // only needs to depend on this one service.

    public List<LostReport> reportsForOffice(Office office) {
        return lostReportService.forOffice(office);
    }

    public Optional<LostReport> reportById(Long id) {
        return lostReportService.findById(id);
    }

    public boolean belongsToOffice(LostReport report, Office office) {
        return lostReportService.belongsToOffice(report, office);
    }

    public List<CaseUpdate> allCaseUpdates(LostReport report) {
        return lostReportService.allUpdates(report);
    }

    public LostReport updateReportStatus(LostReport report, Office office, User actor,
                                         ReportStatus status, String message, boolean visibleToUser) {
        return lostReportService.updateStatus(report, office, actor, status, message, visibleToUser);
    }

    @Transactional
    public void deleteReport(LostReport report) {
        lostReportService.delete(report);
    }

    public long reportCount(Office office) {
        return lostReportService.countForOffice(office);
    }

    public long returnedReportCount(Office office) {
        return lostReportService.countReturnedForOffice(office);
    }
}
