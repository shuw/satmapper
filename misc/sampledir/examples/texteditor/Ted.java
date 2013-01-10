/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package examples.texteditor;

import java.awt.FileDialog;
import java.io.*;

/**
 *
 * @author
 * @version
 */
public class Ted extends javax.swing.JFrame {

    /** Initializes the Form */
    public Ted() {
        initComponents ();
        setSize(500,300);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        jMenuBar1 = new javax.swing.JMenuBar ();
        jMenu1 = new javax.swing.JMenu ();
        jMenuItem1 = new javax.swing.JMenuItem ();
        jMenuItem4 = new javax.swing.JMenuItem ();
        jMenuItem5 = new javax.swing.JMenuItem ();
        jMenuItem6 = new javax.swing.JMenuItem ();
        jMenuItem7 = new javax.swing.JMenuItem ();
        jMenu2 = new javax.swing.JMenu ();
        jMenuItem2 = new javax.swing.JMenuItem ();
        jMenu3 = new javax.swing.JMenu ();
        jMenuItem3 = new javax.swing.JMenuItem ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        textBox = new javax.swing.JTextArea ();

        jMenu1.setText ("File");

        jMenuItem1.setText ("New");
        jMenuItem1.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem1ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu1.add (jMenuItem1);
        jMenuItem4.setText ("Open ...");
        jMenuItem4.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem4ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu1.add (jMenuItem4);
        jMenuItem5.setText ("Save");
        jMenuItem5.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem5ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu1.add (jMenuItem5);
        jMenuItem6.setText ("Save As ...");
        jMenuItem6.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem6ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu1.add (jMenuItem6);
        jMenuItem7.setText ("Exit");
        jMenuItem7.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem7ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu1.add (jMenuItem7);
        jMenuBar1.add (jMenu1);
        jMenu2.setText ("Edit");

        jMenuItem2.setText ("Find ...");
        jMenuItem2.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem2ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu2.add (jMenuItem2);
        jMenuBar1.add (jMenu2);
        jMenu3.setText ("Help");

        jMenuItem3.setText ("About ...");
        jMenuItem3.addActionListener (new java.awt.event.ActionListener () {
                                          public void actionPerformed (java.awt.event.ActionEvent evt) {
                                              jMenuItem3ActionPerformed (evt);
                                          }
                                      }
                                     );

        jMenu3.add (jMenuItem3);
        jMenuBar1.add (jMenu3);
        setTitle ("Ted");
        addWindowListener (new java.awt.event.WindowAdapter () {
                               public void windowClosing (java.awt.event.WindowEvent evt) {
                                   exitForm (evt);
                               }
                           }
                          );



        jScrollPane1.setViewportView (textBox);


        getContentPane ().add (jScrollPane1, java.awt.BorderLayout.CENTER);

        setJMenuBar (jMenuBar1);

    }//GEN-END:initComponents

    private void jMenuItem5ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // Add your handling code here:
        if ("".equals(fileName))
            doSaveAs();
        else
            doSave(fileName);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem7ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        // Add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem2ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // Add your handling code here:
        new Finder (this, textBox).show();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // Add your handling code here:
        new About(this). show();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem6ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        // Add your handling code here:
        doSaveAs();
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // Add your handling code here:
        FileDialog fileDialog = new FileDialog (this, "Open...", FileDialog.LOAD);
        fileDialog.show ();
        if (fileDialog.getFile () == null)
            return;
        fileName = fileDialog.getDirectory () + File.separator + fileDialog.getFile ();

        FileInputStream fis = null;
        String str = null;
        try {
            fis = new FileInputStream (fileName);
            int size = fis.available ();
            byte[] bytes = new byte [size];
            fis.read (bytes);
            str = new String (bytes);
        } catch (IOException e) {
        } finally {
            try {
                fis.close ();
            } catch (IOException e2) {
            }
        }

        if (str != null)
            textBox.setText (str);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // Add your handling code here:
        fileName = "";
        textBox.setText ("");
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm

    private void doSave (String fileName) {
        FileOutputStream fos = null;
        String str = textBox.getText ();
        try {
            fos = new FileOutputStream (fileName);
            fos.write (str.getBytes ());
        } catch (IOException e) {
        } finally {
            try {
                fos.close ();
            } catch (IOException e2) {
            }
        }
    }

    private void doSaveAs () {
        FileDialog fileDialog = new FileDialog (this, "Save As...", FileDialog.SAVE);
        fileDialog.show ();
        if (fileDialog.getFile () == null)
            return;
        fileName = fileDialog.getDirectory () + File.separator + fileDialog.getFile ();

        doSave (fileName);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea textBox;
    // End of variables declaration//GEN-END:variables


    public static void main(java.lang.String[] args) {
        new Ted ().show ();
    }

    private String fileName = "";
}
