package com.csa.qks.serv;

import java.awt.image.*;
import java.awt.Graphics;
import javax.swing.*;
import java.io.*;
import java.applet.*;
import com.csa.qks.jpg.*;
import com.csa.qks.vwr.*;
/**
 * Insert the type's description here.
 * Creation date: (3/12/2002 1:29:42 PM)
 * @author: 
 */
public class JpgWriter extends Thread{
	SatMapper myMapper; 
/**
 * JpgWritter constructor comment.
 * @param applet com.csa.qks.vwr.SatMapper
 */
public JpgWriter(SatMapper myMapper) {
this.myMapper = myMapper;
this.setName("JPG Encoder");
this.start();

}
/**
 * Insert the method's description here.
 * Creation date: (3/12/2002 1:31:09 PM)
 */
public void run() {
    File firstFile = null;
    File secondFile = null;
    File dir = null;
    dir = new File(AppEnvironment.getInstance().getImagepath());
    
    System.out.println("The JPG Writter is now online using : " + dir + " as directory for created images.");

    BufferedImage bi = new java.awt.image.BufferedImage(800, 600, java.awt.image.BufferedImage.TYPE_INT_RGB);

    while (true) {

        myMapper.createImg(bi.getGraphics());

        try {
            this.sleep(120000); //Encode a new picture every 2 minutes
        } catch (Exception inter) {
            inter.printStackTrace();
        }

        if (myMapper.isPainted) {
            try {


              
                if (dir.isDirectory()) {
                    firstFile = new File(dir + "\\mapImg.jpg");
                    secondFile = new File(dir + "\\map.jpg");
                       
                }

                java.io.FileOutputStream out = new java.io.FileOutputStream(firstFile);

                JpegEncoder jpg = new JpegEncoder(this.myMapper.offscreenImage, 20, out);
                jpg.Compress();
                out.close();

            } catch (Exception io) {
                System.out.println("error from encoder or firstfile");
                io.printStackTrace();
            }

            //  File renamedFile = new File("c:\\TEMP\\map.jpg");
            if (secondFile != null) {
                if (secondFile.exists()) {
                    secondFile.delete();
                }
            }
            // secondFile = new File(dir + "\\mapImg2.jpg");
try{
            boolean done = firstFile.renameTo(new File(dir + "\\map.jpg"));
}
catch(Exception rn){
	
	System.out.println("Could not rename the image file correctly, the thread will now terminate");
	rn.printStackTrace();
	Thread.currentThread().stop();
	
	}
        }
    }

}
}
