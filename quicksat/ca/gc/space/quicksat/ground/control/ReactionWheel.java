/*
 * ReactionWheel.java
 *
 * Created on June 4, 2001, 3:22 PM
 */

package ca.gc.space.quicksat.ground.control;

/**===========================================================================*/
/* Represents an on-board reaction wheel, implementing the protocole to       */
/* speak to it.                                                               */
/* @author  jfcusson                                                          */
/* @version                                                                   */
/*============================================================================*/
public class ReactionWheel extends Object {

private static final float MIN_CODE    = (float)-32768;
private static final float MAX_CODE    = (float) 32767.0;
private static final float CODE_RANGE  = (float) MAX_CODE - MIN_CODE;
private static final float MIN_SPEED   = (float)-1060.0;  //rpm
private static final float MAX_SPEED   = (float) 1060.0;
private static final float SPEED_RANGE = (float) MAX_SPEED - MIN_SPEED;
private static final float MIN_TORQUE  = (float)-4.0;     //oz-in
private static final float MAX_TORQUE  = (float) 3.998;   
private static final float TORQUE_RANGE= (float) MAX_TORQUE - MIN_TORQUE;
    
    /** Creates new ReactionWheel */
    public ReactionWheel() {
    }

    public static float calcSpeedFromCode( int code ) {
        /*-------------------------------------------------------------------*/
        /* NOTE: Due to a problem with our version of the wheel, code -32768 */
        /* must be avoided for speed control...                              */
        /*-------------------------------------------------------------------*/
        if( code == (int)MIN_CODE ) code = (int)MIN_CODE+1;
        return( (float)((((float)code - MIN_CODE)/CODE_RANGE) * SPEED_RANGE) + MIN_SPEED );
    }
    
    public static float calcTorqueFromCode( int code ) {
        return( (float)((((float)code - MIN_CODE)/CODE_RANGE) * TORQUE_RANGE) + MIN_TORQUE );
    }
    
    public static int calcCodeFromSpeed( float speed ) {
        int res = (int) ((((speed - MIN_SPEED)/SPEED_RANGE) * CODE_RANGE) + MIN_CODE);
        /*-------------------------------------------------------------------*/
        /* NOTE: Due to a problem with our version of the wheel, code -32768 */
        /* must be avoided for speed control...                              */
        /*-------------------------------------------------------------------*/
        if( res == (int)MIN_CODE ) res = (int)MIN_CODE+1;
        return( res );
    }
    
    public static int calcCodeFromTorque( float torque ) {
        return( (int)((((torque - MIN_TORQUE)/TORQUE_RANGE) * CODE_RANGE) + MIN_CODE) );
    }
    
    /*-----------------------------*/
    /* Note: percent = -100 to 100 */
    /*-----------------------------*/
    public static int calcCodeFromPercent( int percent ) {
        if( percent > 0 ) {
            return( (int) ((MAX_CODE * percent)/100)  );
        } else if( percent < 0 ) {
            return( (int) -((MIN_CODE * percent)/100)  );
        } else {
            return( 0 );
        }
    }

    /*-----------------------------*/
    /* Note: percent = -100 to 100 */
    /*-----------------------------*/
    public static int calcSpeedFromPercent( int percent ) {
        if( percent > 0 ) {            
            return( (int) ((MAX_SPEED * percent)/100)  );
        } else if( percent < 0 ) {
            return( (int) -((MIN_SPEED * percent)/100)  );
        } else {
            return( 0 );
        }
    }
    
    public static float calcTorqueFromPercent( int percent ) {
        if( percent > 0 ) {
            float res = ((float)(MAX_TORQUE * (float)percent)/(float)100);
            if( res > MAX_TORQUE ) return( (float)MAX_TORQUE );
            else return( (float) res );
        } else if( percent < 0 ) {
            float res = (float) -((MIN_TORQUE * (float)percent)/(float)100);
            if( res < MIN_TORQUE ) return( (float) MIN_TORQUE );
            else return( (float) res );
        } else {
            return( (float)0 );
        }
    }
    
    public static int calcPercentFromSpeed( float speed ) {
        return( (int) ((((speed - MIN_SPEED)/SPEED_RANGE) * 200) + (-100)) );
    }
    
    public static int calcPercentFromTorque( float torque ) {
        return( (int) ((((torque - MIN_TORQUE)/TORQUE_RANGE) * 200) + (-100)) );
    }
    
}
