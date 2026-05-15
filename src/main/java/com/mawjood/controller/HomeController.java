package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.repository.LocationRepository;
import com.mawjood.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the public home page at the root URL.
 *
 * The home page lists all available locations so visitors can see which
 * Lost &amp; Found offices are on the platform before deciding to submit a
 * report. If a user is already logged in their unread notification count is
 * passed to the template for the nav badge.
 *
 * Route: GET /
 */
@Controller
public class HomeController {

    private final LocationRepository locationRepository;
    private final NotificationService notificationService;

    public HomeController(LocationRepository locationRepository, NotificationService notificationService) {
        this.locationRepository = locationRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        if (SessionHelper.isLoggedIn(session)) {
            model.addAttribute("unreadCount",
                    notificationService.unreadCount(SessionHelper.getUser(session)));
        }
        model.addAttribute("locations", locationRepository.findAll());
        return "index";
    }
}
