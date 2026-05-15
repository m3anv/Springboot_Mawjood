package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Legacy redirect controller for the old /items URL structure.
 *
 * All actual item/report functionality has been moved to
 * {@link LostReportController} under /reports. These mappings exist only to
 * prevent broken links from old bookmarks or external references.
 */
@Controller
@RequestMapping("/items")
public class ItemController {

    /** Redirects old "create item" URL to the current lost-report form. */
    @GetMapping("/new")
    public String createPage(Model model, HttpSession session) {
        if (!SessionHelper.isUser(session)) return "redirect:/login";
        return "redirect:/reports/new";
    }

    @PostMapping("/new")
    public String create(HttpSession session) {
        if (!SessionHelper.isUser(session)) return "redirect:/login";
        return "redirect:/reports/new";
    }

    /** Redirects old item-detail URL to the current report-detail page. */
    @GetMapping("/{id}")
    public String details(@PathVariable Long id) {
        return "redirect:/reports/" + id;
    }

    /** Search feature was never built; redirects to dashboard. */
    @GetMapping("/search")
    public String searchPage() {
        return "redirect:/dashboard";
    }

    /** Legacy contact endpoint removed; redirects to dashboard. */
    @PostMapping("/{id}/contact")
    public String disabledLegacyContact() {
        return "redirect:/dashboard";
    }
}
