package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.model.User;
import com.mawjood.service.LostReportService;
import com.mawjood.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the citizen dashboard at /dashboard.
 *
 * Shows the logged-in user their lost reports (as DTOs, not raw entities) and
 * their notifications. Only USER-role accounts can access this page; OFFICE
 * users are redirected to /office instead.
 *
 * Route: GET /dashboard
 */
@Controller
public class DashboardController {

    private final LostReportService lostReportService;
    private final NotificationService notificationService;

    public DashboardController(LostReportService lostReportService,
                               NotificationService notificationService) {
        this.lostReportService = lostReportService;
        this.notificationService = notificationService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User user = SessionHelper.getUser(session);
        if (user == null) return "redirect:/login";
        // Office and admin users have their own home pages.
        if (!SessionHelper.isUser(session)) return "redirect:/office";
        model.addAttribute("currentUser", user);
        // Convert entities to DTOs so templates never access sensitive entity data directly.
        model.addAttribute("reports", lostReportService.forUser(user).stream()
                .map(lostReportService::toDTO).toList());
        model.addAttribute("notifications", notificationService.getForUser(user));
        return "dashboard";
    }
}
