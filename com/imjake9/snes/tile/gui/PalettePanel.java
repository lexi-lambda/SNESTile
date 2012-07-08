package com.imjake9.snes.tile.gui;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JApplet;


public class PalettePanel extends JApplet {
    
    public PalettePanel() {
        setBackground(Color.BLACK);
    }
    
    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
    }
    
    public Color getColor(byte index) {
        return Color.getHSBColor((float)index / 16, 1, 1);
    }
    
}
