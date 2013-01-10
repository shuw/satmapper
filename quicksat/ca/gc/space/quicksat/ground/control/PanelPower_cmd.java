/*
 * PanelPower.java
 *
 * Created on July 25, 2001, 4:39 PM
 */

package ca.gc.space.quicksat.ground.control;

import javax.swing.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.util.*;

/**
 *
 * @author  jfcusson
 */
public class PanelPower_cmd extends javax.swing.JPanel {
ServerLink  srvLnk               = null;
Satellite   sat                  = null;
Log         log                  = null;
private final String LED_OFF="/ca/gc/space/quicksat/ground/images/led_off.gif";
private final String LED_GREEN_ON="/ca/gc/space/quicksat/ground/images/led_green_on.gif";
private final String LED_RED_ON="/ca/gc/space/quicksat/ground/images/led_red_on.gif";
private final String LED_WHITE="/ca/gc/space/quicksat/ground/images/led_white.gif";
    
    /** Creates new form PanelPower */
    public PanelPower_cmd( Log log ) {
        initComponents();
        this.log = log;                
        if( this.log == null ) this.log = new Log();
    }
    /*========================================================================*/    
    /** Set the object that is in charge of managing the link with the ground
     *  station server.
     *  @param srvLnk ServerLink used to communicate with the server.     */
    /*========================================================================*/
    public void setServerLink( ServerLink srvLnk ) {
    /*========================================================================*/    
        this.srvLnk = srvLnk;
    }
    
    /*========================================================================*/
    /** Set the object representing the satellite that we want to communicate
     *  with, and understanding the format of the commands.
     *  @param sat Satellite object representing our satellite.               */
    /*========================================================================*/
    public void setSatellite( Satellite sat ) {
    /*========================================================================*/    
        this.sat = sat;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
            panelPower = new javax.swing.JPanel();
            jPanel225 = new javax.swing.JPanel();
            jLabel161 = new javax.swing.JLabel();
            lblPanelsCommandMode = new javax.swing.JLabel();
            jPanel248 = new javax.swing.JPanel();
            jPanel226 = new javax.swing.JPanel();
            jLabel134 = new javax.swing.JLabel();
            btnPanel1Normal = new javax.swing.JButton();
            btnPanel1Shutdown = new javax.swing.JButton();
            jPanel227 = new javax.swing.JPanel();
            jLabel135 = new javax.swing.JLabel();
            btnPanel2Normal = new javax.swing.JButton();
            btnPanel2Shutdown = new javax.swing.JButton();
            jPanel150 = new javax.swing.JPanel();
            jLabel160 = new javax.swing.JLabel();
            jPanel151 = new javax.swing.JPanel();
            jLabel61 = new javax.swing.JLabel();
            btnDAC1ON = new javax.swing.JButton();
            btnDAC1OFF = new javax.swing.JButton();
            jLabel66 = new javax.swing.JLabel();
            txtDAC1DutyCycle = new javax.swing.JTextField();
            jLabel67 = new javax.swing.JLabel();
            slidDAC1DutyCycle = new javax.swing.JSlider();
            btnDAC1Set = new javax.swing.JButton();
            jPanel152 = new javax.swing.JPanel();
            jLabel64 = new javax.swing.JLabel();
            btnDAC2ON = new javax.swing.JButton();
            btnDAC2OFF = new javax.swing.JButton();
            jLabel68 = new javax.swing.JLabel();
            txtDAC2DutyCycle = new javax.swing.JTextField();
            jLabel70 = new javax.swing.JLabel();
            slidDAC2DutyCycle = new javax.swing.JSlider();
            btnDAC2Set = new javax.swing.JButton();
            
            setLayout(new java.awt.BorderLayout());
            
            panelPower.setPreferredSize(new java.awt.Dimension(700, 150));
            panelPower.setMinimumSize(new java.awt.Dimension(700, 101));
            jPanel225.setLayout(new javax.swing.BoxLayout(jPanel225, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel225.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel225.setPreferredSize(new java.awt.Dimension(700, 75));
            jPanel225.setMinimumSize(new java.awt.Dimension(700, 63));
            jLabel161.setText(" SOLAR PANELS POWER OUTPUT ");
            jLabel161.setForeground(java.awt.Color.yellow);
            jLabel161.setBackground(java.awt.Color.blue);
            jLabel161.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel161.setFont(new java.awt.Font("Arial Black", 0, 14));
            jLabel161.setPreferredSize(new java.awt.Dimension(137, 21));
            jLabel161.setMinimumSize(new java.awt.Dimension(137, 21));
            jLabel161.setAlignmentX(0.5F);
            jLabel161.setMaximumSize(new java.awt.Dimension(2000, 21));
            jLabel161.setOpaque(true);
            jPanel225.add(jLabel161);
            
            lblPanelsCommandMode.setText("Prime Commands");
            lblPanelsCommandMode.setForeground(java.awt.Color.yellow);
            lblPanelsCommandMode.setBackground(java.awt.Color.blue);
            lblPanelsCommandMode.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblPanelsCommandMode.setFont(new java.awt.Font("Verdana", 2, 12));
            lblPanelsCommandMode.setAlignmentX(0.5F);
            lblPanelsCommandMode.setMaximumSize(new java.awt.Dimension(1000, 21));
            lblPanelsCommandMode.setOpaque(true);
            jPanel225.add(lblPanelsCommandMode);
            
            jPanel248.setLayout(new javax.swing.BoxLayout(jPanel248, javax.swing.BoxLayout.X_AXIS));
                
                jPanel248.setPreferredSize(new java.awt.Dimension(620, 35));
                jPanel226.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
                
                jPanel226.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                jPanel226.setPreferredSize(new java.awt.Dimension(310, 44));
                jLabel134.setText(" PANEL #1 ");
                jLabel134.setForeground(java.awt.Color.white);
                jLabel134.setBackground(java.awt.Color.darkGray);
                jLabel134.setFont(new java.awt.Font("Arial", 1, 12));
                jLabel134.setOpaque(true);
                jPanel226.add(jLabel134);
                
                btnPanel1Normal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnPanel1Normal.setText("NORMAL");
                btnPanel1Normal.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnPanel1NormalActionPerformed(evt);
                    }
                });
                
