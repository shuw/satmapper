/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

/*
 * BeanPatterns.java
 *
 * Created on September 6, 2000, 1:59 PM
 */

import java.beans.*;

public class BeanPatterns extends Object implements java.io.Serializable {


    private PropertyChangeSupport propertySupport;

    /** Holds value of property intValue. */
    private int intValue;

    /** Holds value of property floatValue. */
    private float floatValue;

    /** Holds value of property charValue. */
    private char charValue;

    /** Holds value of property booleanValue. */
    private boolean booleanValue;

    /** Holds value of property stringValue. */
    private String stringValue;

    /** Holds value of property integerObj. */
    private Integer integerObj;

    /** Holds value of property readOnly. */
    private String readOnly="readOnly";

    /** Holds value of property writeOnly. */
    private String writeOnly;

    /** Creates new BeanPatterns */
    public BeanPatterns() {
        propertySupport = new PropertyChangeSupport ( this );
    }


    public void addPropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener (PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener (listener);
    }

    /** Getter for property intValue.
     * @return Value of property intValue.
     */
    public int getIntValue() {
        return intValue;
    }

    /** Setter for property intValue.
     * @param intValue New value of property intValue.
     */
    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    /** Getter for property floatValue.
     * @return Value of property floatValue.
     */
    public float getFloatValue() {
        return floatValue;
    }

    /** Setter for property floatValue.
     * @param floatValue New value of property floatValue.
     */
    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    /** Getter for property charValue.
     * @return Value of property charValue.
     */
    public char getCharValue() {
        return charValue;
    }

    /** Setter for property charValue.
     * @param charValue New value of property charValue.
     */
    public void setCharValue(char charValue) {
        this.charValue = charValue;
    }

    /** Getter for property booleanValue.
     * @return Value of property booleanValue.
     */
    public boolean isBooleanValue() {
        return booleanValue;
    }

    /** Setter for property booleanValue.
     * @param booleanValue New value of property booleanValue.
     */
    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /** Getter for property stringValue.
     * @return Value of property stringValue.
     */
    public String getStringValue() {
        return stringValue;
    }

    /** Setter for property stringValue.
     * @param stringValue New value of property stringValue.
     */
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /** Getter for property integerObj.
     * @return Value of property integerObj.
     */
    public Integer getIntegerObj() {
        return integerObj;
    }

    /** Setter for property integerObj.
     * @param integerObj New value of property integerObj.
     */
    public void setIntegerObj(Integer integerObj) {
        this.integerObj = integerObj;
    }

    // overloaded methods for properties

    public void setIntValue(String intValueStrig) {
        try {
            this.intValue = Integer.parseInt(intValueStrig);
        }catch(Exception ex) {}
    }

    public void setFloatValue(String floatValueString) {
        try {
            Float f = new Float(floatValueString);
            this.floatValue = f.floatValue();
        }catch(Exception ex) {}
    }

    /** Getter for property readOnly.
     * @return Value of property readOnly.
     */
    public String getReadOnly() {
        return readOnly;
    }

    /** Setter for property writeOnly.
     * @param writeOnly New value of property writeOnly.
     */
    public void setWriteOnly(String writeOnly) {
        this.writeOnly = writeOnly;
    }

}
