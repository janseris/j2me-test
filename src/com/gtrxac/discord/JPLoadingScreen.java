package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * Full-screen loading animation shown by JPThread while a JSONPlaceholder request is
 * in flight, for screens that can't host their own inline loading overlay (the List/
 * Form-based JP screens - a native List or Form can't have custom graphics drawn over
 * it). JPStartScreen is a plain Canvas and instead shows the same spinner (see
 * JPSpinner) as an overlay on itself, staying put rather than switching to this screen
 * - see JPThread and JPLoadingHost.
 *
 * Deliberately independent from the Discord app's own LoadingScreen/Theme/font
 * system, since these screens are meant to be simple and self-contained.
 */
public class JPLoadingScreen extends Canvas {
    private static final int BG_COLOR = 0x2B2D31;
    private static final int SPINNER_COLOR = 0x5865F2;
    private static final int TEXT_COLOR = 0xB5BAC1;

    private boolean running = true;
    private int frame = 0;

    public JPLoadingScreen() {
        setTitle("Loading...");

        Thread ticker = new Thread() {
            public void run() {
                while (running) {
                    frame++;
                    repaint();
                    try {
                        Thread.sleep(JPSpinner.TICK_MS);
                    }
                    catch (InterruptedException e) {}
                }
            }
        };
        ticker.start();
    }

    /** Stops the animation thread once this screen is no longer the one shown. */
    protected void hideNotify() {
        running = false;
    }

    protected void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        g.setColor(BG_COLOR);
        g.fillRect(0, 0, w, h);

        int size = Math.min(w, h) / 3;
        if (size < 24) size = 24;
        JPSpinner.draw(g, w / 2, h / 2 - 6, size, frame, SPINNER_COLOR, BG_COLOR);

        g.setColor(TEXT_COLOR);
        g.drawString("Loading...", w / 2, h / 2 + size / 2 + 4, Graphics.TOP | Graphics.HCENTER);
    }
}
