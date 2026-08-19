package com.idss.common.config;

/**
 * Shared UI theme constants for the IDSS shell (master_context_file.md Section 4).
 * Every module screen uses these so Group Report screenshots look like one system.
 */
public final class Theme {

    public static final String PRIMARY = "#0F4C75"; // headers, primary buttons
    public static final String ACCENT  = "#3282B8"; // links, highlights
    public static final String BG      = "#F5F7FA"; // background
    public static final String CARD    = "#FFFFFF"; // cards
    public static final String TEXT    = "#1B262C";
    public static final String SUCCESS = "#2E7D32";
    public static final String WARNING = "#EF6C00";
    public static final String FONT    = "Segoe UI / Inter, 14px";

    private Theme() {
        throw new AssertionError("Theme is a constants holder; do not instantiate.");
    }
}
