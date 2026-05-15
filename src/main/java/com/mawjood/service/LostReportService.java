package com.mawjood.service;

import com.mawjood.dto.LostReportDTO;
import com.mawjood.model.*;
import com.mawjood.repository.CaseMatchRepository;
import com.mawjood.repository.CaseUpdateRepository;
import com.mawjood.repository.LocationRepository;
import com.mawjood.repository.LostReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for citizen-submitted {@link LostReport} records.
 *
 * Handles the full lifecycle of a lost report: creation, status updates,
 * visible timeline entries (CaseUpdates), and deletion. This service is
 * also used by {@link OfficeWorkflowService} as a delegate for report
 * operations so that all report-related logic stays in one place.
 */
@Service
public class LostReportService {

    private final LostReportRepository lostReportRepository;
    private final LocationRepository locationRepository;
    private final CaseMatchRepository caseMatchRepository;
    private final CaseUpdateRepository caseUpdateRepository;

    public LostReportService(LostReportRepository lostReportRepository,
                             LocationRepository locationRepository,
                             CaseMatchRepository caseMatchRepository,
                             CaseUpdateRepository caseUpdateRepository) {
        this.lostReportRepository = lostReportRepository;
        this.locationRepository = locationRepository;
        this.caseMatchRepository = caseMatchRepository;
        this.caseUpdateRepository = caseUpdateRepository;
    }

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Creates a new lost report and records the initial "received" case update.
     *
     * @param user       the citizen submitting the report
     * @param locationId the location where the item was lost
     * @return the saved LostReport entity
     */
    public LostReport create(User user, Long locationId, String itemName, String category, String color,
                             String description, LocalDate lostDate, String lostArea, String contactInfo) {
        Location location = locationRepository.findById(locationId).orElseThrow();
        LostReport report = new LostReport();
        report.setUser(user);
        report.setLocation(location);
        report.setItemName(itemName);
        report.setCategory(category);
        report.setColor(color);
        report.setDescription(description);
        report.setLostDate(lostDate);
        report.setLostArea(lostArea);
        report.setContactInfo(contactInfo);
        LostReport saved = lostReportRepository.save(report);
        // Automatically create the first case update confirming receipt.
        addUpdate(saved, null, null, ReportStatus.SUBMITTED,
                "Your lost report was received by " + location.getName() + " Lost & Found Office.", true);
        return saved;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    /** Returns all reports submitted by a specific user, newest first. */
    public List<LostReport> forUser(User user) {
        return lostReportRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /** Returns all reports routed to an office's location, newest first. */
    public List<LostReport> forOffice(Office office) {
        return lostReportRepository.findByLocationOrderByCreatedAtDesc(office.getLocation());
    }

    /** Returns every report in the system; used by the admin dashboard. */
    public List<LostReport> all() {
        return lostReportRepository.findAll();
    }

    /** Looks up a single report by its primary key. */
    public Optional<LostReport> findById(Long id) {
        return lostReportRepository.findById(id);
    }

    // ── Ownership Checks ──────────────────────────────────────────────────────

    /** Returns true if the report was submitted by the given user; prevents
     *  one user from viewing another user's report. */
    public boolean belongsToUser(LostReport report, User user) {
        return report.getUser().getId().equals(user.getId());
    }

    /** Returns true if the report's location belongs to the given office; prevents
     *  one office from accessing another office's cases. */
    public boolean belongsToOffice(LostReport report, Office office) {
        return report.getLocation().getId().equals(office.getLocation().getId());
    }

    // ── Status Updates ────────────────────────────────────────────────────────

    /**
     * Advances the report to a new status and records a CaseUpdate timeline entry.
     *
     * @param office        the office making the change (null for system actions)
     * @param actor         the staff member making the change (null for automated)
     * @param visibleToUser when true the update is shown to the citizen
     * @throws IllegalStateException if the transition is not allowed by
     *         {@link ReportStatus#canTransitionTo}
     */
    public LostReport updateStatus(LostReport report, Office office, User actor,
                                   ReportStatus status, String message, boolean visibleToUser) {
        if (!report.getStatus().canTransitionTo(status)) {
            throw new IllegalStateException(
                    "Cannot transition report from " + report.getStatus() + " to " + status);
        }
        report.setStatus(status);
        report.setUpdatedAt(LocalDateTime.now());
        LostReport saved = lostReportRepository.save(report);
        addUpdate(saved, office, actor, status, message, visibleToUser);
        return saved;
    }

    // ── Timeline Queries ──────────────────────────────────────────────────────

    /** Returns only the updates that the citizen can see (visible timeline). */
    public List<CaseUpdate> visibleUpdates(LostReport report) {
        return caseUpdateRepository.findByLostReportAndVisibleToUserOrderByCreatedAtDesc(report, true);
    }

    /** Returns all updates including internal office notes (used by office views). */
    public List<CaseUpdate> allUpdates(LostReport report) {
        return caseUpdateRepository.findByLostReportOrderByCreatedAtDesc(report);
    }

    // ── Dashboard Stats ───────────────────────────────────────────────────────

    /** Total reports at an office location; used on the office dashboard. */
    public long countForOffice(Office office) {
        return lostReportRepository.countByLocation(office.getLocation());
    }

    /** Number of RETURNED reports at an office location. */
    public long countReturnedForOffice(Office office) {
        return lostReportRepository.countByLocationAndStatus(office.getLocation(), ReportStatus.RETURNED);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Deletes a report along with all its associated matches and case updates.
     * Wrapped in a transaction so all three deletions succeed or fail together.
     */
    @Transactional
    public void delete(LostReport report) {
        caseMatchRepository.deleteByLostReport(report);
        caseUpdateRepository.deleteByLostReport(report);
        lostReportRepository.delete(report);
    }

    // ── DTO Conversion ────────────────────────────────────────────────────────

    /** Converts a LostReport entity to the citizen-safe DTO used in templates. */
    public LostReportDTO toDTO(LostReport report) {
        return new LostReportDTO(
                report.getId(),
                report.getItemName(),
                report.getCategory(),
                report.getColor(),
                report.getDescription(),
                report.getLostDate(),
                report.getLostArea(),
                report.getStatus(),
                report.getLocation().getName()
        );
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    /**
     * Creates and saves a CaseUpdate entry attached to the given report.
     * If no message is supplied a default "Status updated to X" message is used.
     */
    private void addUpdate(LostReport report, Office office, User actor,
                           ReportStatus status, String message, boolean visibleToUser) {
        CaseUpdate update = new CaseUpdate();
        update.setLostReport(report);
        update.setOffice(office);
        update.setCreatedBy(actor);
        update.setStatus(status);
        update.setMessage(message == null || message.isBlank()
                ? "Status updated to " + status.name() + "."
                : message.trim());
        update.setVisibleToUser(visibleToUser);
        caseUpdateRepository.save(update);
    }
}
