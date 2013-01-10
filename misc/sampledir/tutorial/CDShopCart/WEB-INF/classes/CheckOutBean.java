/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2001 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

/*
 * CheckOutBean.java
 *
 * Created on October 9, 2000, 5:38 PM
 */


     import java.beans.*;
     import com.sun.forte4j.persistence.*;
     import java.util.*;
     import CDPackage.*;

     public class CheckOutBean extends Object implements java.io.Serializable {

     private PersistenceManager pm;

     /** 
        Creates new CheckOutBean 
        and initialize the PersistenceManagerFactory & PersistenceManager
     **/
     public CheckOutBean() {
         PersistenceManagerFactory pmf = new PersistenceManagerFactoryImpl();
         pmf.setConnectionUserName("PUBLIC");
         pmf.setConnectionPassword("PUBLIC");
         pmf.setConnectionDriverName("com.pointbase.jdbc.jdbcUniversalDriver");
         pmf.setConnectionURL("jdbc:pointbase://localhost/cdshopcart,database.home=d://forte4j\\pointbase\\network\\databases");
         // Typical Oracle URL connection string
         //pmf.setConnectionURL("jdbc:oracle:thin:@GABSO:1521:GABSO");
         pmf.setOptimistic(false);
         this.pm = pmf.getPersistenceManager();         
     }
       
     // get the CD based on an id
     
     public Cd getCd(long id) {
         Query q = this.pm.newQuery();
         q.setClass(Cd.class);
         q.setCandidates(pm.getExtent(Cd.class, false));
         q.setFilter("id == CDid");		// define placeholder
	   String param = "Long CDid";	//define parameter for placeholder
	   q.declareParameters(param);
         Collection result = (Collection)q.execute(new Long(id));
         Iterator i = result.iterator();
         Cd theCd = null;
         if (i.hasNext()) {
             theCd = (Cd)i.next();
         }
         return theCd;

     }

     // Checkout - add an order & line items  for each item in the cart
     
     public int checkout(Cart myCart) {

        Transaction tx = pm.currentTransaction();
        tx.begin();

        // create the ORDER
        int ordNum = this.getSequenceNumber("CDORDER", 1);  // get the next Order sequence number 
        Order ord = new Order();                            // create a new order
        ord.setId(ordNum);                                  // set the primary key
        ord.setOrderdate(new Date());                       // set current date
        pm.makePersistent(ord);                             // tell PM to mark for db update

        // for each item in the cart, add a line item to the order
        int itemNum = 1;                        // initialize the Line Item number
        HashSet itemList = new HashSet();       // create a new hash list to store all Line Items
        Iterator i = myCart.lineItems.iterator();
        while (i.hasNext()) {                   
            CartLineItem c = (CartLineItem)i.next();        // next item in the cart
            Orderitem item = new Orderitem();               // create new line item
            item.setOrderid(ordNum);                        // set the primary key
            item.setLineitemid(itemNum++);
            item.setCdOfProductid(getCd(c.getId()));            
            itemList.add(item);                             // add to collection
            pm.makePersistent(item);                        // tell PM to mark for db update
        }
        
        // Because of managed relationship between order and orderitem,
        // the OrderitemCollection is now automatically updated for you.
        // The line below should be ok, although unnecessary.
        // It is commented out because of a bug in the ea release.
        
        //ord.setOrderitemCollectionForOrderid(itemList);

        tx.commit();
        return ordNum;
     }
      
     // get the next sequence number for the ORDER
     public int getSequenceNumber(String tableName, int amount) {
         int key = 0;
         Query sequenceQuery = pm.newQuery();
         sequenceQuery.setClass(Sequence.class);
         sequenceQuery.setCandidates(pm.getExtent(Sequence.class, false));
         sequenceQuery.setFilter("tablename == name");
         String param = "String name";
         sequenceQuery.declareParameters(param);
         Collection result = (Collection)sequenceQuery.execute(tableName);
         Iterator i = result.iterator();
         Sequence s = (Sequence)i.next();
         key = s.getNextpk().intValue();
         s.setNextpk(new Integer(key + amount));
         // Oracle connection will expect Long rather than Integer.
         // s.setNextpk(new Long(key + amount));
         
         return key;
     }

}