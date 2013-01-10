package com.csa.qks.vwr;

/*
 * Swing.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class ImagePanel extends JPanel {
    Image image;
	   Image mapImg;

    Image satGen;
public ImagePanel() {
    satGen = Toolkit.getDefaultToolkit().getImage(ImageDisplayer.satFile);
     mapImg = Toolkit.getDefaultToolkit().getImage(ImageDisplayer.mapFile);
}
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background

        //Draw image at its natural size first.
        g.drawImage(this.satGen, 0, 0, this); //85x62 image
          g.drawImage(this.mapImg, 0, 0, this); //85x62 image

        //Now draw the image scaled.
        //g.drawImage(image, 90, 0, 300, 62, this);
    }
}
