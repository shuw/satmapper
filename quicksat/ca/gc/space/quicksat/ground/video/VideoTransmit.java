package ca.gc.space.quicksat.ground.video;
/*
 *VideoTransmit class is greatly insppired by the AVTransmit example from JMF API examples
 *It is used to creaye a video stream (RTP Stream) and send it to multiple clients over Internet.
 *Each CLients must have a STRMReceiver object running to view the stream sent by this server
 *For testing purposes , the JMStudio executable from SUN can be used
 *Sun's version of this class was created to handle (and send) multiple tracks (audio and/or video)
 *this feature was kept in the code , for eventual use, it does not affect performance if not used
 *
 * @author Louis-Philippe Ouellet for the Canadian Space Agency
 */

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import javax.media.*;
import javax.media.protocol.*;
import javax.media.protocol.DataSource;
import javax.media.format.*;
import javax.media.control.TrackControl;
import javax.media.control.QualityControl;
import javax.media.rtp.*;
import javax.media.rtp.rtcp.*;
import com.sun.media.rtp.*;
import java.util.*;
import com.sun.media.vfw.*;
import java.net.*;
import javax.media.Format;


public class VideoTransmit {


    private MediaLocator locator; //This is the "driver" used for capture , in our case it is the USB adapter in which we connect the video feed
    private String ipAddress; // This is the servers IP Address
    private      int port=9904;   // The port we use to send the Stream
    private Processor processor = null;
    private RTPManager rtpMgrs[];
    private DataSource dataOutput = null;
    public boolean deviceFound= false;




    public VideoTransmit() {


        //We must find out what capture devices are available
        Vector devices = null;
        Enumeration enum = null;
        devices = (Vector) CaptureDeviceManager.getDeviceList(null).clone();
        enum = devices.elements();
        //The video capture device is a vfw:


                try{
                    // We know what device we want to use so try loading it
                   // System.out.println("Autodetect of capture device failed trying hardcoded way");

                    locator = new MediaLocator("vfw:\\0");
                    System.out.println("[Capture device enabled]");
                }
                catch(Exception ee){
                    System.out.println("Device detection has failed. Program will now terminate");
                    ee.printStackTrace();
                    System.exit(0); // If the second detection has failed, no need to continue
                }
                //*********************************
                //If the capture device changes we can try using the following code to load it
                //*********************************
            /*try{
            while (enum.hasMoreElements()) {
                CaptureDeviceInfo cdi = (CaptureDeviceInfo) enum.nextElement();
                String name = cdi.getName();
                if (name.startsWith("vfw:")){
                    String name2 = VFWCapture.capGetDriverDescriptionName(0);
                    com.sun.media.protocol.vfw.VFWSourceStream.autoDetect(0);
                    locator = cdi.getLocator();
                    CaptureDeviceManager.removeDevice(cdi);
                    this.deviceFound= true; // A capture device has been found no need to search anymore
                    System.out.println("[Capture device enabled by autodetect]");
                }
            }}
            catch(Exception d){
                System.out.println("Device detection has failed. Program will now terminate");
                d.printStackTrace();
                System.exit(0); // If the detection has failed, no need to continue

            }
*/            // If the device has not been found we can try loading it "the hard way..."
    }

    /**
     * Start starts the transmission, first by creating a processor and then by
     * creating a transmitter with the client to which we want to trsanfer as parameter
     */
    public synchronized String start(Vector clients) {

        String result;
        if (clients.size()!=0){
            // Create a processor for the specified media locator
            System.out.println("Building processor");
            result = createProcessor();
            if (result != null)
                return result;

            // The transmitter creates the RTP session and send it to the clients
            System.out.println("Building transmitter");
            result = createTransmitter(clients);
            if (result != null) {
                processor.close();
                processor = null;
                return result;
            }

            // Start the transmission
            processor.start();
        }else{
            System.out.println("No clients");
        }
        return null;
    }

    /**
     * Stops the transmission if already started
     */
    public void stop() {
        synchronized (this) {
            if (processor != null) {
                processor.stop();
                processor.close();
                processor = null;
                for (int i = 0; i < rtpMgrs.length; i++) {
                    rtpMgrs[i].removeTargets( "Session ended.");
                    rtpMgrs[i].dispose();
                }
            }
        }
    }

