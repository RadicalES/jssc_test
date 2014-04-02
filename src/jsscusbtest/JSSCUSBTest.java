/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jsscusbtest;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

/**
 *
 * @author janz
 */
public class JSSCUSBTest implements SerialPortEventListener {

    private SerialPort gSerialPort;
    private String gPortName;
    private boolean gOpen;
    
    public JSSCUSBTest( String port ) {
        gPortName = port;
        gOpen = false;
        gSerialPort = null;
    }    
    
    public boolean openPort() {
        
        if(gOpen) {
            System.out.println("Port already open");
            return false;
        }
        
        gSerialPort = new SerialPort(gPortName);
        try {
            gSerialPort.openPort();
        } catch (SerialPortException e) {   
            System.out.println("Port exception");
            return false;
        }
        
        try {
            gSerialPort.addEventListener(this);
        } catch (SerialPortException e) {
            try {
                gSerialPort.closePort();                           
            } catch (SerialPortException ex) {                
            }
            System.out.println("Port event listener");
            return false;
        }
        
        

        // Set notifyOnBreakInterrup to allow event driven break handling.
        try {
            int mask = SerialPort.MASK_CLOSED | SerialPort.MASK_RXCHAR;// | SerialPort.MASK_TXEMPTY | SerialPort.MASK_CTS;
            gSerialPort.setEventsMask(mask);
        }
        catch(SerialPortException e) {
            try {
                gSerialPort.closePort();                
            } catch (SerialPortException ex) {                
            }
            System.out.println("Port mask");
            return false;
        }
        gOpen = true;
        return true;
    }
    
    public void closePort() {
        gOpen = false;
        try {
            gSerialPort.closePort();
        } catch (SerialPortException ex) {
        
        }
    }
    
    public void listenPort() {
        boolean r = true;
        while(r) {
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                r = false;
            }
            
            if(!gOpen) {
                openPort();
            }
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JSSCUSBTest test;
        
        System.out.println("JSSC Serial Port Tester");
        
        if(args.length > 0) {        
            System.out.println("Finding port: " + args[0]);
            test = new JSSCUSBTest(args[0]);
            System.out.println("Opening port");
            test.openPort();
            System.out.println("Listening on port");
            test.listenPort();
            System.out.println("Closing port");
            test.closePort();
        }
        else {
            System.out.println("No port name specified!");
        }
    }
    
    public void printCharacters() throws SerialPortException {
        int l = this.gSerialPort.getInputBufferBytesCount();
        byte[] b = this.gSerialPort.readBytes(l);
        String s = new String(b);
        System.out.print(s);
    }

    @Override
    public void serialEvent(SerialPortEvent spe) {
        if(spe.getEventType() == SerialPort.MASK_CLOSED){
            System.out.println("Port closed");
            closePort();
        }
        else if(spe.getEventType() == SerialPort.MASK_RXCHAR) {           
            try {
                printCharacters();
            } catch (SerialPortException ex) {
                //Logger.getLogger(JSSCUSBTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            System.out.println("Unknown event " + spe.getEventType());
        }
    }
    
}
