package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.model.*;
import com.mawjood.service.LookupService;
import com.mawjood.service.OfficeWorkflowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * All office-staff pages and actions (OFFICE role only).
 *
 * Covers the full office workflow: dashboard overview, lost-report queue,
 * individual case management, found-item inventory (create/edit/delete),
 * match management, and match status updates.
 *
 * Every handler first calls {@link #requireOffice} which validates the session
 * and loads the office entity. Non-office sessions are redirected to /login.
 *
 * Routes (all under /office):
 * <pre>
 * GET  /office                               – office dashboard
 * GET  /office/reports                       – full lost report queue
 * GET  /office/reports/{id}                  – single case details
 * POST /office/reports/{id}/status           – update report status
 * POST /office/reports/{id}/delete           – delete a report
 * GET  /office/found/new                     – add found item form
 * POST /office/found/new                     – save new found item
 * GET  /office/inventory                     – found inventory list
 * GET  /office/inventory/{id}/edit           – edit found item form
 * POST /office/inventory/{id}/edit           – save found item edits
 * POST /office/inventory/{id}/delete         – delete found item
 * GET  /office/matches                       – match management page
 * POST /office/reports/{rId}/matches/{fId}   – manually create a match
 * POST /office/matches/{id}/status           – update match status
 * </pre>
 */
@Controller
@RequestMapping("/office")
public class OfficeController {

    private final OfficeWorkflowService officeWorkflowService;
    private final LookupService lookupService;

    public OfficeController(OfficeWorkflowService officeWorkflowService, LookupService lookupService) {
        this.officeWorkflowService = officeWorkflowService;
        this.lookupService = lookupService;
    }

    // ── Office Dashboard ──────────────────────────────────────────────────────

    /** Loads the office home page with summary statistics and recent data. */
    @GetMapping
    public String dashboard(Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser",    SessionHelper.getUser(session));
        model.addAttribute("office",         office);
        model.addAttribute("reports",        officeWorkflowService.reportsForOffice(office));
        model.addAttribute("inventory",      officeWorkflowService.inventory(office));
        model.addAttribute("matches",        officeWorkflowService.matches(office));
        model.addAttribute("reportCount",    officeWorkflowService.reportCount(office));
        model.addAttribute("inventoryCount", officeWorkflowService.inventoryCount(office));
        model.addAttribute("matchCount",     officeWorkflowService.matchCount(office));
        // Combine returned reports + returned found items for the summary counter.
        model.addAttribute("returnedCount",
                officeWorkflowService.returnedReportCount(office) + officeWorkflowService.returnedCount(office));
        return "office-dashboard";
    }

    // ── Lost Report Queue ─────────────────────────────────────────────────────

    /** Shows all lost reports assigned to this office's location. */
    @GetMapping("/reports")
    public String reports(Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        model.addAttribute("office",      office);
        model.addAttribute("reports",     officeWorkflowService.reportsForOffice(office));
        return "office-reports";
    }

    /**
     * Shows the detailed case view for a single lost report, including all
     * case updates, existing matches, and possible matches from inventory.
     */
    @GetMapping("/reports/{id}")
    public String caseDetails(@PathVariable Long id, Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        LostReport report = officeWorkflowService.reportById(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(report, office)) return "redirect:/office";
        model.addAttribute("currentUser",     SessionHelper.getUser(session));
        model.addAttribute("office",          office);
        model.addAttribute("report",          report);
        model.addAttribute("updates",         officeWorkflowService.allCaseUpdates(report));
        model.addAttribute("possibleMatches", officeWorkflowService.possibleMatches(office, report));
        model.addAttribute("matches",         officeWorkflowService.matchesForReport(report));
        model.addAttribute("statuses",        ReportStatus.values());
        return "office-case";
    }

    /**
     * Updates the status of a lost report and records a case update message.
     * Redirects back to the case details page after saving.
     */
    @PostMapping("/reports/{id}/status")
    public String updateReport(@PathVariable Long id,
                               @RequestParam ReportStatus status,
                               @RequestParam String message,
                               @RequestParam(defaultValue = "true") boolean visibleToUser,
                               HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        LostReport report = officeWorkflowService.reportById(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(report, office)) return "redirect:/office";
        officeWorkflowService.updateReportStatus(report, office,
                SessionHelper.getUser(session), status, message, visibleToUser);
        return "redirect:/office/reports/" + id;
    }

    /** Deletes a report and all its related data, then redirects to the reports list. */
    @PostMapping("/reports/{id}/delete")
    public String deleteReport(@PathVariable Long id, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        LostReport report = officeWorkflowService.reportById(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(report, office)) return "redirect:/office";
        officeWorkflowService.deleteReport(report);
        return "redirect:/office/reports";
    }

    // ── Found Item – Create ───────────────────────────────────────────────────

    /** Shows the form for adding a new found item to the office inventory. */
    @GetMapping("/found/new")
    public String foundForm(Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        model.addAttribute("office",      office);
        addFormLookups(model);
        return "office-found-form";
    }

    /**
     * Validates and saves a new found item. On success, auto-match detection
     * runs immediately and the user is redirected to the inventory list.
     */
    @PostMapping("/found/new")
    public String createFound(@RequestParam String itemName,
                              @RequestParam String category,
                              @RequestParam(required = false) String color,
                              @RequestParam(required = false) String description,
                              @RequestParam String foundDate,
                              @RequestParam String foundArea,
                              @RequestParam(required = false) String storageReference,
                              Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser",      SessionHelper.getUser(session));
        model.addAttribute("office",           office);
        addFormLookups(model);
        // Preserve entered values for form re-render on error.
        model.addAttribute("itemName",         clean(itemName));
        model.addAttribute("category",         lookupService.normalize(category));
        model.addAttribute("color",            lookupService.normalize(color));
        model.addAttribute("description",      clean(description));
        model.addAttribute("foundDate",        clean(foundDate));
        model.addAttribute("foundArea",        clean(foundArea));
        model.addAttribute("storageReference", clean(storageReference));

        if (isBlank(itemName) || isBlank(category) || isBlank(color)
                || isBlank(foundDate) || isBlank(foundArea)) {
            model.addAttribute("error",
                    "Item name, category, color, found date, and found area are required.");
            return "office-found-form";
        }
        if (!lookupService.isValidCategory(category) || !lookupService.isValidColor(color)) {
            model.addAttribute("error", "Please choose a valid category and color from the lists.");
            return "office-found-form";
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(foundDate);
        } catch (DateTimeParseException ex) {
            model.addAttribute("error", "Please enter a valid found date.");
            return "office-found-form";
        }

        officeWorkflowService.createFoundItem(office, SessionHelper.getUser(session),
                clean(itemName), lookupService.normalize(category), lookupService.normalize(color),
                clean(description), parsedDate, clean(foundArea), clean(storageReference));
        return "redirect:/office/inventory";
    }

    // ── Found Item – Inventory List ───────────────────────────────────────────

    /** Shows all found items in the office's inventory. */
    @GetMapping("/inventory")
    public String inventory(Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        model.addAttribute("office",      office);
        model.addAttribute("inventory",   officeWorkflowService.inventory(office));
        return "office-inventory";
    }

    // ── Found Item – Edit ─────────────────────────────────────────────────────

    /**
     * Shows the edit form for an existing found item, pre-filled with current
     * values. Only the office that owns the item can edit it.
     */
    @GetMapping("/inventory/{id}/edit")
    public String editFoundForm(@PathVariable Long id, Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        FoundItem item = officeWorkflowService.foundItem(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(item, office)) return "redirect:/office/inventory";
        model.addAttribute("currentUser",      SessionHelper.getUser(session));
        model.addAttribute("office",           office);
        model.addAttribute("item",             item);
        // Pre-populate form fields with the current item values.
        model.addAttribute("itemName",         item.getItemName());
        model.addAttribute("category",         item.getCategory());
        model.addAttribute("color",            item.getColor());
        model.addAttribute("description",      item.getDescription());
        model.addAttribute("foundDate",        item.getFoundDate());
        model.addAttribute("foundArea",        item.getFoundArea());
        model.addAttribute("storageReference", item.getStorageReference());
        addFormLookups(model);
        return "office-found-edit-form";
    }

    /**
     * Validates and saves edits to an existing found item, then redirects to
     * the inventory list.
     */
    @PostMapping("/inventory/{id}/edit")
    public String updateFoundItem(@PathVariable Long id,
                                  @RequestParam String itemName,
                                  @RequestParam String category,
                                  @RequestParam(required = false) String color,
                                  @RequestParam(required = false) String description,
                                  @RequestParam String foundDate,
                                  @RequestParam String foundArea,
                                  @RequestParam(required = false) String storageReference,
                                  Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        FoundItem item = officeWorkflowService.foundItem(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(item, office)) return "redirect:/office/inventory";
        model.addAttribute("currentUser",      SessionHelper.getUser(session));
        model.addAttribute("office",           office);
        model.addAttribute("item",             item);
        model.addAttribute("itemName",         clean(itemName));
        model.addAttribute("category",         lookupService.normalize(category));
        model.addAttribute("color",            lookupService.normalize(color));
        model.addAttribute("description",      clean(description));
        model.addAttribute("foundDate",        clean(foundDate));
        model.addAttribute("foundArea",        clean(foundArea));
        model.addAttribute("storageReference", clean(storageReference));
        addFormLookups(model);

        if (isBlank(itemName) || isBlank(category) || isBlank(color)
                || isBlank(foundDate) || isBlank(foundArea)) {
            model.addAttribute("error",
                    "Item name, category, color, found date, and found area are required.");
            return "office-found-edit-form";
        }
        if (!lookupService.isValidCategory(category) || !lookupService.isValidColor(color)) {
            model.addAttribute("error", "Please choose a valid category and color from the lists.");
            return "office-found-edit-form";
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(foundDate);
        } catch (DateTimeParseException ex) {
            model.addAttribute("error", "Please enter a valid found date.");
            return "office-found-edit-form";
        }

        officeWorkflowService.updateFoundItem(item, clean(itemName),
                lookupService.normalize(category), lookupService.normalize(color),
                clean(description), parsedDate, clean(foundArea), clean(storageReference));
        return "redirect:/office/inventory";
    }

    // ── Found Item – Delete ───────────────────────────────────────────────────

    /** Deletes a found item and its related matches, then redirects to inventory. */
    @PostMapping("/inventory/{id}/delete")
    public String deleteInventoryItem(@PathVariable Long id, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        FoundItem item = officeWorkflowService.foundItem(id).orElseThrow();
        if (!officeWorkflowService.belongsToOffice(item, office)) return "redirect:/office/inventory";
        officeWorkflowService.deleteFoundItem(item);
        return "redirect:/office/inventory";
    }

    // ── Match Management ──────────────────────────────────────────────────────

    /** Shows all matches for this office with full lost/found item details for comparison. */
    @GetMapping("/matches")
    public String matches(Model model, HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        model.addAttribute("office",      office);
        model.addAttribute("matches",     officeWorkflowService.matches(office));
        model.addAttribute("statuses",    MatchStatus.values());
        return "office-matches";
    }

    /**
     * Manually creates a match between a specific report and found item.
     * Also advances the report status to POSSIBLE_MATCH and redirects back to
     * the case details page.
     */
    @PostMapping("/reports/{reportId}/matches/{foundItemId}")
    public String createMatch(@PathVariable Long reportId,
                              @PathVariable Long foundItemId,
                              @RequestParam(required = false) String notes,
                              HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        LostReport report = officeWorkflowService.reportById(reportId).orElseThrow();
        FoundItem item    = officeWorkflowService.foundItem(foundItemId).orElseThrow();
        // Both the report and the item must belong to this office.
        if (!officeWorkflowService.belongsToOffice(report, office)
                || !item.getOffice().getId().equals(office.getId())) {
            return "redirect:/office";
        }
        officeWorkflowService.createMatch(office, report, item, notes);
        officeWorkflowService.updateReportStatus(report, office, SessionHelper.getUser(session),
                ReportStatus.POSSIBLE_MATCH,
                "The office is reviewing a possible match for your report.", true);
        return "redirect:/office/reports/" + reportId;
    }

    /**
     * Updates a match's status and optional notes. If the match is confirmed
     * (MATCHED) or returned (RETURNED), the linked lost report status is also
     * advanced automatically.
     */
    @PostMapping("/matches/{id}/status")
    public String updateMatch(@PathVariable Long id,
                              @RequestParam MatchStatus status,
                              @RequestParam(required = false) String notes,
                              HttpSession session) {
        Office office = requireOffice(session);
        if (office == null) return "redirect:/login";
        CaseMatch match = officeWorkflowService.match(id).orElseThrow();
        if (!match.getOffice().getId().equals(office.getId())) return "redirect:/office";
        officeWorkflowService.updateMatch(match, status, notes);
        // Mirror the match milestone on the linked lost report.
        if (status == MatchStatus.MATCHED) {
            officeWorkflowService.updateReportStatus(match.getLostReport(), office,
                    SessionHelper.getUser(session), ReportStatus.MATCHED,
                    "The office confirmed a match and will coordinate the return process.", true);
        }
        if (status == MatchStatus.RETURNED) {
            officeWorkflowService.updateReportStatus(match.getLostReport(), office,
                    SessionHelper.getUser(session), ReportStatus.RETURNED,
                    "The item has been marked as returned by the Lost & Found Office.", true);
        }
        return "redirect:/office/matches";
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    /**
     * Validates the session and loads the office entity. Returns null if the
     * session is not an active OFFICE session, which causes the calling handler
     * to redirect to /login.
     */
    private Office requireOffice(HttpSession session) {
        if (!SessionHelper.isOffice(session)) return null;
        return officeWorkflowService.officeFor(SessionHelper.getUser(session)).orElse(null);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Adds category and color lists to the model for form dropdowns. */
    private void addFormLookups(Model model) {
        model.addAttribute("categories", lookupService.categories());
        model.addAttribute("colors",     lookupService.colors());
    }
}
