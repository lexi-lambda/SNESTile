package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.DataConverter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JViewport;


public class DrawingPanel extends JPanel {
    
    private BufferedImage buffer;
    private PalettePanel palette;
    private byte[] data;
    private int scalingFactor = 2;
    
    public DrawingPanel() {
        this.setBackground(Color.BLACK);
    }
    
    public void setPalette(PalettePanel palette) {
        this.palette = palette;
    }
    
    public void setData(byte[] data) {
        this.data = DataConverter.fromSNES4BPP(data);
        repaintAll();
    }
    
    public byte[] getData() {
        return DataConverter.toSNES4BPP(data);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        if (palette == null || data == null || buffer == null) {
            return;
        }
        
        g.drawImage(buffer, 0, 0, null);
    }
    
    public void repaintAll() {
        Dimension size = recalculatePreferredSize();
        buffer = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
        
        Graphics b = buffer.createGraphics();
        b.fillRect(0, 0, buffer.getWidth(), buffer.getHeight());
        
        int rowPos = 0, colPos = 0;
        for (int i = 0; i < data.length; i++) {
            int tileRow = (i % 64) / 8;
            int tileCol = i % 8;
            
            if (i != 0 && tileRow == 0 && tileCol == 0) {
                colPos += 8;
                if (colPos > 15 * 8) {
                    colPos = 0;
                    rowPos += 8;
                }
            }
            
            b.setColor(palette.getColor(data[i]));
            b.fillRect((colPos + tileCol) * scalingFactor, (rowPos + tileRow) * scalingFactor, scalingFactor, scalingFactor);
        }
        
        repaint();
    }
    
    private Dimension recalculatePreferredSize() {
        Dimension size = new Dimension(8 * 16 * scalingFactor, (data.length / (8 * 16)) * scalingFactor);
        setPreferredSize(size);
        JViewport viewport = (JViewport) getParent();
        viewport.doLayout();
        return size;
    }
    
    public void incrementScalingFactor() {
        scalingFactor *= 2;
        if (scalingFactor > 32) {
            scalingFactor = 32;
        }
        repaintAll();
    }
    
    public void decrementScalingFactor() {
        scalingFactor /= 2;
        if (scalingFactor < 1) {
            scalingFactor = 1;
        }
        repaintAll();
    }
    
}
