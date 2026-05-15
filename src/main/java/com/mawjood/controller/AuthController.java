package com.mawjood.controller;

import com.mawjood.config.SessionHelper;
import com.mawjood.model.Role;
import com.mawjood.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user authentication: login, logout, and new user registration.
 *
 * Routes:
 * <ul>
 *   <li>GET  /login          – show login form</li>
 *   <li>POST /login          – validate credentials and start session</li>
 *   <li>GET  /register/user  – show user registration form</li>
 *   <li>POST /register/user  – create new USER account</li>
 *   <li>GET  /logout         – invalidate session and redirect to home</li>
 * </ul>
 */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        return "login";
    }

    /**
     * Attempts login with the submitted credentials.
     * On success, stores the User in the session and redirects based on role.
     * On failure, re-renders the login page with an error message.
     */
    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpSession session, Model model) {
        String cleanEmail = email == null ? "" : email.trim();
        return authService.login(cleanEmail, password)
                .map(user -> {
                    session.setAttribute("user", user);
                    // Redirect each role to its own home page after login.
                    if (user.getRole() == Role.OFFICE) return "redirect:/office";
                    if (user.getRole() == Role.ADMIN)  return "redirect:/admin";
                    return "redirect:/dashboard";
                })
                .orElseGet(() -> {
                    model.addAttribute("currentUser", SessionHelper.getUser(session));
                    model.addAttribute("email", cleanEmail);
                    model.addAttribute("error", "Invalid email or password.");
                    return "login";
                });
    }

    // ── Registration ──────────────────────────────────────────────────────────

    @GetMapping("/register")
    public String registerPage() {
        return "redirect:/register/user";
    }

    @GetMapping("/register/user")
    public String userRegisterPage(Model model, HttpSession session) {
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        return "register-user";
    }

    /**
     * Creates a new USER account after validating name, email, and password.
     * Redirects to /login on success.
     */
    @PostMapping("/register/user")
    public String registerUser(@RequestParam String name, @RequestParam String email,
                               @RequestParam String password, Model model, HttpSession session) {
        String cleanName  = clean(name);
        String cleanEmail = clean(email);
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        model.addAttribute("name", cleanName);
        model.addAttribute("email", cleanEmail);

        if (isBlank(cleanName) || isBlank(cleanEmail) || isBlank(password)) {
            model.addAttribute("error", "Name, email, and password are required.");
            return "register-user";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters.");
            return "register-user";
        }
        if (authService.emailExists(cleanEmail)) {
            model.addAttribute("error", "Email already exists.");
            return "register-user";
        }
        authService.registerUser(cleanName, cleanEmail, password);
        return "redirect:/login";
    }

    /** Legacy routes redirect to login; office accounts are issued by admin only. */
    @GetMapping({"/register/organization", "/office/login"})
    public String officeEntry(Model model, HttpSession session) {
        model.addAttribute("currentUser", SessionHelper.getUser(session));
        return "redirect:/login";
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /** Destroys the session and redirects the user to the home page. */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    // ── Internal Helpers ──────────────────────────────────────────────────────

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
