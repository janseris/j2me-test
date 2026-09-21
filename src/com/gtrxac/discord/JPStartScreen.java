package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * New start screen for this fork: a graphical button grid linking to
 * https://jsonplaceholder.typicode.com (a free fake REST API), used to test HTTP + JSON
 * handling on real hardware over the native TLS 1.2 patch, before wiring up real Discord
 * requests.
 *
 * This is a plain Canvas (not MyCanvas/WrapperCanvas), same as the List-based screen it
 * replaces, so it stays decoupled from the Discord app's own UI framework.
 *
 * Series 80 Communicators (Nokia 9210/9300/9500) are pointer-driven, not D-pad-driven:
 * the navi-wheel moves an on-screen mouse cursor, and there's no "left/right action key"
 * like on a typical feature phone - it's closer to a small computer than a phone from a
 * UX standpoint. So pointerPressed()/pointerDragged() are the PRIMARY way to use this
 * screen (tap a button to open it, drag across to move the highlight); keyPressed() with
 * getGameAction() is only a secondary fallback, for devices/emulators without pointer
 * support.
 *
 * There's no image assets or 3D API here (this is plain javax.microedition.lcdui), so
 * the buttons are genuine extruded polygons built by hand: a flat front face plus a
 * right face and a bottom face, each a parallelogram made of two fillTriangle() calls,
 * shaded darker than the front so the button reads as a solid block with real depth
 * rather than a flat rect with a painted-on highlight. The focused/selected button uses
 * a shallow depth and a darker front face, so it reads as pressed in instead of popped
 * out.
 *
 * The original Discord client is still fully present in this codebase and untouched -
 * see App.startApp()/App.login() - this screen has just been made the default for now.
 */
public class JPStartScreen extends Canvas implements CommandListener {
    private static final Command INFO_COMMAND = new Command("Info", Command.HELP, 5);
    private static final Command OPEN_COMMAND = new Command("Open", Command.OK, 1);

    private static final String[] LABELS = {"Posts", "Comments", "Albums", "Photos", "Todos", "Users"};
    private static final String[] RESOURCES = {
        JPApi.POSTS, JPApi.COMMENTS, JPApi.ALBUMS, JPApi.PHOTOS, JPApi.TODOS, JPApi.USERS
    };

    private static final int BG_COLOR = 0x2B2D31;
    private static final int BUTTON_COLOR = 0x383A40;
    private static final int BUTTON_FOCUS_COLOR = 0x5865F2;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int ICON_COLOR = 0xDBDEE1;
    private static final int ICON_FOCUS_COLOR = 0xFFFFFF;

    // How far the extruded side/bottom faces reach beyond the front face, in pixels.
    // CELL_PAD has to leave at least this much room around each button, or the
    // extrusion of one button would be drawn over its neighbor.
    private static final int DEPTH = 6;
    private static final int CELL_PAD = 8;

    // Grid geometry, recomputed from getWidth()/getHeight() by layout() before every
    // paint and every pointer/key hit-test, so painting and hit-testing never drift
    // apart. Cheap enough for a 6-button grid to just redo every time.
    private int cols, rows, cellW, cellH;

    // The currently highlighted button: set on pointerDragged() (hover) or by keyboard
    // navigation, and opened on pointerPressed() (tap) or FIRE/OPEN_COMMAND (select).
    private int selected = 0;

    public JPStartScreen() {
        setTitle("JSONPlaceholder");
        addCommand(OPEN_COMMAND);
        addCommand(INFO_COMMAND);
        setCommandListener(this);
    }

