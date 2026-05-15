package com.mawjood.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides the fixed lists of categories and colors used throughout the platform.
 *
 * Both lists are stored as uppercase English codes (e.g. "PHONE", "BLACK").
 * The same codes are used in the database, Arabic UI, and English UI — the
 * Thymeleaf templates translate them to the display language via message keys
 * like {@code category.PHONE} and {@code color.BLACK}.
 *
 * Storing codes instead of raw text means an Arabic user submitting "هاتف" and
 * an English user submitting "Phone" both produce the code "PHONE", so the
 * matching algorithm can compare them correctly.
 */
@Service
public class LookupService {

    // ── Fixed Lookup Lists ────────────────────────────────────────────────────

    private static final List<String> CATEGORIES = List.of(
            "WALLET",
            "PHONE",
            "KEYS",
            "BAG",
            "LAPTOP",
            "WATCH",
            "GLASSES",
            "DOCUMENTS",
            "JEWELRY",
            "CLOTHING",
            "OTHER"
    );

    private static final List<String> COLORS = List.of(
            "BLACK",
            "WHITE",
            "GRAY",
            "SILVER",
            "GOLD",
            "BROWN",
            "RED",
            "BLUE",
            "GREEN",
            "YELLOW",
            "ORANGE",
            "PINK",
            "PURPLE",
            "MULTICOLOR",
            "UNKNOWN"
    );

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Returns the full list of valid categories for use in form dropdowns. */
    public List<String> categories() {
        return CATEGORIES;
    }

    /** Returns the full list of valid colors for use in form dropdowns. */
    public List<String> colors() {
        return COLORS;
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** Returns true if the given string (after normalisation) is a known category. */
    public boolean isValidCategory(String category) {
        return CATEGORIES.contains(normalize(category));
    }

    /**
     * Returns true if the color is blank (allowed — user may not know) or
     * matches a known color code.
     */
    public boolean isValidColor(String color) {
        String normalized = normalize(color);
        return normalized.isBlank() || COLORS.contains(normalized);
    }

    // ── Normalisation ─────────────────────────────────────────────────────────

    /**
     * Converts a raw input value into the standard uppercase code format.
     * Trims whitespace, uppercases all letters, and replaces spaces with
     * underscores so multi-word inputs like "laptop bag" become "LAPTOP_BAG".
     */
    public String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase().replace(' ', '_');
    }
}
