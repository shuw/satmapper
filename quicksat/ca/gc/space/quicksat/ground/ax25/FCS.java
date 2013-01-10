/*
 * FCS.java
 *
 * Created on October 22, 2001, 11:58 AM
 */

package ca.gc.space.quicksat.ground.ax25;
import java.io.*;

/**
 *
 * @author  jfcusson
 * @version 
 */
public class FCS {

private static final int EXPECTED_FINAL_FCS_VALUE = 0xF0B8;    

/*---------------------------------------------------*/
/*This is the FCS lookup table, according to ISO-3309*/
/*---------------------------------------------------*/
private static final int FCSTab[] = {    
0,4489,8978,12955,17956,22445,25910,29887,
35912,40385,44890,48851,51820,56293,59774,63735,
4225,264,13203,8730,22181,18220,30135,25662,
40137,36160,49115,44626,56045,52068,63999,59510,
8450,12427,528,5017,26406,30383,17460,21949,
44362,48323,36440,40913,60270,64231,51324,55797,
12675,8202,4753,792,30631,26158,21685,17724,
48587,44098,40665,36688,64495,60006,55549,51572,
16900,21389,24854,28831,1056,5545,10034,14011,
52812,57285,60766,64727,34920,39393,43898,47859,
21125,17164,29079,24606,5281,1320,14259,9786,
57037,53060,64991,60502,39145,35168,48123,43634,
25350,29327,16404,20893,9506,13483,1584,6073,
61262,65223,52316,56789,43370,47331,35448,39921,
29575,25102,20629,16668,13731,9258,5809,1848,
65487,60998,56541,52564,47595,43106,39673,35696,
33800,38273,42778,46739,49708,54181,57662,61623,
2112,6601,11090,15067,20068,24557,28022,31999,
38025,34048,47003,42514,53933,49956,61887,57398,
6337,2376,15315,10842,24293,20332,32247,27774,
42250,46211,34328,38801,58158,62119,49212,53685,
10562,14539,2640,7129,28518,32495,19572,24061,
46475,41986,38553,34576,62383,57894,53437,49460,
14787,10314,6865,2904,32743,28270,23797,19836,
50700,55173,58654,62615,32808,37281,41786,45747,
19012,23501,26966,30943,3168,7657,12146,16123,
54925,50948,62879,58390,37033,33056,46011,41522,
23237,19276,31191,26718,7393,3432,16371,11898,
59150,63111,50204,54677,41258,45219,33336,37809,
27462,31439,18516,23005,11618,15595,3696,8185,
63375,58886,54429,50452,45483,40994,37561,33584,
31687,27214,22741,18780,15843,11370,7921,3960
};    
    
    /*========================================================================*/
    /** Creates new FCS. For now this class is destined to be
     *  used as a static class, so constructor does nothing...                */
    /*========================================================================*/
    public FCS() {          
    /*========================================================================*/    
    }

    /*========================================================================*/
    /** Tells us if the given frame passes the checksum test (CRC16 - ISO3309). 
     *  rawFrame must contain also the original FCS. We use the "non-bitwise" 
     *  method: We recalculate the FCS on the full frame (including the original
     *  non-(de)bitwised FCS, and compare to value 0xF0B8 to see if the FCS is 
     *  valid. Otherwise we would have to "de-bitwise" the original FCS before 
     *  using it in the FCS calculation and then the FCS should be zero to be 
     *  considered OK.
     *  @param rawFrame The frame to test, INCLUDING the original, un-modified
     *                  16 bits FCS as the two last bytes.
     *  @return <b>true</b> is the frame passes the test and can be declared valid, or
     *          <b>false</b> otherwise.
    /*========================================================================*/
    public static boolean isValid( byte[] rawFrame ) {
    /*========================================================================*/    
        
        /*-------------------------------------------*/
        /* first compute the CRC16 on the full frame */
        /*-------------------------------------------*/
        int crc = compute( rawFrame );
        
        /*-----------------------------------------*/
        /* Then compare it with the expected value */
        /*-----------------------------------------*/
        if( crc == EXPECTED_FINAL_FCS_VALUE )
            return( true );
        else
            return( false );
    }
    
    /*========================================================================*/
    /** Calculates and returns the FCS (CRC16 - ISO3309) on the given frame.
     *  UNTESTED!!! Uses a static lookup table to go faster.
     *  @param rawFrame The frame which we want the CRC16 to be calculated. This
     *                  byte array will NOT be modified in any way.
     *  @return the FCS, bitwised and ready to be inserted into a frame. <I>NOTE 
     *          that for now if the frame is null, we do not throw an exception
     *          for now but rather simply return 0.</I>
    /*========================================================================*/
    public static int compute( byte[] rawFrame ) {
    /*========================================================================*/    
        if( rawFrame == null ) return(0);
        int fcs = 0xFFFF;
        for( int i=0; i<rawFrame.length; i++ ) {
            fcs = (fcs>>8)^FCSTab[ (fcs^(rawFrame[i]&0xFF))&0xFF ];
        }        
        return( fcs^0xFFFF ); //bitwise
    }
    
    /*========================================================================*/
    /** Simple application to re-calculate the FCS lookup table. Not used 
     *  normally.
     *  @param args command line arguments.
    /*========================================================================*/
    public static void main( String args[] ) {
    /*========================================================================*/    
    final int P = 0x8408;
    int b, v;
    int i;    
    FileOutputStream fos = null;
    PrintWriter pw = null;
        
        /*-----------------------------------------------------------*/
        /* The HDLC polynomial: x**0 + x**5 + x**12 + x**16 (0x8408).*/
        /*-----------------------------------------------------------*/
    
      try {
        fos = new FileOutputStream("/fcs.txt");        
        pw = new PrintWriter(fos);
        pw.println("typedef unsigned short u16;"); 
        pw.print("static u16 fcstab[256] = {");
        for( b = 0; ; ) {
           if( (b % 8) == 0 )
               pw.println("");
                v = b;
                for( i = 8; (i--)>0; )
                    v = ((v & 1)==1) ? (v >> 1) ^ P : v >> 1;
                pw.print("" + (v & 0xFFFF)); 
                if( ++b == 256 )
                    break;
                pw.print(",");
        }
        pw.print("\n};\n");
      } catch( IOException ioe ) {
          System.out.println("ERROR writing to FCS.txt file: "+ioe);
      } finally {
          pw.close();
          try{fos.close();} catch( IOException ioe ){}
      }
      
      for( int k=0; k<256; k++ ) 
          System.out.println(""+k+" > "+FCSTab[k]);
      
    }

}
