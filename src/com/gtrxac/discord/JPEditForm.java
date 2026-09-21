package com.gtrxac.discord;

import javax.microedition.lcdui.*;
import cc.nnproject.json.*;

/**
 * Form for creating a new post (POST /posts, when existingId is null) or replacing an
 * existing one (PUT /posts/{existingId}). JSONPlaceholder fakes both - nothing is
 * actually persisted server-side, it just echoes back what looks like a saved result.
 */
public class JPEditForm extends Form implements CommandListener {
    private static final Command SAVE_COMMAND = new Command("Save", Command.OK, 0);
    private static final Command CANCEL_COMMAND = new Command("Cancel", Command.BACK, 0);

    private TextField titleField;
    private TextField bodyField;
    private TextField userIdField;
    private String existingId;
    private Displayable back;

    public JPEditForm(String title, String existingId, String initialTitle, String initialBody, String initialUserId, Displayable back) {
        super(title);
        this.existingId = existingId;
        this.back = back;

        titleField = new TextField("Title", initialTitle, 200, TextField.ANY);
        bodyField = new TextField("Body", initialBody, 1000, TextField.ANY);
        userIdField = new TextField("User ID", initialUserId, 10, TextField.NUMERIC);

        append(titleField);
        append(bodyField);
        append(userIdField);

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
            data.put("body", bodyField.getString());
            try {
                data.put("userId", Integer.parseInt(userIdField.getString()));
            }
            catch (Exception e) {
                data.put("userId", 1);
            }

            final boolean isNew = (existingId == null);

            JPCallback cb = new JPCallback() {
                public Object request() throws Exception {
                    if (isNew) {
                        return JPApi.create(JPApi.POSTS, data);
                    }
                    return JPApi.update(JPApi.POSTS, existingId, data);
                }
                public void onSuccess(Object result) {
                    JSONObject saved = (JSONObject) result;
                    String savedId = isNew ? saved.getString("id", "101") : existingId;
                    String note;
                    if (isNew) {
                        note = "Created via POST /posts (JSONPlaceholder fakes this - nothing is really saved).";
                    } else if (JPApi.lastRequestUsedFallback) {
                        note = "\"Updated\" post #" + existingId + ". This is only simulated: this device's HTTP " +
                            "stack can't send a real PUT request, so it was sent as POST + an " +
                            "X-HTTP-Method-Override header instead - and JSONPlaceholder doesn't really " +
                            "save it either way.";
                    } else {
                        note = "Updated via a real PUT /posts/" + existingId + " request " +
                            "(JSONPlaceholder fakes the save itself either way - nothing is really persisted).";
                    }
                    App.disp.setCurrent(new JPDetailScreen(JPApi.POSTS, savedId, saved, back, note));
                }
            };
            new JPThread(cb, this).start();
        }
    }
}
