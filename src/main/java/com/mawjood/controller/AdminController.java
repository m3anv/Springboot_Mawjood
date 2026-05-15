package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.model.LostReport;
import com.mawjood.model.Role;
import com.mawjood.model.User;
import com.mawjood.repository.UserRepository;
import com.mawjood.repository.LocationRepository;
import com.mawjood.repository.OfficeRepository;
import com.mawjood.service.LostReportService;
import com.mawjood.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin dashboard and management actions (ADMIN role only).
 *
 * Provides a read-only overview of all users, locations, offices, and reports
 * on the platform. Admins can also delete any lost report if needed.
 *
 * All routes require an ADMIN-role session; non-admins are redirected to /login.
 *
 * Routes (under /admin):
 * <ul>
 *   <li>GET  /admin                       – main admin overview page</li>
 *   <li>POST /admin/reports/{id}/delete   – delete any lost report</li>
 * </ul>
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final OfficeRepository officeRepository;
    private final LostReportService lostReportService;
    private final NotificationService notificationService;

    public AdminController(UserRepository userRepository,
                           LocationRepository locationRepository,
                           OfficeRepository officeRepository,
                           LostReportService lostReportService,
                           NotificationService notificationService) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.officeRepository = officeRepository;
        this.lostReportService = lostReportService;
        this.notificationService = notificationService;
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    /**
     * Loads the admin overview page.
     *
     * @param filter optional query param: "all" (default), "user", or "office".
     *               Controls which rows appear in the Accounts table.
     *               Stats always reflect the total counts regardless of the filter.
     */
    @GetMapping
    public String adminPage(Model model, HttpSession session,
                            @RequestParam(defaultValue = "all") String filter) {
        if (!SessionHelper.isAdmin(session)) return "redirect:/login";

        List<User> allUsers = userRepository.findAll();

        // Apply the role filter for the accounts table only.
        List<User> filteredUsers = switch (filter) {
            case "user"   -> allUsers.stream().filter(u -> u.getRole() == Role.USER).toList();
            case "office" -> allUsers.stream().filter(u -> u.getRole() == Role.OFFICE || u.getRole() == Role.ADMIN).toList();
            default       -> allUsers;
        };

        model.addAttribute("currentUser",    SessionHelper.getUser(session));
        model.addAttribute("allUsers",       allUsers);       // used for stats
        model.addAttribute("filteredUsers",  filteredUsers);  // used for the accounts table
        model.addAttribute("filter",         filter);
        model.addAttribute("locations",      locationRepository.findAll());
        model.addAttribute("offices",        officeRepository.findAll());
        model.addAttribute("reports",        lostReportService.all());
        model.addAttribute("notifications",
                notificationService.getForUser(SessionHelper.getUser(session)));
        return "admin";
    }

    // ── Report Management ─────────────────────────────────────────────────────

    /** Deletes any report by ID (admin override, not restricted to report owner). */
    @PostMapping("/reports/{id}/delete")
    public String deleteReport(@PathVariable Long id, HttpSession session) {
        if (!SessionHelper.isAdmin(session)) return "redirect:/login";
        LostReport report = lostReportService.findById(id).orElseThrow();
        lostReportService.delete(report);
        return "redirect:/admin";
    }

    /** Placeholder for a future organization-approval flow; currently a no-op. */
    @PostMapping("/organizations/{id}/approve")
    public String approveOrg(@PathVariable Long id, HttpSession session) {
        return "redirect:/admin";
    }
}
