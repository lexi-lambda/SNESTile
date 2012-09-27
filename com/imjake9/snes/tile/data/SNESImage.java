package com.imjake9.snes.tile.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 * Holds SNES GFX data and provides methods for converting back
 * and forth between AWT Image objects.
 */
public class SNESImage {
    
    private byte[] rawData;
    private Color[] palette = new Color[16];
    {
        for (int i = 0; i < 16; i++) {
            palette[i] = new Color(0x111111 * i);
        }
    }
    private BufferedImage buffer;
    
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
            
            buffer.setRGB(colPos + tileCol, rowPos + tileRow, palette[rawData[i]].getRGB());
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
        return buffer;
    }
    
    /**
     * Gets the raw SNES data as an array of 4BPP palette values.
     * @return data
     */
    public byte[] getData() {
        return rawData;
    }
    
    public Color[] getPalette() {
        return palette;
    }
    
    public void setPalette(Color[] palette) {
        this.palette = palette;
        rebuildBuffer();
    }
    
    /**
     * Gets the associated palette value for a given RGB color.
     * @param color
     * @return index
     */
    public byte getIndexForColor(Color color) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i].equals(color))
                return (byte) i;
        }
        return 0;
    }
    
    /**
     * Rebuilds the underlying SNES data based on modifications to the buffer.
     */
    public void commitChanges() {
        rawData = new byte[buffer.getWidth() * buffer.getHeight()];
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
            
            rawData[i] = getIndexForColor(new Color(buffer.getRGB(colPos + tileCol, rowPos + tileRow)));
        }
    }
    
    /**
     * Generates an {@link java.awt.image.IndexColorModel IndexColorModel}
     * from the current palette array.
     * @return color model
     */
    private IndexColorModel getColorModel() {
        byte[] r = new byte[16], g = new byte[16], b = new byte[16];
        for (int i = 0; i < 16; i++) {
            Color color = palette[i];
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
