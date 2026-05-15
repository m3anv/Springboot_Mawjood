// ── Navigation Toggle ────────────────────────────────────────────────────────
// Handles the mobile hamburger menu open/close behaviour.
// The nav fragment uses data-nav-toggle (button) and data-nav-menu (collapsible
// list) attributes so this script works on every page without any extra setup.

document.addEventListener("DOMContentLoaded", function () {
    const toggle = document.querySelector("[data-nav-toggle]");
    const menu   = document.querySelector("[data-nav-menu]");

    if (toggle && menu) {
        toggle.addEventListener("click", function () {
            // Toggle the is-open class on both elements so CSS can animate the menu.
            const isOpen = menu.classList.toggle("is-open");
            toggle.classList.toggle("is-open", isOpen);
            // Update aria-expanded for screen-reader accessibility.
            toggle.setAttribute("aria-expanded", String(isOpen));
        });
    }
});