    private String createProcessor() {
        if (locator == null)
            return "Locator is null";

        DataSource ds;
        DataSource clone;

        try {
            ds = javax.media.Manager.createDataSource(locator);
        } catch (Exception e) {
            return "Couldn't create DataSource";
        }

        // Try to create a processor to handle the input media locator
        try {
            processor = javax.media.Manager.createProcessor(ds);
        } catch (NoProcessorException npe) {
            return "Couldn't create processor";
        } catch (IOException ioe) {
            return "IOException creating processor";
        }

        // Wait for it to configure
        boolean result = waitForState(processor, Processor.Configured);
        if (result == false)
            return "Couldn't configure processor";

        // Get the tracks from the processor
        TrackControl [] tracks = processor.getTrackControls();

        // Do we have atleast one track?
        if (tracks == null || tracks.length < 1)
            return "Couldn't find tracks in processor";

        // Set the output content descriptor to RAW_RTP
        // This will limit the supported formats reported from
        // Track.getSupportedFormats to only valid RTP formats.
        ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
        processor.setContentDescriptor(cd);

        Format supported[];
        Format chosen;
        boolean atLeastOneTrack = false;

        // Program the tracks.
        for (int i = 0; i < tracks.length; i++) {
            Format format = tracks[i].getFormat();
            if (tracks[i].isEnabled()) {

                supported = tracks[i].getSupportedFormats();

                // We've set the output content to the RAW_RTP.
                // So all the supported formats should work with RTP.
                // We'll just pick the first one.

                if (supported.length > 0) {
                    if (supported[1] instanceof VideoFormat) {
                        // For video formats, we should double check the
                        // sizes since not all formats work in all sizes.
                        chosen = checkForVideoSizes(tracks[i].getFormat(),
                        supported[1]);
                    } else
                        chosen = supported[1];
                    tracks[i].setFormat(chosen);
                    System.err.println("Track " + i + " is set to transmit as:");
                    System.err.println("  " + chosen);
                    atLeastOneTrack = true;
                } else
                    tracks[i].setEnabled(false);
            } else
                tracks[i].setEnabled(false);
        }

        if (!atLeastOneTrack)
            return "Couldn't set any of the tracks to a valid RTP format";

        // Realize the processor.

        result = waitForState(processor, Controller.Realized);
        if (result == false)
            return "Couldn't realize processor";

        // Set the JPEG quality to .5.
        setJPEGQuality(processor, 0.5f);

        // Get the output data source of the processor
        dataOutput = processor.getDataOutput();

        return null;
    }


    /**
     * Use the RTPManager API to create sessions for each media
     * track of the processor.
     */
    public String createTransmitter(Vector clients) {

        Vector myClients = clients;
        PushBufferDataSource pbds = (PushBufferDataSource)dataOutput;
        PushBufferStream pbss[] = pbds.getStreams();

        rtpMgrs = new RTPManager[pbss.length];
        SendStream sendStream;

        SourceDescription srcDesList[];

        for (int i = 0; i < pbss.length; i++) {
            try {
                rtpMgrs[i] = RTPManager.newInstance();

                //The local address is used to initialize the Manager
                SessionAddress localAddr = new SessionAddress( InetAddress.getLocalHost(),
                port);
                rtpMgrs[i].initialize( localAddr);

                Enumeration myDestinations = myClients.elements(); // This is the vector containing all the clients we wich to send data to

                if(clients.size()!=0){ // This is also checked in start() method; fatal exception if null so check twice just to make sure!
                    for( int y=0; y<clients.size(); y++ ) {
                        ClientLink clt = (ClientLink)clients.get(y); // Get a client
                        Socket cliSocket = clt.getClientSocket();
                        String ipAddress = cliSocket.getInetAddress().getHostAddress(); // Get this client's IP address

                        InetAddress ipAddr = InetAddress.getByName(ipAddress);
                        SessionAddress destAddr = new SessionAddress( ipAddr, port);
                        rtpMgrs[i].addTarget( destAddr); // Add the client to the recipients list

                        System.err.println( "Created RTP session: " + ipAddress + " " + port);
                    }
                    sendStream = rtpMgrs[i].createSendStream(dataOutput, i);

                    sendStream.start();
                }
                else{
                    System.out.println("No clients connected... Waiting for clients to create Stream");
                }
            } catch (Exception  e) {

                e.printStackTrace();

            }
        }

        return null;
    }

