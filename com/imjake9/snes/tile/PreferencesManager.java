package com.imjake9.snes.tile;

import java.util.prefs.Preferences;


public class PreferencesManager {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(SNESTile.class);
    
    public static void set(PrefKey key, String value) {
        prefs.put(key.name(), value);
    }
    
    public static void set(PrefKey key, byte[] value) {
        prefs.putByteArray(key.name(), value);
    }
    
    public static String getString(PrefKey key) {
        return getString(key, null);
    }
    
    public static String getString(PrefKey key, String def) {
        return prefs.get(key.name(), def);
    }
    
    public static byte[] getByteArray(PrefKey key) {
        return getByteArray(key, null);
    }
    
    public static byte[] getByteArray(PrefKey key, byte[] def) {
        return prefs.getByteArray(key.name(), def);
    }
    
    public static enum PrefKey {
        GFX_PATH,
        PALETTE_PATH,
        DEFAULT_PALETTES;
    }
    
}
