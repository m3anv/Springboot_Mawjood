package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.model.LostReport;
import com.mawjood.model.User;
import com.mawjood.repository.LocationRepository;
import com.mawjood.service.LookupService;
import com.mawjood.service.LostReportService;
import com.mawjood.service.OfficeWorkflowService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Handles citizen-facing lost report submission, details, and deletion.
 *
 * All routes require a USER-role session; anything else redirects to /login.
 * After a report is saved, auto-match detection runs immediately so the report
 * may already have a POSSIBLE_MATCH status before the user sees the details page.
 *
 * Routes (all under /reports):
 * <ul>
 *   <li>GET  /reports/new     – show the lost report submission form</li>
 *   <li>POST /reports/new     – validate and create a new lost report</li>
 *   <li>GET  /reports/{id}    – view the details and timeline for one report</li>
 *   <li>POST /reports/{id}/delete – delete a report (owner only)</li>
 * </ul>
 */
@Controller
@RequestMapping("/reports")
public class LostReportController {

    private final LostReportService lostReportService;
    private final LocationRepository locationRepository;
    private final LookupService lookupService;
    private final OfficeWorkflowService officeWorkflowService;

    public LostReportController(LostReportService lostReportService,
                                LocationRepository locationRepository,
                                LookupService lookupService,
                                OfficeWorkflowService officeWorkflowService) {
        this.lostReportService = lostReportService;
        this.locationRepository = locationRepository;
        this.lookupService = lookupService;
        this.officeWorkflowService = officeWorkflowService;
    }

    // ── New Report Form ───────────────────────────────────────────────────────

    @GetMapping("/new")
    public String newReport(Model model, HttpSession session) {
        if (!SessionHelper.isUser(session)) return "redirect:/login";
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        addFormLookups(model);
        return "report-lost";
    }

    /**
     * Validates and saves a new lost report.
     *
     * Re-renders the form with the submitted values and an error message if
     * validation fails. On success, runs auto-match detection and redirects to
     * the new report's details page.
     */
    @PostMapping("/new")
    public String create(@RequestParam Long locationId,
                         @RequestParam String itemName,
                         @RequestParam String category,
                         @RequestParam(required = false) String color,
                         @RequestParam(required = false) String description,
                         @RequestParam String lostDate,
                         @RequestParam String lostArea,
                         @RequestParam String contactInfo,
                         Model model, HttpSession session) {
        User user = SessionHelper.getUser(session);
        if (!SessionHelper.isUser(session)) return "redirect:/login";

        // Preserve entered values so the form is not cleared on validation error.
        model.addAttribute("currentUser", user);
        addFormLookups(model);
        model.addAttribute("itemName",    clean(itemName));
        model.addAttribute("category",    lookupService.normalize(category));
        model.addAttribute("color",       lookupService.normalize(color));
        model.addAttribute("description", clean(description));
        model.addAttribute("lostDate",    clean(lostDate));
        model.addAttribute("lostArea",    clean(lostArea));
        model.addAttribute("contactInfo", clean(contactInfo));
        model.addAttribute("locationId",  locationId);

        // Required field check.
        if (locationId == null || isBlank(itemName) || isBlank(category) || isBlank(color)
                || isBlank(lostDate) || isBlank(lostArea) || isBlank(contactInfo)) {
            model.addAttribute("error",
                    "Location, item name, category, color, date, area, and contact info are required.");
            return "report-lost";
        }
        if (!lookupService.isValidCategory(category) || !lookupService.isValidColor(color)) {
            model.addAttribute("error", "Please choose a valid category and color from the lists.");
            return "report-lost";
        }

        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(lostDate);
        } catch (DateTimeParseException ex) {
            model.addAttribute("error", "Please enter a valid lost date.");
            return "report-lost";
        }

        LostReport report = lostReportService.create(user, locationId,
                clean(itemName), lookupService.normalize(category), lookupService.normalize(color),
                clean(description), parsedDate, clean(lostArea), clean(contactInfo));
        // Trigger immediate match detection so the report status may already
        // reflect a possible match when the user lands on the details page.
        officeWorkflowService.autoDetectMatchesForReport(report);
        return "redirect:/reports/" + report.getId();
    }

    // ── Report Details ────────────────────────────────────────────────────────

    /**
     * Shows the details and citizen-visible timeline for a single report.
     * Only the owner of the report may access this page.
     */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model, HttpSession session) {
        User user = SessionHelper.getUser(session);
        if (!SessionHelper.isUser(session)) return "redirect:/login";
        LostReport report = lostReportService.findById(id).orElseThrow();
        if (!lostReportService.belongsToUser(report, user)) return "redirect:/dashboard";
        model.addAttribute("currentUser", user);
        model.addAttribute("report",  lostReportService.toDTO(report));
        model.addAttribute("updates", lostReportService.visibleUpdates(report));
        return "report-details";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Deletes a report along with its matches and updates, then redirects to
     * the dashboard. Only the owner can delete their own report.
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = SessionHelper.getUser(session);
        if (!SessionHelper.isUser(session)) return "redirect:/login";
        LostReport report = lostReportService.findById(id).orElseThrow();
        if (!lostReportService.belongsToUser(report, user)) return "redirect:/dashboard";
        lostReportService.delete(report);
        return "redirect:/dashboard";
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** Adds the location list and lookup lists to the model for form dropdowns. */
    private void addFormLookups(Model model) {
        model.addAttribute("locations",  locationRepository.findAll());
        model.addAttribute("categories", lookupService.categories());
        model.addAttribute("colors",     lookupService.colors());
    }
}
