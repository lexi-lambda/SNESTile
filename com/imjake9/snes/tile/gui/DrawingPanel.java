package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.DataConverter;
import com.imjake9.snes.tile.SNESTile;
import com.imjake9.snes.tile.utils.GuiUtils;
import com.imjake9.snes.tile.utils.Pair;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.undo.AbstractUndoableEdit;


public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener, Scrollable {
    
    private BufferedImage buffer;
    private BufferedImage overlay;
    private PalettePanel palette;
    private byte[] data;
    private int scalingFactor = 2;
    private Tool currentTool = Tool.MARQUEE;
    private boolean gridEnabled;
    
    public DrawingPanel() {
        this.setBackground(Color.BLACK);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
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
    
    public int getScalingFactor() {
        return scalingFactor;
    }
    
    public void setCurrentTool(Tool tool) {
        currentTool = tool;
    }
    
    public void setCurrentTool(String tool) {
        currentTool = Tool.valueOf(tool);
    }
    
    public Tool getCurrentTool() {
        return currentTool;
    }
    
    public void setGridEnabled(boolean gridEnabled) {
        this.gridEnabled = gridEnabled;
        repaint();
    }
    
    public boolean getGridEnabled() {
        return gridEnabled;
    }
    
    public void setPixelColor(Point location, byte index) {
        Color color = palette.getColor(index);
        buffer.setRGB(location.x, location.y, color.getRGB());
        
        int tile = location.x/8 + (location.y / 8)*16;
        int pixel = location.x%8 + (location.y%8)*8;
        data[tile*64 + pixel] = index;
        
        repaint();
    }
    
    private byte getPixelColor(Point location) {
        int tile = location.x/8 + (location.y / 8)*16;
        int pixel = location.x%8 + (location.y%8)*8;
        return data[tile*64 + pixel];
    }
    
    public Graphics2D getOverlay() {
        return overlay.createGraphics();
    }
    
    public void clearOverlay() {
        Graphics2D g = getOverlay();
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0x00000000, true));
        g.fillRect(0, 0, overlay.getWidth(), overlay.getHeight());
        g.setComposite(c);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        if (palette == null || data == null || buffer == null || overlay == null) {
            return;
        }
        
        g.drawImage(buffer, 0, 0, buffer.getWidth() * scalingFactor, buffer.getHeight() * scalingFactor, null);
        g.drawImage(overlay, 0, 0, overlay.getWidth() * scalingFactor, overlay.getHeight() * scalingFactor, null);
        
        if (gridEnabled) {
            int interval = scalingFactor > 8 ? 4
                         : scalingFactor > 4 ? 8
                                             : 16;
            for (int i = 0; i < buffer.getWidth() * scalingFactor; i += interval * scalingFactor) {
                if (i % (16 * scalingFactor) == 0) {
                    g.setColor(new Color(0xFFFFFFFF, true));
                } else {
                    g.setColor(new Color(0x7FFFFFFF, true));
                }
                g.drawLine(i, 0, i, buffer.getHeight() * scalingFactor);
            }
            for (int i = 0; i < buffer.getHeight() * scalingFactor; i += interval * scalingFactor) {
                if (i % (16 * scalingFactor) == 0) {
                    g.setColor(new Color(0xFFFFFFFF, true));
                } else {
                    g.setColor(new Color(0x7FFFFFFF, true));
                }
                g.drawLine(0, i, buffer.getWidth() * scalingFactor, i);
            }
        }
    }
    
    public void repaintAll() {
        if (data == null) {
            return;
        }
        Dimension size = recalculatePreferredSize();
        buffer = new BufferedImage(size.width / scalingFactor, size.height / scalingFactor, BufferedImage.TYPE_INT_RGB);
        overlay = new BufferedImage(buffer.getWidth(), buffer.getHeight(), BufferedImage.TYPE_INT_ARGB);
        
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
            
            buffer.setRGB(colPos + tileCol, rowPos + tileRow, palette.getColor(data[i]).getRGB());
        }
        
        repaint();
    }
    
    private Dimension recalculatePreferredSize() {
        Dimension size = new Dimension(8 * 16 * scalingFactor, (data.length/(8 * 16) + 7) / 8 * 8 * scalingFactor);
        setPreferredSize(size);
        JViewport viewport = (JViewport) getParent();
        viewport.doLayout();
        return size;
    }
    
    public void incrementScalingFactor() {
        scalingFactor *= 2;
        boolean rescale = true;
        Point initialPoint = ((JViewport) getParent()).getViewPosition();
        if (scalingFactor > 32) {
            rescale = false;
            scalingFactor = 32;
        }
        repaintAll();
        if (rescale) {
            ((JViewport) getParent()).setViewPosition(new Point(initialPoint.x, initialPoint.y * 2));
        }
    }
    
    public void decrementScalingFactor() {
        scalingFactor /= 2;
        boolean rescale = true;
        Point initialPoint = ((JViewport) getParent()).getViewPosition();
        if (scalingFactor < 1) {
            rescale = false;
            scalingFactor = 1;
        }
        repaintAll();
        if (rescale) {
            ((JViewport) getParent()).setViewPosition(new Point(initialPoint.x, initialPoint.y / 2));
        }
    }
    
    private Point getSelectedPixel(MouseEvent me) {
        return new Point(me.getX()/scalingFactor, me.getY()/scalingFactor);
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseClicked(getSelectedPixel(me));
        } else if (GuiUtils.isRightClick(me)) {
            palette.setCurrentColor(getPixelColor(getSelectedPixel(me)));
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseDown(getSelectedPixel(me));
            currentTool.mouseDragged(getSelectedPixel(me));
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseUp(getSelectedPixel(me));
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}
    
    @Override
    public void mouseDragged(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseDragged(getSelectedPixel(me));
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent me) {}
    
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
    
    public static enum Tool {
        MARQUEE("Marquee"),
        PENCIL("Pencil") {
            @Override
            public void mouseDragged(Point location) {
                SNESTile window = SNESTile.getInstance();
                window.getDrawingPanel().setPixelColor(location, window.getPalettePanel().getCurrentColor());
            }
        },
        FILL_RECT("Fill Rectangle") {
            private Point rectStart;
            @Override
            public void mouseDown(Point location) {
                rectStart = location;
            }
            @Override
            public void mouseDragged(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Graphics2D g = panel.getOverlay();
                Rectangle rect = getDrawableRect(rectStart, location);
                g.setColor(palette.getColor(palette.getCurrentColor()));
                g.fillRect(rect.x, rect.y, rect.width + 1, rect.height + 1);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Rectangle rect = getDrawableRect(rectStart, location);
                for (int i = rect.x; i < rect.x + rect.width + 1; i++) {
                    for (int j = rect.y; j < rect.y + rect.height + 1; j++) {
                        panel.setPixelColor(new Point(i, j), palette.getCurrentColor());
                    }
                }
                panel.repaint();
            }
        },
        STROKE_RECT("Stroke Rectangle") {
            private Point rectStart;
            @Override
            public void mouseDown(Point location) {
                rectStart = location;
            }
            @Override
            public void mouseDragged(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Graphics2D g = panel.getOverlay();
                Rectangle rect = getDrawableRect(rectStart, location);
                g.setColor(palette.getColor(palette.getCurrentColor()));
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Rectangle rect = getDrawableRect(rectStart, location);
                for (int i = rect.x; i < rect.x + rect.width; i++) {
                    panel.setPixelColor(new Point(i, rect.y), palette.getCurrentColor());
                    panel.setPixelColor(new Point(i, rect.y + rect.height), palette.getCurrentColor());
                }
                for (int i = rect.y; i < rect.y + rect.height; i++) {
                    panel.setPixelColor(new Point(rect.x, i), palette.getCurrentColor());
                    panel.setPixelColor(new Point(rect.x + rect.width, i), palette.getCurrentColor());
                }
                panel.setPixelColor(new Point(rect.x + rect.width, rect.y + rect.height), palette.getCurrentColor());
                panel.repaint();
            }
        },
        FILL_ELLIPSE("Fill Ellipse"),
        STROKE_ELLIPSE("Stroke Ellipse");
        
        private static Rectangle getDrawableRect(Point a, Point b) {
            return new Rectangle(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x), Math.abs(a.y - b.y));
        }
        
        private final String displayName;
        
        Tool(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void mouseClicked(Point location) {}
        public void mouseDown(Point location) {}
        public void mouseUp(Point location) {}
        public void mouseDragged(Point location) {}
    }
    
    public static class PaintAction extends AbstractUndoableEdit {
        
        protected final Map<Point, Pair<Byte, Byte>> modifiedPixels;
        private final Tool tool;
        
        public PaintAction(Map<Point, Pair<Byte, Byte>> modifiedPixels) {
            this(modifiedPixels, null);
        }
        
        public PaintAction(Map<Point, Pair<Byte, Byte>> modifiedPixels, Tool tool) {
            this.modifiedPixels = modifiedPixels;
            this.tool = tool;
        }
        
        @Override
        public void undo() {
            super.undo();
            DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
            for (Entry<Point, Pair<Byte, Byte>> entry : modifiedPixels.entrySet()) {
                panel.setPixelColor(entry.getKey(), entry.getValue().getLeft());
            }
        }
        
        @Override
        public void redo() {
            super.redo();
            DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
            for (Entry<Point, Pair<Byte, Byte>> entry : modifiedPixels.entrySet()) {
                panel.setPixelColor(entry.getKey(), entry.getValue().getRight());
            }
        }
        
        @Override
        public String getPresentationName() {
            return tool == null ? "Draw" : tool.getDisplayName();
        }
        
    }
    
}
