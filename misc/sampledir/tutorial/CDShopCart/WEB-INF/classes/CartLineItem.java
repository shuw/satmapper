/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

/*
 * CartLineItem.java
 *
 * Created on August 22, 2000, 3:52 PM
 */


import java.beans.*;

/**
 *
 * @author  christag
 * @version 
 */
public class CartLineItem extends Object implements java.io.Serializable {

    private static final String PROP_SAMPLE_PROPERTY = "SampleProperty";

    private PropertyChangeSupport propertySupport;
   
    /** Holds value of property id. */
    private int id;
    
    /** Holds value of property cdtitle. */
    private String cdtitle;
    
    /** Holds value of property price. */
    private double price;
        
    /** Creates new CartLineItem */
    public CartLineItem() {
        propertySupport = new PropertyChangeSupport ( this );
    }

    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }
   
    /** Getter for property id.
 * @return Value of property id.
 */
    public int getId() {
  return id;
    }
    
    /** Setter for property id.
 * @param pId New value of property id.
 */
    public void setId(int pId) {
  this.id = pId;
    }
    
    /** Setter for property ID.
 * @param pId New value of property ID.
     * Overloaded method with String parameter.
 */
    
public void setId(java.lang.String pId) {
    int val = Integer.parseInt(pId);    // no error checking done here
    this.setId(val);
}
    
    /** Getter for property cdtitle.
 * @return Value of property cdtitle.
 */
    public String getCdtitle() {
  return cdtitle;
    }
    
    /** Setter for property cdtitle.
 * @param pCdtitle New value of property cdtitle.
 */
    public void setCdtitle(String pCdtitle) {
  this.cdtitle = pCdtitle;
    }
    /** Getter for property price.
 * @return Value of property price.
 */ 
    public double getPrice() {
  return price;
    }
   
    /** Setter for property price.
 * @param pPrice New value of property price.
 */

    public void setPrice(double pPrice) {
  this.price = pPrice;
    }

        /** Setter for property price.
 * @param pPrice New value of property price.
 *Overloaded method with String parameter.
 */
    public void setPrice(String pPrice) {
        double val = Double.parseDouble(pPrice);
        this.setPrice(val);        
    }

}