    /*THE METHODS BELOW WERE ALL TAKEN FROM THE SUN WEB SITE
     *
     **/
    /**
     * For JPEG and H263, we know that they only work for particular
     * sizes.  So we'll perform extra checking here to make sure they
     * are of the right sizes.
     */
    Format checkForVideoSizes(Format original, Format supported) {

        int width, height;
        Dimension size = ((VideoFormat)original).getSize();
        Format jpegFmt = new Format(VideoFormat.JPEG_RTP);
        Format h263Fmt = new Format(VideoFormat.H263_RTP);

        if (supported.matches(jpegFmt)) {
            // For JPEG, make sure width and height are divisible by 8.
            width = (size.width % 8 == 0 ? size.width :
                (int)(size.width / 8) * 8);
                height = (size.height % 8 == 0 ? size.height :
                    (int)(size.height / 8) * 8);
        } else if (supported.matches(h263Fmt)) {
            // For H.263, we only support some specific sizes.
            if (size.width < 128) {
                width = 128;
                height = 96;
            } else if (size.width < 176) {
                width = 176;
                height = 144;
            } else {
                width = 352;
                height = 288;
            }
        } else {
            // We don't know this particular format.  We'll just
            // leave it alone then.
            return supported;
        }

        return (new VideoFormat(null,
        new Dimension(width, height),
        Format.NOT_SPECIFIED,
        null,
        Format.NOT_SPECIFIED)).intersects(supported);
    }


    /**
     * Setting the encoding quality to the specified value on the JPEG encoder.
     * 0.5 is a good default.
     */
    void setJPEGQuality(Player p, float val) {

        Control cs[] = p.getControls();
        QualityControl qc = null;
        VideoFormat jpegFmt = new VideoFormat(VideoFormat.JPEG);

        // Loop through the controls to find the Quality control for
        // the JPEG encoder.
        for (int i = 0; i < cs.length; i++) {

            if (cs[i] instanceof QualityControl &&
            cs[i] instanceof Owned) {
                Object owner = ((Owned)cs[i]).getOwner();

                // Check to see if the owner is a Codec.
                // Then check for the output format.
                if (owner instanceof Codec) {
                    Format fmts[] = ((Codec)owner).getSupportedOutputFormats(null);
                    for (int j = 0; j < fmts.length; j++) {
                        if (fmts[j].matches(jpegFmt)) {
                            qc = (QualityControl)cs[i];
                            qc.setQuality(val);
                            System.err.println("- Setting quality to " +
                            val + " on " + qc);
                            break;
                        }
                    }
                }
                if (qc != null)
                    break;
            }
        }
    }


    /****************************************************************
     * Convenience methods to handle processor's state changes.
     ****************************************************************/

    private Integer stateLock = new Integer(0);
    private boolean failed = false;

    Integer getStateLock() {
        return stateLock;
    }

    void setFailed() {
        failed = true;
    }

    private synchronized boolean waitForState(Processor p, int state) {
        p.addControllerListener(new StateListener());
        failed = false;

        // Call the required method on the processor
        if (state == Processor.Configured) {
            p.configure();
        } else if (state == Processor.Realized) {
            p.realize();
        }

        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        // See StateListener inner class
        while (p.getState() < state && !failed) {
            synchronized (getStateLock()) {
                try {
                    getStateLock().wait();
                } catch (InterruptedException ie) {
                    return false;
                }
            }
        }

        if (failed)
            return false;
        else
            return true;
    }

    /****************************************************************
     * Inner Classes
     ****************************************************************/

    class StateListener implements ControllerListener {

        public void controllerUpdate(ControllerEvent ce) {

            // If there was an error during configure or
            // realize, the processor will be closed
            if (ce instanceof ControllerClosedEvent)
                setFailed();

            // All controller events, send a notification
            // to the waiting thread in waitForState method.
            if (ce instanceof ControllerEvent) {
                synchronized (getStateLock()) {
                    getStateLock().notifyAll();
                }
            }
        }
    }

}

