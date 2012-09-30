package com.imjake9.snes.tile.data;

import com.imjake9.snes.tile.utils.Pair;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds SNES GFX data and provides methods for converting back
 * and forth between AWT Image objects.
 */
public class SNESImage {
    
    private byte[] rawData;
    private Palette palette;
    {
        Color[] colors = new Color[16];
        for (int i = 0; i < 16; i++) {
            colors[i] = new Color(0x111111 * i);
        }
        palette = new Palette(colors);
    }
    private BufferedImage buffer;
    private BufferedImage overlay;
    
    /**
     * Creates a new object based on an array of 4BPP palette values.
     * @param data 
     */
    public SNESImage(byte[] data) {
        rawData = data;
        rebuildBuffer();
    }
    
    /**
     * Rebuilds the entire BufferedImage buffer.
     */
    private void rebuildBuffer() {
        buffer = new BufferedImage(128, (rawData.length/128 + 7) / 8 * 8, BufferedImage.TYPE_BYTE_INDEXED, getColorModel());
        int rowPos = 0, colPos = 0;
        for (int i = 0; i < rawData.length; i++) {
            int tileRow = (i % 64) / 8;
            int tileCol = i % 8;
            
            if (i != 0 && tileRow == 0 && tileCol == 0) {
                colPos += 8;
                if (colPos > 15 * 8) {
                    colPos = 0;
                    rowPos += 8;
                }
            }
            
            buffer.setRGB(colPos + tileCol, rowPos + tileRow, palette.getColor(rawData[i]).getRGB());
        }
    }
    
    /**
     * Gets a representation of this object's SNES data as a BufferedImage.
     * You may make modifications to this object by using its Graphics object,
     * but remember to call {@link #commitChanges() commitChanges()}
     * to modify the underlying data.
     * @return image
     */
    public BufferedImage getImage() {
        if (overlay != null) {
            BufferedImage compound = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = compound.createGraphics();
            g.drawImage(buffer, 0, 0, null);
            g.drawImage(overlay, 0, 0, null);
            g.dispose();
            return compound;
        }
        
        return buffer;
    }
    
    /**
     * Gets the raw SNES data as an array of 4BPP palette values.
     * @return data
     */
    public byte[] getData() {
        return rawData;
    }
    
    public Palette getPalette() {
        return palette;
    }
    
    public void setPalette(Palette palette) {
        this.palette = palette;
        rebuildBuffer();
    }
    
    /**
     * Gets the associated palette value for a given RGB color.
     * @param color
     * @return index
     */
    public byte getIndexForColor(Color color) {
        for (byte i = 0; i < palette.size(); i++) {
            if (palette.getColor(i).equals(color))
                return (byte) i;
        }
        return 0;
    }
    
    /**
     * Creates a new Graphics2D object which paints to an "overlay" which will
     * be reflected in {@link #getImage() getImage()}. These changes will
     * not be reflected in the underlying data until commitChanges() is called.
     * @return graphics object
     */
    public Graphics2D createGraphics() {
        if (overlay == null)
            overlay = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
        return overlay.createGraphics();
    }
    
    /**
     * Rebuilds the underlying SNES data based on the current state of the overlay.
     * Returns a map of changed pixels.
     * @return change map
     */
    public Map<Point, Pair<Byte, Byte>> commitChanges() {
        Map<Point, Pair<Byte, Byte>> changeMap = new HashMap<Point, Pair<Byte, Byte>>();
        
        int rowPos = 0, colPos = 0;
        for (int i = 0; i < overlay.getWidth() * overlay.getHeight(); i++) {
            int tileRow = (i % 64) / 8;
            int tileCol = i % 8;
            
            if (i != 0 && tileRow == 0 && tileCol == 0) {
                colPos += 8;
                if (colPos > 15 * 8) {
                    colPos = 0;
                    rowPos += 8;
                }
            }
            
            int x = colPos + tileCol;
            int y = rowPos + tileRow;
            
            Color color = new Color(overlay.getRGB(x, y), true);
            if (color.getAlpha() == 0) continue;
            
            changeMap.put(new Point(x, y), new Pair<Byte, Byte>(getIndexForColor(new Color(buffer.getRGB(x, y))), getIndexForColor(color)));
            
            buffer.setRGB(x, y, color.getRGB());
            rawData[i] = getIndexForColor(color);
        }
        overlay = null;
        
        return changeMap;
    }
    
    /**
     * Generates an {@link java.awt.image.IndexColorModel IndexColorModel}
     * from the current palette array.
     * @return color model
     */
    private IndexColorModel getColorModel() {
        byte[] r = new byte[16], g = new byte[16], b = new byte[16];
        for (byte i = 0; i < 16; i++) {
            Color color = palette.getColor(i);
            r[i] = (byte) color.getRed();
            g[i] = (byte) color.getGreen();
            b[i] = (byte) color.getBlue();
        }
        return new IndexColorModel(4, 16, r, g, b);
    }
    
    /**
     * Sets the RGB value of a pixel directly. This affects both the buffer
     * and the underlying data. Note that you must pass an RGB actually specified
     * in the palette, or you may cause glitches and inconsistencies.
     * @param x
     * @param y
     * @param rgb 
     */
    public void setRGB(int x, int y, int rgb) {
        try {
            buffer.setRGB(x, y, rgb);
            int tile = x/8 + (y / 8)*16;
            int pixel = x%8 + (y%8)*8;
            rawData[tile*64 + pixel] = getIndexForColor(new Color(rgb));
        } catch (ArrayIndexOutOfBoundsException ex) {}
    }
    
}
