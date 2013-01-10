/*==============================================================================
 * PanelTX.java
 *
 * Created on July 25, 2001, 10:18 AM
 =============================================================================*/

package ca.gc.space.quicksat.ground.control;

import javax.swing.*;
import java.io.*;
import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.util.*;

/*============================================================================*/
/**
 * @author  jfcusson
 * @version                                                                   */
/*============================================================================*/
public class PanelTX_cmd extends javax.swing.JPanel {
ServerLink  srvLnk               = null;
Satellite   sat                  = null;
Log         log                  = null;
private final String LED_OFF="/ca/gc/space/quicksat/ground/images/led_off.gif";
private final String LED_GREEN_ON="/ca/gc/space/quicksat/ground/images/led_green_on.gif";
private final String LED_RED_ON="/ca/gc/space/quicksat/ground/images/led_red_on.gif";
private final String LED_WHITE="/ca/gc/space/quicksat/ground/images/led_white.gif";

    /*========================================================================*/
    /** Creates new form PanelTX.                                             */
    /*========================================================================*/
    public PanelTX_cmd( Log log ) {
    /*========================================================================*/    
        initComponents ();
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
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
            jPanel57 = new javax.swing.JPanel();
            jPanel194 = new javax.swing.JPanel();
            jLabel21 = new javax.swing.JLabel();
            jPanel70 = new javax.swing.JPanel();
            jPanel71 = new javax.swing.JPanel();
            jPanel1 = new javax.swing.JPanel();
            btnTX1ON = new javax.swing.JButton();
            txtTX1Timer = new javax.swing.JTextField();
            jLabel1 = new javax.swing.JLabel();
            btnTX1OFF = new javax.swing.JButton();
            slidTX1Power = new javax.swing.JSlider();
            txtTX1Power = new javax.swing.JTextField();
            jLabel22 = new javax.swing.JLabel();
            btnTX1Power = new javax.swing.JButton();
            jPanel193 = new javax.swing.JPanel();
            btnTX1SourceA = new javax.swing.JButton();
            btnTX1SourceB = new javax.swing.JButton();
            jPanel195 = new javax.swing.JPanel();
            jLabel23 = new javax.swing.JLabel();
            jPanel192 = new javax.swing.JPanel();
            jPanel72 = new javax.swing.JPanel();
            jPanel3 = new javax.swing.JPanel();
            btnTX2ON = new javax.swing.JButton();
            txtTX2Timer = new javax.swing.JTextField();
            jLabel2 = new javax.swing.JLabel();
            btnTX2OFF = new javax.swing.JButton();
            slidTX2Power = new javax.swing.JSlider();
            txtTX2Power = new javax.swing.JTextField();
            jLabel24 = new javax.swing.JLabel();
            btnTX2Power = new javax.swing.JButton();
            jPanel196 = new javax.swing.JPanel();
            btnTX2SourceA = new javax.swing.JButton();
            btnTX2SourceB = new javax.swing.JButton();
            jPanel167 = new javax.swing.JPanel();
            jLabel25 = new javax.swing.JLabel();
            jPanel175 = new javax.swing.JPanel();
            txtTXDelay = new javax.swing.JTextField();
            jLabel59 = new javax.swing.JLabel();
            jPanel77 = new javax.swing.JPanel();
            btnTXDelay = new javax.swing.JButton();
            jPanel99 = new javax.swing.JPanel();
            
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS));
            
            jPanel57.setLayout(new javax.swing.BoxLayout(jPanel57, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel194.setLayout(new javax.swing.BoxLayout(jPanel194, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel194.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jLabel21.setText("  TX1  ");
            jLabel21.setForeground(java.awt.Color.yellow);
            jLabel21.setBackground(java.awt.Color.blue);
            jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel21.setFont(new java.awt.Font("Arial Black", 0, 14));
            jLabel21.setAlignmentX(0.5F);
            jLabel21.setMaximumSize(new java.awt.Dimension(1500, 21));
            jLabel21.setOpaque(true);
            jPanel194.add(jLabel21);
            
            jPanel70.setLayout(new javax.swing.BoxLayout(jPanel70, javax.swing.BoxLayout.X_AXIS));
                  
                  jPanel70.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                  jPanel71.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
                  
                  jPanel71.setBorder(new javax.swing.border.TitledBorder("Power"));
                  jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));
                  
                  jPanel1.setPreferredSize(new java.awt.Dimension(120, 29));
                  jPanel1.setMinimumSize(new java.awt.Dimension(20, 10));
                  btnTX1ON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                  btnTX1ON.setFont(new java.awt.Font("Arial Narrow", 0, 10));
                  btnTX1ON.setAlignmentX(0.5F);
                  btnTX1ON.setLabel("ON");
                  btnTX1ON.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                  btnTX1ON.addActionListener(new java.awt.event.ActionListener() {
                      public void actionPerformed(java.awt.event.ActionEvent evt) {
                          btnTX1ONActionPerformed(evt);
                      }
                  });
                  
                  jPanel1.add(btnTX1ON);
                  
                  txtTX1Timer.setText("12");
                  txtTX1Timer.setPreferredSize(new java.awt.Dimension(25, 21));
                  txtTX1Timer.setMaximumSize(new java.awt.Dimension(25, 2147483647));
                  txtTX1Timer.setMinimumSize(new java.awt.Dimension(10, 21));
                  jPanel1.add(txtTX1Timer);
                  
                  jLabel1.setText("Min.");
                  jPanel1.add(jLabel1);
                  
                  jPanel71.add(jPanel1);
                
                btnTX1OFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX1OFF.setFont(new java.awt.Font("Arial Narrow", 0, 10));
                btnTX1OFF.setText("OFF");
                btnTX1OFF.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX1OFFActionPerformed(evt);
                    }
                });
                
                jPanel71.add(btnTX1OFF);
                
                slidTX1Power.setPaintLabels(true);
                slidTX1Power.setPaintTicks(true);
                slidTX1Power.setMajorTickSpacing(10);
                slidTX1Power.setValue(10);
                slidTX1Power.setMinimumSize(new java.awt.Dimension(200, 43));
                txtTX1Power.setText(""+slidTX1Power.getValue());
                slidTX1Power.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent evt) {
                        slidTX1PowerStateChanged(evt);
                    }
                });
                
                jPanel71.add(slidTX1Power);
                
                txtTX1Power.setPreferredSize(new java.awt.Dimension(30, 20));
                txtTX1Power.setMinimumSize(new java.awt.Dimension(30, 20));
                txtTX1Power.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtTX1PowerKeyReleased(evt);
                    }
                });
                
                jPanel71.add(txtTX1Power);
                
                jLabel22.setText("%");
                jPanel71.add(jLabel22);
                
                btnTX1Power.setText("SET");
                btnTX1Power.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX1PowerActionPerformed(evt);
                    }
                });
                
                jPanel71.add(btnTX1Power);
                
                jPanel70.add(jPanel71);
              
              jPanel193.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
                
                jPanel193.setBorder(new javax.swing.border.TitledBorder("Data Source (Modem)"));
                jPanel193.setPreferredSize(new java.awt.Dimension(60, 56));
                jPanel193.setMinimumSize(new java.awt.Dimension(50, 56));
                btnTX1SourceA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX1SourceA.setText("A");
                btnTX1SourceA.setPreferredSize(new java.awt.Dimension(50, 29));
                btnTX1SourceA.setMargin(new java.awt.Insets(2, 5, 2, 5));
                btnTX1SourceA.setMinimumSize(new java.awt.Dimension(40, 29));
                btnTX1SourceA.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX1SourceAActionPerformed(evt);
                    }
                });
                
                jPanel193.add(btnTX1SourceA);
                
                btnTX1SourceB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX1SourceB.setText("B");
                btnTX1SourceB.setPreferredSize(new java.awt.Dimension(50, 29));
                btnTX1SourceB.setMaximumSize(new java.awt.Dimension(100, 29));
                btnTX1SourceB.setMargin(new java.awt.Insets(2, 5, 2, 5));
                btnTX1SourceB.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX1SourceBActionPerformed(evt);
                    }
                });
                
                jPanel193.add(btnTX1SourceB);
                
                jPanel70.add(jPanel193);
              
              jPanel194.add(jPanel70);
            
            jPanel57.add(jPanel194);
          
          jPanel195.setLayout(new javax.swing.BoxLayout(jPanel195, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel195.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jLabel23.setText("  TX2  ");
            jLabel23.setForeground(java.awt.Color.yellow);
            jLabel23.setBackground(java.awt.Color.blue);
            jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            jLabel23.setFont(new java.awt.Font("Arial Black", 0, 14));
            jLabel23.setAlignmentX(0.5F);
            jLabel23.setMaximumSize(new java.awt.Dimension(1500, 21));
            jLabel23.setOpaque(true);
            jPanel195.add(jLabel23);
            
            jPanel192.setLayout(new javax.swing.BoxLayout(jPanel192, javax.swing.BoxLayout.X_AXIS));
                  
                  jPanel192.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                  jPanel72.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 1));
                  
                  jPanel72.setBorder(new javax.swing.border.TitledBorder("Power"));
                  jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));
                  
                  jPanel3.setPreferredSize(new java.awt.Dimension(120, 29));
                  btnTX2ON.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                  btnTX2ON.setFont(new java.awt.Font("Arial Narrow", 0, 10));
                  btnTX2ON.setText("ON");
                  btnTX2ON.addActionListener(new java.awt.event.ActionListener() {
                      public void actionPerformed(java.awt.event.ActionEvent evt) {
                          btnTX2ONActionPerformed(evt);
                      }
                  });
                  
                  jPanel3.add(btnTX2ON);
                  
                  txtTX2Timer.setText("12");
                  txtTX2Timer.setPreferredSize(new java.awt.Dimension(25, 21));
                  txtTX2Timer.setMaximumSize(new java.awt.Dimension(25, 2147483647));
                  jPanel3.add(txtTX2Timer);
                  
                  jLabel2.setText("Min.");
                  jPanel3.add(jLabel2);
                  
                  jPanel72.add(jPanel3);
                
                btnTX2OFF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX2OFF.setFont(new java.awt.Font("Arial Narrow", 0, 10));
                btnTX2OFF.setText("OFF");
                btnTX2OFF.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX2OFFActionPerformed(evt);
                    }
                });
                
                jPanel72.add(btnTX2OFF);
                
                slidTX2Power.setPaintLabels(true);
                slidTX2Power.setPaintTicks(true);
                slidTX2Power.setMajorTickSpacing(10);
                slidTX2Power.setValue(10);
                txtTX2Power.setText(""+slidTX2Power.getValue());
                slidTX2Power.addChangeListener(new javax.swing.event.ChangeListener() {
                    public void stateChanged(javax.swing.event.ChangeEvent evt) {
                        slidTX2PowerStateChanged(evt);
                    }
                });
                
                jPanel72.add(slidTX2Power);
                
                txtTX2Power.setText(" ");
                txtTX2Power.setPreferredSize(new java.awt.Dimension(30, 20));
                txtTX2Power.setMinimumSize(new java.awt.Dimension(30, 20));
                txtTX2Power.addKeyListener(new java.awt.event.KeyAdapter() {
                    public void keyReleased(java.awt.event.KeyEvent evt) {
                        txtTX2PowerKeyReleased(evt);
                    }
                });
                
                jPanel72.add(txtTX2Power);
                
                jLabel24.setText("%");
                jPanel72.add(jLabel24);
                
                btnTX2Power.setText("SET");
                btnTX2Power.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX2PowerActionPerformed(evt);
                    }
                });
                
                jPanel72.add(btnTX2Power);
                
                jPanel192.add(jPanel72);
              
              jPanel196.setBorder(new javax.swing.border.TitledBorder("Data Source"));
                jPanel196.setPreferredSize(new java.awt.Dimension(60, 64));
                jPanel196.setMinimumSize(new java.awt.Dimension(50, 64));
                btnTX2SourceA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX2SourceA.setText("A");
                btnTX2SourceA.setPreferredSize(new java.awt.Dimension(50, 29));
                btnTX2SourceA.setMaximumSize(new java.awt.Dimension(100, 29));
                btnTX2SourceA.setMargin(new java.awt.Insets(2, 5, 2, 5));
                btnTX2SourceA.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX2SourceAActionPerformed(evt);
                    }
                });
                
                jPanel196.add(btnTX2SourceA);
                
                btnTX2SourceB.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ca/gc/space/quicksat/ground/images/led_white.gif")));
                btnTX2SourceB.setText("B");
                btnTX2SourceB.setPreferredSize(new java.awt.Dimension(50, 29));
                btnTX2SourceB.setMaximumSize(new java.awt.Dimension(100, 29));
                btnTX2SourceB.setMargin(new java.awt.Insets(2, 5, 2, 5));
                btnTX2SourceB.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        btnTX2SourceBActionPerformed(evt);
                    }
                });
                
                jPanel196.add(btnTX2SourceB);
                
                jPanel192.add(jPanel196);
              
              jPanel195.add(jPanel192);
            
            jPanel57.add(jPanel195);
          
          add(jPanel57);
          
          jPanel167.setLayout(new javax.swing.BoxLayout(jPanel167, javax.swing.BoxLayout.Y_AXIS));
          
          jPanel167.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          jPanel167.setPreferredSize(new java.awt.Dimension(95, 100));
          jPanel167.setMinimumSize(new java.awt.Dimension(93, 42));
          jLabel25.setText("TX Delay");
          jLabel25.setForeground(java.awt.Color.yellow);
          jLabel25.setBackground(java.awt.Color.blue);
          jLabel25.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
          jLabel25.setFont(new java.awt.Font("Arial Black", 0, 14));
          jLabel25.setPreferredSize(new java.awt.Dimension(95, 21));
          jLabel25.setAlignmentX(0.5F);
          jLabel25.setMaximumSize(new java.awt.Dimension(1500, 21));
          jLabel25.setOpaque(true);
          jPanel167.add(jLabel25);
          
          jPanel175.setPreferredSize(new java.awt.Dimension(93, 30));
            txtTXDelay.setText(" ");
            txtTXDelay.setPreferredSize(new java.awt.Dimension(30, 20));
            txtTXDelay.setMinimumSize(new java.awt.Dimension(30, 20));
            jPanel175.add(txtTXDelay);
            
            jLabel59.setText("x20 mSec");
            jPanel175.add(jLabel59);
            
            jPanel167.add(jPanel175);
          
          btnTXDelay.setText("SET");
            btnTXDelay.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnTXDelayActionPerformed(evt);
                }
            });
            
            jPanel77.add(btnTXDelay);
            
            jPanel167.add(jPanel77);
          
          jPanel99.setPreferredSize(new java.awt.Dimension(10, 200));
          jPanel167.add(jPanel99);
          
          add(jPanel167);
        
    }//GEN-END:initComponents

    private void slidTX2PowerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slidTX2PowerStateChanged
      txtTX2Power.setText(""+slidTX2Power.getValue());
    }//GEN-LAST:event_slidTX2PowerStateChanged

    private void slidTX1PowerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_slidTX1PowerStateChanged
      txtTX1Power.setText(""+slidTX1Power.getValue());
    }//GEN-LAST:event_slidTX1PowerStateChanged

    private void txtTX2PowerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTX2PowerKeyReleased
      try {
          slidTX2Power.setValue( Integer.parseInt(txtTX2Power.getText()) );
      }catch( NumberFormatException nfe ) {
          slidTX2Power.setValue( 0 );
      }
    }//GEN-LAST:event_txtTX2PowerKeyReleased

    private void txtTX1PowerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtTX1PowerKeyReleased
      try {
          slidTX1Power.setValue( Integer.parseInt(txtTX1Power.getText()) );
      }catch( NumberFormatException nfe ) {
          slidTX1Power.setValue( 0 );
      }
    }//GEN-LAST:event_txtTX1PowerKeyReleased

  private void btnTX2SourceBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX2SourceBActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    showButtonInactive( btnTX2SourceA );
    SatControl ctrl = sat.getControl();
    if( ctrl != null )
       srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterSource(1,"B"));    
  }//GEN-LAST:event_btnTX2SourceBActionPerformed

  private void btnTX2SourceAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX2SourceAActionPerformed

    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    
    showButtonInactive( btnTX2SourceB );
    
    SatControl ctrl = sat.getControl();
    if( ctrl != null )
        srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterSource(2,"A"));      
  }//GEN-LAST:event_btnTX2SourceAActionPerformed

  private void btnTX2PowerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX2PowerActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;

    }
    /*--------------------------------*/
    /* Validate the value and confirm */
    /*--------------------------------*/
    int val = 0;
    try {
          val = Integer.parseInt(txtTX2Power.getText());
    } catch( NumberFormatException nfe ) {}
    if( val > 100 ) val = 100;
    if( val < 0 ) val = 0;
    txtTX2Power.setText(""+val);
    int result = JOptionPane.showConfirmDialog( this,
                                        "Set the transmitter power to "
                                        + val
                                        + "%?",
                                        "TX1", JOptionPane.OK_CANCEL_OPTION);
    if( result != JOptionPane.OK_OPTION ) {
          System.out.println("Cancelling set transmit power");
          return;
    }
    /*-------------------------------------------------*/
    /* Send the packet, after putting value to 12 bits */
    /*-------------------------------------------------*/
    val = (int)((val * 4095)/100);    
    SatControl ctrl = sat.getControl();
    if( ctrl != null )
        srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterPower(0,val));
  }//GEN-LAST:event_btnTX2PowerActionPerformed

  private void btnTX2OFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX2OFFActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    showButtonInactive( btnTX2ON );
    if( txtTX2Timer.getText().trim().equals("") ) {
        /*-----------------*/
        /* QuickSat format */
        /*-----------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterOFF(2));
    } else {
        /*-------------------*/
        /* SPACEQUEST format */
        /*-------------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterOFF(0));
    }
    
  }//GEN-LAST:event_btnTX2OFFActionPerformed

  private void btnTX2ONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX2ONActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    showButtonInactive( btnTX2OFF );
    if( txtTX1Timer.getText().trim().equals("") ) {
        /*-----------------*/
        /* QuickSat format */
        /*-----------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterON(2,""));    
    } else {
        /*-------------------*/
        /* SPACEQUEST format */
        /*-------------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(
                    ctrl.messageToTurnTransmitterON(0,txtTX2Timer.getText()) );
    }
  }//GEN-LAST:event_btnTX2ONActionPerformed

  private void btnTXDelayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTXDelayActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
   int delay = 0;      
   try{ delay = Integer.parseInt(txtTXDelay.getText().trim()); }
   catch( NumberFormatException nfe) { delay = 0; }
   SatControl ctrl = sat.getControl();
   if( ctrl != null )
      srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterDelay(0,delay));
  }//GEN-LAST:event_btnTXDelayActionPerformed

  private void btnTX1SourceBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX1SourceBActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
   showButtonInactive( btnTX1SourceA );
   SatControl ctrl = sat.getControl();
   if( ctrl != null )
      srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterSource(1,"B"));
  }//GEN-LAST:event_btnTX1SourceBActionPerformed

  private void btnTX1SourceAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX1SourceAActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
   showButtonInactive( btnTX1SourceB );
   SatControl ctrl = sat.getControl();
   if( ctrl != null )
      srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterSource(1,"A"));
  }//GEN-LAST:event_btnTX1SourceAActionPerformed

  private void btnTX1PowerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX1PowerActionPerformed

    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    /*--------------------------------*/
    /* Validate the value and confirm */
    /*--------------------------------*/
    int val = 0;
    try {
          val = Integer.parseInt(txtTX1Power.getText());
    } catch( NumberFormatException nfe ) {}
    if( val > 100 ) val = 100;
    if( val < 0 ) val = 0;
    txtTX1Power.setText( "" + val );
    int result = JOptionPane.showConfirmDialog( this,
                                                "Set the transmitter power to "
                                                + val
                                                + "%?",
                                                "TX1", 
                                                JOptionPane.OK_CANCEL_OPTION);
    if( result != JOptionPane.OK_OPTION ) {
          System.out.println("Cancelling set transmit power");
          return;
    }
    /*-------------------------------------------------*/
    /* Send the packet, after putting value to 12 bits */
    /*-------------------------------------------------*/
    val = (int)((val * 4095)/100);    
    SatControl ctrl = sat.getControl();
    if( ctrl != null )
        srvLnk.sendPacketToSpacecraft(ctrl.messageToSetTransmitterPower(0,val));
    
  }//GEN-LAST:event_btnTX1PowerActionPerformed

  private void btnTX1OFFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX1OFFActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    showButtonInactive( btnTX1ON );
    if( txtTX1Timer.getText().trim().equals("") ) {
        /*-----------------*/
        /* QuickSat format */
        /*-----------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterOFF(1));        
    } else {
        /*-------------------*/
        /* SPACEQUEST format */
        /*-------------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterOFF(0));
    }
  }//GEN-LAST:event_btnTX1OFFActionPerformed

  private void btnTX1ONActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTX1ONActionPerformed
    if( (srvLnk == null) || (sat == null) ) {
        JOptionPane.showMessageDialog( this, 
        "Unable to comply: Either the satellite or the server link is unset");
        return;
    }
    showButtonInactive( btnTX1OFF );
    
    if( txtTX1Timer.getText().trim().equals("") ) {
        /*-----------------*/
        /* QuickSat format */
        /*-----------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
           srvLnk.sendPacketToSpacecraft(ctrl.messageToTurnTransmitterON(1,""));    
    } else {
        /*-------------------*/
        /* SPACEQUEST format */
        /*-------------------*/
        SatControl ctrl = sat.getControl();
        if( ctrl != null )
            srvLnk.sendPacketToSpacecraft(
                    ctrl.messageToTurnTransmitterON(0,txtTX1Timer.getText()) );
    }
  }//GEN-LAST:event_btnTX1ONActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jPanel57;
  private javax.swing.JPanel jPanel194;
  private javax.swing.JLabel jLabel21;
  private javax.swing.JPanel jPanel70;
  private javax.swing.JPanel jPanel71;
  private javax.swing.JPanel jPanel1;
  public javax.swing.JButton btnTX1ON;
  private javax.swing.JTextField txtTX1Timer;
  private javax.swing.JLabel jLabel1;
  public javax.swing.JButton btnTX1OFF;
  private javax.swing.JSlider slidTX1Power;
  private javax.swing.JTextField txtTX1Power;
  private javax.swing.JLabel jLabel22;
  private javax.swing.JButton btnTX1Power;
  private javax.swing.JPanel jPanel193;
  public javax.swing.JButton btnTX1SourceA;
  public javax.swing.JButton btnTX1SourceB;
  private javax.swing.JPanel jPanel195;
  private javax.swing.JLabel jLabel23;
  private javax.swing.JPanel jPanel192;
  private javax.swing.JPanel jPanel72;
  private javax.swing.JPanel jPanel3;
  public javax.swing.JButton btnTX2ON;
  private javax.swing.JTextField txtTX2Timer;
  private javax.swing.JLabel jLabel2;
  public javax.swing.JButton btnTX2OFF;
  private javax.swing.JSlider slidTX2Power;
  private javax.swing.JTextField txtTX2Power;
  private javax.swing.JLabel jLabel24;
  private javax.swing.JButton btnTX2Power;
  private javax.swing.JPanel jPanel196;
  public javax.swing.JButton btnTX2SourceA;
  public javax.swing.JButton btnTX2SourceB;
  private javax.swing.JPanel jPanel167;
  private javax.swing.JLabel jLabel25;
  private javax.swing.JPanel jPanel175;
  private javax.swing.JTextField txtTXDelay;
  private javax.swing.JLabel jLabel59;
  private javax.swing.JPanel jPanel77;
  private javax.swing.JButton btnTXDelay;
  private javax.swing.JPanel jPanel99;
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
