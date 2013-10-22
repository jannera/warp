package com.rasanenj.warp;

/**
 * @author Janne Rasanen
 */
public class Constants {
    public static final int PORT = 9091;

    public static final String PERSIST_SERVER_HOST = "192.168.2.108:8000";

    public static final String OFFLINE_FLEET =
    "[{'id': 1, 'fleet': 1, 'acceleration': 4.0, 'max_speed': 15.0, " +
       "'turn_speed': 120.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 12.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 1.0, 'weapon_1_cooldown': 5.0, 'weapon_1_tracking': 0.15, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 10.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 6.0, 'weapon_2_cooldown': 7.0, 'weapon_2_tracking': 8.0, " +
       "'weapon_2_optimal': 3.0, 'weapon_2_falloff': 2.0}, " +

       "{'id': 2, 'fleet': 1, 'acceleration': 3.0, 'max_speed': 15.0, " +
       "'turn_speed': 120.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 12.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 7.0, 'weapon_1_cooldown': 8.0, 'weapon_1_tracking': 0.15, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 15.0, 'weapon_1_falloff': 5.0, " +
       "'weapon_2_damage': 4.0, 'weapon_2_cooldown': 5.0, " +
       "'weapon_2_tracking': 6.0, 'weapon_2_optimal': 7.0, 'weapon_2_falloff': 8.0}, " +

       "{'id': 3, 'fleet': 1, 'acceleration': 3.0, 'max_speed': 15.0, " +
       "'turn_speed': 120.0, 'max_health': 40.0, 'afterburner_max_time': 5.0, " +
       "'afterburner_force': 6.0, 'maxAngularAcceleration': 12.0, " +
       "'signature_resolution' : 40, " +
       "'weapon_1_damage': 7.0, 'weapon_1_cooldown': 8.0, 'weapon_1_tracking': 0.15, " +
       "'weapon_1_signature_radius': 30, 'weapon_1_optimal': 30.0, 'weapon_1_falloff': 30.0, " +
       "'weapon_2_damage': 4.0, 'weapon_2_cooldown': 5.0, 'weapon_2_tracking': 6.0, " +
       "'weapon_2_optimal': 7.0, 'weapon_2_falloff': 8.0}" +

            "]";
}