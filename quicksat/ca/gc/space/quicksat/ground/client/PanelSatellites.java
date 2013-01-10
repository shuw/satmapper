/*
 * PanelSatellites.java
 * 
 * Created on July 25, 2001, 5:31 PM
 */

package ca.gc.space.quicksat.ground.client;

import java.util.*;
import java.io.*;
import javax.swing.*;

import ca.gc.space.quicksat.ground.client.*;
import ca.gc.space.quicksat.ground.satellite.*;
import ca.gc.space.quicksat.ground.control.*;
import ca.gc.space.quicksat.ground.util.*;
import ca.gc.space.quicksat.ground.tracking.*;
import ca.gc.space.quicksat.ground.radio.*;
import ca.gc.space.quicksat.ground.satellite.Satellite;

/**
 *
 * @author  jfcusson
 */
public class PanelSatellites extends javax.swing.JPanel {
String baseDir      = null;
Vector satVector    = null;
java.awt.Frame parentFrame = null;
SatMapper mapper;
    
    /** Creates new form PanelSatellites */
    public PanelSatellites( java.awt.Frame parent, String baseDirToUse,SatMapper mapper ) {
        parentFrame = parent; 
        baseDir = baseDirToUse;
        initComponents();
        /*--------------------------------------------------------------------*/
        /*              K E P L E R I A N    E L E M E N T S                  */
        /*--------------------------------------------------------------------*/
        /*--------------------------------------------------------*/
        /* Load the Keplerian elements from file C:/qsgs_keps.txt */
        /*--------------------------------------------------------*/
        this.mapper=mapper;
        satVector = new Vector();
        FileInputStream fis = null;
        DataInputStream dis = null; 
        String lineRead = null;
        try {
            fis = new FileInputStream(baseDir+"qsgs_keps.txt");
            dis = new DataInputStream( fis );
            System.out.println("Found file '"+baseDir+"qsgs_keps.txt': Loading keps");
            
            while( (dis.available()>0) ) {
                
                /*------------------------------*/
                /* Find the first non-empty line*/
                /*------------------------------*/
                lineRead = dis.readLine().trim();
                
                READING_2LINE_ELEMENTS:
                    if( lineRead.length() > 0 ) {
                        
                    /*----------------------------------------------------*/
                    /* Ok we start to read the data, assuming it is valid */
                    /*----------------------------------------------------*/
                        if( lineRead.length() > 20 ) lineRead = lineRead.substring(0,19);
                        Satellite sat = new Satellite( lineRead );
                        
                        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        /* FOR NOW ASSIGN THE SAME SATCONTROL TO */
                        /* EVERYBODY, BUT SHOULD CHANGE...       */
                        /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                        SatControl ctrl = new SatControl(baseDir);
                        ctrl.setControlProtocolId( 0x00 );
                        ctrl.setLoaderProtocolId( 0x79 );
                        ctrl.setExtLoaderProtocolId( 0x7A );
                        sat.setControl( ctrl );
                        
                        String firstLine = dis.readLine().trim();
                        if( firstLine.length() < 69 ) {
                        /*---------------------------------------------------*/
                        /* Each data line of a 2-line element file has to be */
                        /* 69 char long. If not this is not valid, so begin  */
                        /* the process of finding a valid line again...      */
                        /*---------------------------------------------------*/
                            sat = null;
                            break READING_2LINE_ELEMENTS;
                        }
                        
                        String secondLine = dis.readLine().trim();
                        if( secondLine.length() < 69 ) {
                        /*---------------------------------------------------*/
                        /* Each data line of a 2-line element file has to be */
                        /* 69 char long. If not this is not valid, so begin  */
                        /* the process of finding a valid line again...      */
                        /*---------------------------------------------------*/
                            sat = null;
                            break READING_2LINE_ELEMENTS;
                        }
                        
                        /*-------------------------------------------------*/
                        /* Decipher these two line and set the elements of */
                        /* the current satellite...                        */
                        /*-------------------------------------------------*/
                        sat.setKeps( firstLine, secondLine );
                        
                        satVector.addElement( sat );
                        
                    }
            }//finding a set of keplerian elements in the file
            
            fis.close();
        }catch( IOException ioe ){
            System.out.println("Keplerian element file '"+baseDir+"qsgs_keps.txt' not found");
            //if( fis != null ) fis.close();
        }
        
        
        /*--------------------------------------------------------------------*/
        /*                  S A T E L L I T E S    D A T A                    */
        /*--------------------------------------------------------------------*/
        /*---------------------------------------------------*/
        /* Generate the drop down menu to select a satellite */
        /*---------------------------------------------------*/
        for( int i=0; i<satVector.size(); i++ ) {
            Satellite sat = (Satellite)satVector.elementAt(i);
            listSatellites.addItem(sat);
            //System.out.println("Sat"+i+":"+sat.name);
        }
        
        
        /*-------------------------------------------------------*/
        /* Load additional satellite data from file qsgs_sat.txt */
        /*-------------------------------------------------------*/
        try {
            fis = new FileInputStream(baseDir+"qsgs_sat.txt");
            dis = new DataInputStream( fis );
            System.out.println("Found file '"+baseDir+"qsgs_sat.txt': Loading additional satellite data");
            
            Satellite sat = null;
            while( (dis.available()>0) ) {
                
                /*------------------------------------------*/
                /* Find the first line starting with dashes */
                /*------------------------------------------*/
                lineRead = dis.readLine().trim();
                
                READING_A_SATELLITE:
                    if( lineRead.startsWith("---") ) {
                    /*--------------------------------------------*/
                    /* Now until we reach the NAME field we won't */
                    /* know to which satellite we are looking at. */
                    /*--------------------------------------------*/
                        
                        while( (dis.available()>0) ) {
                            lineRead = dis.readLine().trim();
                            //..................................................
                            if( lineRead.startsWith("NAME:") && lineRead.length()>5 ) {
                            //..................................................    
                                String name = lineRead.substring(5).trim();
                                boolean found = false;
                                for( int i=0; i<listSatellites.getItemCount(); i++ ) {
                                    sat = (Satellite)listSatellites.getItemAt(i);
                                    if( sat.getName().equals(name) ) {
                                        found = true;
                                        break;
                                    }
                                }
                                if( !found ) {
                                    /*--------------------------------------*/
                                    /* Satellite was not originally in the  */
                                    /* orbital elements file: Add it anyway */
                                    /* we won't be able to track it...      */
                                    /*--------------------------------------*/
                                    sat = new Satellite( name );
                                    //listSatellites.addItem(sat);
                                    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                    /* FOR NOW ASSIGN THE SAME SATCONTROL TO */
                                    /* EVERYBODY, BUT SHOULD CHANGE...       */
                                    /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                    SatControl ctrl = new SatControl(baseDir);
                                    ctrl.setControlProtocolId( 0x00 );
                                    ctrl.setLoaderProtocolId( 0x79 );
                                    ctrl.setExtLoaderProtocolId( 0x7A );
                                    sat.setControl( ctrl );
                                    listSatellites.insertItemAt(sat,0);
                                }
                            } else                                
                            //..................................................
                            if( lineRead.startsWith("FULLNAME:") && lineRead.length()>9 ) {
                            //..................................................    
                                if( sat != null ) 
                                  sat.setFullName(lineRead.substring(9).trim());
                            } else
                            //..................................................    
                            if( lineRead.startsWith("NOTE:") && lineRead.length()>5 ) {
                            //..................................................    
                                if( sat != null ) {
                                    if( sat.getFeatures() == null )
                                       sat.setFeatures(lineRead.substring(5).trim());
                                    else
                                       sat.appendFeatures(lineRead.substring(5).trim());
                                }
                            } else
                            //..................................................    
                            if( lineRead.startsWith("CALLSIGN:") && lineRead.length()>9 ) {
                            //..................................................    
                                if( sat != null ) sat.setControlCallsign(lineRead.substring(9).trim());
                            } else
                            //..................................................    
                            if( lineRead.startsWith("PACSAT_BROADCAST:") && lineRead.length()>17 ) {
                            //..................................................    
                                if( sat != null ) sat.setPacsatBroadcastCallsign(lineRead.substring(17).trim());
                            } else
                            //..................................................    
                            if( lineRead.startsWith("PACSAT_BBS:") && lineRead.length()>11 ) {
                            //..................................................    
                                if( sat != null ) {                                                        
                                    /*-----------------------*/
                                    /* Format: CALLSIGN-SSID */
                                    /*-----------------------*/
                                    //System.out.println("ADDRESS:"+lineRead);
                                    //try{Thread.currentThread().sleep(1000);}
                                    //catch(InterruptedException e){}
                                    int posOfDash = lineRead.indexOf('-');
                                    if( (posOfDash < 12) || (posOfDash>=lineRead.length()) ) {
                                          sat.setPacsatSSID( 0 );
                                          sat.setPacsatCallsign(
                                                lineRead.substring(11).trim());
                                    } else {
                                          int ssid = 0;
                                          try{
                                             ssid=Integer.parseInt(
                                                  lineRead.substring(
                                                                  posOfDash+1));
                                          } catch(NumberFormatException e){}
                                          sat.setPacsatSSID( ssid );
                                          sat.setPacsatCallsign(
                                                lineRead.
                                                    substring(11,posOfDash).
                                                                        trim());
                                    }
                                    //System.out.println("Dash:"+posOfDash+">"+sat.getPacsatAddress());
                                    
                                }
                            } else
                            //..................................................    
                            if( lineRead.startsWith("TX:") && lineRead.length()>3 && (sat != null) ) {
                            //..................................................    
                                sat.addDownlink( lineRead.substring(3).trim(), dis.readLine().trim() );
                            } else
                            //..................................................    
                            if( lineRead.startsWith("RX:") && lineRead.length()>3 && (sat != null) ) {
                            //..................................................    
                                sat.addUplink( lineRead.substring(3).trim(), dis.readLine().trim() );
                            }
                            
                        }
                    }//READING_A_SATELLITE
                    
            }//finding a set of keplerian elements in the file
            dis.close();
            fis.close();
        }catch( IOException ioe ){
            System.out.println("Additional sat data file '"+baseDir+"qsgs_sat.txt' not found");
        }
        
        listSatellites.setSelectedIndex(0);
        
        Satellite sat = (Satellite)listSatellites.getItemAt(0);
        txtKepsSatelliteName.setText(sat.getName());
        txtKepsCatalogNumber.setText(sat.getCatalogNumber());
        txtKepsEpochTime.setText(sat.getKepsEpochTime());
        txtKepsElementSet.setText(sat.getKepsElementSet());
        txtKepsInclination.setText(""+sat.getInclination());
        txtKepsRAAN.setText(""+sat.getRAAN());
        txtKepsEccentricity.setText(""+sat.getEccentricity());
        txtKepsArgOfPerigee.setText(""+sat.getArgOfPerigee());
        txtKepsMeanAnomaly.setText(""+sat.getMeanAnomaly());
        txtKepsMeanMotion.setText(""+sat.getMeanMotion());
        txtKepsDecayRate.setText(""+sat.getDecayRate());
        txtKepsEpochRev.setText(""+sat.getEpochRevolution());
                        
        sat.getNotes( baseDir, txtSatNotes );


    /*     
       sat.calculatePosition( 
 new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0),
                                new Date().getTime());
        
       */
    }
    
