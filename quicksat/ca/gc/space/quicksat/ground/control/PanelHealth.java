/*
 * PanelHealth.java
 *
 * Created on September 6, 2001, 9:33 AM
 */

package ca.gc.space.quicksat.ground.control;

import javax.swing.*;
import ca.gc.space.quicksat.ground.util.*;

/**
 *
 * @author  jfcusson
 */
public class PanelHealth extends javax.swing.JPanel {
public final int STATUS_NO_CONTACT = 0;
public final int STATUS_OK = 1;
public final int STATUS_LOST_CONTACT = 2;
private int satStatus = STATUS_NO_CONTACT;
Log         log                  = null;    
private final String LED_OFF="/ca/gc/space/quicksat/ground/images/led_off.gif";
private final String LED_GREEN_ON="/ca/gc/space/quicksat/ground/images/led_green_on.gif";
private final String LED_RED_ON="/ca/gc/space/quicksat/ground/images/led_red_on.gif";
private final String LED_WHITE="/ca/gc/space/quicksat/ground/images/led_white.gif";
    
    /** Creates new form PanelHealth */
    public PanelHealth( Log log ) {
        initComponents();
        this.log = log;                
        if( this.log == null ) this.log = new Log();        
    }

    public void setSatName( String nameToSet ) {
        lblSatName.setText( nameToSet );
    }
    
    public void setSatStatus( int statusToSet ) {
    }
    
    public int getSatStatus() {
        return( satStatus );
    }
    
    public void setCriticalLog( String textToSet ) {
        txtFlightCritical.setText( textToSet );
        txtFlightCritical.setCaretPosition(txtFlightCritical.getText().length());
    }
    
    public void appendToCriticalLog( String textToAppend ) {
        txtFlightCritical.append(textToAppend);
        txtFlightCritical.setCaretPosition(txtFlightCritical.getText().length());
    }

    public void setWarningLog( String textToSet ) {
        txtFlightWarning.setText( textToSet );
        txtFlightWarning.setCaretPosition(txtFlightWarning.getText().length());
    }
    
    public void appendToWarningLog( String textToAppend ) {
        txtFlightWarning.append(textToAppend);
        txtFlightWarning.setCaretPosition(txtFlightWarning.getText().length());
    }
    
    public void setFlightLog( String textToSet ) {
        txtFlightLog.setText( textToSet );
        txtFlightLog.setCaretPosition(txtFlightLog.getText().length());
    }
    
