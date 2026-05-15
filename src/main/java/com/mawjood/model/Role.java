package com.mawjood.model;

/**
 * The three access roles in the platform.
 *
 * <ul>
 *   <li>{@code USER}   – a citizen who submits lost reports and tracks status.</li>
 *   <li>{@code OFFICE} – staff at a Lost &amp; Found office; manages inventory,
 *                        case updates, and matches for their location.</li>
 *   <li>{@code ADMIN}  – platform administrator; sees all data and receives
 *                        match notifications.</li>
 * </ul>
 */
public enum Role {
    USER,
    OFFICE,
    ADMIN
}
