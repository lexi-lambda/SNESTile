package com.imjake9.snes.tile.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JApplet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;


public class PalettePanel extends JApplet implements MouseListener {
    
    private DrawingPanel drawingPanel;
    private Color[][] palettes;
    private int currentPalette;
    
    public PalettePanel() {
        setBackground(Color.BLACK);
        addMouseListener(this);
    }
    
    public void setDrawingPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }
    
    public void loadPalette(File f) throws IOException {
        String filetype = FilenameUtils.getExtension(f.getName());
        byte[] data = FileUtils.readFileToByteArray(f);
        if (filetype.equalsIgnoreCase("pal")) {
            palettes = new Color[data.length / (3*16)][16];
            for (int i = 0; i < palettes.length; i++) {
                for (int j = 0; j < 16; j++) {
                    palettes[i][j] = new Color((int) data[i*3*16 + j*3] & 0xFF, (int) data[i*3*16 + j*3 + 1] & 0xFF, (int) data[i*3*16 + j*3 + 2] & 0xFF);
                }
            }
        }
        currentPalette = 0;
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        if (palettes == null) {
            return;
        }
        
        int squareSize = getWidth() / 16;
        for (int i = 0; i < palettes.length; i++) {
            for (int j = 0; j < 16; j++) {
                g.setColor(palettes[i][j]);
                g.fillRect(j * squareSize, i * squareSize, squareSize, squareSize);
            }
        }
        g.setColor(Color.WHITE);
        g.drawRect(0, currentPalette * squareSize, 16*squareSize - 1, squareSize - 1);
    }
    
    public Color getColor(byte index) {
        return palettes[currentPalette][index];
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        int squareSize = getWidth() / 16;
        currentPalette = me.getY() / squareSize;
        if (currentPalette > palettes.length) {
            currentPalette = palettes.length - 1;
        }
        repaint();
        drawingPanel.repaintAll();
    }

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}
    
}
