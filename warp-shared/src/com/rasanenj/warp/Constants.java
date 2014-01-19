package com.rasanenj.warp;

/**
 * @author Janne Rasanen
 */
public class Constants {
    public static final int PORT = 9091;

    public static final String PERSIST_SERVER_HOST = "192.168.2.108:8000";

    public static final String OFFLINE_FLEET =
    "[{'id': 1, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 5.0, " +
       "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 3, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 5.0, 'weapon_1_cooldown': 5.0, 'weapon_1_tracking': 1.5, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 10.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
       "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0}" +

            // "]";
            ", " +

       "{'id': 2, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 5.0, " +
       "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 3.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 5.0, 'weapon_1_cooldown': 5.0, 'weapon_1_tracking': 1.5, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 10.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
       "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0}, " +

       "{'id': 3, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 5.0, " +
       "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 3.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 5.0, 'weapon_1_cooldown': 5.0, 'weapon_1_tracking': 1.5, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 10.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
       "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0}" +

            "]";


    public static final String NPC_FLEET =
       "[{'id': 1, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 5.0, " +
       "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 3.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 5.0, 'weapon_1_cooldown': 5.0, 'weapon_1_tracking': 1.5, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 10.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
       "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0} " +
       "]";

    public static final String NPC_INVENTORY =
        // small fast brawler
        "[{'id': 1, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 5.0, " +
        "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
        "'afterburner_force': 6.0, 'maxAngularAcceleration': 3.0, " +
        "'signature_resolution' : 40, " +
        "'weapon_1_damage': 5.0, 'weapon_1_cooldown': 3.0, 'weapon_1_tracking': 1.5, " +
        "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 5.0, 'weapon_1_falloff': 2.0, " +
        "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
        "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0," +
        "'cost' : 10, 'width': 1, 'height': 0.4}, " +

        // small sniper
        "{'id': 1, 'fleet': 1, 'acceleration': 2.0, 'max_speed': 3.0, " +
        "'turn_speed': 1.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
        "'afterburner_force': 6.0, 'maxAngularAcceleration': 3.0, " +
        "'signature_resolution' : 40, " +
        "'weapon_1_damage': 3.0, 'weapon_1_cooldown': 10.0, 'weapon_1_tracking': 0.5, " +
        "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 15.0, 'weapon_1_falloff': 5.0, " +
        "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
        "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0," +
        "'cost' : 20, 'width': 1, 'height': 0.4}, " +

        // medium brawler
        "{'id': 1, 'fleet': 1, 'acceleration': 1.0, 'max_speed': 2.0, " +
        "'turn_speed': 0.5, 'max_health': 120.0, 'afterburner_max_time': 5.0, " +
        "'afterburner_force': 6.0, 'maxAngularAcceleration': 1.5, " +
        "'signature_resolution' : 120, " +
        "'weapon_1_damage': 15.0, 'weapon_1_cooldown': 3.0, 'weapon_1_tracking': 1.0, " +
        "'weapon_1_signature_radius': 80, 'weapon_1_optimal': 8.0, 'weapon_1_falloff': 2.0, " +
        "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
        "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0," +
        "'cost' : 40, 'width': 1, 'height': 0.4} " +
        "]";

}
