/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

/*
 * Cart.java
 *
 * Created on August 22, 2000, 4:04 PM
 */


import java.beans.*;

/**
 *
 * @author  christag
 * @version 
 */
public class Cart extends Object implements java.io.Serializable {

    private static final String PROP_SAMPLE_PROPERTY = "SampleProperty";

    private String sampleProperty;

    private PropertyChangeSupport propertySupport;

    /** Holds value of property lineItems. */
    public java.util.Vector lineItems;
    
    /** Creates new Cart */
    public Cart() {
        propertySupport = new PropertyChangeSupport ( this );
        lineItems = new java.util.Vector();
    }

    public String getSampleProperty () {
        return sampleProperty;
    }

    public void setSampleProperty (String value) {
        String oldValue = sampleProperty;
        sampleProperty = value;
        propertySupport.firePropertyChange (PROP_SAMPLE_PROPERTY, oldValue, sampleProperty);
    }


    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }

    /** Getter for property lineItems.
 * @return Value of property lineItems.
 */
    public java.util.Vector getLineItems() {
  return lineItems;
    }
    
    /** Setter for property lineItems.
 * @param lineItems New value of property lineItems.
 */
    public void setLineItems(java.util.Vector lineItems) {
  this.lineItems = lineItems;
    }

   /** 
   * Returns the element number of the item in the cartItems as specified by the passed ID
   */  
public int findLineItem(int pID) {
        System.out.println("Entering Cart.findLineItem()");
        int cartSize = (lineItems == null) ? 0 : lineItems.size();
        int i ;
        for ( i = 0 ; i < cartSize ; i++ )
        {
            if  ( pID == ((CartLineItem)lineItems.elementAt(i)).getId() )
                break ;
        }
        if (i >= cartSize) {
            System.out.println("Couldn't find line item for ID: " + pID);
            return -1 ;
        }
        else
            return i ;
    }

   /** Removes a cartItem from the cartItems list.
   * @param pID ID of CartLineItem to remove.
   */
  public void removeLineItem(int pID) {
    System.out.println("Entering cart.removeLineItem()");
    int i = findLineItem(pID);
    if (i != -1) lineItems.remove(i);
    System.out.println("Leaving cart.removeLineItem()");
  }
  
  
}
