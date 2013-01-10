package com.csa.qks.vwr;

/*
 * Swing.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/* 
 * This applet displays a single image twice,
 * once at its normal size and once much wider.
 */

public class ImageDisplayer extends JApplet {
    static String mapFile = "mapHiRes.gif";
    static String satFile = "QuickSat.gif";
 
    public void init() {
       // Image satImg = getImage(getCodeBase(), satFile);
         //    Image mapImg = getImage(getCodeBase(), mapFile);
        ImagePanel imagePanel = new ImagePanel();
        getContentPane().add(imagePanel, BorderLayout.CENTER);
    }
    public static void main(String[] args) {
       SatMapper mapper = new SatMapper("c:\\netscape\\server4\\docs\\");
        //ImagePanel imagePanel = new ImagePanel();

        JFrame f = new JFrame("ImageDisplayer");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        f.getContentPane().add(mapper, BorderLayout.CENTER);
        f.setSize(new Dimension(550,100));
        f.setVisible(true);
    }
}
