package com.rasanenj.warp;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gilead
 */
public class Log {
    private static final Logger logger;

    static {
        logger = Logger.getLogger("WarpGame");
    }

    public static void log(String msg) {
        log(Level.INFO, msg);
    }

    public static void log(Level level, String msg) {
        logger.log(level, msg);
    }

    public static void log(Object o) {
        log(o.toString());
    }

    public static void log(Level level, Object o) {
        log(level, o.toString());
    }
}
