/*
 * SatMapperOptions.java
 *
 * Created on March 11, 2002, 2:02 PM
 */

/**
 *
 * @author  TWu
 */
package ca.gc.space.quicksat.ground.tracking;

public class SatMapperOptions extends javax.swing.JFrame {
    SatMapper parent;
    int updateIntervalSliderValue = 2000;
    int speedTimeXSliderValue = 1;
    
    /** Creates new form SatMapperOptions */
    public SatMapperOptions(SatMapper parent) {
        super("SatMapper Options");
        this.parent = parent;
        initComponents();
        updateIntervalSlider.setEnabled(false);
        updateIntervallabel.setEnabled(false);
        resetTimeCheckBox.setEnabled(false);
        
        
        setLocal(false);
        
        if ( !parent.langEng) {}
        
    }
    public void show() {
        setSize(400,450);
        super.show();
        //        setSize(400,400);
        if ( parent.isLocal ) {
            localCheckBox.setSelected(true);
            setLocal(true);
        }
        else {
            setLocal(false);
            localCheckBox.setSelected(false);
        }
        //removes option for changing maps
        if ( !parent.isHiRes) {
            showCloudsCheckBox.setEnabled(false);
            nightModeCheckBox.setEnabled(false);
        }
        
        if (parent.followSat) followSatCheckBox.setSelected(true);
        else followSatCheckBox.setSelected(false);
        this.repaint();
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        languageButtonGroup = new javax.swing.ButtonGroup();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        addPlaceButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        longLabel = new javax.swing.JLabel();
        longTextField = new javax.swing.JTextField();
        latTextField = new javax.swing.JTextField();
        latLabel = new javax.swing.JLabel();
        showCloudsCheckBox = new javax.swing.JCheckBox();
        nightModeCheckBox = new javax.swing.JCheckBox();
        frenchRadioButton = new javax.swing.JRadioButton();
        englishRadioButton = new javax.swing.JRadioButton();
        message = new javax.swing.JLabel();
        showGridCheckBox = new javax.swing.JCheckBox();
        updateIntervalSlider = new javax.swing.JSlider();
        updateIntervallabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        showRangeCheckBox = new javax.swing.JCheckBox();
        followSatCheckBox = new javax.swing.JCheckBox();
        helpTextLabel = new javax.swing.JLabel();
        resetTimeCheckBox = new javax.swing.JCheckBox();
        showPathCheckBox = new javax.swing.JCheckBox();
        localCheckBox = new javax.swing.JCheckBox();
        speedTimeXSlider = new javax.swing.JSlider();
        speedTimeXLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        
        
        getContentPane().setLayout(null);
        
        setTitle("Options");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(okButton);
        okButton.setBounds(250, 350, 50, 26);
        
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(cancelButton);
        cancelButton.setBounds(310, 350, 73, 26);
        
        addPlaceButton.setText("add location");
        addPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlaceButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(addPlaceButton);
        addPlaceButton.setBounds(10, 130, 103, 26);
        
        getContentPane().add(jPanel1);
        jPanel1.setBounds(305, 104, 10, 10);
        
        applyButton.setText("Apply");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(applyButton);
        applyButton.setBounds(180, 350, 65, 26);
        
        nameTextField.setText("place");
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        
        getContentPane().add(nameTextField);
        nameTextField.setBounds(80, 40, 70, 20);
        
        nameLabel.setText("Name:");
        getContentPane().add(nameLabel);
        nameLabel.setBounds(10, 40, 36, 16);
        
        longLabel.setText("Longitude:");
        getContentPane().add(longLabel);
        longLabel.setBounds(10, 70, 59, 16);
        
        longTextField.setText("0");
        longTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        longTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                longTextFieldMouseEntered(evt);
            }
        });
        
        getContentPane().add(longTextField);
        longTextField.setBounds(80, 70, 60, 20);
        
        latTextField.setText("0");
        latTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        latTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                latTextFieldMouseEntered(evt);
            }
        });
        
        getContentPane().add(latTextField);
        latTextField.setBounds(80, 100, 60, 20);
        
        latLabel.setText("Latitude:");
        getContentPane().add(latLabel);
        latLabel.setBounds(10, 100, 49, 16);
        
        showCloudsCheckBox.setText("Show Clouds");
        showCloudsCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                showCloudsCheckBoxStateChanged(evt);
            }
        });
        
        showCloudsCheckBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showCloudsCheckBoxItemStateChanged(evt);
            }
        });
        
        showCloudsCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                showCloudsCheckBoxMouseEntered(evt);
            }
        });
        
        getContentPane().add(showCloudsCheckBox);
        showCloudsCheckBox.setBounds(190, 50, 99, 20);
        
        nightModeCheckBox.setText("Night Mode");
        nightModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nightModeCheckBoxActionPerformed(evt);
            }
        });
        
        nightModeCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                nightModeCheckBoxMouseEntered(evt);
            }
        });
        
        getContentPane().add(nightModeCheckBox);
        nightModeCheckBox.setBounds(190, 20, 88, 20);
        
        frenchRadioButton.setText("Francais");
        languageButtonGroup.add(frenchRadioButton);
        frenchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                frenchRadioButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(frenchRadioButton);
        frenchRadioButton.setBounds(10, 350, 74, 24);
        
        englishRadioButton.setSelected(true);
        englishRadioButton.setText("English");
        languageButtonGroup.add(englishRadioButton);
        englishRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                englishRadioButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(englishRadioButton);
        englishRadioButton.setBounds(90, 350, 66, 24);
        
        message.setFont(new java.awt.Font("Dialog", 0, 12));
        message.setText("add your home to the map");
        getContentPane().add(message);
        message.setBounds(10, 10, 143, 16);
        
        showGridCheckBox.setSelected(true);
        showGridCheckBox.setText("Show Grid");
        getContentPane().add(showGridCheckBox);
        showGridCheckBox.setBounds(190, 80, 83, 20);
        
        updateIntervalSlider.setMaximum(10000);
        updateIntervalSlider.setMinimum(50);
        updateIntervalSlider.setValue(2000);
        updateIntervalSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                updateIntervalSliderStateChanged(evt);
            }
        });
        
        getContentPane().add(updateIntervalSlider);
        updateIntervalSlider.setBounds(60, 240, 270, 30);
        
        updateIntervallabel.setText("Update Interval for Satellite Movements");
        getContentPane().add(updateIntervallabel);
        updateIntervallabel.setBounds(90, 270, 222, 16);
        
        jLabel3.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel3.setText("50X");
        getContentPane().add(jLabel3);
        jLabel3.setBounds(30, 300, 19, 15);
        
        jLabel4.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel4.setText("10000ms");
        getContentPane().add(jLabel4);
        jLabel4.setBounds(330, 250, 44, 15);
        
        showRangeCheckBox.setSelected(true);
        showRangeCheckBox.setText("Show Range of Satellites");
        showRangeCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                showRangeCheckBoxMouseEntered(evt);
            }
        });
        
        getContentPane().add(showRangeCheckBox);
        showRangeCheckBox.setBounds(190, 110, 166, 20);
        
        followSatCheckBox.setText("Follow Selected Satellite");
        followSatCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                followSatCheckBoxMouseEntered(evt);
            }
        });
        
        getContentPane().add(followSatCheckBox);
        followSatCheckBox.setBounds(190, 140, 163, 20);
        
        helpTextLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        getContentPane().add(helpTextLabel);
        helpTextLabel.setBounds(10, 380, 370, 20);
        
        resetTimeCheckBox.setText("Reset clock to current time");
        getContentPane().add(resetTimeCheckBox);
        resetTimeCheckBox.setBounds(170, 210, 179, 24);
        
        showPathCheckBox.setText("Show Path of Satellite");
        showPathCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPathCheckBoxActionPerformed(evt);
            }
        });
        
        getContentPane().add(showPathCheckBox);
        showPathCheckBox.setBounds(10, 210, 149, 24);
        
        localCheckBox.setText("Perform calculations on Local Machine");
        localCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localCheckBoxActionPerformed(evt);
            }
        });
        
        localCheckBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                localCheckBoxMouseEntered(evt);
            }
        });
        
        getContentPane().add(localCheckBox);
        localCheckBox.setBounds(10, 180, 246, 20);
        
        speedTimeXSlider.setMaximum(50);
        speedTimeXSlider.setMinimum(-50);
        speedTimeXSlider.setValue(1);
        speedTimeXSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedTimeXSliderStateChanged(evt);
            }
        });
        
        getContentPane().add(speedTimeXSlider);
        speedTimeXSlider.setBounds(60, 300, 270, 16);
        
        speedTimeXLabel.setText("Speed of Time");
        getContentPane().add(speedTimeXLabel);
        speedTimeXLabel.setBounds(91, 320, 220, 16);
        
        jLabel5.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel5.setText("50X");
        getContentPane().add(jLabel5);
        jLabel5.setBounds(340, 300, 19, 15);
        
        jLabel6.setFont(new java.awt.Font("Dialog", 0, 11));
        jLabel6.setText("50ms");
        getContentPane().add(jLabel6);
        jLabel6.setBounds(30, 250, 26, 15);
        
        pack();
    }//GEN-END:initComponents
    
    private void speedTimeXSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedTimeXSliderStateChanged
        if (parent.isLocal) {
            speedTimeXLabel.setText("Speed of Time: " + speedTimeXSliderValue + "X");
            speedTimeXSliderValue = speedTimeXSlider.getValue();
        }
    }//GEN-LAST:event_speedTimeXSliderStateChanged
    
    private void showPathCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPathCheckBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_showPathCheckBoxActionPerformed
    
    private void localCheckBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_localCheckBoxMouseEntered
        helpTextLabel.setText("selected if server is down or needed for additional features");
    }//GEN-LAST:event_localCheckBoxMouseEntered
    
    private void localCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localCheckBoxActionPerformed
        if (localCheckBox.isSelected()) setLocal(true);
        else  setLocal(false);
    }//GEN-LAST:event_localCheckBoxActionPerformed
    
    private void nightModeCheckBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_nightModeCheckBoxMouseEntered
        helpTextLabel.setText("uses a map of the earth at night");
    }//GEN-LAST:event_nightModeCheckBoxMouseEntered
    
    private void showRangeCheckBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showRangeCheckBoxMouseEntered
        helpTextLabel.setText("shows the the range circle in which satellite is visible");
    }//GEN-LAST:event_showRangeCheckBoxMouseEntered
    
    private void followSatCheckBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_followSatCheckBoxMouseEntered
        helpTextLabel.setText("always centers the screen on the selected Satellite");
    }//GEN-LAST:event_followSatCheckBoxMouseEntered
    
    private void showCloudsCheckBoxMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_showCloudsCheckBoxMouseEntered
        helpTextLabel.setText("shows clouds in zoom levels below 2");
    }//GEN-LAST:event_showCloudsCheckBoxMouseEntered
    
    private void latTextFieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_latTextFieldMouseEntered
        helpTextLabel.setText("for south, use a negative number");
    }//GEN-LAST:event_latTextFieldMouseEntered
    
    private void longTextFieldMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_longTextFieldMouseEntered
        helpTextLabel.setText("for west, use a negative number");
    }//GEN-LAST:event_longTextFieldMouseEntered
    
    private void updateIntervalSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_updateIntervalSliderStateChanged
        updateIntervalSliderValue = updateIntervalSlider.getValue();
        if (parent.isLocal) {
            updateIntervallabel.setText("Update Interval: " + updateIntervalSliderValue + "ms");
            updateIntervalSliderValue = updateIntervalSlider.getValue();
        }
        else updateIntervallabel.setText("Only Local Version's update frequency can be changed");
    }//GEN-LAST:event_updateIntervalSliderStateChanged
    
    private void frenchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_frenchRadioButtonActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_frenchRadioButtonActionPerformed
    
    private void nightModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nightModeCheckBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_nightModeCheckBoxActionPerformed
    
    private void showCloudsCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showCloudsCheckBoxItemStateChanged
        // Add your handling code here
    }//GEN-LAST:event_showCloudsCheckBoxItemStateChanged
    
    private void showCloudsCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_showCloudsCheckBoxStateChanged
        // Add your handling code here:
    }//GEN-LAST:event_showCloudsCheckBoxStateChanged

    private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_nameTextFieldActionPerformed
    
    private void addPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPlaceButtonActionPerformed
        double longitude, latitude;  // The user's number.
        Double d;
        try {
            d = new Double( longTextField.getText() );
            longitude = d.doubleValue();
        }
        catch (NumberFormatException e) {
            message.setText("\"" + longTextField.getText() +
            "\" must be a number.");
            longTextField.selectAll();
            longTextField.requestFocus();
            return;
        }
        
        try {
            d = new Double( latTextField.getText() );
            latitude = d.doubleValue();
        }
        catch (NumberFormatException e) {
            message.setText("\"" + latTextField.getText() +
            "\" must be a number.");
            latTextField.selectAll();
            latTextField.requestFocus();
            return;
        }
        
        if ( longitude > 180 || longitude < -180)
            message.setText("Longitude must be  (-180 - 180)");
        else if ( latitude > 90 || latitude < -90)
            message.setText("Latitude must be  (-90 - 90)");
        else {
            parent.addPlace( latitude,
            longitude,
            nameTextField.getText(), true            );
        }
        parent.repaint();
        
    }//GEN-LAST:event_addPlaceButtonActionPerformed
    
    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        applyChanges();
    }//GEN-LAST:event_applyButtonActionPerformed
    
    private void englishRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_englishRadioButtonActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_englishRadioButtonActionPerformed
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.hide();
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        //        if (evt.getStateChange() )
        //
        //            parent.showClouds = true;
        //        evt.
        //
        //        else parent.showClouds = false;
        
        applyChanges();
        
        this.hide();
        
        
    }//GEN-LAST:event_okButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    private void applyChanges() {
        if ( showCloudsCheckBox.isSelected() )
            parent.removeClouds = false;
        else
            parent.removeClouds = true;
        
        if ( nightModeCheckBox.isSelected() && parent.nightTransition == 0)
            parent.switchNightMode();
        else if ( !nightModeCheckBox.isSelected() && parent.nightTransition == 1)
            parent.switchNightMode();
        
        if ( englishRadioButton.isSelected() )
            parent.langEng = true;
        else if ( frenchRadioButton.isSelected() )
            parent.langEng = false;
        else
            System.out.println("No Language Selected");
        
        if ( showGridCheckBox.isSelected() ) {
            parent.setWindowSize( parent.ScreenX - 30, parent.ScreenY - 15);
            parent.showGrid = true;
        }
        else {
            parent.setWindowSize( parent.ScreenX, parent.ScreenY);
            parent.showGrid = false;
        }
        
        if ( showRangeCheckBox.isSelected() )
            parent.showRange = true;
        else
            parent.showRange = false;
        
        if ( followSatCheckBox.isSelected() )
            parent.followSat = true;
        else
            parent.followSat = false;
        
        if ( resetTimeCheckBox.isSelected() ) {
            parent.myData.timeJump = 0;
            resetTimeCheckBox.setSelected(false);
            speedTimeXSliderValue = 1;
            speedTimeXSlider.setValue(1);
        }
        
        if ( localCheckBox.isSelected() ) parent.isLocal = true;
        else parent.isLocal = false;
        
        if ( showPathCheckBox.isSelected() ) {
            parent.showPath = true;
        }
        else {
            parent.showPath = false;
        }
        
        parent.myData.updateInterval = updateIntervalSliderValue;
        parent.myData.timeSpeedX = speedTimeXSliderValue;
        parent.repaint();
    }
    
    public void setLocal(boolean isEnabled) {
        showPathCheckBox.setEnabled(isEnabled);
        updateIntervalSlider.setEnabled(isEnabled);
        updateIntervallabel.setEnabled(isEnabled);
        resetTimeCheckBox.setEnabled(isEnabled);
        
        speedTimeXLabel.setEnabled(isEnabled);
        speedTimeXSlider.setEnabled(isEnabled);
        jLabel3.setEnabled(isEnabled);
        jLabel4.setEnabled(isEnabled);
        jLabel5.setEnabled(isEnabled);
        jLabel6.setEnabled(isEnabled);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup languageButtonGroup;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton addPlaceButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton applyButton;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel longLabel;
    private javax.swing.JTextField longTextField;
    private javax.swing.JTextField latTextField;
    private javax.swing.JLabel latLabel;
    private javax.swing.JCheckBox showCloudsCheckBox;
    private javax.swing.JCheckBox nightModeCheckBox;
    private javax.swing.JRadioButton frenchRadioButton;
    private javax.swing.JRadioButton englishRadioButton;
    private javax.swing.JLabel message;
    private javax.swing.JCheckBox showGridCheckBox;
    private javax.swing.JSlider updateIntervalSlider;
    private javax.swing.JLabel updateIntervallabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JCheckBox showRangeCheckBox;
    private javax.swing.JCheckBox followSatCheckBox;
    private javax.swing.JLabel helpTextLabel;
    private javax.swing.JCheckBox resetTimeCheckBox;
    private javax.swing.JCheckBox showPathCheckBox;
    private javax.swing.JCheckBox localCheckBox;
    private javax.swing.JSlider speedTimeXSlider;
    private javax.swing.JLabel speedTimeXLabel;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    // End of variables declaration//GEN-END:variables
    public void setLangFrench() {
        
        
        
    }
//    this.
    public void setLangEnglish() {
        
    }
    

}
