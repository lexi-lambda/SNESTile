package com.imjake9.snes.tile.gui;

import com.imjake9.snes.tile.PreferencesManager.KeyboardShortcut;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


public class PreferencesPanel extends JFrame implements KeyListener {
    
    private JTable keyTable;
    private JTextField keyField;
    private JButton setButton;
    private boolean isSetting;
    
    public PreferencesPanel() {
        setLayout(new BorderLayout());
        
        keyTable = new JTable();
        keyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyTable.setRowSelectionAllowed(true);
        keyTable.setColumnSelectionAllowed(false);
        keyTable.setFillsViewportHeight(true);
        keyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int row = keyTable.getSelectedRow();
                if (row == -1) {
                    setButton.setEnabled(false);
                    keyField.setText("");
                    return;
                }
                setButton.setEnabled(true);
                keyField.setText((String) keyTable.getValueAt(row, 1));
            }
        });
        JScrollPane scrollPane = new JScrollPane(keyTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        keyField = new JTextField();
        keyField.setEditable(false);
        controlPanel.add(keyField, BorderLayout.CENTER);
        setButton = new JButton("Set");
        setButton.setEnabled(false);
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                isSetting = true;
            }
        });
        controlPanel.add(setButton, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);
        
        addKeyListener(this);
    }
    
    private void updateLayout() {
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(new Object[] {"Shortcut Action", "Shortcut Key"});
        for (KeyboardShortcut shortcut : KeyboardShortcut.values()) {
            model.addRow(new Object[] {shortcut.toString(), KeyEvent.getKeyText(shortcut.getShortcut())});
        }
        keyTable.setModel(model);
        pack();
    }
    
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            updateLayout();
        }
        super.setVisible(visible);
    }
    
    public boolean isSetting() {
        return isSetting;
    }

    @Override
    public void keyTyped(KeyEvent ke) {}

    @Override
    public void keyPressed(KeyEvent ke) {}

    @Override
    public void keyReleased(KeyEvent ke) {
        int row = keyTable.getSelectedRow();
        if (!isSetting || row == -1) {
            return;
        }
        KeyboardShortcut.values()[row].setShortcut(ke.getKeyCode());
        isSetting = false;
        updateLayout();
    }
    
}
