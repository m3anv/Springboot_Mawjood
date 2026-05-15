package com.mawjood.config;

import com.mawjood.model.Role;
import com.mawjood.model.User;
import jakarta.servlet.http.HttpSession;

/**
 * Utility class for reading authentication state from the HTTP session.
 *
 * Mawjood does not use Spring Security; instead, the logged-in {@link User}
 * object is stored directly in the session under the key {@code "user"} when
 * login succeeds. Every controller calls these helpers to check who is logged
 * in and what role they hold before handling a request.
 *
 * All methods are static so they can be called without injecting this class.
 */
public class SessionHelper {

    // ── Session Access ────────────────────────────────────────────────────────

    /**
     * Returns the logged-in user from the session, or {@code null} if nobody
     * is logged in.
     */
    public static User getUser(HttpSession session) {
        Object user = session.getAttribute("user");
        return user instanceof User ? (User) user : null;
    }

    // ── Role Checks ───────────────────────────────────────────────────────────

    /** Returns true if the session belongs to a logged-in ADMIN user. */
    public static boolean isAdmin(HttpSession session) {
        User user = getUser(session);
        return user != null && user.getRole() == Role.ADMIN;
    }

    /**
     * Returns true if the session belongs to an OFFICE user who has an office
     * assigned. Both conditions must be true to access office routes.
     */
    public static boolean isOffice(HttpSession session) {
        User user = getUser(session);
        return user != null && user.getRole() == Role.OFFICE && user.getOffice() != null;
    }

    /** Returns true if the session belongs to a regular USER. */
    public static boolean isUser(HttpSession session) {
        User user = getUser(session);
        return user != null && user.getRole() == Role.USER;
    }

    /** Returns true if any user is currently logged in. */
    public static boolean isLoggedIn(HttpSession session) {
        return getUser(session) != null;
    }
}
