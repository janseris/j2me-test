package com.gtrxac.discord;

/**
 * Implemented by a JP screen that can show its own inline loading animation - see
 * JPStartScreen - instead of having JPThread switch away to the separate
 * JPLoadingScreen while a request is in flight.
 *
 * Only a plain Canvas can do this (it owns its own paint()); the List/Form-based JP
 * screens can't draw over a native widget, so they don't implement this and JPThread
 * falls back to JPLoadingScreen for them.
 */
public interface JPLoadingHost {
    void setLoading(boolean loading);
}
