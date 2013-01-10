/*
 * WarningFrame.java
 *
 * Created on March 28, 2002, 12:56 PM
 */

package com.csa.qks.vwr;

/**
 *
 * @author  TWu
 */
public class WarningFrame extends javax.swing.JFrame {

    /** Creates new form WarningFrame */
    public WarningFrame() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jLabel2 = new javax.swing.JLabel();
        okButton = new javax.swing.JButton();
        
        getContentPane().setLayout(new java.awt.FlowLayout());
        
        setTitle("Warning");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        jLabel2.setText("WARNING LOCAL VERSION");
        getContentPane().add(jLabel2);
        
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(okButton);
        
        pack();
    }//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        this.setVisible(false);
        this.hide();
    }//GEN-LAST:event_okButtonActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new WarningFrame().show();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel2;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
