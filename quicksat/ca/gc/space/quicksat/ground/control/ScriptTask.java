/*
 * ScriptTask.java
 *
 * Created on February 13, 2002, 10:20 AM
 */

package ca.gc.space.quicksat.ground.control;

import javax.swing.*;
/**
 *
 * @author  jfcusson
 * @version 
 */
public class ScriptTask {

private final int   TYPE_NO_ACTION                  = -1;
private final int   TYPE_WAIT_N_SECONDS             = 0;
private final int   TYPE_WAIT_FOR_INCOMING_DATA     = 1;
private final int   TYPE_UPLOAD_TASK                = 2;
private final int   TYPE_EXTUPLOAD_TASK             = 3;
private final int   TYPE_START_HOUSEKEEPING         = 4;
private final int   TYPE_EXECUTE_UPLOADED_TASKS     = 5;
private final int   TYPE_CHANGE_SSID                = 6;
private int         type                            = TYPE_NO_ACTION;

private static final int   INDEX_NO_ACTION                 = 0;
private static final int   INDEX_WAIT_N_SECONDS            = 1;
private static final int   INDEX_WAIT_FOR_INCOMING_DATA    = 2;
private static final int   INDEX_UPLOAD_TASK               = 3;
private static final int   INDEX_EXTUPLOAD_TASK            = 4;
private static final int   INDEX_START_HOUSEKEEPING        = 5;
private static final int   INDEX_EXECUTE_UPLOADED_TASKS    = 6;
private static final int   INDEX_CHANGE_SSID               = 7;

private String      strParam                        = "";
private long        dParam                          = 0;
private String      fileName                        = "";
private String      status                          = "N/A";
private boolean     started                         = false;
private boolean     completed                       = false;
private String      progress                        = "N/A";
private long        timer                           = 0;

    /** Creates new ScriptTask */
    public ScriptTask() {
    }
    
    /*------------------------------------------------------------------------*/
    /** No action                                                             */
    /*------------------------------------------------------------------------*/
    public synchronized void setNoAction() {
        type = TYPE_NO_ACTION;
        dParam = 0;
        status = "";
        progress = "";
        completed = false;
        started = false;
        //System.out.println("Setting task as NO ACTION");
    }
    public synchronized boolean isNoAction() {
        return(type==TYPE_NO_ACTION?true:false);
    }
    public static String getNoActionToken() {
        return("NO ACTION");
    }
    
