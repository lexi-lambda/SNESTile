package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.PreferencesManager;
import com.imjake9.snes.tile.PreferencesManager.PrefKey;
import com.imjake9.snes.tile.data.Palette;
import com.imjake9.snes.tile.data.PaletteSet;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;


public class PalettePanel extends JPanel implements MouseListener, Scrollable {
    
    private DrawingPanel drawingPanel;
    private PaletteSet palettes;
    
    public PalettePanel() {
        byte[] savedPalette = PreferencesManager.getByteArray(PrefKey.DEFAULT_PALETTES);
        if (savedPalette != null) {
            palettes = new PaletteSet(savedPalette, PaletteSet.PaletteFormat.PAL);
        }
        addMouseListener(this);
    }
    
    public void setDrawingPanel(DrawingPanel drawingPanel) {
        this.drawingPanel = drawingPanel;
    }
    
    public PaletteSet getPaletteSet() {
        return palettes;
    }
    
    public void setPaletteSet(PaletteSet palettes) {
        this.palettes = palettes;
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        recalculatePreferredSize();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        if (palettes == null) {
            return;
        }
        
        int squareSize = getWidth() / 16;
        for (byte i = 0; i < palettes.size(); i++) {
            for (byte j = 0; j < 16; j++) {
                g.setColor(palettes.getPalette(i).getColor(j));
                g.fillRect(j * squareSize, i * squareSize, squareSize, squareSize);
            }
        }
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.drawRect(0, palettes.getSelectedPaletteIndex() * squareSize, 16*squareSize - 1, squareSize - 1);
        g.fillOval(squareSize*palettes.getSelectedColorIndex() + squareSize/2 - 4, squareSize*palettes.getSelectedPaletteIndex() + squareSize/2 - 4, 7, 7);
        g.setColor(Color.BLACK);
        g.drawOval(squareSize*palettes.getSelectedColorIndex() + squareSize/2 - 5, squareSize*palettes.getSelectedPaletteIndex() + squareSize/2 - 5, 8, 8);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    
    private Dimension recalculatePreferredSize() {
        if (palettes == null) {
            return new Dimension(0, 0);
        }
        Dimension size = new Dimension(getWidth(), (getWidth() / 16) * palettes.size());
        setPreferredSize(size);
        JViewport viewport = (JViewport) getParent();
        viewport.doLayout();
        return size;
    }
    
    
    @Override
    public void mouseClicked(MouseEvent me) {
        int squareSize = getWidth() / 16;
        Palette oldPalette = palettes.getSelectedPalette();
        palettes.setSelectedPalette((byte) (me.getY() / squareSize));
        if (palettes.getSelectedPaletteIndex() > palettes.size()) {
            palettes.setSelectedPalette((byte) (palettes.size() - 1));
        }
        palettes.setSelectedColor((byte) (me.getX() / squareSize));
        if (palettes.getSelectedColorIndex() > 0xF) {
            palettes.setSelectedColor((byte) 0xF);
        }
        repaint();
        if (oldPalette != palettes.getSelectedPalette()) {
            drawingPanel.repaintAll();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle rctngl, int i, int i1) {
        return getWidth() / 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle rctngl, int i, int i1) {
        return getWidth() / 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
}
