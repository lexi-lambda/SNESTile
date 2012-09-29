package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.SNESTile;
import com.imjake9.snes.tile.data.DataConverter;
import com.imjake9.snes.tile.data.SNESImage;
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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;


public class DrawingPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, Scrollable {
    
    private SNESImage image;
    private BufferedImage overlay;
    private PalettePanel palette;
    private int scalingFactor = 2;
    private Tool currentTool = Tool.PENCIL;
    private boolean gridEnabled;
    
    public DrawingPanel() {
        this.setBackground(Color.BLACK);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addMouseWheelListener(this);
    }
    
    public void setPalette(PalettePanel palette) {
        this.palette = palette;
    }
    
    public void setData(byte[] data) {
        image = new SNESImage(DataConverter.fromSNES4BPP(data));
        repaintAll();
    }
    
    public byte[] getData() {
        return DataConverter.toSNES4BPP(image.getData());
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
        Color color = palette.getPaletteSet().getSelectedPalette().getColor(index);
        image.setRGB(location.x, location.y, color.getRGB());
        repaint();
    }
    
    private byte getPixelColor(Point location) {
        try {
            return image.getIndexForColor(new Color(image.getImage().getRGB(location.x, location.y)));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 0;
        }
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
        
        if (palette == null || image == null || image.getImage() == null || overlay == null) {
            return;
        }
        
        g.drawImage(image.getImage(), 0, 0, image.getImage().getWidth() * scalingFactor, image.getImage().getHeight() * scalingFactor, null);
        g.drawImage(overlay, 0, 0, overlay.getWidth() * scalingFactor, overlay.getHeight() * scalingFactor, null);
        
        if (gridEnabled) {
            int interval = scalingFactor > 8 ? 4
                         : scalingFactor > 4 ? 8
                                             : 16;
            for (int i = 0; i < image.getImage().getWidth() * scalingFactor; i += interval * scalingFactor) {
                if (i % (16 * scalingFactor) == 0) {
                    g.setColor(new Color(0xFFFFFFFF, true));
                } else {
                    g.setColor(new Color(0x7FFFFFFF, true));
                }
                g.drawLine(i, 0, i, image.getImage().getHeight() * scalingFactor);
            }
            for (int i = 0; i < image.getImage().getHeight() * scalingFactor; i += interval * scalingFactor) {
                if (i % (16 * scalingFactor) == 0) {
                    g.setColor(new Color(0xFFFFFFFF, true));
                } else {
                    g.setColor(new Color(0x7FFFFFFF, true));
                }
                g.drawLine(0, i, image.getImage().getWidth() * scalingFactor, i);
            }
        }
    }
    
