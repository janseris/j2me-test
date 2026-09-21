package com.gtrxac.discord;

import cc.nnproject.json.*;

/**
 * Small display helpers shared by the JSONPlaceholder test screens (JPStartScreen,
 * JPListScreen, JPDetailScreen).
 */
public class JPUtil {
    public static String pluralLabel(String resource) {
        if (resource.equals(JPApi.POSTS)) return "Posts";
        if (resource.equals(JPApi.COMMENTS)) return "Comments";
        if (resource.equals(JPApi.ALBUMS)) return "Albums";
        if (resource.equals(JPApi.PHOTOS)) return "Photos";
        if (resource.equals(JPApi.TODOS)) return "Todos";
        if (resource.equals(JPApi.USERS)) return "Users";
        return resource;
    }

    public static String singularLabel(String resource) {
        if (resource.equals(JPApi.POSTS)) return "Post";
        if (resource.equals(JPApi.COMMENTS)) return "Comment";
        if (resource.equals(JPApi.ALBUMS)) return "Album";
        if (resource.equals(JPApi.PHOTOS)) return "Photo";
        if (resource.equals(JPApi.TODOS)) return "Todo";
        if (resource.equals(JPApi.USERS)) return "User";
        return resource;
    }

    /** One-line label for showing an item inside a List. */
    public static String itemLabel(String resource, JSONObject item) {
        String id = item.getString("id", "?");

        if (resource.equals(JPApi.POSTS) || resource.equals(JPApi.ALBUMS) || resource.equals(JPApi.PHOTOS)) {
            return "#" + id + " " + item.getString("title", "");
        }
        if (resource.equals(JPApi.COMMENTS)) {
            return "#" + id + " " + item.getString("name", "");
        }
        if (resource.equals(JPApi.TODOS)) {
            boolean done = item.getBoolean("completed", false);
            return (done ? "[x] " : "[ ] ") + "#" + id + " " + item.getString("title", "");
        }
        if (resource.equals(JPApi.USERS)) {
            return item.getString("name", "") + " (@" + item.getString("username", "") + ")";
        }
        return "#" + id;
    }
}
