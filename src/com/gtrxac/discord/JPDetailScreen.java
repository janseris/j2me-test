package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;
import java.util.Enumeration;

/**
 * Shows one JSONPlaceholder resource's fields, plus commands to follow its nested
 * relations (a post's comments, an album's photos, a user's posts/albums/todos), and,
 * for posts, to edit (PUT), rename (PATCH) or delete it.
 */
public class JPDetailScreen extends Form implements CommandListener {
    private static final Command BACK_COMMAND = new Command("Back", Command.BACK, 0);
    private static final Command COMMENTS_COMMAND = new Command("Comments", Command.SCREEN, 1);
    private static final Command PHOTOS_COMMAND = new Command("Photos", Command.SCREEN, 1);
    private static final Command USER_POSTS_COMMAND = new Command("Posts", Command.SCREEN, 1);
    private static final Command USER_ALBUMS_COMMAND = new Command("Albums", Command.SCREEN, 2);
    private static final Command USER_TODOS_COMMAND = new Command("Todos", Command.SCREEN, 3);
    private static final Command EDIT_COMMAND = new Command("Edit (PUT)", Command.SCREEN, 2);
    private static final Command RENAME_COMMAND = new Command("Rename (PATCH)", Command.SCREEN, 3);
    private static final Command DELETE_COMMAND = new Command("Delete", Command.SCREEN, 4);

    private String resource;
    private String id;
    private JSONObject item;
    private Displayable back;

    JPDetailScreen(String resource, String id, JSONObject item, Displayable back) {
        this(resource, id, item, back, null);
    }

    JPDetailScreen(String resource, String id, JSONObject item, Displayable back, String note) {
        super(JPUtil.singularLabel(resource) + " #" + id);
        this.resource = resource;
        this.id = id;
        this.item = item;
        this.back = back;

        if (note != null) {
            append(new StringItem("Note:", note));
        }
        addFields("", item);

        addCommand(BACK_COMMAND);
        if (resource.equals(JPApi.POSTS)) {
            addCommand(COMMENTS_COMMAND);
            addCommand(EDIT_COMMAND);
            addCommand(RENAME_COMMAND);
            addCommand(DELETE_COMMAND);
        }
        else if (resource.equals(JPApi.ALBUMS)) {
            addCommand(PHOTOS_COMMAND);
        }
        else if (resource.equals(JPApi.USERS)) {
            addCommand(USER_POSTS_COMMAND);
            addCommand(USER_ALBUMS_COMMAND);
            addCommand(USER_TODOS_COMMAND);
        }
        setCommandListener(this);
    }

    private void addFields(String prefix, JSONObject obj) {
        Enumeration e = obj.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            Object val = obj.get(key, null);
            String label = (prefix.length() == 0) ? key : (prefix + "." + key);
            if (val instanceof JSONObject) {
                addFields(label, (JSONObject) val);
            } else {
                append(new StringItem(label + ":", String.valueOf(val)));
            }
        }
    }

    /** Fetches GET /{resource}/{id} and shows it. */
    public static void open(final String resource, final String id, final Displayable back) {
        JPCallback cb = new JPCallback() {
            public Object request() throws Exception {
                return JPApi.get(resource, id);
            }
            public void onSuccess(Object result) {
                App.disp.setCurrent(new JPDetailScreen(resource, id, (JSONObject) result, back));
            }
        };
        new JPThread(cb, back).start();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_COMMAND) {
            App.disp.setCurrent(back);
        }
        else if (c == COMMENTS_COMMAND) {
            JPListScreen.openNested(JPApi.POSTS, id, JPApi.COMMENTS, "Comments on post #" + id, this);
        }
        else if (c == PHOTOS_COMMAND) {
            JPListScreen.openNested(JPApi.ALBUMS, id, JPApi.PHOTOS, "Photos in album #" + id, this);
        }
        else if (c == USER_POSTS_COMMAND) {
            JPListScreen.openNested(JPApi.USERS, id, JPApi.POSTS, "Posts by user #" + id, this);
        }
        else if (c == USER_ALBUMS_COMMAND) {
            JPListScreen.openNested(JPApi.USERS, id, JPApi.ALBUMS, "Albums by user #" + id, this);
        }
        else if (c == USER_TODOS_COMMAND) {
            JPListScreen.openNested(JPApi.USERS, id, JPApi.TODOS, "Todos by user #" + id, this);
        }
        else if (c == EDIT_COMMAND) {
            App.disp.setCurrent(new JPEditForm(
                "Edit post #" + id, id,
                item.getString("title", ""), item.getString("body", ""), item.getString("userId", "1"),
                this
            ));
        }
        else if (c == RENAME_COMMAND) {
            App.disp.setCurrent(new JPRenameForm(id, item.getString("title", ""), this));
        }
        else if (c == DELETE_COMMAND) {
            final String delResource = resource;
            final String delId = id;
            final Displayable target = back;

            JPCallback cb = new JPCallback() {
                public Object request() throws Exception {
                    JPApi.delete(delResource, delId);
                    return null;
                }
                public void onSuccess(Object result) {
                    String message = JPApi.lastRequestUsedFallback
                        ? "\"Deleted\" post #" + delId + ". This is only simulated: this device's HTTP stack " +
                          "can't send a real DELETE request, so it was sent as POST + an " +
                          "X-HTTP-Method-Override header instead - and JSONPlaceholder doesn't really " +
                          "remove anything either way."
                        : "Deleted post #" + delId + " via a real DELETE request " +
                          "(JSONPlaceholder fakes the removal itself either way - nothing is really removed).";
                    Alert alert = new Alert("Deleted", message, null, AlertType.CONFIRMATION);
                    App.disp.disp.setCurrent(alert, target);
                }
            };
            new JPThread(cb, this).start();
        }
    }
}
