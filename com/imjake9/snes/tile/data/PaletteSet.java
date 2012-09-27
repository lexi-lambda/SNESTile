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
        Palette[] palettes = null;
        
        // Handle various file formats
        if (filetype.equalsIgnoreCase("pal")) {
            palettes = new Palette[data.length / 48];
            for (int i = 0; i < palettes.length; i++) {
                Color[] colors = new Color[16];
                for (int j = 0; j < colors.length; j++) {
                    colors[j] = new Color((int) data[i*3*16 + j*3] & 0xFF, (int) data[i*3*16 + j*3 + 1] & 0xFF, (int) data[i*3*16 + j*3 + 2] & 0xFF);
                }
                palettes[i] = new Palette(colors);
            }
        }
        
        return new PaletteSet(palettes);
    }
    
    /**
     * Creates a new palette set with the given palettes.
     * @param palettes 
     */
    public PaletteSet(Palette[] palettes) {
        this.palettes = palettes;
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
    
}