    /*========================================================================*/
    /*========================================================================*/
    public Satellite getSelectedSatellite() {
    /*========================================================================*/        
        return((Satellite)listSatellites.getSelectedItem());
    }    
    
    
    /*========================================================================*/
    /*========================================================================*/
    public void loadSecurityCodes( String codeFilename ) {
    /*========================================================================*/                                
        FileInputStream fis = null;
        DataInputStream dis = null; 
        String lineRead = null;        
        try {
            fis = new FileInputStream( codeFilename );
            dis = new DataInputStream( fis );
            System.out.println("Found file '"+codeFilename+": Loading satellite security codes");
            
            Satellite sat = null;
            
            while( (dis.available()>0) ) {
                
                /*------------------------------------------*/
                /* Find the first line starting with dashes */
                /*------------------------------------------*/
                lineRead = dis.readLine().trim();
                
                READING_A_SATELLITE2:
                if( lineRead.startsWith("---") ) {

                    /*--------------------------------------------*/
                    /* Now until we reach the NAME field we won't */
                    /* know to which satellite we are looking at. */
                    /*--------------------------------------------*/                        
                    while( (dis.available()>0) ) {
                        lineRead = dis.readLine().trim();
                        //......................................................
                        if( lineRead.startsWith("NAME:") && lineRead.length()>5 ) {
                        //......................................................    
                            String name = lineRead.substring(5).trim();
                            boolean found = false;
                            for( int i=0; i<listSatellites.getItemCount(); i++ ) {
                                sat = (Satellite)listSatellites.getItemAt(i);
                                if( sat.getName().equals(name) ) {
                                    found = true;
                                    break;
                                }
                            }
                            if( !found ) {
                                /*--------------------------------------*/
                                /* Satellite was not originally in the  */
                                /* orbital elements file: Add it anyway */
                                /* we won't be able to track it...      */
                                /*--------------------------------------*/
                                sat = new Satellite( name );
                                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                /* FOR NOW ASSIGN THE SAME SATCONTROL TO */
                                /* EVERYBODY, BUT SHOULD CHANGE...       */
                                /*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
                                SatControl ctrl = new SatControl(baseDir);
                                ctrl.setControlProtocolId( 0x00 );
                                ctrl.setLoaderProtocolId( 0x79 );
                                ctrl.setExtLoaderProtocolId( 0x7A );
                                sat.setControl( ctrl );
                                listSatellites.addItem(sat);
                            }                        
                        } 
                        //......................................................    
                        else if( lineRead.startsWith("CONTROL:") && lineRead.length()>9 ) {
                        //......................................................        
                            if( sat != null ) 
                                sat.setControlCallsign(lineRead.substring(8).trim());
                        } 
                        //......................................................    
                        else if( lineRead.startsWith("MAGIC:") && lineRead.length()>7 ) {
                        //......................................................    
                            if( sat != null ) 
                                sat.setControlKey(lineRead.substring(6).trim());
                        } 
                        //......................................................    
                        else if( lineRead.startsWith("BOOTLOADER_SCID:") && lineRead.length()>17 ) {
                        //......................................................        
                            if( sat != null ) {
                                try{
                                    sat.setBootloaderSCID(Integer.parseInt(lineRead.substring(16).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderSCID( 0 );
                                }
                            }
                        //......................................................    
                        } else if( lineRead.startsWith("BOOTLOADER_AVAL:") && lineRead.length()>17 ) {
                        //......................................................        
                            if( sat != null ) {
                                try{
                                    sat.setBootloaderAVAL(Integer.parseInt(lineRead.substring(16).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderAVAL( 0 );
                                }
                            }
                        //......................................................                                
                        } else if( lineRead.startsWith("BOOTLOADER_BVAL:") && lineRead.length()>17 ) {
                        //......................................................        
                            if( sat != null ) {
                                try{
                                    sat.setBootloaderBVAL(Integer.parseInt(lineRead.substring(16).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderBVAL( 0 );
                                }
                            }
                        //......................................................            
                        } else if( lineRead.startsWith("BOOTLOADER_D3VAL:") && lineRead.length()>18 ) {
                        //......................................................        
                            if( sat != null ) {
                                try{
                                    sat.setBootloaderD3VAL(Integer.parseInt(lineRead.substring(17).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderD3VAL( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_K1H:") && lineRead.length()>16 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderK1H(Integer.parseInt(lineRead.substring(15).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderK1H( 0 );
                                }
                            }
                        //......................................................
                        } else if( lineRead.startsWith("BOOTLOADER_K1L:") && lineRead.length()>16 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderK1L(Integer.parseInt(lineRead.substring(15).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderK1L( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_LOADCMD:") && lineRead.length()>20 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderLoadCmd(Integer.parseInt(lineRead.substring(19).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderLoadCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_DUMPCMD:") && lineRead.length()>20 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderDumpCmd(Integer.parseInt(lineRead.substring(19).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderDumpCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_EXECCMD:") && lineRead.length()>20 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderExecCmd(Integer.parseInt(lineRead.substring(19).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderExecCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_MEMECMD:") && lineRead.length()>20 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderMemeCmd(Integer.parseInt(lineRead.substring(19).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderMemeCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_MEMWCMD:") && lineRead.length()>20 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderMemwCmd(Integer.parseInt(lineRead.substring(19).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderMemwCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_IOECMD:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderIOeCmd(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderIOeCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_IOPCMD:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderIOpCmd(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderIOpCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_TLMCMD:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    System.out.println("Setting beacon bootloader command");
                                    sat.setBootloaderTlmCmd(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderTlmCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_MOVCMD:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderMovCmd(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderMovCmd( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_NOCMD1:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderNoCmd1(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderNoCmd1( 0 );
                                }
                            }
                        //......................................................        
                        } else if( lineRead.startsWith("BOOTLOADER_NOCMD2:") && lineRead.length()>19 ) {
                        //......................................................        
                             if( sat != null ) {
                                try{
                                    sat.setBootloaderNoCmd2(Integer.parseInt(lineRead.substring(18).trim(),16));
                                } catch( NumberFormatException nfe ) {
                                    sat.setBootloaderNoCmd2( 0 );
                                }
                            }

                             
                        }
                        
                    }
                }//READING_A_SATELLITE2
                    
            }//finding a set of security codes in the file
            
            fis.close();
        }catch( IOException ioe ){
            System.out.println("Security codes file '"+codeFilename+"' not found");
            //if( fis != null ) fis.close();
        }
    }

        
        
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
              panelSatellites = new javax.swing.JPanel();
              jPanel157 = new javax.swing.JPanel();
              jScrollPane3 = new javax.swing.JScrollPane();
              txtSatNotes = new javax.swing.JTextArea();
              jPanel164 = new javax.swing.JPanel();
              btnSatNotesSave = new javax.swing.JButton();
              jPanel158 = new javax.swing.JPanel();
              jLabel71 = new javax.swing.JLabel();
              txtKepsSatelliteName = new javax.swing.JTextField();
              jLabel73 = new javax.swing.JLabel();
              txtKepsCatalogNumber = new javax.swing.JTextField();
              jLabel74 = new javax.swing.JLabel();
              txtKepsEpochTime = new javax.swing.JTextField();
              jLabel75 = new javax.swing.JLabel();
              txtKepsElementSet = new javax.swing.JTextField();
              jLabel76 = new javax.swing.JLabel();
              txtKepsInclination = new javax.swing.JTextField();
              jLabel77 = new javax.swing.JLabel();
              txtKepsRAAN = new javax.swing.JTextField();
              jLabel78 = new javax.swing.JLabel();
              txtKepsEccentricity = new javax.swing.JTextField();
              jLabel79 = new javax.swing.JLabel();
              txtKepsArgOfPerigee = new javax.swing.JTextField();
              jLabel80 = new javax.swing.JLabel();
              txtKepsMeanAnomaly = new javax.swing.JTextField();
              jLabel81 = new javax.swing.JLabel();
              txtKepsMeanMotion = new javax.swing.JTextField();
              jLabel82 = new javax.swing.JLabel();
              txtKepsDecayRate = new javax.swing.JTextField();
              jLabel83 = new javax.swing.JLabel();
              txtKepsEpochRev = new javax.swing.JTextField();
              jPanel131 = new javax.swing.JPanel();
              jPanel133 = new javax.swing.JPanel();
              jPanel154 = new javax.swing.JPanel();
              jLabel52 = new javax.swing.JLabel();
              listSatellites = new javax.swing.JComboBox();
              btnGetKeps = new javax.swing.JButton();
              jPanel156 = new javax.swing.JPanel();
              txtSatFeatures = new javax.swing.JTextArea();
              listSatDownlinks = new javax.swing.JComboBox();
              listSatUplinks = new javax.swing.JComboBox();
              jPanel163 = new javax.swing.JPanel();
              btnSatPic = new javax.swing.JButton();
              lblSatName = new javax.swing.JLabel();
              lblSatFullName = new javax.swing.JLabel();
              jPanel155 = new javax.swing.JPanel();
              jLabel72 = new javax.swing.JLabel();
              txtSatCallsign = new javax.swing.JTextField();
              jLabel84 = new javax.swing.JLabel();
              txtSatBroadcast = new javax.swing.JTextField();
              jLabel94 = new javax.swing.JLabel();
              txtSatBBS = new javax.swing.JTextField();
              jLabel1 = new javax.swing.JLabel();
              txtControlCallsign = new javax.swing.JTextField();
              
              setLayout(new java.awt.BorderLayout());
              
              panelSatellites.setLayout(new javax.swing.BoxLayout(panelSatellites, javax.swing.BoxLayout.X_AXIS));
              
              jPanel157.setLayout(new javax.swing.BoxLayout(jPanel157, javax.swing.BoxLayout.Y_AXIS));
              
              jPanel157.setBackground(java.awt.Color.gray);
              jScrollPane3.setMaximumSize(new java.awt.Dimension(1000, 1000));
              txtSatNotes.setToolTipText("User's notes on this satellite: Modify and press \"Save Notes\"");
              txtSatNotes.setFont(new java.awt.Font("Arial Narrow", 0, 11));
              txtSatNotes.setBackground(new java.awt.Color(255, 255, 204));
              txtSatNotes.setMargin(new java.awt.Insets(2, 5, 2, 5));
              jScrollPane3.setViewportView(txtSatNotes);
              
              jPanel157.add(jScrollPane3);
            
            jPanel164.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
              jPanel164.setMaximumSize(new java.awt.Dimension(32767, 39));
              btnSatNotesSave.setToolTipText("Saves the User's Notes on Disk");
              btnSatNotesSave.setText("Save Notes");
              btnSatNotesSave.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnSatNotesSaveActionPerformed(evt);
                  }
              });
              
              jPanel164.add(btnSatNotesSave);
              
              jPanel157.add(jPanel164);
            
            panelSatellites.add(jPanel157);
          
          jPanel158.setLayout(new java.awt.GridLayout(12, 2));
            
            jPanel158.setBorder(new javax.swing.border.TitledBorder("Orbital Elements"));
            jPanel158.setMaximumSize(new java.awt.Dimension(170, 2147483647));
            jLabel71.setText("Satellite:");
            jPanel158.add(jLabel71);
            
            txtKepsSatelliteName.setEditable(false);
            txtKepsSatelliteName.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsSatelliteName);
            
            jLabel73.setText("Catalog Number:");
            jPanel158.add(jLabel73);
            
            txtKepsCatalogNumber.setEditable(false);
            txtKepsCatalogNumber.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsCatalogNumber);
            
            jLabel74.setText("Epoch Time:");
            jPanel158.add(jLabel74);
            
            txtKepsEpochTime.setEditable(false);
            txtKepsEpochTime.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsEpochTime.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsEpochTime);
            
            jLabel75.setText("Element Set:");
            jPanel158.add(jLabel75);
            
            txtKepsElementSet.setEditable(false);
            txtKepsElementSet.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsElementSet.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsElementSet);
            
            jLabel76.setText("Inclination:");
            jPanel158.add(jLabel76);
            
            txtKepsInclination.setEditable(false);
            txtKepsInclination.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsInclination.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsInclination);
            
            jLabel77.setText("RAAN:");
            jPanel158.add(jLabel77);
            
            txtKepsRAAN.setEditable(false);
            txtKepsRAAN.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsRAAN.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsRAAN);
            
            jLabel78.setText("Eccentricity:");
            jPanel158.add(jLabel78);
            
            txtKepsEccentricity.setEditable(false);
            txtKepsEccentricity.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsEccentricity.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsEccentricity);
            
            jLabel79.setText("Arg. of Perigee:");
            jPanel158.add(jLabel79);
            
            txtKepsArgOfPerigee.setEditable(false);
            txtKepsArgOfPerigee.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsArgOfPerigee.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsArgOfPerigee);
            
            jLabel80.setText("Mean Anomaly:");
            jPanel158.add(jLabel80);
            
            txtKepsMeanAnomaly.setEditable(false);
            txtKepsMeanAnomaly.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsMeanAnomaly.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsMeanAnomaly);
            
            jLabel81.setText("Mean Motion:");
            jPanel158.add(jLabel81);
            
            txtKepsMeanMotion.setEditable(false);
            txtKepsMeanMotion.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsMeanMotion.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsMeanMotion);
            
            jLabel82.setText("Decay Rate:");
            jPanel158.add(jLabel82);
            
            txtKepsDecayRate.setEditable(false);
            txtKepsDecayRate.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsDecayRate.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsDecayRate);
            
            jLabel83.setText("Epoch Rev.:");
            jPanel158.add(jLabel83);
            
            txtKepsEpochRev.setEditable(false);
            txtKepsEpochRev.setFont(new java.awt.Font("Arial Narrow", 0, 10));
            txtKepsEpochRev.setPreferredSize(new java.awt.Dimension(75, 20));
            jPanel158.add(txtKepsEpochRev);
            
            panelSatellites.add(jPanel158);
          
          jPanel131.setLayout(new javax.swing.BoxLayout(jPanel131, javax.swing.BoxLayout.Y_AXIS));
                
                jPanel131.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                jPanel131.setMaximumSize(new java.awt.Dimension(350, 32767));
                jPanel133.setBorder(new javax.swing.border.TitledBorder("Setup"));
                jPanel154.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
                jLabel52.setText("Sat:");
                jPanel154.add(jLabel52);
                
                listSatellites.setToolTipText("Selects a Satellite to Become the Active One");
                listSatellites.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        listSatellitesActionPerformed(evt);
                    }
                });
                
                jPanel154.add(listSatellites);
                
                jPanel133.add(jPanel154);
              
              btnGetKeps.setToolTipText("Not Implemented Yet");
              btnGetKeps.setText("Update Keps");
              btnGetKeps.setMaximumSize(new java.awt.Dimension(600, 27));
              btnGetKeps.addActionListener(new java.awt.event.ActionListener() {
                  public void actionPerformed(java.awt.event.ActionEvent evt) {
                      btnGetKepsActionPerformed(evt);
                  }
              });
              
              jPanel133.add(btnGetKeps);
              
              jPanel131.add(jPanel133);
            
            jPanel156.setLayout(new javax.swing.BoxLayout(jPanel156, javax.swing.BoxLayout.X_AXIS));
              
              jPanel156.setBorder(new javax.swing.border.TitledBorder("Features of Selected Satellite"));
              txtSatFeatures.setLineWrap(true);
              txtSatFeatures.setEditable(false);
              txtSatFeatures.setPreferredSize(new java.awt.Dimension(200, 100));
              txtSatFeatures.setMargin(new java.awt.Insets(0, 5, 0, 5));
              jPanel156.add(txtSatFeatures);
              
              jPanel131.add(jPanel156);
            
            listSatDownlinks.setToolTipText("Selects Satellite's Transmitter");
            listSatDownlinks.setPreferredSize(new java.awt.Dimension(300, 55));
            listSatDownlinks.setBorder(new javax.swing.border.TitledBorder("Transmitter"));
            listSatDownlinks.setMinimumSize(new java.awt.Dimension(300, 45));
            listSatDownlinks.setMaximumSize(new java.awt.Dimension(800, 55));
            listSatDownlinks.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listSatDownlinksActionPerformed(evt);
                }
            });
            
            jPanel131.add(listSatDownlinks);
            
            listSatUplinks.setToolTipText("Selects Satellite's Receiver");
            listSatUplinks.setPreferredSize(new java.awt.Dimension(300, 55));
            listSatUplinks.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Receiver"));
            listSatUplinks.setMinimumSize(new java.awt.Dimension(300, 45));
            listSatUplinks.setMaximumSize(new java.awt.Dimension(800, 55));
            listSatUplinks.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    listSatUplinksActionPerformed(evt);
                }
            });
            
            jPanel131.add(listSatUplinks);
            
            panelSatellites.add(jPanel131);
          
          jPanel163.setLayout(new javax.swing.BoxLayout(jPanel163, javax.swing.BoxLayout.Y_AXIS));
            
            jPanel163.setBorder(new javax.swing.border.LineBorder(java.awt.Color.black));
            jPanel163.setPreferredSize(new java.awt.Dimension(150, 235));
            jPanel163.setMinimumSize(new java.awt.Dimension(100, 232));
            jPanel163.setMaximumSize(new java.awt.Dimension(150, 163837));
            btnSatPic.setBackground(java.awt.Color.white);
            btnSatPic.setAlignmentX(0.5F);
            btnSatPic.setPreferredSize(new java.awt.Dimension(100, 100));
            btnSatPic.setVerifyInputWhenFocusTarget(false);
            btnSatPic.setMaximumSize(new java.awt.Dimension(100, 100));
            btnSatPic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            btnSatPic.setVerticalAlignment(javax.swing.SwingConstants.TOP);
            btnSatPic.setMargin(new java.awt.Insets(0, 0, 0, 0));
            btnSatPic.setMinimumSize(new java.awt.Dimension(100, 100));
            btnSatPic.setBorderPainted(false);
            jPanel163.add(btnSatPic);
            
            lblSatName.setText("QSAT-1");
            lblSatName.setToolTipText("Satellite Identification");
            lblSatName.setForeground(java.awt.Color.yellow);
            lblSatName.setBackground(java.awt.Color.blue);
            lblSatName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblSatName.setFont(new java.awt.Font("Arial Black", 0, 12));
            lblSatName.setPreferredSize(new java.awt.Dimension(150, 18));
            lblSatName.setAlignmentX(0.5F);
            lblSatName.setMaximumSize(new java.awt.Dimension(300, 18));
            lblSatName.setOpaque(true);
            lblSatName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            lblSatName.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    lblSatNameMouseClicked(evt);
                }
            });
            
            jPanel163.add(lblSatName);
            
            lblSatFullName.setText("CSA QUICKSAT");
            lblSatFullName.setToolTipText("Satellite Name");
            lblSatFullName.setForeground(java.awt.Color.yellow);
            lblSatFullName.setBackground(java.awt.Color.blue);
            lblSatFullName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            lblSatFullName.setFont(new java.awt.Font("Arial Narrow", 0, 12));
            lblSatFullName.setPreferredSize(new java.awt.Dimension(150, 18));
            lblSatFullName.setAlignmentX(0.5F);
            lblSatFullName.setMaximumSize(new java.awt.Dimension(300, 18));
            lblSatFullName.setOpaque(true);
            lblSatFullName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            jPanel163.add(lblSatFullName);
            
            jPanel155.setLayout(new java.awt.GridLayout(4, 2));
              
              jPanel155.setBorder(new javax.swing.border.TitledBorder("Callsigns"));
              jPanel155.setPreferredSize(new java.awt.Dimension(140, 97));
              jPanel155.setMinimumSize(new java.awt.Dimension(100, 97));
              jPanel155.setMaximumSize(new java.awt.Dimension(150, 100));
              jLabel72.setText("Callsign:");
              jLabel72.setPreferredSize(new java.awt.Dimension(30, 16));
              jLabel72.setMinimumSize(new java.awt.Dimension(20, 16));
              jPanel155.add(jLabel72);
              
              txtSatCallsign.setToolTipText("Satellite General Callsign");
              txtSatCallsign.setPreferredSize(new java.awt.Dimension(50, 20));
              txtSatCallsign.setMaximumSize(new java.awt.Dimension(50, 2147483647));
              jPanel155.add(txtSatCallsign);
              
              jLabel84.setText("Broadcast:");
              jLabel84.setPreferredSize(new java.awt.Dimension(40, 16));
              jLabel84.setMinimumSize(new java.awt.Dimension(40, 16));
              jLabel84.setMaximumSize(new java.awt.Dimension(40, 16));
              jPanel155.add(jLabel84);
              
              txtSatBroadcast.setToolTipText("PACSAT Broadcast Callsign");
              txtSatBroadcast.setPreferredSize(new java.awt.Dimension(50, 20));
              txtSatBroadcast.setMaximumSize(new java.awt.Dimension(50, 2147483647));
              jPanel155.add(txtSatBroadcast);
              
              jLabel94.setText("BBS:");
              jPanel155.add(jLabel94);
              
              txtSatBBS.setToolTipText("PACSAT BBS Callsign");
              txtSatBBS.setPreferredSize(new java.awt.Dimension(50, 20));
              txtSatBBS.setMaximumSize(new java.awt.Dimension(50, 2147483647));
              jPanel155.add(txtSatBBS);
              
              jLabel1.setText("Control:");
              jPanel155.add(jLabel1);
              
              jPanel155.add(txtControlCallsign);
              
              jPanel163.add(jPanel155);
            
            panelSatellites.add(jPanel163);
          
          add(panelSatellites, java.awt.BorderLayout.CENTER);
        
    }//GEN-END:initComponents

    private void lblSatNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblSatNameMouseClicked
        Satellite sat = (Satellite)listSatellites.getSelectedItem();
        System.out.print("Load: "+sat.getBootloaderLoadCmd());
        System.out.print(" Dump: "+sat.getBootloaderDumpCmd());
        System.out.print(" Exec: "+sat.getBootloaderExecCmd());
        System.out.print(" MemE: "+sat.getBootloaderMemeCmd());
        System.out.print(" MemW: "+sat.getBootloaderMemwCmd());
        System.out.print(" IOEx: "+sat.getBootloaderIOeCmd());
        System.out.print(" IOPo: "+sat.getBootloaderIOpCmd());
        System.out.print(" Tlmy: "+sat.getBootloaderTlmCmd());
        System.out.print(" Move: "+sat.getBootloaderMovCmd());
        System.out.print(" Cmd1: "+sat.getBootloaderNoCmd1());
        System.out.println(" Cmd2: "+sat.getBootloaderNoCmd2());

      
                
    }//GEN-LAST:event_lblSatNameMouseClicked

    private void btnGetKepsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetKepsActionPerformed

        /*---------------------------------------------------*/
        /* Our prime source for orbital element is CELESTRAK */        
        /*  http://celestrak.com/NORAD/elements/amateur.txt  */
        /*---------------------------------------------------*/
        DialogLoadKeps dialog = new DialogLoadKeps( parentFrame, baseDir );
        dialog.setVisible( true );        
        
    }//GEN-LAST:event_btnGetKepsActionPerformed

    private void listSatUplinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSatUplinksActionPerformed
        Satellite sat = (Satellite)listSatellites.getSelectedItem();
        Link link = (Link)listSatUplinks.getSelectedItem();
        System.out.println("Setting uplink " + link + " for " + sat );
        sat.setActiveUplink( (Link)listSatUplinks.getSelectedItem() );
    }//GEN-LAST:event_listSatUplinksActionPerformed

    private void listSatDownlinksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSatDownlinksActionPerformed
        Satellite sat = (Satellite)listSatellites.getSelectedItem();
        Link link = (Link)listSatDownlinks.getSelectedItem();
        System.out.println("Setting downlink " + link + " for " + sat );
        sat.setActiveDownlink( (Link)listSatDownlinks.getSelectedItem() );
    }//GEN-LAST:event_listSatDownlinksActionPerformed

    private void listSatellitesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSatellitesActionPerformed
      Satellite sat = (Satellite)listSatellites.getSelectedItem();
sat.calculatePosition( 
 new GroundStation("montreal", "10.20.36.71", 45.5D, 73.582999999999998D, 0),
                                new Date().getTime());
        mapper.satsVect.removeAllElements();
        mapper.createSats(sat);
        
 // panelOrbit.updateSat(sat);
      txtKepsSatelliteName.setText(sat.getName());
      txtKepsCatalogNumber.setText(sat.getCatalogNumber());
      txtKepsEpochTime.setText(sat.getKepsEpochTime());
      txtKepsElementSet.setText(sat.getKepsElementSet());
      txtKepsInclination.setText(""+sat.getInclination());
      txtKepsRAAN.setText(""+sat.getRAAN());
      txtKepsEccentricity.setText(""+sat.getEccentricity());
      txtKepsArgOfPerigee.setText(""+sat.getArgOfPerigee());
      txtKepsMeanAnomaly.setText(""+sat.getMeanAnomaly());
      txtKepsMeanMotion.setText(""+sat.getMeanMotion());
      txtKepsDecayRate.setText(""+sat.getDecayRate());
      txtKepsEpochRev.setText(""+sat.getEpochRevolution());
      
      sat.getNotes( baseDir, txtSatNotes );
      
      /*---------------------------*/
      /* Set button on radio panel */
      /*---------------------------*/
      //btnRadSetToSatellite.setText("Set to "+sat.name+" freq's");
      
        /*--------------------*/
        /* Look for a picture */
        /*--------------------*/
      ImageIcon satImage = new ImageIcon(baseDir+"satpic/"+sat.getName().trim()+".gif");
      if( satImage.getIconHeight() > 0 )
          btnSatPic.setIcon(satImage);
      else
          btnSatPic.setIcon(new ImageIcon(baseDir+"satpic/generic.gif"));
      
      lblSatName.setText(sat.getName());
      lblSatFullName.setText(sat.getFullName());
      txtSatFeatures.setText(sat.getFeatures());
      txtSatCallsign.setText(sat.getControlCallsign());
      txtSatBroadcast.setText(sat.getPacsatBroadcastCallsign());
      txtSatBBS.setText(sat.getPacsatCallsign()+"-"+sat.getPacsatSSID());
      txtControlCallsign.setText(sat.getControlCallsign());
      
      listSatUplinks.removeAllItems();
      if( sat.getFirstUplink() != null ) {
            listSatUplinks.addItem(sat.getFirstUplink());            
            for( int i=1; i<sat.getUplinksCount(); i++ ) {
                Link link = sat.getNextUplink();
                if( link != null )  {                    
                    listSatUplinks.addItem(link);
                }
            }
      } 

      listSatDownlinks.removeAllItems();
      if( sat.getFirstDownlink() != null ) {
            listSatDownlinks.addItem(sat.getFirstDownlink());            
            for( int i=1; i<sat.getDownlinksCount(); i++ ) {
                if( sat.getNextDownlink() != null ) 
                    listSatDownlinks.addItem(sat.getNextDownlink());
            }
      } 
      

    }//GEN-LAST:event_listSatellitesActionPerformed

    private void btnSatNotesSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSatNotesSaveActionPerformed
        Satellite sat = (Satellite)listSatellites.getSelectedItem();
        if( sat != null )
            sat.saveNotes( baseDir, txtSatNotes.getText() );
    }//GEN-LAST:event_btnSatNotesSaveActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel panelSatellites;
    private javax.swing.JPanel jPanel157;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea txtSatNotes;
    private javax.swing.JPanel jPanel164;
    private javax.swing.JButton btnSatNotesSave;
    private javax.swing.JPanel jPanel158;
    private javax.swing.JLabel jLabel71;
    private javax.swing.JTextField txtKepsSatelliteName;
    private javax.swing.JLabel jLabel73;
    private javax.swing.JTextField txtKepsCatalogNumber;
    private javax.swing.JLabel jLabel74;
    private javax.swing.JTextField txtKepsEpochTime;
    private javax.swing.JLabel jLabel75;
    private javax.swing.JTextField txtKepsElementSet;
    private javax.swing.JLabel jLabel76;
    private javax.swing.JTextField txtKepsInclination;
    private javax.swing.JLabel jLabel77;
    private javax.swing.JTextField txtKepsRAAN;
    private javax.swing.JLabel jLabel78;
    private javax.swing.JTextField txtKepsEccentricity;
    private javax.swing.JLabel jLabel79;
    private javax.swing.JTextField txtKepsArgOfPerigee;
    private javax.swing.JLabel jLabel80;
    private javax.swing.JTextField txtKepsMeanAnomaly;
    private javax.swing.JLabel jLabel81;
    private javax.swing.JTextField txtKepsMeanMotion;
    private javax.swing.JLabel jLabel82;
    private javax.swing.JTextField txtKepsDecayRate;
    private javax.swing.JLabel jLabel83;
    private javax.swing.JTextField txtKepsEpochRev;
    private javax.swing.JPanel jPanel131;
    private javax.swing.JPanel jPanel133;
    private javax.swing.JPanel jPanel154;
    private javax.swing.JLabel jLabel52;
    public javax.swing.JComboBox listSatellites;
    private javax.swing.JButton btnGetKeps;
    private javax.swing.JPanel jPanel156;
    private javax.swing.JTextArea txtSatFeatures;
    public javax.swing.JComboBox listSatDownlinks;
    public javax.swing.JComboBox listSatUplinks;
    private javax.swing.JPanel jPanel163;
    private javax.swing.JButton btnSatPic;
    private javax.swing.JLabel lblSatName;
    private javax.swing.JLabel lblSatFullName;
    private javax.swing.JPanel jPanel155;
    private javax.swing.JLabel jLabel72;
    private javax.swing.JTextField txtSatCallsign;
    private javax.swing.JLabel jLabel84;
    private javax.swing.JTextField txtSatBroadcast;
    private javax.swing.JLabel jLabel94;
    private javax.swing.JTextField txtSatBBS;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField txtControlCallsign;
    // End of variables declaration//GEN-END:variables

}
