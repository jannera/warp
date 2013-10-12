package com.rasanenj.warp;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * @author Janne Rasanen
 */
public class ShipJSON extends JavaScriptObject {
    protected ShipJSON() {
    }

    public final native int getID() /*-{ return this.id; }-*/;
    public final native float getAcceleration()  /*-{ return this.acceleration;  }-*/;
    public final native float getMaxSpeed()  /*-{ return this.max_speed;  }-*/;
    public final native float getTurnSpeed()  /*-{ return this.turn_speed;  }-*/;
    public final native float getMaxHealth()  /*-{ return this.max_health;  }-*/;
}
