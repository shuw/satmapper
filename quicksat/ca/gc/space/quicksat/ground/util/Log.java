/*============================================================================*/
/*
 * Log.java
 *
 * Created on February 7, 2002, 11:36 AM
 */
/*============================================================================*/
package ca.gc.space.quicksat.ground.util;

import java.util.*;
import java.text.*;

/*============================================================================*/
/** This is a simple log system, for normal progress messages up to critical
 *  error reporting. A vector is kept of at most MAX_MESSAGE_QUEUE messages.
 *
 * @author  jfcusson
 * @version 
 */
/*============================================================================*/
public class Log {
private Vector  logVector           = null;
private boolean printToConsole      = true;
private int     MAX_MESSAGE_QUEUE   = 100;
private int     MESSAGE_QUEUE_FLUSH = 50;
    
    /*========================================================================*/
    /** Creates new Log                                                       */
    /*========================================================================*/
    public Log() {
    /*========================================================================*/    
        logVector = new Vector();
    }
    
    /*========================================================================*/
    /** Add an test message (normally during development) to the log.
     *  @param message A String with the message.                             */
    /*========================================================================*/
    public synchronized void test( String message ) {
    /*========================================================================*/    
        if( printToConsole )
            System.out.println( "TEST: " + message );
//        logVector.addElement( new LogEntry( LogEntry.TYPE_INFO, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }
    
    /*========================================================================*/
    /** Add an information message (normal progress) to the log.
     *  @param message A String with the message.                             */
    /*========================================================================*/
    public synchronized void info( String message ) {
    /*========================================================================*/    
        if( printToConsole )
            System.out.println( "INFO: " + message );
//        logVector.addElement( new LogEntry( LogEntry.TYPE_INFO, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }
    
    /*========================================================================*/
    /** Add a warning message (normal operation can continue, but user has to
     *  be warned that the situation is not optimal and he may have to take 
     *  action to correct this) to the log.
     *  @param message A String with the message.                             */
    /*========================================================================*/
    public synchronized void warning( String message ) {
    /*========================================================================*/    
        if( printToConsole )
            System.out.println( "WARNING: " + message );
//        logVector.addElement( new LogEntry( LogEntry.TYPE_WARNING, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }
    
    /*========================================================================*/
    /** Add an error message (An error has occured, and although the program
     *  will attempt to continue its normal operations it may not be possible)
     *  to the log.
     *  @param message A String with the message.                             */
    /*========================================================================*/
    public synchronized void error( String message ) {
    /*========================================================================*/    
        if( printToConsole )
            System.out.println( "ERROR: " + message );
//        logVector.addElement( new LogEntry( LogEntry.TYPE_ERROR, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }

    /*========================================================================*/
    /** Add a critical error message ( A critical error has occured, the program
     *  will shut down or terminate one of its current operations) to the log.
     *  @param message A String with the message.                             */
    /*========================================================================*/
    public synchronized void criticalError( String message ) {
    /*========================================================================*/    
        if( printToConsole )
            System.out.println( "CRITICAL ERROR: " + message );
//        logVector.addElement( new LogEntry( LogEntry.TYPE_CRITICAL, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }
    
    /*========================================================================*/
    /** Add a frame log entry message (record received/transmitted frames)
     *  @param isReceived True if this frame was received, false if it was sent.
     *  @param info String containing additional info concerning the frame.
     *  @param frame A byte array containing the frame sent.                  */
    /*========================================================================*/
    public synchronized void frame(boolean isReceived,String info,byte[] frame){
    /*========================================================================*/    
        if( printToConsole ) {
            char c = 0;
            String str = "";
            for( int i=0; i<frame.length; i++ ) {
                c = (char)(frame[i]&0xFF);
                /*--------------------------------------------------------*/
                /* Keep only printable characters as is, otherwise insert */
                /* the value of the byte between parenthesis              */
                /*--------------------------------------------------------*/
                if( (c>=0x20) && (c<0x7F) ) 
                    str += c;
                else 
                    str += "(" + (int)(frame[i]&0xFF) + ")";
            }                                          
            System.out.print( "FRAME " 
                                + (isReceived?"(RX-":"(TX-")
                                + info
                                + ") :"
                                + str );                                
                
        }
//        logVector.addElement( new LogEntry( LogEntry.TYPE_CRITICAL, message ) );
//        if( logVector.size() > MAX_MESSAGE_QUEUE ) {
//            for( int i=0; i<MESSAGE_QUEUE_FLUSH; i++ ) {
//                logVector.removeElementAt( i );
//            }
//        }
        return;
    }
    
    
    
    /*========================================================================*/
    /** Return the number of messages in the log queue
     *  @return The number of messages in the log queue, of all types.        */
    /*========================================================================*/
    public int available() {
    /*========================================================================*/    
        return( logVector.size() );
    }
    
    /*========================================================================*/
    /** Return the log message at position "index" in the queue. Note that the
     *  queue is regurlarly being flushed and appended to, so always verify
     *  if the return value is valid or not...
     *  @param index The index of the log entry to retrieve.
     *  @return A message string giving information on the selected log entry.
    /*========================================================================*/
    public String getMessageAt( int index ) {
    /*========================================================================*/    
        LogEntry logEntry = null;
        try{
             logEntry = (LogEntry)logVector.elementAt( index );
        } catch( ArrayIndexOutOfBoundsException e ) {
            return( "" );
        }
        if( logEntry != null )
            return( logEntry.toString() );
        else
            return( "" );
    }
    
    /*========================================================================*/
    /** Inner class representing a log entry.                                 */
    /*========================================================================*/
    private class LogEntry {
    /*========================================================================*/    
        public String message           = "";
        public static final int TYPE_INFO      = 0;
        public static final int TYPE_WARNING   = 1;
        public static final int TYPE_ERROR     = 2;
        public static final int TYPE_CRITICAL  = 3;
        public int type = TYPE_INFO;
        Date date = null;
        public LogEntry( int type, String message ) {
            this.type = type;
            this.message = message;
            date = new Date();
        }
        public String toString() {
            switch( type ) {
                default:
                case TYPE_INFO:                    
                    return( DateFormat.getDateTimeInstance().format(date)
                            + " - INFO: " + message );
                case TYPE_WARNING:
                    return( DateFormat.getDateTimeInstance().format(date)
                            + " - WARNING: " + message );
                case TYPE_ERROR:
                    return( DateFormat.getDateTimeInstance().format(date)
                            + " - ERROR: " + message );
                case TYPE_CRITICAL:
                    return( DateFormat.getDateTimeInstance().format(date)
                            + " - CRITICAL ERROR: " + message );
            }
        }
    }

}
