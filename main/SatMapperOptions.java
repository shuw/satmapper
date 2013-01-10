/*
 * SatMapperOptions.java
 *
 * Created on March 11, 2002, 2:02 PM
 */

/**
 *
 * @author  TWu
 */
public class SatMapperOptions extends javax.swing.JFrame {
    SatMapper parent;
    /** Creates new form SatMapperOptions */
    public SatMapperOptions(SatMapper parent) {
        super("SatMapper Options");
        this.parent = parent;
        initComponents();
        
        if ( !parent.langEng) {}
            
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
        switchNightModeCheckBox = new javax.swing.JCheckBox();
        frenchRadioButton = new javax.swing.JRadioButton();
        englishRadioButton = new javax.swing.JRadioButton();
        message = new javax.swing.JLabel();
        
        
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        
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
        
        getContentPane().add(okButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 240, -1, -1));
        
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(cancelButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 240, -1, -1));
        
        addPlaceButton.setText("add location");
        addPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPlaceButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(addPlaceButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 130, -1, -1));
        
        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(305, 104, -1, -1));
        
        applyButton.setText("jButton4");
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(applyButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 240, -1, -1));
        
        nameTextField.setText("place");
        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });
        
        getContentPane().add(nameTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 40, 70, -1));
        
        nameLabel.setText("Name:");
        getContentPane().add(nameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));
        
        longLabel.setText("Longitude:");
        getContentPane().add(longLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));
        
        longTextField.setText("0");
        longTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        getContentPane().add(longTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 70, -1, -1));
        
        latTextField.setText("0");
        latTextField.setPreferredSize(new java.awt.Dimension(60, 20));
        getContentPane().add(latTextField, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 100, -1, -1));
        
        latLabel.setText("Latitude:");
        getContentPane().add(latLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));
        
        showCloudsCheckBox.setText("Show Clouds (zoom 2X)");
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
        
        getContentPane().add(showCloudsCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 40, -1, -1));
        
        switchNightModeCheckBox.setText("Switch Night/Day");
        switchNightModeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                switchNightModeCheckBoxActionPerformed(evt);
            }
        });
        
        getContentPane().add(switchNightModeCheckBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 70, -1, -1));
        
        frenchRadioButton.setText("Francais");
        languageButtonGroup.add(frenchRadioButton);
        getContentPane().add(frenchRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 140, -1, -1));
        
        englishRadioButton.setSelected(true);
        englishRadioButton.setText("English");
        languageButtonGroup.add(englishRadioButton);
        englishRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                englishRadioButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(englishRadioButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 110, -1, -1));
        
        message.setFont(new java.awt.Font("Dialog", 0, 12));
        message.setText("add your home to the map");
        getContentPane().add(message, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));
        
        pack();
    }//GEN-END:initComponents

    private void switchNightModeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_switchNightModeCheckBoxActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_switchNightModeCheckBoxActionPerformed

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
            message.setText("Longitude must be between -180 and 180");        
        else if ( latitude > 90 || latitude < -90)
            message.setText("Latitude must be between -90 and 90");
        else {
            parent.addPlace( latitude,
                             longitude,
                             nameTextField.getText()            );
        }

    }//GEN-LAST:event_addPlaceButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        // Add your handling code here:
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
        
        if ( showCloudsCheckBox.isSelected() )
            parent.removeClouds = true;
        else
            parent.removeClouds = false;
        
        if ( switchNightModeCheckBox.isSelected() )
            parent.switchNightMode();
        
        if ( englishRadioButton.isSelected() )
            parent.langEng = true;
        else if ( frenchRadioButton.isSelected() )
            parent.langEng = false;
        else
            System.out.println("No Language Selected");
        this.hide();        
        
        
    }//GEN-LAST:event_okButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SatMapperOptions(new SatMapper()).show();
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
    private javax.swing.JCheckBox switchNightModeCheckBox;
    private javax.swing.JRadioButton frenchRadioButton;
    private javax.swing.JRadioButton englishRadioButton;
    private javax.swing.JLabel message;
    // End of variables declaration//GEN-END:variables

}
