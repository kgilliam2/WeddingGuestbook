package app;

import java.io.*;
import java.util.*;
import javax.comm.*;

/**
 * SerialCommunicator
 */
public class SerialCommunicator implements Runnable, SerialPortEventListener {
    static CommPortIdentifier portId;
    static Enumeration portList;
    boolean connected;
    // map the port names to CommPortIdentifiers
    private HashMap portMap = new HashMap();

    // this is the object that contains the opened port
    private CommPortIdentifier selectedPortIdentifier = null;
    private SerialPort serialPort = null;

    // input and output streams for sending and receiving data
    private InputStream input = null;
    private OutputStream output = null;

    // the timeout value for connecting with the port
    final static int TIMEOUT = 2000;

    // some ascii values for for certain things
    final static int SPACE_ASCII = 32;
    final static int DASH_ASCII = 45;
    final static int NEW_LINE_ASCII = 10;

    // a string for recording what goes on in the program
    // this string is written to the GUI
    String logText = "";

    @Override
    public void run()  {
    
    }

    public void initSerialCommunication(){
        portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                System.out.println(portId.getName());
                if (portId.getName().equals("COM1")) {
                    // if (portId.getName().equals("/dev/term/a")) {
                    SerialCommunicator communicator = new SerialCommunicator();
                }
            }
        }
    }
    // search for all the serial ports
    // pre style="font-size: 11px;": none
    // post: adds all the found ports to a combo box on the GUI
    public void searchForPorts() {
        portList = CommPortIdentifier.getPortIdentifiers();

        while (portList.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) portList.nextElement();

            // get only serial ports
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                // window.cboxPorts.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    // connect to the selected port in the combo box
    // pre style="font-size: 11px;": ports are already found by using the
    // searchForPorts
    // method
    // post: the connected comm port is stored in commPort, otherwise,
    // an exception is generated
    public void connect() {
        String selectedPort = "/dev/ttyUSB0"; // (String)window.cboxPorts.getSelectedItem();
        selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);

        CommPort commPort = null;

        try {
            // the method below returns an object of type CommPort
            commPort = selectedPortIdentifier.open("TigerControlPanel", TIMEOUT);
            // the CommPort object can be casted to a SerialPort object
            serialPort = (SerialPort) commPort;

            // for controlling GUI elements
            setConnected(true);

            // logging
            logText = selectedPort + " opened successfully.";
            // window.txtLog.setForeground(Color.black);
            // window.txtLog.append(logText + "n");

            // CODE ON SETTING BAUD RATE ETC OMITTED
            // XBEE PAIR ASSUMED TO HAVE SAME SETTINGS ALREADY

            // enables the controls on the GUI if a successful connection is made
            // window.keybindingController.toggleControls();
        } catch (PortInUseException e) {
            logText = selectedPort + " is in use. (" + e.toString() + ")";

            // window.txtLog.setForeground(Color.RED);
            // window.txtLog.append(logText + "n");
        } catch (Exception e) {
            logText = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            // window.txtLog.append(logText + "n");
            // window.txtLog.setForeground(Color.RED);
        }
    }

    // open the input and output streams
    // pre style="font-size: 11px;": an open port
    // post: initialized input and output streams for use to communicate data
    public boolean initIOStream() {
        // return value for whether opening the streams is successful or not
        boolean successful = false;

        try {
            //
            input = serialPort.getInputStream();
            output = serialPort.getOutputStream();
            writeData(0);

            successful = true;
            return successful;
        } catch (IOException e) {
            logText = "I/O Streams failed to open. (" + e.toString() + ")";
            // window.txtLog.setForeground(Color.red);
            // window.txtLog.append(logText + "n");
            return successful;
        }
    }

    // starts the event listener that knows whenever data is available to be read
    // pre style="font-size: 11px;": an open serial port
    // post: an event listener for the serial port that knows when data is received
    public void initListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException e) {
            logText = "Too many listeners. (" + e.toString() + ")";
            // window.txtLog.setForeground(Color.red);
            // window.txtLog.append(logText + "n");
        }
    }

    // disconnect the serial port
    // pre style="font-size: 11px;": an open serial port
    // post: closed serial port
    public void disconnect() {
        // close the serial port
        try {
            writeData(0);

            serialPort.removeEventListener();
            serialPort.close();
            input.close();
            output.close();
            setConnected(false);
            // window.keybindingController.toggleControls();

            // logText = "Disconnected.";
            // window.txtLog.setForeground(Color.red);
            // window.txtLog.append(logText + "n");
        } catch (Exception e) {
            // logText = "Failed to close " + serialPort.getName()
            // + "(" + e.toString() + ")";
        }
    }

    // what happens when data is received
    // serial event is triggered
    // post: processing on the data it reads
    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                byte singleData = (byte) input.read();

                if (singleData != NEW_LINE_ASCII) {
                    logText = new String(new byte[] { singleData });
                    // window.txtLog.append(logText);
                } else {
                    // window.txtLog.append("n");
                }
            } catch (Exception e) {
                logText = "Failed to read data. (" + e.toString() + ")";
                // window.txtLog.setForeground(Color.red);
                // window.txtLog.append(logText + "n");
            }
        }
    }

    // method that can be called to send data
    // pre style="font-size: 11px;": open serial port
    // post: data sent to the other device
    public void writeData(int data) {
        try {
            output.write('?');
            output.flush();

        } catch (Exception e) {
            logText = "Failed to write data. (" + e.toString() + ")";
            // window.txtLog.setForeground(Color.red);
            // window.txtLog.append(logText + "n");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}