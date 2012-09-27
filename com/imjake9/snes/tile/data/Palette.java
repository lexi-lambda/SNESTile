package com.imjake9.snes.tile.data;

import java.awt.Color;

/**
 * Represents a single SNES 4BPP palette.
 */
public class Palette {
    
    private Color[] colors;
    
    /**
     * Creates a new palette with the given colors.
     * @param colors 
     */
    public Palette(Color[] colors) {
        this.colors = colors;
    }
    
    /**
     * Gets the number of colors in this palette.
     * @return size
     */
    public byte size() {
        return (byte) colors.length;
    }
    
    /**
     * Overwrites a color in the palette.
     * @param index
     * @param color 
     */
    public void setColor(byte index, Color color) {
        colors[index] = color;
    }
    
    /**
     * Gets a color from this palette at the given index.
     * @param index
     * @return color
     */
    public Color getColor(byte index) {
        return colors[index];
    }
    
}