    public void repaintAll() {
        Dimension size = recalculatePreferredSize();
        image.setPalette(palette.getPaletteSet().getSelectedPalette());
        overlay = new BufferedImage(image.getImage().getWidth(), image.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
        repaint();
    }
    
    private Dimension recalculatePreferredSize() {
        Dimension size = new Dimension(8 * 16 * scalingFactor, (image.getData().length/(8 * 16) + 7) / 8 * 8 * scalingFactor);
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
    
    private Point previousLocation;
    
    @Override
    public void mouseClicked(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseClicked(getSelectedPixel(me));
        } else if (GuiUtils.isRightClick(me)) {
            palette.getPaletteSet().setSelectedColor(getPixelColor(getSelectedPixel(me)));
            palette.repaint();
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (GuiUtils.isLeftClick(me)) {
            currentTool.mouseDown(getSelectedPixel(me));
            currentTool.mouseDragged(getSelectedPixel(me));
        } else if (GuiUtils.isMiddleClick(me)) {
            previousLocation = me.getLocationOnScreen();
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
        } else if (GuiUtils.isMiddleClick(me) && previousLocation != null) {
            JViewport viewport = (JViewport) getParent();
            Point newLocation = me.getLocationOnScreen();
            Rectangle viewRect = viewport.getViewRect();
            viewRect.x += previousLocation.x - newLocation.x;
            viewRect.y += previousLocation.y - newLocation.y;
            scrollRectToVisible(viewRect);
            previousLocation = newLocation;
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent me) {}
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent mwe) {
        if (GuiUtils.isMenuShortcutKeyDown(mwe)) {
            if (mwe.getWheelRotation() < 0) {
                incrementScalingFactor();
            } else {
                decrementScalingFactor();
            }
        } else {
            getParent().getParent().dispatchEvent(mwe);
        }
    }
    
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
            private Map<Point, Pair<Byte, Byte>> actionMap;
            @Override
            public void mouseDown(Point location) {
                actionMap = new HashMap<Point, Pair<Byte, Byte>>();
            }
            @Override
            public void mouseDragged(Point location) {
                SNESTile window = SNESTile.getInstance();
                if (!actionMap.containsKey(location))
                    actionMap.put(location, new Pair<Byte, Byte>(window.getDrawingPanel().getPixelColor(location), window.getPalettePanel().getPaletteSet().getSelectedColorIndex()));
                window.getDrawingPanel().setPixelColor(location, window.getPalettePanel().getPaletteSet().getSelectedColorIndex());
            }
            @Override
            public void mouseUp(Point location) {
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
                actionMap = null;
            }
        },
        LINE("Line") {
            private Point lineStart;
            @Override
            public void mouseDown(Point location) {
                lineStart = location;
            }
            @Override
            public void mouseDragged(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Graphics2D g = panel.getOverlay();
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.drawLine(lineStart.x, lineStart.y, location.x, location.y);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                Rectangle rect = getDrawableRect(lineStart, location);
                for (int i = rect.x; i < rect.x + rect.width + 1; i++) {
                    for (int j = rect.y; j < rect.y + rect.height + 1; j++) {
                        Point p = new Point(i, j);
                        if (!actionMap.containsKey(p) && p.x >= 0 && p.y >= 0)
                            actionMap.put(p, new Pair<Byte, Byte>(panel.getPixelColor(p), palette.getPaletteSet().getSelectedColorIndex()));
                    }
                }
                Graphics2D g = panel.image.getImage().createGraphics();
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.drawLine(lineStart.x, lineStart.y, location.x, location.y);
                panel.image.commitChanges();
                panel.repaint();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
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
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.fillRect(rect.x, rect.y, rect.width + 1, rect.height + 1);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                Rectangle rect = getDrawableRect(rectStart, location);
                for (int i = rect.x; i < rect.x + rect.width + 1; i++) {
                    for (int j = rect.y; j < rect.y + rect.height + 1; j++) {
                        Point p = new Point(i, j);
                        if (!actionMap.containsKey(p))
                            actionMap.put(p, new Pair<Byte, Byte>(panel.getPixelColor(p), palette.getPaletteSet().getSelectedColorIndex()));
                        panel.setPixelColor(p, palette.getPaletteSet().getSelectedColorIndex());
                    }
                }
                panel.repaint();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
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
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                Rectangle rect = getDrawableRect(rectStart, location);
                for (int i = rect.x; i < rect.x + rect.width; i++) {
                    Point p1 = new Point(i, rect.y);
                    Point p2 = new Point(i, rect.y + rect.height);
                    if (!actionMap.containsKey(p1))
                        actionMap.put(p1, new Pair<Byte, Byte>(panel.getPixelColor(p1), palette.getPaletteSet().getSelectedColorIndex()));
                    if (!actionMap.containsKey(p2))
                        actionMap.put(p2, new Pair<Byte, Byte>(panel.getPixelColor(p2), palette.getPaletteSet().getSelectedColorIndex()));
                    panel.setPixelColor(p1, palette.getPaletteSet().getSelectedColorIndex());
                    panel.setPixelColor(p2, palette.getPaletteSet().getSelectedColorIndex());
                }
                for (int i = rect.y; i < rect.y + rect.height; i++) {
                    Point p1 = new Point(rect.x, i);
                    Point p2 = new Point(rect.x + rect.width, i);
                    if (!actionMap.containsKey(p1))
                        actionMap.put(p1, new Pair<Byte, Byte>(panel.getPixelColor(p1), palette.getPaletteSet().getSelectedColorIndex()));
                    if (!actionMap.containsKey(p2))
                        actionMap.put(p2, new Pair<Byte, Byte>(panel.getPixelColor(p2), palette.getPaletteSet().getSelectedColorIndex()));
                    panel.setPixelColor(p1, palette.getPaletteSet().getSelectedColorIndex());
                    panel.setPixelColor(p2, palette.getPaletteSet().getSelectedColorIndex());
                }
                Point p = new Point(rect.x + rect.width, rect.y + rect.height);
                if (!actionMap.containsKey(p))
                    actionMap.put(p, new Pair<Byte, Byte>(panel.getPixelColor(p), palette.getPaletteSet().getSelectedColorIndex()));
                panel.setPixelColor(p, palette.getPaletteSet().getSelectedColorIndex());
                panel.repaint();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
            }
        },
        FILL_ELLIPSE("Fill Ellipse") {
            private Point ellipseStart;
            @Override
            public void mouseDown(Point location) {
                ellipseStart = new Point(location.x - 1, location.y - 1);
            }
            @Override
            public void mouseDragged(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Graphics2D g = panel.getOverlay();
                Rectangle rect = getDrawableRect(ellipseStart, location);
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.fillOval(rect.x, rect.y, rect.width, rect.height);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                Rectangle rect = getDrawableRect(ellipseStart, location);
                for (int i = rect.x; i < rect.x + rect.width + 1; i++) {
                    for (int j = rect.y; j < rect.y + rect.height + 1; j++) {
                        Point p = new Point(i, j);
                        if (!actionMap.containsKey(p) && p.x >= 0 && p.y >= 0)
                            actionMap.put(p, new Pair<Byte, Byte>(panel.getPixelColor(p), palette.getPaletteSet().getSelectedColorIndex()));
                    }
                }
                Graphics2D g = panel.image.getImage().createGraphics();
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.fillOval(rect.x, rect.y, rect.width, rect.height);
                panel.image.commitChanges();
                panel.repaint();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
            }
        },
        STROKE_ELLIPSE("Stroke Ellipse") {
            private Point ellipseStart;
            @Override
            public void mouseDown(Point location) {
                ellipseStart = new Point(location.x - 1, location.y - 1);
            }
            @Override
            public void mouseDragged(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Graphics2D g = panel.getOverlay();
                Rectangle rect = getDrawableRect(ellipseStart, location);
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.drawOval(rect.x, rect.y, rect.width, rect.height);
                panel.repaint();
            }
            @Override
            public void mouseUp(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                panel.clearOverlay();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                Rectangle rect = getDrawableRect(ellipseStart, location);
                for (int i = rect.x; i < rect.x + rect.width + 1; i++) {
                    for (int j = rect.y; j < rect.y + rect.height + 1; j++) {
                        Point p = new Point(i, j);
                        if (!actionMap.containsKey(p) && p.x >= 0 && p.y >= 0)
                            actionMap.put(p, new Pair<Byte, Byte>(panel.getPixelColor(p), palette.getPaletteSet().getSelectedColorIndex()));
                    }
                }
                Graphics2D g = panel.image.getImage().createGraphics();
                g.setColor(palette.getPaletteSet().getSelectedColor());
                g.drawOval(rect.x, rect.y, rect.width, rect.height);
                panel.image.commitChanges();
                panel.repaint();
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
            }
        },
        FILL("Fill") {
            @Override
            public void mouseClicked(Point location) {
                DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
                PalettePanel palette = SNESTile.getInstance().getPalettePanel();
                byte target = panel.getPixelColor(location);
                byte replacement = palette.getPaletteSet().getSelectedColorIndex();
                LinkedList<Point> openPixels = new LinkedList<Point>();
                Map<Point, Pair<Byte, Byte>> actionMap = new HashMap<Point, Pair<Byte, Byte>>();
                openPixels.addFirst(location);
                Point node;
                while ((node = openPixels.poll()) != null) {
                    if (panel.getPixelColor(node) == target) {
                        panel.setPixelColor(node, replacement);
                        if (!actionMap.containsKey(node))
                            actionMap.put(node, new Pair<Byte, Byte>(target, replacement));
                        openPixels.addFirst(new Point(node.x - 1, node.y));
                        openPixels.addFirst(new Point(node.x + 1, node.y));
                        openPixels.addFirst(new Point(node.x, node.y - 1));
                        openPixels.addFirst(new Point(node.x, node.y + 1));
                    }
                }
                SNESTile.getInstance().addUndoableEdit(new UndoableEditEvent(this, new DrawAction(actionMap, this)));
            }
        };
        
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
    
    public static class DrawAction extends AbstractUndoableEdit {
        
        protected final Map<Point, Pair<Byte, Byte>> modifiedPixels;
        private final Tool tool;
        
        public DrawAction(Map<Point, Pair<Byte, Byte>> modifiedPixels) {
            this(modifiedPixels, null);
        }
        
        public DrawAction(Map<Point, Pair<Byte, Byte>> modifiedPixels, Tool tool) {
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
            panel.repaint();
        }
        
        @Override
        public void redo() {
            super.redo();
            DrawingPanel panel = SNESTile.getInstance().getDrawingPanel();
            for (Entry<Point, Pair<Byte, Byte>> entry : modifiedPixels.entrySet()) {
                panel.setPixelColor(entry.getKey(), entry.getValue().getRight());
            }
            panel.repaint();
        }
        
        @Override
        public String getPresentationName() {
            return tool == null ? "Draw" : tool.getDisplayName();
        }
        
    }
    
}
