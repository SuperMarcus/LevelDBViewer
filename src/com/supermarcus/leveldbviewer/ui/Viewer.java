package com.supermarcus.leveldbviewer.ui;

import org.iq80.leveldb.Options;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

public class Viewer {
    private JTextField findField;
    private JTextField dbPathField;
    private JButton openButton;
    private JList<DBItem> dataList;

    private JDialog dialog = new JDialog();

    private JPanel pane;
    private JTextField key;
    private JTextField value;
    private JButton putButton;
    private JTextArea hexValue;
    private JTextArea stringValue;
    private JLabel lengthLabel;
    private JLabel keyLength;
    private JLabel valueLength;
    private JTextArea hexKey;
    private JTextArea stringKey;
    private JButton deleteButton;
    private JButton saveButton;
    private JLabel notice;
    private JComboBox<PutType> putType;

    private boolean isSet = false;

    private JFileChooser leveldbStore = new JFileChooser();

    private Options options = null;

    public Viewer() {
        leveldbStore.setMultiSelectionEnabled(false);
        leveldbStore.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        putButton.setEnabled(false);
        key.setEnabled(false);
        value.setEnabled(false);
        findField.setEnabled(false);
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);
        putType.setEnabled(false);
        putType.setEditable(false);

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (openButton.isEnabled()) {
                    openButton.setEnabled(false);
                    new Thread() {
                        public void run() {
                            if (leveldbStore.showOpenDialog(pane) == JFileChooser.APPROVE_OPTION) {
                                File select = leveldbStore.getSelectedFile();
                                if (select.isDirectory()) {
                                    new OpenLevelDBDialog(Viewer.this, select);
                                    openDatabase(select);
                                    dbPathField.setText(select.getAbsolutePath());
                                } else {
                                    JOptionPane.showMessageDialog(pane, "The selecting item must be a directory", "Unable to load database", JOptionPane.WARNING_MESSAGE);
                                }
                            } else {
                                openButton.setEnabled(true);
                            }
                        }
                    }.start();
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dataList.getSelectedValue() != null) {
                    delete(dataList.getSelectedValue().key);
                }
                openDatabase(leveldbStore.getSelectedFile());
            }
        });

        putButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                put(((PutType) putType.getSelectedItem()).getBytes(key.getText()), ((PutType) putType.getSelectedItem()).getBytes(value.getText()));
                openDatabase(leveldbStore.getSelectedFile());
            }
        });

        findField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }
        });
        findField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }
        });
        findField.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }
        });

        hexKey.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(hexKey);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(hexKey);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(hexKey);
            }
        });
        hexKey.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                update(hexKey);
            }
        });

        stringKey.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(stringKey);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(stringKey);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(stringKey);
            }
        });
        stringKey.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                update(stringKey);
            }
        });

        hexValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(hexValue);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(hexValue);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(hexValue);
            }
        });
        hexValue.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                update(hexValue);
            }
        });

        stringValue.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(stringValue);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update(stringValue);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update(stringValue);
            }
        });
        stringValue.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                update(stringValue);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
                openDatabase(leveldbStore.getSelectedFile());
            }
        });

        dataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DBItem item = dataList.getSelectedValue();
                if (item != null) {
                    hexValue.setText(cutToLine(new BigInteger(item.value).toString(16), 64));
                    stringValue.setText(cutToLine(new String(item.value), 64));
                    hexKey.setText(cutToLine(new BigInteger(item.key).toString(16), 64));
                    stringKey.setText(cutToLine(new String(item.key), 64));

                    lengthLabel.setText(String.valueOf(item.value.length + item.key.length));
                    keyLength.setText(String.valueOf(item.key.length));
                    valueLength.setText(String.valueOf(item.value.length));
                }
            }
        });

        for(PutType t : PutType.values()){
            putType.addItem(t);
        }
        putType.setSelectedItem(PutType.STRING);
        putType.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }
        });
        putType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDatabase(leveldbStore.getSelectedFile());
            }
        });

        dialog.setLocationByPlatform(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(pane);
        dialog.setTitle("LevelDB Viewer By Marcus (https://github.com/SuperMarcus)");
        dialog.getRootPane().setDefaultButton(openButton);
        dialog.pack();
        dialog.setVisible(true);
    }

    public void update(JTextArea area){
        DBItem item = dataList.getSelectedValue();
        if(item != null && !isSet){
            isSet = true;
            try{
                if(area == hexKey){
                    if(area.getText().isEmpty()){
                        return;
                    }
                    item.key = new BigInteger(area.getText().replaceAll("\n", "").replaceAll("%\\{EOL}", "\n"), 16).toByteArray();
                    stringKey.setText(cutToLine(new String(item.key), 64));
                    dataList.updateUI();
                }else if(area == stringKey){
                    if(area.getText().isEmpty()){
                        return;
                    }
                    item.key = area.getText().replaceAll("\n", "").replaceAll("%\\{EOL}", "\n").getBytes();
                    hexKey.setText(cutToLine(new BigInteger(item.key).toString(16), 64));
                    dataList.updateUI();
                }else if(area == hexValue){
                    item.value = new BigInteger(area.getText().replaceAll("\n", "").replaceAll("%\\{EOL}", "\n"), 16).toByteArray();
                    stringValue.setText(cutToLine(new String(item.value), 64));
                }else if(area == stringValue){
                    item.value = area.getText().replaceAll("\n", "").replaceAll("%\\{EOL}", "\n").getBytes();
                    hexValue.setText(cutToLine(new BigInteger(item.value).toString(16), 64));
                }
                notice.setVisible(false);
                notice.setText("");
                saveButton.setEnabled(true);
            }catch (Exception e){
                notice.setVisible(true);
                notice.setText("Invalid number!");
            }finally {
                lengthLabel.setText(String.valueOf(item.value.length + item.key.length));
                keyLength.setText(String.valueOf(item.key.length));
                valueLength.setText(String.valueOf(item.value.length));
                isSet = false;
            }
        }
    }

    public void save(){
        boolean isNoticed = false;
        if(!notice.isVisible()){
            isNoticed = true;
            notice.setVisible(true);
            notice.setText("Saving...");
        }
        if(getOptions() != null){
            DB database = null;
            try{
                database = factory.open(this.leveldbStore.getSelectedFile(), getOptions());
                DBIterator iterator = database.iterator();
                HashSet<byte[]> keys = new HashSet<>();

                for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    keys.add(iterator.peekNext().getKey());
                }
                iterator.close();

                for(byte[] key : keys){
                    database.delete(key);
                }

                for(int i = 0; i < dataList.getModel().getSize(); ++i){
                    DBItem item = dataList.getModel().getElementAt(i);
                    database.put(item.key, item.value);
                }
            }catch (Exception e){
                JOptionPane.showMessageDialog(pane, "Unable to open database:\n" + e);
                e.printStackTrace();
            }finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(pane, "Unable to close database:\n" + e);
                        e.printStackTrace();
                    }
                }
                saveButton.setEnabled(false);
            }
        }
        if(isNoticed){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            notice.setVisible(false);
            notice.setText("");
        }
    }

    public void openDatabase(File select){
        if(getOptions() != null){
            DB database = null;
            try{
                database = factory.open(select, getOptions());
                DBIterator iterator = database.iterator();

                ArrayList<DBItem> data = new ArrayList<>();

                String reg = findField.getText().trim();

                for(iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    if(reg.isEmpty() || new BigInteger(iterator.peekNext().getKey()).toString(16).contains(reg) || new BigInteger(iterator.peekNext().getValue()).toString(16).contains(reg) || new String(iterator.peekNext().getKey()).contains(reg) || new String(iterator.peekNext().getValue()).contains(reg))
                        data.add(new DBItem(iterator.peekNext().getKey(), iterator.peekNext().getValue()));
                }

                iterator.close();

                dialog.getRootPane().setDefaultButton(putButton);

                dataList.getSelectionModel().clearSelection();
                dataList.setListData(data.toArray(new DBItem[data.size()]));

                putButton.setEnabled(true);
                key.setEnabled(true);
                value.setEnabled(true);
                findField.setEnabled(true);
                deleteButton.setEnabled(true);
                putType.setEnabled(true);
                //saveButton.setEnabled(true);

                hexValue.setText("");
                stringValue.setText("");
                hexKey.setText("");
                stringKey.setText("");

                lengthLabel.setText("");
                keyLength.setText("");
                valueLength.setText("");
            }catch (Exception e){
                JOptionPane.showMessageDialog(pane, "Unable to open database:\n" + e);
                e.printStackTrace();
            }finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(pane, "Unable to close database:\n" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void put(byte[] key, byte[] value){
        if(getOptions() != null){
            DB database = null;
            try{
                database = factory.open(this.leveldbStore.getSelectedFile(), getOptions());
                database.put(key, value);
            }catch (Exception e){
                JOptionPane.showMessageDialog(pane, "Unable to open database:\n" + e);
                e.printStackTrace();
            }finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(pane, "Unable to close database:\n" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void delete(byte[] key){
        if(getOptions() != null){
            DB database = null;
            try{
                database = factory.open(this.leveldbStore.getSelectedFile(), getOptions());
                database.delete(key);
            }catch (Exception e){
                JOptionPane.showMessageDialog(pane, "Unable to open database:\n" + e);
                e.printStackTrace();
            }finally {
                if (database != null) {
                    try {
                        database.close();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(pane, "Unable to close database:\n" + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public String cutToLine(String str, int lineChars){
        String eol = "\n";
        str = str.replace("\n", "%{EOL}");
        StringBuilder builder = new StringBuilder();
        int pointer = 0;
        for(char c : str.toCharArray()){
            if((++pointer % lineChars) == 0){
                builder.append(eol);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public class DBItem{
        private byte[] key;

        private byte[] value;

        public DBItem(byte[] key, byte[] value){
            this.key = key;
            this.value = value;
        }

        public String toString(){
            String s = ((PutType) putType.getSelectedItem()).toString(key);
            return s.length() > 8 ? s.substring(0, 8) + "..." : s;
        }
    }

    public enum PutType {
        STRING,
        HEX;

        public byte[] getBytes(String value){
            switch (this){
                case STRING:
                    return bytes(value);
                case HEX:
                    return new BigInteger(value, 16).toByteArray();
            }
            return new byte[0];
        }

        public String toString(byte[] bytes){
            switch (this){
                case STRING:
                    return asString(bytes);
                case HEX:
                    return new BigInteger(bytes).toString(16);
            }
            return "";
        }
    }
}