                jPanel226.add(btnPanel1Normal);
                
                btnPanel1Shutdown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnPanel1Shutdown.setText("SHUT DOWN");
                btnPanel1Shutdown.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnPanel1ShutdownActionPerformed(evt);
                    }
                });
                
                jPanel226.add(btnPanel1Shutdown);
                
                jPanel248.add(jPanel226);
              
              jPanel227.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
                
                jPanel227.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                jPanel227.setPreferredSize(new java.awt.Dimension(310, 44));
                jLabel135.setText(" PANEL #2 ");
                jLabel135.setForeground(java.awt.Color.white);
                jLabel135.setBackground(java.awt.Color.darkGray);
                jLabel135.setFont(new java.awt.Font("Arial", 1, 12));
                jLabel135.setOpaque(true);
                jPanel227.add(jLabel135);
                
                btnPanel2Normal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnPanel2Normal.setText("NORMAL");
                btnPanel2Normal.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnPanel2NormalActionPerformed(evt);
                    }
                });
                
                jPanel227.add(btnPanel2Normal);
                
                btnPanel2Shutdown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnPanel2Shutdown.setText("SHUT DOWN");
                btnPanel2Shutdown.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnPanel2ShutdownActionPerformed(evt);
                    }
                });
                
                jPanel227.add(btnPanel2Shutdown);
                
                jPanel248.add(jPanel227);
              
              jPanel225.add(jPanel248);
            
            panelPower.add(jPanel225);
          
          jPanel150.setLayout(new javax.swing.BoxLayout(jPanel150, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel150.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel150.setPreferredSize(new java.awt.Dimension(700, 95));
            jLabel160.setText(" CHARGE CONTROL ");
            jLabel160.setForeground(java.awt.Color.yellow);
            jLabel160.setBackground(java.awt.Color.blue);
            jLabel160.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel160.setFont(new java.awt.Font("Arial Black", 0, 14));
            jLabel160.setPreferredSize(new java.awt.Dimension(137, 21));
            jLabel160.setMinimumSize(new java.awt.Dimension(137, 21));
            jLabel160.setAlignmentX(0.5F);
            jLabel160.setMaximumSize(new java.awt.Dimension(2000, 21));
            jLabel160.setOpaque(true);
            jPanel150.add(jLabel160);
            
            jPanel151.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
              
              jPanel151.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              jLabel61.setText(" DAC1 ");
              jLabel61.setForeground(java.awt.Color.white);
              jLabel61.setBackground(java.awt.Color.darkGray);
              jLabel61.setFont(new java.awt.Font("Arial", 1, 12));
              jLabel61.setOpaque(true);
              jPanel151.add(jLabel61);
              
              btnDAC1ON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
              btnDAC1ON.setText("ON");
              btnDAC1ON.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC1ONActionPerformed(evt);
                  }
              });
              
              jPanel151.add(btnDAC1ON);
              
              btnDAC1OFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
              btnDAC1OFF.setText("OFF");
              btnDAC1OFF.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC1OFFActionPerformed(evt);
                  }
              });
              
              jPanel151.add(btnDAC1OFF);
              
              jLabel66.setText("Duty Cycle:");
              jPanel151.add(jLabel66);
              
              txtDAC1DutyCycle.setPreferredSize(new java.awt.Dimension(50, 20));
              txtDAC1DutyCycle.setMinimumSize(new java.awt.Dimension(50, 20));
              txtDAC1DutyCycle.addKeyListener(new java.awt.event.KeyAdapter() {
                  public void keyReleased(java.awt.event.KeyEvent evt) {
                      txtDAC1DutyCycleKeyReleased(evt);
                  }
              });
              
              jPanel151.add(txtDAC1DutyCycle);
              
              jLabel67.setText("%");
              jPanel151.add(jLabel67);
              
              slidDAC1DutyCycle.setPaintLabels(true);
              slidDAC1DutyCycle.setMajorTickSpacing(10);
              txtDAC1DutyCycle.setText(""+slidDAC1DutyCycle.getValue());
              slidDAC1DutyCycle.addChangeListener(new javax.swing.event.ChangeListener() {
                  public void stateChanged(javax.swing.event.ChangeEvent evt) {
                      slidDAC1DutyCycleStateChanged(evt);
                  }
              });
              
              jPanel151.add(slidDAC1DutyCycle);
              
              btnDAC1Set.setText("SET");
              btnDAC1Set.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC1SetActionPerformed(evt);
                  }
              });
              
              jPanel151.add(btnDAC1Set);
              
              jPanel150.add(jPanel151);
            
            jPanel152.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));
              
              jPanel152.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              jLabel64.setText(" DAC2 ");
              jLabel64.setForeground(java.awt.Color.white);
              jLabel64.setBackground(java.awt.Color.darkGray);
              jLabel64.setFont(new java.awt.Font("Arial", 1, 12));
              jLabel64.setOpaque(true);
              jPanel152.add(jLabel64);
              
              btnDAC2ON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
              btnDAC2ON.setText("ON");
              btnDAC2ON.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC2ONActionPerformed(evt);
                  }
              });
              
              jPanel152.add(btnDAC2ON);
              
              btnDAC2OFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
              btnDAC2OFF.setText("OFF");
              btnDAC2OFF.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC2OFFActionPerformed(evt);
                  }
              });
              
              jPanel152.add(btnDAC2OFF);
              
              jLabel68.setText("Duty Cycle:");
              jPanel152.add(jLabel68);
              
              txtDAC2DutyCycle.setPreferredSize(new java.awt.Dimension(50, 20));
              txtDAC2DutyCycle.setMinimumSize(new java.awt.Dimension(50, 20));
              txtDAC2DutyCycle.addKeyListener(new java.awt.event.KeyAdapter() {
                  public void keyReleased(java.awt.event.KeyEvent evt) {
                      txtDAC2DutyCycleKeyReleased(evt);
                  }
              });
              
              jPanel152.add(txtDAC2DutyCycle);
              
              jLabel70.setText("%");
              jPanel152.add(jLabel70);
              
              slidDAC2DutyCycle.setPaintLabels(true);
              slidDAC2DutyCycle.setMajorTickSpacing(10);
              txtDAC2DutyCycle.setText(""+slidDAC2DutyCycle.getValue());
              slidDAC2DutyCycle.addChangeListener(new javax.swing.event.ChangeListener() {
                  public void stateChanged(javax.swing.event.ChangeEvent evt) {
                      slidDAC2DutyCycleStateChanged(evt);
                  }
              });
              
              jPanel152.add(slidDAC2DutyCycle);
              
              btnDAC2Set.setText("SET");
              btnDAC2Set.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnDAC2SetActionPerformed(evt);
                  }
              });
              
              jPanel152.add(btnDAC2Set);
              
              jPanel150.add(jPanel152);
            
            panelPower.add(jPanel150);
          
          add(panelPower, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents

    private void btnDAC2ONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC2ONActionPerformed
        showButtonInactive( btnDAC2OFF );
      //SatControl ctrl = sat.getControl();
      //if( (ctrl==null) || (srvLnk==null) ) return;
      //srvLnk.sendPacketToSpacecraft(ctrl.messageToActivatePayloadLine(3));      
      //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC2_ON ) );      
      JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnDAC2ONActionPerformed

    private void slidDAC2DutyCycleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slidDAC2DutyCycleStateChanged
      txtDAC2DutyCycle.setText(""+slidDAC2DutyCycle.getValue());
    }//GEN-LAST:event_slidDAC2DutyCycleStateChanged

    private void btnPanel1NormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPanel1NormalActionPerformed
        showButtonInactive( btnPanel1Shutdown );
        //if( PHT.isCommandPrime() )
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel1Normal_Prime) );
        //else 
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel1Normal_Backup) );
        JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnPanel1NormalActionPerformed

    private void btnDAC2OFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC2OFFActionPerformed
      showButtonInactive( btnDAC2ON );
      //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC2_OFF ) );      
      JOptionPane.showMessageDialog(this, "This command not implemented");      
    }//GEN-LAST:event_btnDAC2OFFActionPerformed

    private void btnDAC2SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC2SetActionPerformed
        int value;
      
        /*---------------------------*/
        /* Ge the value (percentage) */
        /*---------------------------*/
        try{
          value = Integer.parseInt(txtDAC2DutyCycle.getText());
        } catch( NumberFormatException nfe ) { value = 0; }
        if( value > 100 ) value = 100;
        if( value < 0 ) value = 0;
        txtDAC2DutyCycle.setText(""+value);
      
        /*-----------------------------------------------------------------*/
        /* Translate it into the 0x000-0xFFF range, and then into a string */
        /*-----------------------------------------------------------------*/
        value = (int)((4095 * value) / 100);
      
        /*-------------------------*/
        /* Finally send the packet */
        /*-------------------------*/
        //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC2, value ) );      
        JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnDAC2SetActionPerformed

    private void btnPanel2NormalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPanel2NormalActionPerformed
        showButtonInactive( btnPanel2Shutdown );
        //if( PHT.isCommandPrime() )
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel2Normal_Prime) );
        //else 
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel2Normal_Backup) );      
        JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnPanel2NormalActionPerformed

    private void btnDAC1OFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC1OFFActionPerformed
      showButtonInactive( btnDAC1ON );
      //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC1_OFF ) );      
      JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnDAC1OFFActionPerformed

    private void txtDAC1DutyCycleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDAC1DutyCycleKeyReleased
      try {
          slidDAC1DutyCycle.setValue( Integer.parseInt(txtDAC1DutyCycle.getText()) );
      }catch( NumberFormatException nfe ) {
          slidDAC1DutyCycle.setValue( 0 );
      }
    }//GEN-LAST:event_txtDAC1DutyCycleKeyReleased

    private void btnDAC1SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC1SetActionPerformed
        int value;
      
        /*---------------------------*/
        /* Ge the value (percentage) */
        /*---------------------------*/
        try{
          value = Integer.parseInt(txtDAC1DutyCycle.getText());
        } catch( NumberFormatException nfe ) { value = 0; }
        if( value > 100 ) value = 100;
        if( value < 0 ) value = 0;
        txtDAC1DutyCycle.setText(""+value);
      
        /*-----------------------------------------------------------------*/
        /* Translate it into the 0x000-0xFFF range, and then into a string */
        /*-----------------------------------------------------------------*/
        value = (int)((4095 * value) / 100);
      
        /*-------------------------*/
        /* Finally send the packet */
        /*-------------------------*/
        //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC1, value ) );      
        JOptionPane.showMessageDialog(this, "This command not implemented");
      
    }//GEN-LAST:event_btnDAC1SetActionPerformed

    private void btnDAC1ONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDAC1ONActionPerformed
      showButtonInactive( btnDAC1OFF );
      //phtLink.sendPacket( PHT.dataPacket( PHT.cmdChargeControl_DAC1_ON ) );      
      JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnDAC1ONActionPerformed

    private void txtDAC2DutyCycleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDAC2DutyCycleKeyReleased
      try {
          slidDAC2DutyCycle.setValue( Integer.parseInt(txtDAC2DutyCycle.getText()) );
      }catch( NumberFormatException nfe ) {
          slidDAC2DutyCycle.setValue( 0 );
      }
    }//GEN-LAST:event_txtDAC2DutyCycleKeyReleased

    private void btnPanel2ShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPanel2ShutdownActionPerformed
        showButtonInactive( btnPanel2Normal );
        //if( PHT.isCommandPrime() )
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel2Shutdown_Prime) );
        //else 
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel2Shutdown_Backup) );      
        JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnPanel2ShutdownActionPerformed

    private void slidDAC1DutyCycleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slidDAC1DutyCycleStateChanged
      txtDAC1DutyCycle.setText(""+slidDAC1DutyCycle.getValue());
    }//GEN-LAST:event_slidDAC1DutyCycleStateChanged

    private void btnPanel1ShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPanel1ShutdownActionPerformed
        showButtonInactive( btnPanel1Normal );
        //if( PHT.isCommandPrime() )
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel1Shutdown_Prime) );
        //else 
        //    phtLink.sendPacket( PHT.dataPacket(PHT.cmdSolarPanel1Shutdown_Backup) );      
        JOptionPane.showMessageDialog(this, "This command not implemented");
    }//GEN-LAST:event_btnPanel1ShutdownActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelPower;
    private javax.swing.JPanel jPanel225;
    private javax.swing.JLabel jLabel161;
    private javax.swing.JLabel lblPanelsCommandMode;
    private javax.swing.JPanel jPanel248;
    private javax.swing.JPanel jPanel226;
    private javax.swing.JLabel jLabel134;
    public javax.swing.JButton btnPanel1Normal;
    public javax.swing.JButton btnPanel1Shutdown;
    private javax.swing.JPanel jPanel227;
    private javax.swing.JLabel jLabel135;
    public javax.swing.JButton btnPanel2Normal;
    public javax.swing.JButton btnPanel2Shutdown;
    private javax.swing.JPanel jPanel150;
    private javax.swing.JLabel jLabel160;
    private javax.swing.JPanel jPanel151;
    private javax.swing.JLabel jLabel61;
    public javax.swing.JButton btnDAC1ON;
    public javax.swing.JButton btnDAC1OFF;
    private javax.swing.JLabel jLabel66;
    private javax.swing.JTextField txtDAC1DutyCycle;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JSlider slidDAC1DutyCycle;
    private javax.swing.JButton btnDAC1Set;
    private javax.swing.JPanel jPanel152;
    private javax.swing.JLabel jLabel64;
    public javax.swing.JButton btnDAC2ON;
    public javax.swing.JButton btnDAC2OFF;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JTextField txtDAC2DutyCycle;
    private javax.swing.JLabel jLabel70;
    private javax.swing.JSlider slidDAC2DutyCycle;
    private javax.swing.JButton btnDAC2Set;
    // End of variables declaration//GEN-END:variables
    /*------------------------------------------------------------------------*/
    /** Change the appearance of the button so that user knows that the
     *  function associated with this button is not known, but will be as soon
     *  as we receive telemetry information.
     *  @param button The swing button.                                       */
    /*------------------------------------------------------------------------*/
    public void showButtonInactive( JButton button ) {
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
    public void showButtonActiveON( JButton button ) {
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
    public void showButtonActiveOFF( JButton button ) {
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
    public void showButtonNeutral( JButton button ) {
    /*------------------------------------------------------------------------*/
     button.setIcon(
       new javax.swing.ImageIcon(
        getClass().getResource(LED_WHITE)));
    }
}