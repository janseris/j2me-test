package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Tiny form demonstrating PATCH /posts/{id} - sends only the changed field (title),
 * unlike JPEditForm's "Edit" command which replaces the whole post via PUT.
 */
public class JPRenameForm extends Form implements CommandListener {
    private static final Command SAVE_COMMAND = new Command("Save", Command.OK, 0);
    private static final Command CANCEL_COMMAND = new Command("Cancel", Command.BACK, 0);

    private String id;
    private Displayable back;
    private TextField titleField;

    public JPRenameForm(String id, String currentTitle, Displayable back) {
        super("Rename post #" + id + " (PATCH)");
        this.id = id;
        this.back = back;

        titleField = new TextField("New title", currentTitle, 200, TextField.ANY);
        append(titleField);

        addCommand(SAVE_COMMAND);
        addCommand(CANCEL_COMMAND);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == CANCEL_COMMAND) {
            App.disp.setCurrent(back);
        }
        else if (c == SAVE_COMMAND) {
            final JSONObject data = new JSONObject();
            data.put("title", titleField.getString());

            JPCallback cb = new JPCallback() {
                public Object request() throws Exception {
                    return JPApi.patch(JPApi.POSTS, id, data);
                }
                public void onSuccess(Object result) {
                    JSONObject saved = (JSONObject) result;
                    String note = JPApi.lastRequestUsedFallback
                        ? "\"Patched\" post #" + id + ". This is only simulated: this device's HTTP stack " +
                          "can't send a real PATCH request, so it was sent as POST + an " +
                          "X-HTTP-Method-Override header instead - and JSONPlaceholder doesn't really " +
                          "save it either way."
                        : "Patched via a real PATCH /posts/" + id + " request " +
                          "(JSONPlaceholder fakes the save itself either way - nothing is really persisted).";
                    App.disp.setCurrent(new JPDetailScreen(JPApi.POSTS, id, saved, back, note));
                }
            };
            new JPThread(cb, this).start();
        }
    }
}
