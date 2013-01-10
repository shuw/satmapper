/*
 * mgc_admin.java
 *
 * Created on July 2, 2001, 12:56 PM
 */

package ca.gc.space.quicksat.ground.server;
import java.net.*;
import java.io.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class mgc_admin extends javax.swing.JApplet implements Runnable {
int i = 0;
DataOutputStream dos = null;
DataInputStream dis = null;
Thread thread;

    /*========================================================================*/
    /** Creates new form mgc_admin                                            */
    /*========================================================================*/
    public void init() {
    /*========================================================================*/    
    final int port = 9901;
    
        initComponents ();                        
        try{
            /*-----------------------------------*/
            /* Setup the socket's target address */
            /*-----------------------------------*/
            //InetAddress address = InetAddress.getByName("localhost");//local
            InetAddress address = InetAddress.getLocalHost();//local
            System.out.println("Local host: "+address.toString());
            Socket socket = new Socket(address,port);
            
            /*--------------------------------*/
            /* Setup the input/output streams */
            /*--------------------------------*/
            OutputStream os = socket.getOutputStream();
            dos = new DataOutputStream( os );            
            InputStream is = socket.getInputStream();
            dis = new DataInputStream( is );
            System.out.println("Setup for all streams complete");
            
        } catch( IOException ioe ) {System.out.println("Error connecting");}        
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public void start() {
    /*========================================================================*/    
        thread = new Thread(this);
        thread.start();
    }

    /*========================================================================*/
    /*========================================================================*/
    public void stop() {
    /*========================================================================*/    
        try{
            dos.writeInt( -1 ); //Kill my thread on the other side
        } catch( IOException ioe ) {}    
        thread = null;
        
    }

    /*========================================================================*/
    /*========================================================================*/
    public void run() {
    /*========================================================================*/    
        Thread me = Thread.currentThread();
        RUNNING_LOOP:
        while( thread == me ) {
            try {
                //Thread.currentThread().sleep(100);
                int command = dis.readInt();
                switch( command ) {
                    case -1:
                        System.out.println("Got a request to exit");
                        /*--------------------------------*/
                        /* Command: exit this application */
                        /*--------------------------------*/
                        break RUNNING_LOOP;                        
                    case 0:
                        System.out.println("Got a command #0");
                        break;
                    case 1:
                        /*-------------------------------------------------*/
                        /* Command: receiving admin threads status         */
                        /* Proto: [NbAdmins:int]                           */
                        /*        [Adm1Id:int]                             */
                        /*        [Adm1UsernameSize]                       */
                        /*        [Adm1User:str50]                         */
                        /*        [Adm1Status:int]                         */
                        /*        ... until all admins are reported...     */
                        /*-------------------------------------------------*/
                        System.out.println("Got data on admin connections");
                        int nbAdmins = dis.readInt();
                        System.out.println("We have " + nbAdmins + "admin connections alike");
                        //javax.swing.JButton btnKill[] = new javax.swing.JButton[nbAdmins];
                        for( i=0; i<10; i++ ) {
                            tblAdmins.setValueAt("",i,0);
                            tblAdmins.setValueAt("",i,1);
                            tblAdmins.setValueAt("",i,2);                            
                            tblAdmins.setValueAt("",i,3);                            
                        }
                        //tblAdmins.selectAll();
                        //tblAdmins.clearSelection();
                        for( int i=0; i<nbAdmins; i++ ) {
                            int id = dis.readInt();
                            int usernameSize = dis.readInt();
                            byte strData[] = new byte[usernameSize];
                            dis.readFully(strData);
                            String user = new String( strData );
                            int status = dis.readInt();
                            tblAdmins.setValueAt(new Integer(id),i,0);
                            tblAdmins.setValueAt(user,i,1);
                            tblAdmins.setValueAt(new Integer(status),i,2);
                            //btnKill[i] = new javax.swing.JButton();
                            //tblAdmins.setValueAt(btnKill[i],id,3);
                           
                        }
                        break;
                    default:
                        System.out.println("Got unrecognized message");
                        break;
                }
            } catch (IOException ioe) {}
        } //end of RUNNING_LOOP
        try{
            dis.close();
            dos.close();
        } catch( IOException ioe ) {}
        System.exit(0);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        btnGetStatus = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblAdmins = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtKillAdmin = new javax.swing.JTextField();
        btnKillAdmin = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnKillServer = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        listSatLinkPort = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        listSatLinkSpeed = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        listRadioPort = new javax.swing.JComboBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        listRadioSpeed = new javax.swing.JComboBox();
        jPanel10 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtQBLData = new javax.swing.JTextField();
        btnQBLDataSend = new javax.swing.JButton();
        getContentPane().setLayout(new java.awt.FlowLayout());
        
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, 1));
        jPanel1.setBorder(new javax.swing.border.TitledBorder("Admin Threads"));
        
        btnGetStatus.setText("Update Status");
          btnGetStatus.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  btnGetStatusActionPerformed(evt);
              }
          }
          );
          jPanel1.add(btnGetStatus);
          
          
        
          tblAdmins.setModel(new javax.swing.table.DefaultTableModel (
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "#", "User", "Status", "Title 4"
            }
            ) {
                Class[] types = new Class [] {
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
                };
                
                public Class getColumnClass (int columnIndex) {
                    return types [columnIndex];
                }
            });
            jScrollPane1.setViewportView(tblAdmins);
            
            jPanel1.add(jScrollPane1);
          
          
        jPanel2.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
          
          jLabel1.setText("Thread Id:");
            jPanel2.add(jLabel1);
            
            
          txtKillAdmin.setPreferredSize(new java.awt.Dimension(30, 20));
            jPanel2.add(txtKillAdmin);
            
            
          btnKillAdmin.setText("Kill");
            btnKillAdmin.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    btnKillAdminActionPerformed(evt);
                }
            }
            );
            jPanel2.add(btnKillAdmin);
            
            jPanel1.add(jPanel2);
          
          
        getContentPane().add(jPanel1);
        
        
        jPanel3.setBorder(new javax.swing.border.TitledBorder("General"));
        
        btnKillServer.setText("KILL SERVER");
          btnKillServer.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  btnKillServerActionPerformed(evt);
              }
          }
          );
          jPanel3.add(btnKillServer);
          
          
        getContentPane().add(jPanel3);
        
        
        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, 1));
        jPanel4.setBorder(new javax.swing.border.TitledBorder("Satellite Link Serial Port"));
        
        jPanel5.setLayout(new java.awt.FlowLayout(2, 5, 1));
          
          jLabel2.setText("Port:");
            jPanel5.add(jLabel2);
            
            
          jPanel5.add(listSatLinkPort);
            
            jPanel4.add(jPanel5);
          
          
        jPanel6.setLayout(new java.awt.FlowLayout(2, 5, 1));
          
          jLabel3.setText("Speed:");
            jPanel6.add(jLabel3);
            
            
          jPanel6.add(listSatLinkSpeed);
            
            jPanel4.add(jPanel6);
          
          
        getContentPane().add(jPanel4);
        
        
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, 1));
        jPanel7.setBorder(new javax.swing.border.TitledBorder(
        new javax.swing.border.EtchedBorder(), "Radio Control Serial Port"));
        
        jPanel8.setLayout(new java.awt.FlowLayout(2, 5, 1));
          
          jLabel4.setText("Port:");
            jPanel8.add(jLabel4);
            
            
          jPanel8.add(listRadioPort);
            
            jPanel7.add(jPanel8);
          
          
        jPanel9.setLayout(new java.awt.FlowLayout(2, 5, 1));
          
          jLabel5.setText("Speed:");
            jPanel9.add(jLabel5);
            
            
          jPanel9.add(listRadioSpeed);
            
            jPanel7.add(jPanel9);
          
          
        getContentPane().add(jPanel7);
        
        
        jPanel10.setBorder(new javax.swing.border.TitledBorder("Bootloader Data"));
        
        jLabel6.setText("Data:");
          jPanel10.add(jLabel6);
          
          
        txtQBLData.setPreferredSize(new java.awt.Dimension(100, 20));
          jPanel10.add(txtQBLData);
          
          
        btnQBLDataSend.setText("Send");
          btnQBLDataSend.addActionListener(new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent evt) {
                  btnQBLDataSendActionPerformed(evt);
              }
          }
          );
          jPanel10.add(btnQBLDataSend);
          
          
        getContentPane().add(jPanel10);
        
    }//GEN-END:initComponents

  private void btnQBLDataSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnQBLDataSendActionPerformed
      try{
          System.out.println("Sending data to QBL");
          dos.writeInt( 10 );
          dos.writeInt( txtQBLData.getText().length() );
          dos.writeBytes( txtQBLData.getText() );         
      }catch( IOException ioe ) {}
  }//GEN-LAST:event_btnQBLDataSendActionPerformed

  private void btnKillServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKillServerActionPerformed
      try{
          System.out.println("Sending command to kill the server");
          dos.writeInt( -2 );
      }catch( IOException ioe ) {}
  }//GEN-LAST:event_btnKillServerActionPerformed

  private void btnKillAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKillAdminActionPerformed
      try{
          if( dos == null ) {
              System.out.println("output stream is null");
              return;
          }
          dos.writeInt( 2 );
          dos.writeInt( Integer.parseInt(txtKillAdmin.getText()) );
      } catch( IOException ioe ) {System.out.println("Unable to write");}
  }//GEN-LAST:event_btnKillAdminActionPerformed

  private void btnGetStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetStatusActionPerformed
      try{
          if( dos == null ) {
              System.out.println("output stream is null");
              return;
          }
          dos.writeInt( 1 );
      } catch( IOException ioe ) {System.out.println("Unable to write");}
  }//GEN-LAST:event_btnGetStatusActionPerformed

  private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
      try{
          if( dos == null ) {
              System.out.println("output stream is null");
              return;
          }
          dos.writeInt( 2 );
      } catch( IOException ioe ) {System.out.println("Unable to write");}
  }//GEN-LAST:event_jButton1ActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel jPanel1;
  private javax.swing.JButton btnGetStatus;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTable tblAdmins;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JTextField txtKillAdmin;
  private javax.swing.JButton btnKillAdmin;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JButton btnKillServer;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JComboBox listSatLinkPort;
  private javax.swing.JPanel jPanel6;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JComboBox listSatLinkSpeed;
  private javax.swing.JPanel jPanel7;
  private javax.swing.JPanel jPanel8;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JComboBox listRadioPort;
  private javax.swing.JPanel jPanel9;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JComboBox listRadioSpeed;
  private javax.swing.JPanel jPanel10;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JTextField txtQBLData;
  private javax.swing.JButton btnQBLDataSend;
  // End of variables declaration//GEN-END:variables

}
