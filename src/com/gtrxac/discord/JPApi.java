package com.gtrxac.discord;

import cc.nnproject.json.*;
import fi.gtrxac.bluewap.http.HTTP;
import fi.gtrxac.bluewap.http.StandardHTTP;

/**
 * Minimal client for https://jsonplaceholder.typicode.com, a free fake REST API used
 * here to test HTTP + JSON handling on real hardware (over the native TLS 1.2 patch)
 * before wiring up real Discord requests.
 *
 * See https://jsonplaceholder.typicode.com/guide for the endpoints this wraps.
 */
public class JPApi {
    public static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    // Resource endpoint names
    public static final String POSTS = "posts";
    public static final String COMMENTS = "comments";
    public static final String ALBUMS = "albums";
    public static final String PHOTOS = "photos";
    public static final String TODOS = "todos";
    public static final String USERS = "users";

    /**
     * True if the most recent update()/patch()/delete() call had to fall back to the
     * simulated POST+override (see below), rather than sending the real verb. Only
     * meaningful right after that call returns - set by request() below.
     */
    public static boolean lastRequestUsedFallback;

    /**
     * Makes one HTTP request to JSONPlaceholder and returns the raw response body.
     * path must start with "/", e.g. "/posts" or "/posts/1". body may be null for
     * requests that don't send one (GET, DELETE).
     */
    private static String request(String method, String path, String body) throws Exception {
        lastRequestUsedFallback = false;
        return request(method, path, body, false);
    }

    /**
     * forcePost: retry as a plain POST with an X-HTTP-Method-Override header instead of the
     * real verb. Most phones' javax.microedition.io.HttpConnection only reliably supports
     * GET/HEAD/POST (that's all the MIDP spec guarantees) - setRequestMethod("PUT"/"PATCH"/
     * "DELETE") throws StandardHTTP.requestMethodException on many real devices (and,
     * apparently, in KEmulator too). This is the same wall the Discord API calls in
     * HTTPThread hit, which is why those fall back to POST-based /edit and /delete aliases.
     * JSONPlaceholder has no such alias, so the best available fallback is the (fairly
     * common, but not guaranteed) X-HTTP-Method-Override convention.
     */
    private static String request(String method, String path, String body, boolean forcePost) throws Exception {
        String wireMethod = forcePost ? "POST" : method;
        HTTP req = HTTP.createRequest(wireMethod, BASE_URL + path);
        req.setHeader("User-Agent", "discord-j2me-fork (JSONPlaceholder test screen)");
        if (forcePost) {
            req.setHeader("X-HTTP-Method-Override", method);
        }

        if (body != null) {
            req.setHeader("Content-Type", "application/json; charset=UTF-8");
            req.setData(body);
        }

        int code;
        try {
            code = req.getResponseCode();
        }
        catch (Exception e) {
            if (!forcePost && e == StandardHTTP.requestMethodException) {
                return request(method, path, body, true);
            }
            throw e;
        }
        String responseText = req.getResponseString();

        if (code >= 400) {
            if (forcePost) {
                throw new Exception(
                    "HTTP " + code + " - this device only supports GET/POST, and the POST+" +
                    "X-HTTP-Method-Override(" + method + ") fallback wasn't accepted either: " +
                    responseText
                );
            }
            throw new Exception("HTTP " + code + ": " + responseText);
        }
        lastRequestUsedFallback = forcePost;
        return responseText;
    }

    /** GET /{resource} */
    public static JSONArray list(String resource) throws Exception {
        return new JSONArray(request("GET", "/" + resource, null));
    }

    /** GET /{resource}?_page={page}&_limit={limit} (json-server pagination) */
    public static JSONArray listPage(String resource, int page, int limit) throws Exception {
        return new JSONArray(request("GET", "/" + resource + "?_page=" + page + "&_limit=" + limit, null));
    }

    /** GET /{resource}?{filterKey}={filterValue}, e.g. list("posts", "userId", "1") */
    public static JSONArray list(String resource, String filterKey, String filterValue) throws Exception {
        return new JSONArray(request("GET", "/" + resource + "?" + filterKey + "=" + filterValue, null));
    }

    /** GET /{parentResource}/{parentId}/{subResource}, e.g. nested("posts", "1", "comments") */
    public static JSONArray nested(String parentResource, String parentId, String subResource) throws Exception {
        return new JSONArray(request("GET", "/" + parentResource + "/" + parentId + "/" + subResource, null));
    }

    /** GET /{resource}/{id} */
    public static JSONObject get(String resource, String id) throws Exception {
        return new JSONObject(request("GET", "/" + resource + "/" + id, null));
    }

    /** POST /{resource} */
    public static JSONObject create(String resource, JSONObject data) throws Exception {
        return new JSONObject(request("POST", "/" + resource, data.toString()));
    }

    /** PUT /{resource}/{id} - full replace */
    public static JSONObject update(String resource, String id, JSONObject data) throws Exception {
        return new JSONObject(request("PUT", "/" + resource + "/" + id, data.toString()));
    }

    /** PATCH /{resource}/{id} - partial update */
    public static JSONObject patch(String resource, String id, JSONObject data) throws Exception {
        return new JSONObject(request("PATCH", "/" + resource + "/" + id, data.toString()));
    }

    /** DELETE /{resource}/{id} */
    public static void delete(String resource, String id) throws Exception {
        request("DELETE", "/" + resource + "/" + id, null);
    }
}
