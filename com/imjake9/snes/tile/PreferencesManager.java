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
        TOOL_MARQUEE(KeyEvent.VK_M, "Marquee Tool"),
        TOOL_PENCIL(KeyEvent.VK_B, "Pencil Tool"),
        TOOL_LINE(KeyEvent.VK_N, "Line Tool"),
        TOOL_FILL_RECT(KeyEvent.VK_R, "Fill Rectangle Tool"),
        TOOL_STROKE_RECT(KeyEvent.VK_T, "Stroke Rectangle Tool"),
        TOOL_FILL_ELLIPSE(KeyEvent.VK_C, "Fill Ellipse Tool"),
        TOOL_STROKE_ELLIPSE(KeyEvent.VK_V, "Stroke Ellipse Tool"),
        TOOL_FILL(KeyEvent.VK_F, "Fill Tool"),
        TOGGLE_GRID(KeyEvent.VK_G, "Toggle Grid");
        
        private int def;
        private int shortcut;
        private String displayName;
        
        KeyboardShortcut(int def, String displayName) {
            this.def = def;
            shortcut = keys.getInt(name() + "/key", def);
            this.displayName = displayName;
        }
        
        public void setShortcut(int shortcutKey) {
            if (shortcutKey == def) {
                keys.remove(name() + "/key");
            } else {
                keys.putInt(name() + "/key", shortcutKey);
            }
            shortcut = shortcutKey;
        }
        
        public int getShortcut() {
            return shortcut;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
        
    }
    
}
