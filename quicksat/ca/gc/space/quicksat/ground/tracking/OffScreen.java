package ca.gc.space.quicksat.ground.tracking;

/**
 * Insert the type's description here.
 * Creation date: (3/13/2002 2:05:12 PM)
 * @author: 
 */
public class OffScreen extends java.awt.Image {
/**
 * OffScreen constructor comment.
 */
public OffScreen() {
	super();
}
	/**
	 * Flushes all resources being used by this Image object.  This
	 * includes any pixel data that is being cached for rendering to
	 * the screen as well as any system resources that are being used
	 * to store data or pixels for the image.  The image is reset to
	 * a state similar to when it was first created so that if it is
	 * again rendered, the image data will have to be recreated or
	 * fetched again from its source.
	 * <p>
	 * This method always leaves the image in a state such that it can 
	 * be reconstructed.  This means the method applies only to cached 
	 * or other secondary representations of images such as those that 
	 * have been generated from an <tt>ImageProducer</tt> (read from a 
	 * file, for example). It does nothing for off-screen images that 
	 * have only one copy of their data.
	 */
public void flush() {}
	/**
	 * Creates a graphics context for drawing to an off-screen image. 
	 * This method can only be called for off-screen images. 
	 * @return  a graphics context to draw to the off-screen image. 
	 * @see     java.awt.Graphics
	 * @see     java.awt.Component#createImage(int, int)
	 */
public java.awt.Graphics getGraphics() {
	return null;
}
	/**
	 * Determines the height of the image. If the height is not yet known, 
	 * this method returns <code>-1</code> and the specified  
	 * <code>ImageObserver</code> object is notified later.
	 * @param     observer   an object waiting for the image to be loaded.
	 * @return    the height of this image, or <code>-1</code> 
	 *                   if the height is not yet known.
	 * @see       java.awt.Image#getWidth
	 * @see       java.awt.image.ImageObserver
	 */
public int getHeight(java.awt.image.ImageObserver observer) {
	return 0;
}
	/**
	 * Gets a property of this image by name. 
	 * <p>
	 * Individual property names are defined by the various image 
	 * formats. If a property is not defined for a particular image, this 
	 * method returns the <code>UndefinedProperty</code> object. 
	 * <p>
	 * If the properties for this image are not yet known, this method 
	 * returns <code>null</code>, and the <code>ImageObserver</code> 
	 * object is notified later. 
	 * <p>
	 * The property name <code>"comment"</code> should be used to store 
	 * an optional comment which can be presented to the application as a 
	 * description of the image, its source, or its author. 
	 * @param       name   a property name.
	 * @param       observer   an object waiting for this image to be loaded.
	 * @return      the value of the named property.
	 * @see         java.awt.image.ImageObserver
	 * @see         java.awt.Image#UndefinedProperty
	 */
public Object getProperty(String name, java.awt.image.ImageObserver observer) {
	return null;
}
	/**
	 * Gets the object that produces the pixels for the image.
	 * This method is called by the image filtering classes and by 
	 * methods that perform image conversion and scaling.
	 * @return     the image producer that produces the pixels 
	 *                                  for this image.
	 * @see        java.awt.image.ImageProducer
	 */
public java.awt.image.ImageProducer getSource() {
	return null;
}
	/**
	 * Determines the width of the image. If the width is not yet known, 
	 * this method returns <code>-1</code> and the specified   
	 * <code>ImageObserver</code> object is notified later.
	 * @param     observer   an object waiting for the image to be loaded.
	 * @return    the width of this image, or <code>-1</code> 
	 *                   if the width is not yet known.
	 * @see       java.awt.Image#getHeight
	 * @see       java.awt.image.ImageObserver
	 */
public int getWidth(java.awt.image.ImageObserver observer) {
	return 0;
}
}
