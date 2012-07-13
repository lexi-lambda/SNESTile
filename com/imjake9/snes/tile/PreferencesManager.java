package com.imjake9.snes.tile;

import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;


public class PreferencesManager {
    
    private static final Preferences prefs = Preferences.userNodeForPackage(SNESTile.class);
    private static final Preferences keys = prefs.node("KEYBOARD");
    
    public static void set(PrefKey key, String value) {
        prefs.put(key.name(), value);
    }
    
    public static void set(PrefKey key, boolean value) {
        prefs.putBoolean(key.name(), value);
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
    
    public static boolean getBoolean(PrefKey key) {
        return getBoolean(key, false);
    }
    
    public static boolean getBoolean(PrefKey key, boolean def) {
        return prefs.getBoolean(key.name(), def);
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
        DEFAULT_PALETTES,
        GRID_ENABLED;
    }
    
    public static enum KeyboardShortcut {
        TOOL_MARQUEE(KeyEvent.VK_M),
        TOOL_PENCIL(KeyEvent.VK_B),
        TOOL_FILL_RECT(KeyEvent.VK_R),
        TOOL_STROKE_RECT(KeyEvent.VK_T),
        TOOL_FILL_ELLIPSE(KeyEvent.VK_C),
        TOOL_STROKE_ELLIPSE(KeyEvent.VK_V),
        TOGGLE_GRID(KeyEvent.VK_G);
        
        private int def;
        private int shortcut;
        
        KeyboardShortcut(int def) {
            this.def = def;
            shortcut = keys.getInt(name() + "/key", def);
        }
        
        public void setShortcut(int shortcutKey) {
            keys.putInt(name() + "/key", shortcutKey == def ? null : shortcutKey);
            shortcut = shortcutKey;
        }
        
        public int getShortcut() {
            return shortcut;
        }
        
    }
    
}
