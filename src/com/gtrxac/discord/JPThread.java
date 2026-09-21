package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Runs one JSONPlaceholder API call (see JPApi, JPCallback) on a background thread, so
 * the request never blocks the LCDUI event thread.
 *
 * If errorReturnScreen implements JPLoadingHost (currently just JPStartScreen), it's
 * asked to show its own inline loading overlay instead of navigating away - the screen
 * stays exactly where it is and only its own paint() changes. Otherwise (the List/
 * Form-based JP screens, which can't host an overlay on top of a native widget), this
 * falls back to switching to JPLoadingScreen, a small full-screen loading animation.
 *
 * Either way, a dismissable error alert (then back to errorReturnScreen) is shown if
 * the request fails.
 */
public class JPThread extends Thread {
    private JPCallback callback;
    private Displayable errorReturnScreen;

    public JPThread(JPCallback callback, Displayable errorReturnScreen) {
        this.callback = callback;
        this.errorReturnScreen = errorReturnScreen;
    }

    public void run() {
        JPLoadingHost host = (errorReturnScreen instanceof JPLoadingHost)
            ? (JPLoadingHost) errorReturnScreen : null;

        if (host != null) {
            host.setLoading(true);
        }
        else {
            App.disp.setCurrent(new JPLoadingScreen());
        }

        try {
            Object result = callback.request();
            if (host != null) host.setLoading(false);
            callback.onSuccess(result);
        }
        catch (Exception e) {
            if (host != null) host.setLoading(false);
            e.printStackTrace();
            Alert alert = new Alert("Request failed", e.toString(), null, AlertType.ERROR);
            alert.setTimeout(Alert.FOREVER);
            App.disp.disp.setCurrent(alert, errorReturnScreen);
        }
    }
}
