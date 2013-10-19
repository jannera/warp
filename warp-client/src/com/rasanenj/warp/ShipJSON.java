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
    public final native float getMaxAngularAcceleration()  /*-{ return this.maxAngularAcceleration;  }-*/;
    public final native float getSignatureResolution()  /*-{ return this.signature_resolution;  }-*/;
    public final native float getWeaponTracking()  /*-{ return this.weapon_1_tracking;  }-*/;
    public final native float getWeaponOptimal()  /*-{ return this.weapon_1_optimal;  }-*/;
    public final native float getWeaponFalloff()  /*-{ return this.weapon_1_falloff;  }-*/;
    public final native float getWeaponSignatureRadius()  /*-{ return this.weapon_1_signature_radius;  }-*/;
}
