package com.imjake9.snes.tile.utils;

import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;


public class GuiUtils {
    
    public static boolean isLeftClick(MouseEvent me) {
        return SwingUtilities.isLeftMouseButton(me) && !me.isControlDown();
    }
    
    public static boolean isRightClick(MouseEvent me) {
        return SwingUtilities.isRightMouseButton(me) || (SwingUtilities.isLeftMouseButton(me) && me.isControlDown());
    }
    
    public static boolean isMiddleClick(MouseEvent me) {
        return SwingUtilities.isMiddleMouseButton(me);
    }
    
    public static boolean isMenuShortcutKeyDown(MouseEvent me) {
        return me.getModifiers() == Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }
    
}