    private void layout() {
        int w = getWidth();
        int h = getHeight();
        int n = LABELS.length;
        if (w <= 0 || h <= 0) {
            cols = n;
            rows = 1;
        }
        else {
            int bestCols = 1;
            int bestDiff = Integer.MAX_VALUE;
            for (int c = 1; c <= n; c++) {
                int r = (n + c - 1) / c;
                int cw = w / c;
                int ch = h / r;
                int diff = (cw > ch) ? (cw - ch) : (ch - cw);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestCols = c;
                }
            }
            cols = bestCols;
            rows = (n + cols - 1) / cols;
        }
        cellW = w / cols;
        cellH = h / rows;
    }

    /** Returns the button index at (x, y), or -1 if there isn't one there. */
    private int cellAt(int x, int y) {
        if (cellW <= 0 || cellH <= 0 || x < 0 || y < 0) return -1;
        int col = x / cellW;
        int row = y / cellH;
        if (col < 0 || col >= cols || row < 0 || row >= rows) return -1;
        int idx = row * cols + col;
        return (idx >= 0 && idx < LABELS.length) ? idx : -1;
    }

    private void openSelected() {
        JPListScreen.openTopLevel(RESOURCES[selected], this);
    }

    // --- Pointer input (primary) ---------------------------------------------------

    public void pointerPressed(int x, int y) {
        layout();
        int idx = cellAt(x, y);
        if (idx >= 0) {
            // Not calling serviceRepaints() here: it throws IllegalStateException when
            // invoked from the event thread, which is exactly where pointerPressed()
            // runs. A plain repaint() is enough - JPThread shows its own loading screen
            // right after, so there's no need to block on this frame first.
            selected = idx;
            repaint();
            openSelected();
        }
    }

    public void pointerDragged(int x, int y) {
        layout();
        int idx = cellAt(x, y);
        if (idx >= 0 && idx != selected) {
            selected = idx;
            repaint();
        }
    }

    // --- Keyboard input (secondary fallback) ----------------------------------------

    protected void keyPressed(int keyCode) {
        int action;
        try {
            action = getGameAction(keyCode);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        layout();
        int col = selected % cols;
        int row = selected / cols;

        if (action == RIGHT) moveSelection(col + 1, row);
        else if (action == LEFT) moveSelection(col - 1, row);
        else if (action == DOWN) moveSelection(col, row + 1);
        else if (action == UP) moveSelection(col, row - 1);
        else if (action == FIRE) openSelected();
    }

    private void moveSelection(int col, int row) {
        if (col < 0) col = cols - 1;
        if (col >= cols) col = 0;
        if (row < 0) row = rows - 1;
        if (row >= rows) row = 0;

        int idx = row * cols + col;
        if (idx >= LABELS.length) idx = LABELS.length - 1;
        selected = idx;
        repaint();
    }

    // --- Drawing ----------------------------------------------------------------

    protected void paint(Graphics g) {
        layout();

        g.setColor(BG_COLOR);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i = 0; i < LABELS.length; i++) {
            int col = i % cols;
            int row = i / cols;
            drawButton(g, i, col * cellW, row * cellH, cellW, cellH);
        }
    }

    private void drawButton(Graphics g, int index, int x, int y, int w, int h) {
        boolean focused = (index == selected);
        int bx = x + CELL_PAD;
        int by = y + CELL_PAD;
        int bw = w - CELL_PAD * 2;
        int bh = h - CELL_PAD * 2;
        if (bw <= 0 || bh <= 0) return;

        int baseColor = focused ? BUTTON_FOCUS_COLOR : BUTTON_COLOR;
        draw3DPanel(g, bx, by, bw, bh, baseColor, focused);

        // Icon in the upper part of the button, label along the bottom. A pressed
        // (focused) button's contents sink down-right by 1px, matching its shallower
        // extrusion.
        int sink = focused ? 1 : 0;

        Font font = g.getFont();
        int labelH = font.getHeight() + 4;
        int iconAreaH = bh - labelH;
        int iconSize = Math.min(bw - 8, iconAreaH - 4);
        if (iconSize < 6) iconSize = 6;
        int iconX = bx + (bw - iconSize) / 2 + sink;
        int iconY = by + (iconAreaH - iconSize) / 2 + sink;

        int iconColor = focused ? ICON_FOCUS_COLOR : ICON_COLOR;
        drawIcon3D(g, index, iconX, iconY, iconSize, iconColor);

        int labelX = bx + bw / 2 + sink;
        int labelY = by + iconAreaH + sink;
        g.setColor(shade(TEXT_COLOR, -130));
        g.drawString(LABELS[index], labelX + 1, labelY + 1, Graphics.TOP | Graphics.HCENTER);
        g.setColor(TEXT_COLOR);
        g.drawString(LABELS[index], labelX, labelY, Graphics.TOP | Graphics.HCENTER);
    }

    /**
     * Draws one button as a genuine extruded 3D box: a flat front face, plus a right
     * face and a bottom face, each a parallelogram built from two fillTriangle() calls
     * (a quad split along its diagonal), shaded progressively darker than the front so
     * the button reads as a solid block rather than a flat rect with a painted-on
     * highlight. Light comes from the upper-left, so the extrusion (and its implied
     * shadow) falls to the lower-right - the same direction as the icon/label emboss.
     *
     * A pressed (focused) button gets a shallow depth and a darker front face, so it
     * reads as sunk in rather than fully popped out.
     */
    private void draw3DPanel(Graphics g, int x, int y, int w, int h, int base, boolean pressed) {
        int depth = pressed ? 2 : DEPTH;
        int frontColor = pressed ? shade(base, -20) : base;
        int rightColor = shade(base, pressed ? -40 : -90);
        int bottomColor = shade(base, pressed ? -55 : -120);

        // Right face: a parallelogram connecting the front face's right edge to that
        // same edge pushed (depth, depth) back into the screen. Two triangles split
        // along the front-top -> back-bottom diagonal.
        g.setColor(rightColor);
        g.fillTriangle(x + w, y, x + w + depth, y + depth, x + w, y + h);
        g.fillTriangle(x + w + depth, y + depth, x + w + depth, y + h + depth, x + w, y + h);

        // Bottom face: same idea, off the front face's bottom edge.
        g.setColor(bottomColor);
        g.fillTriangle(x, y + h, x + depth, y + h + depth, x + w, y + h);
        g.fillTriangle(x + depth, y + h + depth, x + w + depth, y + h + depth, x + w, y + h);

        // Front face, drawn last so it cleanly covers the seams where the other two
        // faces meet it.
        g.setColor(frontColor);
        g.fillRect(x, y, w, h);

        g.setColor(shade(frontColor, 40));
        g.drawLine(x, y, x + w, y);
        g.drawLine(x, y, x, y + h);
    }

    /**
     * Draws one resource's icon as a small emboss: a dark copy offset down-right, a
     * light copy offset up-left, then the base-colored icon on top - gives the plain
     * vector shapes a raised, engraved look instead of a single flat outline.
     */
    private void drawIcon3D(Graphics g, int index, int x, int y, int size, int baseColor) {
        g.setColor(shade(baseColor, -70));
        drawIconShape(g, index, x + 1, y + 1, size);

        g.setColor(shade(baseColor, 70));
        drawIconShape(g, index, x - 1, y - 1, size);

        g.setColor(baseColor);
        drawIconShape(g, index, x, y, size);
    }

    /** Simple vector icons per resource type - no external image assets. */
    private void drawIconShape(Graphics g, int index, int x, int y, int size) {
        switch (index) {
            case 0: // Posts - a page with a few lines of text
                g.drawRoundRect(x, y, size, size, 4, 4);
                int lineY = y + size / 4;
                int lineGap = Math.max(2, size / 5);
                for (int i = 0; i < 3 && lineY < y + size - 3; i++) {
                    g.drawLine(x + size / 6, lineY, x + size - size / 6, lineY);
                    lineY += lineGap;
                }
                break;

            case 1: // Comments - a speech bubble
                g.fillRoundRect(x, y, size, size * 3 / 4, 6, 6);
                g.fillTriangle(
                    x + size / 4, y + size * 3 / 4 - 1,
                    x + size / 4 + size / 6, y + size * 3 / 4 - 1,
                    x + size / 4, y + size - 1
                );
                break;

            case 2: // Albums - stacked squares
                int stackOff = Math.max(2, size / 6);
                g.drawRoundRect(x, y, size - stackOff, size - stackOff, 3, 3);
                g.fillRoundRect(x + stackOff, y + stackOff, size - stackOff, size - stackOff, 3, 3);
                break;

            case 3: // Photos - a picture frame with a sun and a mountain
                g.drawRoundRect(x, y, size, size, 3, 3);
                int sunR = Math.max(2, size / 6);
                g.fillArc(x + size - sunR * 2 - 2, y + 2, sunR * 2, sunR * 2, 0, 360);
                g.fillTriangle(
                    x + 2, y + size - 2,
                    x + size / 2, y + size / 3,
                    x + size - 2, y + size - 2
                );
                break;

            case 4: // Todos - a checkbox with a checkmark
                g.drawRoundRect(x, y, size, size, 3, 3);
                g.drawLine(x + size / 5, y + size / 2, x + size * 2 / 5, y + size * 3 / 4);
                g.drawLine(x + size * 2 / 5, y + size * 3 / 4, x + size * 4 / 5, y + size / 4);
                break;

            case 5: // Users - a head and shoulders
                int headR = Math.max(3, size / 3);
                g.fillArc(x + (size - headR) / 2, y, headR, headR, 0, 360);
                g.fillArc(x, y + size / 2, size, size / 2, 0, 180);
                break;
        }
    }

    /** Shifts each RGB channel of color by delta (positive lightens, negative darkens). */
    private static int shade(int rgb, int delta) {
        int r = clampByte(((rgb >> 16) & 0xFF) + delta);
        int g = clampByte(((rgb >> 8) & 0xFF) + delta);
        int b = clampByte((rgb & 0xFF) + delta);
        return (r << 16) | (g << 8) | b;
    }

    private static int clampByte(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }

    // --- Commands -----------------------------------------------------------------

    public void commandAction(Command c, Displayable d) {
        if (c == OPEN_COMMAND) {
            openSelected();
        }
        else if (c == INFO_COMMAND) {
            Alert alert = new Alert(
                "About this screen",
                "This is a new start screen for this fork. It replaces the Discord " +
                "login screen for now, and talks to " + JPApi.BASE_URL + " " +
                "(JSONPlaceholder, a free fake REST API) to test HTTP and JSON " +
                "handling on this device.\n\n" +
                "Note: Edit, Rename and Delete are only simulated. Most phones' HTTP " +
                "stack (including this one) doesn't actually support sending PUT/PATCH/" +
                "DELETE requests, so those are sent as a POST with a method-override " +
                "header instead - and JSONPlaceholder itself never really saves " +
                "anything either way.\n\n" +
                "The original Discord client is still fully in the code (see App.java) " +
                "- it's just not the default screen at the moment.",
                null, AlertType.INFO
            );
            alert.setTimeout(Alert.FOREVER);
            App.disp.disp.setCurrent(alert, this);
        }
    }
}
