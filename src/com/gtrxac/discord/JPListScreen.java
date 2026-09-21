package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Shows a scrollable list of JSONPlaceholder resource items (see JPApi).
 *
 * Top-level lists (openTopLevel, e.g. GET /posts) are paginated using json-server's
 * _page/_limit query params, since some resources are large (500 comments, 5000
 * photos) and this runs on hardware with very little heap - a "Load more" command
 * fetches the next page on demand instead of pulling everything up front.
 *
 * Nested lists (openNested, e.g. GET /posts/1/comments) are small by construction
 * (a post has a handful of comments, a user a couple dozen posts, etc.) so they're
 * just fetched in full, unpaginated.
 */
public class JPListScreen extends List implements CommandListener {
    private static final int PAGE_SIZE = 25;

    private static final Command BACK_COMMAND = new Command("Back", Command.BACK, 0);
    private static final Command MORE_COMMAND = new Command("Load more", Command.SCREEN, 1);
    private static final Command NEW_COMMAND = new Command("New post", Command.SCREEN, 1);

    private String resource;
    private JSONArray items;
    private Displayable back;

    private boolean paginated;
    private int nextPage;
    private boolean hasMore;

    private JPListScreen(String resource, String title, JSONArray items, Displayable back, boolean paginated) {
        super(title, List.IMPLICIT);
        this.resource = resource;
        this.items = items;
        this.back = back;
        this.paginated = paginated;
        this.nextPage = 2;
        this.hasMore = paginated && items.size() >= PAGE_SIZE;

        for (int i = 0; i < items.size(); i++) {
            try {
                appendItem(items.getObject(i));
            }
            catch (Exception e) {
                append("(invalid item)", null);
            }
        }

        addCommand(BACK_COMMAND);
        if (paginated && resource.equals(JPApi.POSTS)) {
            addCommand(NEW_COMMAND);
        }
        if (hasMore) {
            addCommand(MORE_COMMAND);
        }
        setCommandListener(this);
    }

    private void appendItem(JSONObject item) {
        try {
            append(JPUtil.itemLabel(resource, item), null);
        }
        catch (Exception e) {
            append("(invalid item)", null);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            App.disp.setCurrent(back);
        }
        else if (c == NEW_COMMAND) {
            App.disp.setCurrent(new JPEditForm("New post", null, "", "", "1", this));
        }
        else if (c == MORE_COMMAND) {
            loadMore();
        }
        else if (c == List.SELECT_COMMAND) {
            try {
                JSONObject item = items.getObject(getSelectedIndex());
                JPDetailScreen.open(resource, item.getString("id"), this);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMore() {
        final String res = resource;
        final int page = nextPage;

        JPCallback cb = new JPCallback() {
            public Object request() throws Exception {
                return JPApi.listPage(res, page, PAGE_SIZE);
            }
            public void onSuccess(Object result) {
                JSONArray more = (JSONArray) result;
                for (int i = 0; i < more.size(); i++) {
                    try {
                        JSONObject item = more.getObject(i);
                        items.add(item);
                        appendItem(item);
                    }
                    catch (Exception e) {}
                }
                nextPage++;
                hasMore = more.size() >= PAGE_SIZE;
                if (!hasMore) {
                    removeCommand(MORE_COMMAND);
                }
                App.disp.setCurrent(JPListScreen.this);
            }
        };
        new JPThread(cb, this).start();
    }

    /** Opens a top-level, paginated list, e.g. GET /posts?_page=1&_limit=25 */
    public static void openTopLevel(final String resource, final Displayable back) {
        JPCallback cb = new JPCallback() {
            public Object request() throws Exception {
                return JPApi.listPage(resource, 1, PAGE_SIZE);
            }
            public void onSuccess(Object result) {
                App.disp.setCurrent(new JPListScreen(resource, JPUtil.pluralLabel(resource), (JSONArray) result, back, true));
            }
        };
        new JPThread(cb, back).start();
    }

    /** Opens a small, unpaginated nested list, e.g. GET /posts/1/comments */
    public static void openNested(final String parentResource, final String parentId, final String subResource, final String title, final Displayable back) {
        JPCallback cb = new JPCallback() {
            public Object request() throws Exception {
                return JPApi.nested(parentResource, parentId, subResource);
            }
            public void onSuccess(Object result) {
                App.disp.setCurrent(new JPListScreen(subResource, title, (JSONArray) result, back, false));
            }
        };
        new JPThread(cb, back).start();
    }
}
