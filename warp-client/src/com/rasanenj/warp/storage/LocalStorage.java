package com.rasanenj.warp.storage;

/**
 * @author gilead
 */
public class LocalStorage {
    // used keys
    public static String NAME = "name";

    public final native static String fetch(String key) /*-{
        return localStorage[key];
    }-*/;

    public final native static void store(String key, String value) /*-{
        localStorage[key] = value;
    }-*/;
}
