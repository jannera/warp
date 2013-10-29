package com.rasanenj.warp;

/**
 * @author gilead
 */
public class Utility {
    public static String getHost() {
        return "ws://" + getHostname() + ":" + Constants.PORT;
    }

    private native static String getHostname() /*-{
        return window.location.hostname;
    }-*/;
}
