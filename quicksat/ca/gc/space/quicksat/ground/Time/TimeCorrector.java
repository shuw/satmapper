package ca.gc.space.quicksat.ground.Time;

import java.net.*;
import java.util.*;
import java.io.*;
import java.sql.*;
import ca.gc.space.quicksat.ground.util.*;
public class TimeCorrector {
  Socket socket ;
    DataInputStream input;
    InetAddress add;
    long time;
    public void TimeCorrector(){
      
    }
    public long getTimeOffset(String address) throws Exception{
        
        //Connect to the time server
        try{
                add = InetAddress.getByName(address.trim());
                //byte[] data = new byte[4];
                socket = ca.gc.space.quicksat.ground.util.TimedSocket.getSocket(add,37,10000);
                
                input = new DataInputStream(socket.getInputStream());
                
                int one = input.readUnsignedByte();
                int two = input.readUnsignedByte();
                int three = input.readUnsignedByte();
                int four = input.readUnsignedByte();
                long test = 0;
                
                test|=one;
                test<<=24;
   
                test = two<<16|test;
                test = three<<8|test;
                test = four|test;
                //System.out.println(test);
                System.out.println(System.currentTimeMillis()/1000);
                long t = ((test)-2208988800L)*1000;
                time=t;                
               // Timestamp ts = new Timestamp(t);
                 //ts2 = new Timestamp(System.currentTimeMillis());

               // System.out.println(ts);
               // System.out.println(ts2);
  
                  return (System.currentTimeMillis())-time;
           
        }
        catch (Exception e) {
          throw e;
           
        }
        
        
    }
    

  
}