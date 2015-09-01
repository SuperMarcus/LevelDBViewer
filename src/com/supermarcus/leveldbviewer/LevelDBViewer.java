package com.supermarcus.leveldbviewer;

import com.supermarcus.leveldbviewer.ui.Viewer;

import java.math.BigInteger;

public class LevelDBViewer {
    public static boolean DEFAULT_SINGED = false;

    public static void main(String args[]){
        new Viewer();
    }

    public static String toHexString(byte[] bytes){
        return DEFAULT_SINGED ? new BigInteger(bytes).toString(16) : new BigInteger(1, bytes).toString(16);
    }
}
