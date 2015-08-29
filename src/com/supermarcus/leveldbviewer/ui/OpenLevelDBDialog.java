package com.supermarcus.leveldbviewer.ui;

import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.Options;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

public class OpenLevelDBDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField databasePath;
    private JCheckBox createBox;
    private JComboBox<CompressionType> compressType;
    private JSpinner maxOpenFiles;
    private JCheckBox verifyChecksumsCheckBox;
    private JCheckBox paranoidChecksCheckBox;

    private Viewer viewer;

    public OpenLevelDBDialog(Viewer viewer, File select) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.viewer = viewer;

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        databasePath.setText(select.getAbsolutePath());

        for(CompressionType t : CompressionType.values()){
            compressType.addItem(t);
        }

        compressType.setSelectedItem(CompressionType.NONE);

        maxOpenFiles.setValue(1000);

        this.pack();
        this.setVisible(true);
    }

    private void onOK() {
        Options options = new Options();
        options.createIfMissing(this.createBox.isSelected());
        options.compressionType((CompressionType) this.compressType.getSelectedItem());

        try{
            options.maxOpenFiles(Integer.parseInt(this.maxOpenFiles.getValue().toString()));
        }catch (NumberFormatException e){
            options.maxOpenFiles(1000);
        }

        options.verifyChecksums(this.verifyChecksumsCheckBox.isSelected());
        options.paranoidChecks(this.paranoidChecksCheckBox.isSelected());

        this.viewer.setOptions(options);

        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
