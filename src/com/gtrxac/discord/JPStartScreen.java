package com.gtrxac.discord;

import javax.microedition.lcdui.*;

/**
 * New start screen for this fork: a small browser for https://jsonplaceholder.typicode.com
 * (a free fake REST API), used to test HTTP + JSON handling on real hardware over the
 * native TLS 1.2 patch, before wiring up real Discord requests.
 *
 * The original Discord client is still fully present in this codebase and untouched -
 * see App.startApp()/App.login() - this screen has just been made the default for now.
 */
public class JPStartScreen extends List implements CommandListener {
    private static final Command INFO_COMMAND = new Command("Info", Command.HELP, 5);

    private static final String[] LABELS = {"Posts", "Comments", "Albums", "Photos", "Todos", "Users"};
    private static final String[] RESOURCES = {
        JPApi.POSTS, JPApi.COMMENTS, JPApi.ALBUMS, JPApi.PHOTOS, JPApi.TODOS, JPApi.USERS
    };

    public JPStartScreen() {
        super("JSONPlaceholder", List.IMPLICIT, LABELS, null);
        addCommand(INFO_COMMAND);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            JPListScreen.openTopLevel(RESOURCES[getSelectedIndex()], this);
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
