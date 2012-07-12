package com.imjake9.snes.tile;

import com.imjake9.snes.tile.PreferencesManager.PrefKey;
import com.imjake9.snes.tile.gui.DrawingPanel;
import com.imjake9.snes.tile.gui.DrawingPanel.Tool;
import com.imjake9.snes.tile.gui.PalettePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Insets;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import org.apache.commons.io.FileUtils;


public class SNESTile extends JFrame {
    
    private static SNESTile instance;
    
    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        SNESTile mainWindow = new SNESTile();
        instance = mainWindow;
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setSize(600, 500);
        mainWindow.setVisible(true);
    }
    
    public static SNESTile getInstance() {
        return instance;
    }
    
    private File currentFile;
    private int fileSize;
    private DrawingPanel drawingPanel;
    private PalettePanel palettePanel;
    private UndoManager undoManager = new UndoManager();
    private ToolsBarActionListener toolsBarActionListener;
    private UndoRedoListener undoRedoListener = new UndoRedoListener();
    
    public SNESTile() {
        super("SNESTile");
        setupGUI();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent ke) {
                if (ke.getID() != KeyEvent.KEY_PRESSED) {
                    return false;
                }
                if (ke.getModifiers() != 0) {
                    return false;
                }
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_M:
                        toolsBarActionListener.setSelectedTool(Tool.MARQUEE);
                        return true;
                    case KeyEvent.VK_B:
                        toolsBarActionListener.setSelectedTool(Tool.PENCIL);
                        return true;
                    case KeyEvent.VK_R:
                        toolsBarActionListener.setSelectedTool(Tool.FILL_RECT);
                        return true;
                    case KeyEvent.VK_T:
                        toolsBarActionListener.setSelectedTool(Tool.STROKE_RECT);
                        return true;
                    case KeyEvent.VK_C:
                        toolsBarActionListener.setSelectedTool(Tool.FILL_ELLIPSE);
                        return true;
                    case KeyEvent.VK_V:
                        toolsBarActionListener.setSelectedTool(Tool.STROKE_ELLIPSE);
                        return true;
                    case KeyEvent.VK_G:
                        toolsBarActionListener.toggleGrid();
                        return true;
                }
                return false;
            }
        });
    }
    
    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }
    
    public PalettePanel getPalettePanel() {
        return palettePanel;
    }
    
    public void addUndoableEdit(UndoableEditEvent uee) {
        undoManager.undoableEditHappened(uee);
        undoRedoListener.refreshMenuStates();
    }
    
    private void reloadFile() {
        if (currentFile == null) {
            drawingPanel.setData(new byte[fileSize]);
        } else {
            try {
                drawingPanel.setData(FileUtils.readFileToByteArray(currentFile));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error while reading file.", "Read Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Error: cannot save file while currentPath is unset.", "Save Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                FileUtils.writeByteArrayToFile(currentFile, drawingPanel.getData());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error while writing file.", "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void setupGUI() {
        setMinimumSize(new Dimension(300, 300));
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        drawingPanel = new DrawingPanel();
        JScrollPane drawingPane = new JScrollPane(drawingPanel);
        drawingPane.getViewport().setBackground(Color.BLACK);
        drawingPane.setMinimumSize(new Dimension(0, 100));
        drawingPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        drawingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        split.setTopComponent(drawingPane);
        
        palettePanel = new PalettePanel();
        drawingPanel.setPalette(palettePanel);
        palettePanel.setDrawingPanel(drawingPanel);
        JScrollPane palettePane = new JScrollPane(palettePanel);
        palettePane.getViewport().setBackground(Color.BLACK);
        palettePane.setMinimumSize(new Dimension(0, 100));
        palettePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        palettePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        split.setBottomComponent(palettePane);
        
        split.setResizeWeight(1);
        add(split, BorderLayout.CENTER);
        
        JToolBar toolsBar = setupToolsBar();
        add(toolsBar, BorderLayout.EAST);
        
        setJMenuBar(setupMenuBar());
    }
    
    private JToolBar setupToolsBar() {
        JToolBar toolsBar = new JToolBar();
        toolsBar.setOrientation(SwingConstants.VERTICAL);
        toolsBar.setRollover(true);
        toolsBar.setFloatable(false);
        toolsBar.setMargin(new Insets(3, 1, 1, 1));
        
        JButton marqueeButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/marquee.png")));
        marqueeButton.setName("MARQUEE");
        toolsBar.add(marqueeButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton pencilButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/pencil.png")));
        pencilButton.setName("PENCIL");
        pencilButton.setSelected(true);
        toolsBar.add(pencilButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton filledRectButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/filled-rect.png")));
        filledRectButton.setName("FILL_RECT");
        toolsBar.add(filledRectButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton emptyRectButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/empty-rect.png")));
        emptyRectButton.setName("STROKE_RECT");
        toolsBar.add(emptyRectButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton filledEllipseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/filled-ellipse.png")));
        filledEllipseButton.setName("FILL_ELLIPSE");
        toolsBar.add(filledEllipseButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton emptyEllipseButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/empty-ellipse.png")));
        emptyEllipseButton.setName("STROKE_ELLIPSE");
        toolsBar.add(emptyEllipseButton);
        
        toolsBar.addSeparator();
        
        JButton zoomOutButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/zoom-out.png")));
        zoomOutButton.setName("ZOOM_OUT");
        toolsBar.add(zoomOutButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton zoomInButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/zoom-in.png")));
        zoomInButton.setName("ZOOM_IN");
        toolsBar.add(zoomInButton);
        
        toolsBar.add(Box.createVerticalStrut(4));
        
        JButton gridButton = new JButton(new ImageIcon(getClass().getClassLoader().getResource("images/grid.png")));
        gridButton.setName("GRID");
        gridButton.setSelected(PreferencesManager.getBoolean(PrefKey.GRID_ENABLED, false));
        drawingPanel.setGridEnabled(gridButton.isSelected());
        toolsBar.add(gridButton);
        
        toolsBarActionListener = new ToolsBarActionListener(pencilButton);
        marqueeButton.addActionListener(toolsBarActionListener);
        pencilButton.addActionListener(toolsBarActionListener);
        filledRectButton.addActionListener(toolsBarActionListener);
        emptyRectButton.addActionListener(toolsBarActionListener);
        filledEllipseButton.addActionListener(toolsBarActionListener);
        emptyEllipseButton.addActionListener(toolsBarActionListener);
        zoomOutButton.addActionListener(toolsBarActionListener);
        zoomInButton.addActionListener(toolsBarActionListener);
        gridButton.addActionListener(toolsBarActionListener);
        
        return toolsBar;
    }
    
    private JMenuBar setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem item;
        
        final JFrame window = this;
        
        menu = new JMenu("File");
        item = new JMenuItem("New...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String input = (String) JOptionPane.showInputDialog(window, "Enter a size (in bytes) for the new document.", "New Document", JOptionPane.INFORMATION_MESSAGE);
                int size;
                try {
                    size = Integer.parseInt(input);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(window, "You must input a valid numeric size.", "Invalid Size", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                fileSize = size;
                currentFile = null;
                reloadFile();
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Open...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                FileDialog filePicker = new FileDialog(window, "Open GFX", FileDialog.LOAD);
                filePicker.setDirectory(PreferencesManager.getString(PrefKey.GFX_PATH));
                filePicker.setVisible(true);
                if (filePicker.getFile() == null) {
                    return;
                }
                File file = new File(filePicker.getDirectory() + File.separator + filePicker.getFile());
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(window, "The chosen file does not exist.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!file.canRead()) {
                    JOptionPane.showMessageDialog(window, "The selected file is not readable. You may not have permission to read from the selected location.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
                currentFile = file;
                PreferencesManager.set(PrefKey.GFX_PATH, filePicker.getDirectory());
                reloadFile();
            }
        });
        menu.add(item);
        item = new JMenuItem("Reload...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                reloadFile();
            }
        });
        menu.add(item);
        menu.addSeparator();
        item = new JMenuItem("Save");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (currentFile == null) {
                    FileDialog filePicker = new FileDialog(window, "Save GFX As", FileDialog.SAVE);
                    filePicker.setDirectory(PreferencesManager.getString(PrefKey.GFX_PATH));
                    filePicker.setVisible(true);
                    if (filePicker.getFile() == null) {
                        return;
                    }
                    File file = new File(filePicker.getDirectory() + File.separator + filePicker.getFile());
                    if (!file.canWrite()) {
                        JOptionPane.showMessageDialog(window, "The selected file is not writable. You may not have permission to write to that location.", "Invalid Location", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    currentFile = file;
                    PreferencesManager.set(PrefKey.GFX_PATH, filePicker.getDirectory());
                }
                saveFile();
            }
        });
        menu.add(item);
        item = new JMenuItem("Save As...");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                FileDialog filePicker = new FileDialog(window, "Save GFX As", FileDialog.SAVE);
                filePicker.setDirectory(PreferencesManager.getString(PrefKey.GFX_PATH));
                filePicker.setVisible(true);
                if (filePicker.getFile() == null) {
                    return;
                }
                File file = new File(filePicker.getDirectory() + File.separator + filePicker.getFile());
                if (file.exists() && !file.canWrite()) {
                    JOptionPane.showMessageDialog(window, "The selected file is not writable. You may not have permission to write to that location.", "Invalid Location", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                currentFile = file;
                PreferencesManager.set(PrefKey.GFX_PATH, filePicker.getDirectory());
                saveFile();
            }
        });
        menu.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Edit");
        item = new JMenuItem("Undo");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(undoRedoListener);
        undoRedoListener.setUndoItem(item);
        item.setEnabled(false);
        menu.add(item);
        item = new JMenuItem("Redo");
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        item.addActionListener(undoRedoListener);
        undoRedoListener.setRedoItem(item);
        item.setEnabled(false);
        menu.add(item);
        menuBar.add(menu);
        
        menu = new JMenu("Palette");
        item = new JMenuItem("Load Palette...");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JFileChooser filePicker = new JFileChooser();
                filePicker.setDialogTitle("Load Palette File");
                filePicker.setAcceptAllFileFilterUsed(false);
                String savedPath = PreferencesManager.getString(PrefKey.PALETTE_PATH);
                filePicker.setCurrentDirectory(savedPath == null ? null : new File(savedPath));
                filePicker.setFileFilter(new FileNameExtensionFilter("YY-CHR Palette Files (*.pal)", "pal"));
                int ret = filePicker.showOpenDialog(window);
                if (ret != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                File file = filePicker.getSelectedFile();
                if (!file.exists()) {
                    JOptionPane.showMessageDialog(window, "The chosen file does not exist.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!file.canRead()) {
                    JOptionPane.showMessageDialog(window, "The selected file is not readable. You may not have permission to read from the selected location.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    palettePanel.loadPalette(file);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(window, "Error while reading palette.", "Read Error", JOptionPane.ERROR_MESSAGE);
                }
                PreferencesManager.set(PrefKey.PALETTE_PATH, file.getPath());
            }
        });
        menu.add(item);
        item = new JMenuItem("Set Current Palette As Default");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                palettePanel.savePaletteAsDefault();
            }
        });
        menu.add(item);
        menuBar.add(menu);
        
        return menuBar;
    }
    
    class ToolsBarActionListener implements ActionListener {
        
        private JButton selected;
        
        public ToolsBarActionListener(JButton selected) {
            this.selected = selected;
        }
        
        public void setSelectedTool(Tool tool) {
            drawingPanel.setCurrentTool(tool);
            for (Component c : selected.getParent().getComponents()) {
                if (c.getName() != null && c.getName().equals(tool.name())) {
                    JButton button = (JButton) c;
                    selected.setSelected(false);
                    button.setSelected(true);
                    selected = button;
                    break;
                }
            }
        }
        
        public void toggleGrid() {
            drawingPanel.setGridEnabled(!drawingPanel.getGridEnabled());
            for (Component c : selected.getParent().getComponents()) {
                if (c.getName() != null && c.getName().equals("GRID")) {
                    ((JButton) c).setSelected(drawingPanel.getGridEnabled());
                    PreferencesManager.set(PrefKey.GRID_ENABLED, drawingPanel.getGridEnabled());
                }
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            JButton source = (JButton) ae.getSource();
            if (source.getName().equals("ZOOM_OUT")) {
                drawingPanel.decrementScalingFactor();
                return;
            }
            if (source.getName().equals("ZOOM_IN")) {
                drawingPanel.incrementScalingFactor();
                return;
            }
            if (source.getName().equals("GRID")) {
                drawingPanel.setGridEnabled(!drawingPanel.getGridEnabled());
                source.setSelected(drawingPanel.getGridEnabled());
                PreferencesManager.set(PrefKey.GRID_ENABLED, drawingPanel.getGridEnabled());
                return;
            }
            selected.setSelected(false);
            source.setSelected(true);
            selected = source;
            drawingPanel.setCurrentTool(source.getName());
            System.out.println(source.getName());
        }
        
    }
    
    class UndoRedoListener implements ActionListener {
        
        private JMenuItem undoItem;
        private JMenuItem redoItem;
        
        public void setUndoItem(JMenuItem undoItem) {
            this.undoItem = undoItem;
        }
        
        public void setRedoItem(JMenuItem redoItem) {
            this.redoItem = redoItem;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == undoItem) {
                undoManager.undo();
            } else {
                undoManager.redo();
            }
            refreshMenuStates();
        }
        
        public void refreshMenuStates() {
            undoItem.setEnabled(undoManager.canUndo());
            redoItem.setEnabled(undoManager.canRedo());
        }
        
    }
    
}