    /*------------------------------------------------------------------------*/
    /** Wait for N Seconds                                                    */
    /*------------------------------------------------------------------------*/
    public synchronized void setWaitForNSeconds( int nbSeconds ) {
        type = TYPE_WAIT_N_SECONDS;
        dParam = nbSeconds;
        status = "Ready";
        progress = "Standbye";
        completed = false;
        started = false;
        //System.out.println("Setting task as WAIT "+nbSeconds+" SECONDS");
    }
    public synchronized void setNSeconds( int nbSeconds ) {
        dParam = nbSeconds;        
    }
    public synchronized void setNSeconds( String nbSeconds ) {
        try {
            dParam = Integer.parseInt(nbSeconds);
        } catch( NumberFormatException e ) {
            dParam = 0;
        }
    }
    public synchronized void setWaitForNSeconds() {
        type = TYPE_WAIT_N_SECONDS;
        status = "Ready";
        progress = "Standbye";
        completed = false;
        started = false;
        //System.out.println("Setting task as WAIT N SECONDS");
    }
    public synchronized boolean isWaitForNSeconds() {
        return(type==TYPE_WAIT_N_SECONDS?true:false);
    }
    public synchronized void startWaitForNSeconds() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Left= "+dParam+" Sec.";
        started = true;
    }
    public synchronized boolean isWaitForNSecondsCompleted() {
        long elapsedTime = (timer-System.currentTimeMillis())/(long)1000;        
        if( elapsedTime < (long)0 ) {
            progress = "Done";
            completed = true;
            return( true );
        } else {
            progress = "Left= "+elapsedTime+" Sec.";
            return( false );
        }
    }
    public static String getWaitForNSecondsToken() {
        return("WAIT N SECONDS");
    }

    /*------------------------------------------------------------------------*/
    /** Wait for incoming data                                                */
    /*------------------------------------------------------------------------*/
    public synchronized void setWaitForIncomingData() {
        type = TYPE_WAIT_FOR_INCOMING_DATA;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as WAIT FOR DATA");
    }
    public synchronized boolean isWaitForIncomingData() {
        return(type==TYPE_WAIT_FOR_INCOMING_DATA?true:false);
    }
    public synchronized void startWaitForIncomingData() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Waiting for incoming data";
        started = true;
    }
    public synchronized void setWaitForIncomingDataCompleted() {
        completed = true;
    }
    public synchronized boolean isWaitForIncomingDataCompleted() {
        return( completed );
    }
    public static String getWaitForIncomingDataToken() {
        return("WAIT FOR INCOMING DATA");
    }
    
    /*------------------------------------------------------------------------*/
    /** Upload task                                                           */
    /*------------------------------------------------------------------------*/
    public synchronized void setUploadTask( String fileName ) {
        type = TYPE_UPLOAD_TASK;
        this.fileName = fileName;
        //System.out.println("Setting file name to:"+fileName);
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as UPLOAD");
    }
    public synchronized void setUploadTask() {
        type = TYPE_UPLOAD_TASK;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as UPLOAD");
    }
    public synchronized boolean isUploadTask() {
        return(type==TYPE_UPLOAD_TASK?true:false);
    }
    public synchronized void startUploadTask() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Preparing for upload";
        started = true;
    }
    public synchronized void setUploadTaskCompleted() {
        completed = true;
    }
    public synchronized boolean isUploadTaskCompleted() {
        return( completed );
    }
    public static String getUploadTaskToken() {
        return("UPLOAD TASK");
    }

    /*------------------------------------------------------------------------*/
    /** Extended Upload task                                                  */
    /*------------------------------------------------------------------------*/
    public synchronized void setExtendedUploadTask( String fileName ) {
        type = TYPE_EXTUPLOAD_TASK;
        this.fileName = fileName;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as EXT UPLOAD");
    }
    public synchronized void setExtendedUploadTask() {
        type = TYPE_EXTUPLOAD_TASK;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as EXT UPLOAD");
    }
    public synchronized boolean isExtendedUploadTask() {
        return(type==TYPE_EXTUPLOAD_TASK?true:false);
    }
    public synchronized void startExtendedUploadTask() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Preparing for upload";
        started = true;
    }
    public synchronized void setExtendedUploadTaskCompleted() {
        completed = true;
    }
    public synchronized boolean isExtendedUploadTaskCompleted() {
        return( completed );
    }
    public static String getExtendedUploadTaskToken() {
        return("EXTENDED UPLOAD TASK");
    }
    
    /*------------------------------------------------------------------------*/
    /** Start Housekeeping                                                    */
    /*------------------------------------------------------------------------*/
    public synchronized void setStartHousekeeping() {
        type = TYPE_START_HOUSEKEEPING;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as START HOUSEKEEPING");
    }
    public synchronized boolean isStartHousekeeping() {
        return(type==TYPE_START_HOUSEKEEPING?true:false);
    }
    public synchronized void startStartHousekeeping() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Starting Housekeeping";
        started = true;
    }
    public synchronized void setStartHousekeepingCompleted() {
        completed = true;
    }
    public synchronized boolean isStartHousekeepingCompleted() {
        return( completed );
    }
    public static String getStartHousekeepingToken() {
        return("START HOUSEKEEPING");
    }
    
    /*------------------------------------------------------------------------*/
    /** Execute uploaded tasks                                                */
    /*------------------------------------------------------------------------*/
    public synchronized void setExecuteUploadedTasks() {
        type = TYPE_EXECUTE_UPLOADED_TASKS;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as EXECUTE");
    }
    public synchronized boolean isExecuteUploadedTasks() {
        return(type==TYPE_EXECUTE_UPLOADED_TASKS?true:false);
    }
    public synchronized void startExecuteUploadedTasks() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Starting Tasks";
        started = true;
    }
    public synchronized void setExecuteUploadedTasksCompleted() {
        completed = true;
    }
    public synchronized boolean isExecuteUploadedTasksCompleted() {
        return( completed );
    }
    public static String getExecuteUploadedTasksToken() {
        return("EXECUTE UPLOADED TASKS");
    }
    
    /*------------------------------------------------------------------------*/
    /** Change SSID                                                           */
    /*------------------------------------------------------------------------*/
    public synchronized void setChangeSSID( String SSID ) {
        type = TYPE_CHANGE_SSID;
        strParam = SSID;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as CHANGE SSID");
    }
    public synchronized void setChangeSSID() {
        type = TYPE_CHANGE_SSID;
        status = "Ready";
        progress = "Standbye";
        //System.out.println("Setting task as CHANGE SSID");
    }
    public synchronized boolean isChangeSSID() {
        return(type==TYPE_CHANGE_SSID?true:false);
    }
    public synchronized void startChangeSSID() {
        timer = System.currentTimeMillis();
        status = "EXECUTING";
        progress = "Changing SSID";
        started = true;
    }
    public synchronized void setChangeSSIDCompleted() {
        completed = true;
    }
    public synchronized boolean isChangeSSIDCompleted() {
        return( completed );
    }
    public static String getChangeSSIDToken() {
        return("CHANGE SSID");
    }
    
    /*------------------------------------------------------------------------*/
    /** Utils                                                                 */
    /*------------------------------------------------------------------------*/
    public synchronized String getParam() {
        switch( type ) {
            case TYPE_WAIT_N_SECONDS:
                return( String.valueOf( dParam ) );
            case TYPE_WAIT_FOR_INCOMING_DATA:
            case TYPE_UPLOAD_TASK:
            case TYPE_EXTUPLOAD_TASK:
            case TYPE_START_HOUSEKEEPING:
            case TYPE_EXECUTE_UPLOADED_TASKS:
            case TYPE_CHANGE_SSID:
                return( strParam );
            case TYPE_NO_ACTION:
                return( "" );
            default:
                return( "Undefined" );
        }
    }
    

    public String toFileEntry() {
        String retVal = null;
        String newFileName = null;
        switch( type ) {
            case TYPE_WAIT_N_SECONDS:
                retVal = "\"WAIT N SECONDS\" " + dParam;
                break;
            case TYPE_WAIT_FOR_INCOMING_DATA:
                retVal = "\"WAIT FOR INCOMING DATA\" ";
                break;
            case TYPE_UPLOAD_TASK:
                /*----------------------------------------------------------*/
                /* First make sure that backslashes are converted to double */
                /*----------------------------------------------------------*/
                newFileName = "";
                for( int i=0; i<fileName.length(); i++ ) {
                    char c = fileName.charAt(i);
                    if( c == '\\' ) 
                        newFileName += "\\\\";
                    else
                        newFileName += c;
                }
                retVal = "\"UPLOAD TASK\" \"" 
                         + newFileName 
                         + "\" \"" 
                         + strParam + "\"";
                break;
            case TYPE_EXTUPLOAD_TASK:
                /*----------------------------------------------------------*/
                /* First make sure that backslashes are converted to double */
                /*----------------------------------------------------------*/
                newFileName = "";
                for( int i=0; i<fileName.length(); i++ ) {
                    char c = fileName.charAt(i);
                    if( c == '\\' ) 
                        newFileName += "\\\\";
                    else
                        newFileName += c;
                }
                retVal = "\"EXTENDED UPLOAD TASK\" \""
                         + newFileName 
                         + "\" \"" 
                         + strParam + "\"";
                break;
            case TYPE_START_HOUSEKEEPING:
                retVal = "\"START HOUSEKEEPING\" ";
                break;
            case TYPE_EXECUTE_UPLOADED_TASKS:
                retVal = "\"EXECUTE UPLOADED TASKS\" ";
                break;
            case TYPE_CHANGE_SSID:
                retVal = "\"CHANGE SSID\" \"" + strParam + "\"";
                break;
            case TYPE_NO_ACTION:
            default:
                retVal = "\"NO ACTION\" ";
                break;
        }
        retVal += "\n";
        return( retVal );
    }
    
    /* WARNING: INDEX MUST CORRESPOND! */
    public synchronized static void fillCommandMenu(JComboBox combo) {
        combo.removeAllItems();
        combo.addItem( "NO ACTION         " );
        combo.addItem( "WAIT N SECONDS    " );
        combo.addItem( "WAIT FOR DATA     " );
        combo.addItem( "UPLOAD TASK       " );
        combo.addItem( "EXT UPLOAD TASK   " );
        combo.addItem( "START HOUSEKEEPING" );
        combo.addItem( "EXECUTE TASKS     " );
        combo.addItem( "CHANGE SSID       " );
    }

    public synchronized static void setNoActionSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_NO_ACTION ) 
            combo.setSelectedIndex( INDEX_NO_ACTION );
    }
    public synchronized static void setWaitForNSecondsSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_WAIT_N_SECONDS ) 
            combo.setSelectedIndex( INDEX_WAIT_N_SECONDS );
    }
    public synchronized static void setWaitForIncomingDataSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_WAIT_FOR_INCOMING_DATA ) 
            combo.setSelectedIndex( INDEX_WAIT_FOR_INCOMING_DATA );
    }
    public synchronized static void setUploadTaskSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_UPLOAD_TASK ) 
            combo.setSelectedIndex( INDEX_UPLOAD_TASK );
    }
    public synchronized static void setExtendedUploadTaskSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_EXTUPLOAD_TASK ) 
            combo.setSelectedIndex( INDEX_EXTUPLOAD_TASK );
    }
    public synchronized static void setStartHousekeepingSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_START_HOUSEKEEPING ) 
            combo.setSelectedIndex( INDEX_START_HOUSEKEEPING );
    }
    public synchronized static void setExecuteUploadedTasksSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_EXECUTE_UPLOADED_TASKS ) 
            combo.setSelectedIndex( INDEX_EXECUTE_UPLOADED_TASKS );
    }
    public synchronized static void setChangeSSIDSelected(JComboBox combo) {
        if( combo.getSelectedIndex() != INDEX_CHANGE_SSID ) 
            combo.setSelectedIndex( INDEX_CHANGE_SSID );
    }
    
    public synchronized void setAsSelected(JComboBox combo) {
        switch( combo.getSelectedIndex() ) {
            case INDEX_NO_ACTION:
                setNoAction();
                break;
            case INDEX_WAIT_N_SECONDS:
                setWaitForNSeconds();
                break;
            case INDEX_WAIT_FOR_INCOMING_DATA:
                setWaitForIncomingData();
                break;
            case INDEX_UPLOAD_TASK:
                setUploadTask();
                break;
            case INDEX_EXTUPLOAD_TASK:
                setExtendedUploadTask();
                break;
            case INDEX_START_HOUSEKEEPING:
                setStartHousekeeping();
                break;
            case INDEX_EXECUTE_UPLOADED_TASKS:
                setExecuteUploadedTasks();
                break;
            case INDEX_CHANGE_SSID:
                setChangeSSID();
                break;
        }
    
    }
    
    public synchronized void setFileName( String fileName ) {
        this.fileName = fileName;
    }
    public synchronized String getFileName() {
        return( fileName );
    }
    public synchronized void setArgument( String arg ) {
        this.strParam = arg;
    }
    public synchronized void setArgument( int arg ) {
        this.dParam = arg;
    }
    public synchronized String getArgument() {
        return( strParam );
    }
    
    public synchronized String getProgress() {
        return( progress );
    }
    
    public synchronized void setProgress( String progress ) {
        this.progress = progress;
    }
    
    public synchronized void setProgressPercent( int percent ) {
        if( (percent<0) || (percent>100) ) {
            progress = "???";
            return;
        }
        progress = String.valueOf(percent)+"%";        
    }
    
    public synchronized void setStatus( String status ) {
        this.status = status;
    }
    
    public synchronized String getStatus() {
        return( status );
    }
    
    public synchronized void setStatusReady() {
        this.status = "Ready";
    }
    
    public synchronized void setStatusExecuting() {
        this.status = "Executing";
    }
    
    public synchronized void setStatusDone() {
        this.status = "Done";
    }
    
    public synchronized void setCompleted( boolean completed ) {
        this.completed = completed;
    }
    public synchronized boolean isCompleted() {
        return( completed );
    }
    
    public synchronized void setStarted( boolean started ) {
        this.started = started;
    }
    public synchronized boolean isStarted() {
        return( started );
    }
    
    public synchronized void reset() {
        started = false;
        completed = false;
        status = "Ready";
        progress = "Standbye";
    }
    
    public synchronized String toString() {
        String retMsg = "";
        retMsg = "Type="+type+" Filename="+fileName+" Arg="+strParam+"/"+dParam;
        return( retMsg );
    }

}
