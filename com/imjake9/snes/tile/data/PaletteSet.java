package com.imjake9.snes.tile.data;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Represents a set of SNES 4BPP palettes.
 */
public class PaletteSet {
    
    public static enum PaletteFormat {
        PAL;
    }
    
    private Palette[] palettes;
    private byte selectedPalette;
    private byte selectedColor;
    
    /**
     * Loads a palette set from a file.
     * @param f
     * @return palette set
     * @throws IOException 
     */
    public static PaletteSet loadFile(File f) throws IOException {
        String filetype = FilenameUtils.getExtension(f.getName());
        byte[] data = FileUtils.readFileToByteArray(f);
        return new PaletteSet(data, PaletteFormat.valueOf(filetype.toUpperCase()));
    }
    
    /**
     * Creates a new palette set with the given palettes.
     * @param palettes 
     */
    public PaletteSet(Palette[] palettes) {
        this.palettes = palettes;
    }
    
    /**
     * Creates a palette set from a byte array of raw data and a palette format.
     * @param data
     * @param format 
     */
    public PaletteSet(byte[] data, PaletteFormat format) {
        switch (format) {
        case PAL:
            palettes = new Palette[data.length / 48];
            for (int i = 0; i < palettes.length; i++) {
                Color[] colors = new Color[16];
                for (int j = 0; j < colors.length; j++) {
                    colors[j] = new Color((int) data[i*3*16 + j*3] & 0xFF, (int) data[i*3*16 + j*3 + 1] & 0xFF, (int) data[i*3*16 + j*3 + 2] & 0xFF);
                }
                palettes[i] = new Palette(colors);
            }
            break;
        }
    }
    
    /**
     * Converts a palette set into raw data using a palette format.
     * @param format
     * @return data
     */
    public byte[] toByteArray(PaletteFormat format) {
        switch (format) {
        case PAL:
            byte[] data = new byte[size() * 16 * 3];
            for (byte i = 0; i < size(); i++) {
                for (byte j = 0; j < 16; j++) {
                    Color color = getPalette(i).getColor(j);
                    data[i*3*16 + j*3] = (byte) color.getRed();
                    data[i*3*16 + j*3 + 1] = (byte) color.getGreen();
                    data[i*3*16 + j*3 + 2] = (byte) color.getBlue();
                }
            }
            return data;
        }
        return null;
    }
    
    /**
     * Gets the number of palettes in this pallete set.
     * @return size
     */
    public byte size() {
        return (byte) palettes.length;
    }
    
    /**
     * Gets a palette from this palette set at the given index.
     * @param index
     * @return palette
     */
    public Palette getPalette(byte index) {
        return palettes[index];
    }
    
    /**
     * Sets the currently selected palette number.
     * @param index 
     */
    public void setSelectedPalette(byte index) {
        selectedPalette = index;
    }
    
    /**
     * Gets the current palette.
     * @return palette
     */
    public Palette getSelectedPalette() {
        return palettes[selectedPalette];
    }
    
    /**
     * Gets the index of the current palette.
     * @return 
     */
    public byte getSelectedPaletteIndex() {
        return selectedPalette;
    }
    
    /**
     * Sets the currently selected color.
     * @param index 
     */
    public void setSelectedColor(byte index) {
        selectedColor = index;
    }
    
    /**
     * Gets the current color.
     * @return color
     */
    public Color getSelectedColor() {
        return getSelectedPalette().getColor(selectedColor);
    }
    
    /**
     * Gets the index of the current color.
     * @return 
     */
    public byte getSelectedColorIndex() {
        return selectedColor;
    }
    
}
