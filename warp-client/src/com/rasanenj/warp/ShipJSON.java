package com.rasanenj.warp;

import com.google.gwt.core.client.JavaScriptObject;
import com.rasanenj.warp.entities.ShipStats;

/**
 * @author Janne Rasanen
 */
public class ShipJSON extends JavaScriptObject {
    protected ShipJSON() {
    }

    public final native float getAcceleration()  /*-{ return this.acceleration;  }-*/;
    private final native float getMaxSpeed()  /*-{ return this.max_speed;  }-*/;
    private final native float getTurnSpeed()  /*-{ return this.turn_speed;  }-*/;
    private final native float getMaxHealth()  /*-{ return this.max_health;  }-*/;
    private final native float getMaxAngularAcceleration()  /*-{ return this.maxAngularAcceleration;  }-*/;
    private final native float getSignatureResolution()  /*-{ return this.signature_resolution;  }-*/;
    private final native float getWeaponTracking()  /*-{ return this.weapon_1_tracking;  }-*/;
    private final native float getWeaponOptimal()  /*-{ return this.weapon_1_optimal;  }-*/;
    private final native float getWeaponFalloff()  /*-{ return this.weapon_1_falloff;  }-*/;
    private final native float getWeaponSignatureRadius()  /*-{ return this.weapon_1_signature_radius;  }-*/;
    private final native float getWeaponDamage() /*-{ return this.weapon_1_damage;  }-*/;
    private final native float getWeaponCooldown() /*-{ return this.weapon_1_cooldown;  }-*/;
    private final native float getCost() /*-{ return this.cost;  }-*/;

    public final ShipStats getStats() {
        return new ShipStats(0, 0, 0, 0, 0, 0, getMaxHealth(), getMaxSpeed(),
                getTurnSpeed(), getMaxAngularAcceleration(), getSignatureResolution(), getWeaponTracking(),
                getWeaponSignatureRadius(), getWeaponOptimal(), getWeaponFalloff(), getWeaponDamage(),
                getWeaponCooldown(), getAcceleration(), getCost());
    }
}
