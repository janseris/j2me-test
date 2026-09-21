package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Minimal "please wait" screen shown by JPThread while a JSONPlaceholder request is
 * in flight. Deliberately independent from the Discord app's own LoadingScreen/Theme/
 * font system, since these screens are meant to be simple and self-contained.
 */
public class JPLoadingScreen extends Form {
    public JPLoadingScreen() {
        super("Loading...");
        append(new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING));
    }
}