    public void appendToFlightLog( String textToAppend ) {
        txtFlightLog.append(textToAppend);
        txtFlightLog.setCaretPosition(txtFlightLog.getText().length());
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
            jPanel2 = new javax.swing.JPanel();
            jPanel3 = new javax.swing.JPanel();
            lblSatName = new javax.swing.JLabel();
            jLabel1 = new javax.swing.JLabel();
            lblSatStatus = new javax.swing.JLabel();
            jPanel4 = new javax.swing.JPanel();
            panelFlightLog = new javax.swing.JPanel();
            jLabel4 = new javax.swing.JLabel();
            jScrollPane1 = new javax.swing.JScrollPane();
            txtFlightCritical = new javax.swing.JTextArea();
            jLabel5 = new javax.swing.JLabel();
            jScrollPane2 = new javax.swing.JScrollPane();
            txtFlightWarning = new javax.swing.JTextArea();
            jLabel6 = new javax.swing.JLabel();
            jScrollPane3 = new javax.swing.JScrollPane();
            txtFlightLog = new javax.swing.JTextArea();
            
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
            
            setPreferredSize(new java.awt.Dimension(790, 250));
            setMinimumSize(new java.awt.Dimension(0, 0));
            jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel2.setBackground(java.awt.Color.white);
            jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));
            
            lblSatName.setText("Satellite Name");
            lblSatName.setForeground(java.awt.Color.white);
            lblSatName.setBackground(java.awt.Color.black);
            lblSatName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblSatName.setFont(new java.awt.Font("Verdana", 1, 18));
            lblSatName.setPreferredSize(new java.awt.Dimension(100, 30));
            lblSatName.setAlignmentX(0.5F);
            lblSatName.setMaximumSize(new java.awt.Dimension(1000, 30));
            lblSatName.setOpaque(true);
            jPanel3.add(lblSatName);
            
            jLabel1.setText("Status");
            jLabel1.setForeground(java.awt.Color.white);
            jLabel1.setBackground(java.awt.Color.black);
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel1.setAlignmentX(0.5F);
            jLabel1.setMaximumSize(new java.awt.Dimension(1000, 17));
            jLabel1.setOpaque(true);
            jPanel3.add(jLabel1);
            
            lblSatStatus.setText("NO CONTACT");
            lblSatStatus.setForeground(java.awt.Color.white);
            lblSatStatus.setBackground(java.awt.Color.gray);
            lblSatStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblSatStatus.setFont(new java.awt.Font("Arial Black", 0, 12));
            lblSatStatus.setPreferredSize(new java.awt.Dimension(100, 50));
            lblSatStatus.setAlignmentX(0.5F);
            lblSatStatus.setMaximumSize(new java.awt.Dimension(1000, 1000));
            lblSatStatus.setOpaque(true);
            jPanel3.add(lblSatStatus);
            
            jPanel2.add(jPanel3);
          
          jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 15));
          
          jPanel2.add(jPanel4);
          
          add(jPanel2);
          
          panelFlightLog.setLayout(new javax.swing.BoxLayout(panelFlightLog, javax.swing.BoxLayout.Y_AXIS));
          
          panelFlightLog.setBackground(java.awt.Color.black);
          panelFlightLog.setPreferredSize(new java.awt.Dimension(500, 200));
          panelFlightLog.setOpaque(false);
          jLabel4.setText("Critical Events");
          jLabel4.setForeground(java.awt.Color.yellow);
          jLabel4.setBackground(new java.awt.Color(153, 0, 51));
          jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
          jLabel4.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          jLabel4.setAlignmentX(0.5F);
          jLabel4.setMaximumSize(new java.awt.Dimension(1000, 17));
          jLabel4.setOpaque(true);
          jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
          panelFlightLog.add(jLabel4);
          
          jScrollPane1.setViewportView(txtFlightCritical);
            
            panelFlightLog.add(jScrollPane1);
          
          jLabel5.setText("Warnings");
          jLabel5.setForeground(java.awt.Color.blue);
          jLabel5.setBackground(new java.awt.Color(204, 204, 0));
          jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
          jLabel5.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          jLabel5.setAlignmentX(0.5F);
          jLabel5.setMaximumSize(new java.awt.Dimension(1000, 17));
          jLabel5.setOpaque(true);
          panelFlightLog.add(jLabel5);
          
          jScrollPane2.setViewportView(txtFlightWarning);
            
            panelFlightLog.add(jScrollPane2);
          
          jLabel6.setText("Normal Activity Log");
          jLabel6.setForeground(java.awt.Color.white);
          jLabel6.setBackground(new java.awt.Color(0, 153, 102));
          jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
          jLabel6.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          jLabel6.setAlignmentX(0.5F);
          jLabel6.setMaximumSize(new java.awt.Dimension(1000, 17));
          jLabel6.setOpaque(true);
          panelFlightLog.add(jLabel6);
          
          jScrollPane3.setViewportView(txtFlightLog);
            
            panelFlightLog.add(jScrollPane3);
          
          add(panelFlightLog);
        
    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblSatName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblSatStatus;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel panelFlightLog;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtFlightCritical;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea txtFlightWarning;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea txtFlightLog;
    // End of variables declaration//GEN-END:variables
    /*------------------------------------------------------------------------*/
    /** Change the appearance of the button so that user knows that the
     *  function associated with this button is not known, but will be as soon
     *  as we receive telemetry information.
     *  @param button The swing button.                                       */
    /*------------------------------------------------------------------------*/
    private void showButtonInactive( JButton button ) {
    /*------------------------------------------------------------------------*/
     button.setIcon(
       new javax.swing.ImageIcon(
        getClass().getResource(LED_OFF)));
    }
    
    /*------------------------------------------------------------------------*/
    /** Change the appearance of the button so that user knows that the
     *  function associated with this button is "active" or "on".
     *  @param button The swing button.                                       */
    /*------------------------------------------------------------------------*/
    private void showButtonActiveON( JButton button ) {
    /*------------------------------------------------------------------------*/
     button.setIcon(
       new javax.swing.ImageIcon(
        getClass().getResource(LED_GREEN_ON)));
    }
    
    /*------------------------------------------------------------------------*/
    /** Change the appearance of the button so that user knows that the
     *  function associated with this button is "inactive" or "off".
     *  @param button The swing button.                                       */
    /*------------------------------------------------------------------------*/
    private void showButtonActiveOFF( JButton button ) {
    /*------------------------------------------------------------------------*/
     button.setIcon(
       new javax.swing.ImageIcon(
        getClass().getResource(LED_RED_ON)));
    }
    
    /*------------------------------------------------------------------------*/
    /** Change the appearance of the button so that user knows that the
     *  state of the function associated with this button is unknown.
     *  @param button The swing button.                                       */
    /*------------------------------------------------------------------------*/
    private void showButtonNeutral( JButton button ) {
    /*------------------------------------------------------------------------*/
     button.setIcon(
       new javax.swing.ImageIcon(
        getClass().getResource(LED_WHITE)));
    }
}