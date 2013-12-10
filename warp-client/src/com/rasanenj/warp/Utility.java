package com.rasanenj.warp;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.RootPanel;

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

    public static Element getCanvas() {
        NodeList<Element> canvases = RootPanel.getBodyElement().getElementsByTagName("canvas");
        return canvases.getItem(0);
    }
}
