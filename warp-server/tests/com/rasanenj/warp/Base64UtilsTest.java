package com.rasanenj.warp;

import static junit.framework.Assert.assertEquals;

/**
 * @author gilead
 */
public class Base64UtilsTest {
    private static final String CHARSET = "UTF-8";

    @org.junit.Test
    public void testToAndFromBase64() throws Exception {
        byte[] arr = new String("gilead").getBytes(CHARSET);
        String base64 = Base64Utils.toBase64(arr);
        arr = Base64Utils.fromBase64(base64);
        String name = new String(arr, CHARSET);
        assertEquals("gilead", name);
    }
}
