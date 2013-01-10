/*============================================================================*/
/* TimedSocket, adapted from David Reilly (JavaWorld)                         */
/* This class offers a timeout feature on socket connections.                 */
/* A maximum length of time allowed for a connection can be                   */
/* specified, along with a host and port.                                     */
/*============================================================================*/
package ca.gc.space.quicksat.ground.util;

import java.net.*;
import java.io.*;

/*----------------------------------------------------------------------------*/
public class TimedSocket {
/*----------------------------------------------------------------------------*/
// Polling delay for socket checks (in milliseconds)
private static final int POLL_DELAY = 100;

    /*------------------------------------------------------------------------*/
    /** Attempts to connect to a service at the specified address
     * and port, for a specified maximum amount of time.
     *
     * @param	addr	Address of host
     * @param	port	Port of service
     * @param	delay	Delay in milliseconds                                 */
    /*------------------------------------------------------------------------*/
    public static Socket getSocket ( InetAddress addr, int port, int delay) 
                                    throws InterruptedIOException, IOException {
    /*------------------------------------------------------------------------*/
        
        /*--------------------------------------------------*/
        /* Create a new socket thread, and start it running */
        /*--------------------------------------------------*/
	SocketThread st = new SocketThread( addr, port );
	st.start();

	int timer = 0;
	Socket sock = null;

        CONNECTION_ATTEMPT:
	while( true ) {
            /*---------------------------------------------*/
            /* Check to see if a connection is established */
            /*---------------------------------------------*/
            if( st.isConnected() ) {
                
                /*---------------------------------------------------------*/
		/* Yes ...  assign to sock variable, and break out of loop */
                /*---------------------------------------------------------*/
		sock = st.getSocket();
		break CONNECTION_ATTEMPT;
            }            
            /*----------------------------------------------------*/
            /* If no connection yet, check for errors and timeout */
            /*----------------------------------------------------*/
            else {
                
                /*-----------------------------------*/
		/* Check to see if an error occurred */
                /*-----------------------------------*/
		if( st.isError() ) {
                    
                    /*------------------------------------*/
                    /* No connection could be established */
                    /*------------------------------------*/
                    throw( st.getException() );
		}

                /*----------------------------------*/
                /* Sleep for a short period of time */
                /*----------------------------------*/
		try{Thread.sleep(POLL_DELAY);}catch(InterruptedException ie){}

                /*-----------------*/
                /* Increment timer */
                /*-----------------*/
		timer += POLL_DELAY;

                /*-------------------------------------*/
		/* Check to see if time limit exceeded */
                /*-------------------------------------*/
		if( timer > delay ) {
			throw new InterruptedIOException(
                            "Could not connect for " + delay + " milliseconds");
                }
            }//end check for error and timetout
	}//end infinite loop

	return( sock );
        
    }//end getSocket

    /*------------------------------------------------------------------------*/
    /**Attempts to connect to a service at the specified address
     * and port, for a specified maximum amount of time.
     *
     * @param	host	Hostname of machine
     * @param	port	Port of service
     * @param	delay	Delay in milliseconds                                 */
    /*------------------------------------------------------------------------*/
    public static Socket getSocket ( String host, int port, int delay) 
                                    throws InterruptedIOException, IOException {
    /*------------------------------------------------------------------------*/ 
    
        /*-------------------------------------------------------------*/
        /* Convert host into an InetAddress, and call getSocket method */
        /*-------------------------------------------------------------*/
	InetAddress inetAddr = InetAddress.getByName (host);
	return( getSocket ( inetAddr, port, delay ) );
    }

    /*------------------------------------------------------------------------*/
    /* Inner class for establishing a socket thread                           */
    /* within another thread, to prevent blocking.                            */
    /*------------------------------------------------------------------------*/
    static class SocketThread extends Thread {
    /*------------------------------------------------------------------------*/
    volatile private Socket m_connection = null;//connection to remote host
    private String          m_host = null;      // Hostname to connect to
    private InetAddress     m_inet = null;      // Internet Addr to connect to
    private int             m_port = 0;         // Port number to connect to
    private IOException     m_exception = null; // in event connection error 
    
        /*--------------------------------------------------------------------*/
        /* Connect to the specified host and port number                      */
        /*--------------------------------------------------------------------*/
        public SocketThread ( String host, int port) {
        /*--------------------------------------------------------------------*/
            m_host = host;
            m_port = port;
	}

        /*--------------------------------------------------------------------*/
	/* Connect to the specified host IP and port number                   */
        /*--------------------------------------------------------------------*/
	public SocketThread ( InetAddress inetAddr, int port ) {
        /*--------------------------------------------------------------------*/
            m_inet = inetAddr;
            m_port = port;
        }

        /*--------------------------------------------------------------------*/
        /* Thread started to attempt a connection                             */
        /*--------------------------------------------------------------------*/
	public void run() {
        /*--------------------------------------------------------------------*/

            Socket sock = null; //Socket used for establishing a connection

            try {
                /*------------------------------------*/
                /* Was a string or an inet specified? */
                /*------------------------------------*/
		if( m_host != null ) {
                    
                    /*-----------------------------------------*/
                    /* Connect to a remote host - BLOCKING I/O */
                    /*-----------------------------------------*/
                    sock = new Socket (m_host, m_port);
                }
                else {
                    
                    /*-----------------------------------------*/
                    /* Connect to a remote host - BLOCKING I/O */
                    /*-----------------------------------------*/
                    sock = new Socket (m_inet, m_port);
                }
            } catch (IOException ioe){
                
                /*-----------------------------------------*/
                /* Assign to our exception member variable */
                /*-----------------------------------------*/
		m_exception = ioe;
		return;
            }

            /*-----------------------------------------------*/
            /* If socket constructor returned without error, */
            /* then connection finished                      */
            /*-----------------------------------------------*/
            m_connection = sock;
            
        }//end run

        /*--------------------------------------------------------------------*/
	/* Are we connected?                                                  */
        /*--------------------------------------------------------------------*/
	public boolean isConnected() {
        /*--------------------------------------------------------------------*/    
            if (m_connection == null)
		return false;
            else
		return true;
        }

        /*--------------------------------------------------------------------*/
	/* Did an error occur?                                                */
        /*--------------------------------------------------------------------*/
	public boolean isError() {
        /*--------------------------------------------------------------------*/    
            if (m_exception == null)
		return false;
            else
		return true;
	}

        /*--------------------------------------------------------------------*/
	/* Get socket                                                         */
        /*--------------------------------------------------------------------*/
	public Socket getSocket() {
        /*--------------------------------------------------------------------*/    
            return m_connection;
	}

        /*--------------------------------------------------------------------*/
	/* Get exception                                                      */
        /*--------------------------------------------------------------------*/
	public IOException getException() {
            return m_exception;
	}
        
    }//end SocketThread

}
