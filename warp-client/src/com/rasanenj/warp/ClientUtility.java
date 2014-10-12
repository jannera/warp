package com.rasanenj.warp;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author gilead
 */
public class ClientUtility {
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

    private static int MAX_DECIMALS = 5;
    private static NumberFormat[] formats = new NumberFormat[MAX_DECIMALS];

    static {
        formats[0] = NumberFormat.getFormat("0");
        for (int i=1; i < MAX_DECIMALS; i++) {
            String s = "0.";
            s += new String(new char[i]).replace("\0", "0"); // add i zeros to end
            formats[i] = NumberFormat.getFormat(s);
        }
    }

    public static String format(float f, int decimals) {
        return formats[decimals].format(f);
    }
}
