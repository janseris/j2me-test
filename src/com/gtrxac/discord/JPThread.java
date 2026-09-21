package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Runs one JSONPlaceholder API call (see JPApi, JPCallback) on a background thread, so
 * the request never blocks the LCDUI event thread. Shows a small loading screen while
 * the request is in flight, and a dismissable error alert (then back to
 * errorReturnScreen) if it fails.
 */
public class JPThread extends Thread {
    private JPCallback callback;
    private Displayable errorReturnScreen;

    public JPThread(JPCallback callback, Displayable errorReturnScreen) {
        this.callback = callback;
        this.errorReturnScreen = errorReturnScreen;
    }

    public void run() {
        App.disp.setCurrent(new JPLoadingScreen());
        try {
            Object result = callback.request();
            callback.onSuccess(result);
        }
        catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert("Request failed", e.toString(), null, AlertType.ERROR);
            alert.setTimeout(Alert.FOREVER);
            App.disp.disp.setCurrent(alert, errorReturnScreen);
        }
    }
}
