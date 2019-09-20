package main.java.app;

import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JLabel;

import com.fazecast.jSerialComm.*;

/**
 * GcodeSender This will send messages over serial and let the generator know
 * when it has done so.
 * 
 */
public class GcodeSender implements Runnable {
    // public class GcodeSender extends Thread{
    private Thread t;
    private String threadName;
    private LinkedList<GcodeMessage> gCodeMessages = new LinkedList<GcodeMessage>();
    MessageListener messageListener = new MessageListener();
    private final int MESSAGE_DELAY = 10;
    private SerialPort serialPort;
    private JLabel statusLabel;
    private boolean portOpen = false;
    public boolean grblResponded;
    public int reconnectCount = 0;
    public int noOkayCount = 0;
    private static final boolean PRINT_GCODE = true;
    private static final boolean PRINT_ENGLISH = false;
    private boolean connectionMadeFlag = false;
    // private int sentMessages = 0;
    // private int queueLength = 0;
    // private long lastCommandTime = 0;
    // private boolean gcodeEnabled = false;

    GcodeSender(String name, LinkedList<GcodeMessage> sharedGcodeQueue) {
        threadName = name;
        gCodeMessages = sharedGcodeQueue;
        // System.out.println("Creating " + threadName );
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void run() {
        
        // System.out.println("Running " + threadName);
        // try {
        //     this.autoHome();
        // } catch (IOException | NullPointerException e1) {
        //     portOpen = false;
        //     System.out.println("No Connection. Entering reconnect loop...");
        //     while(!this.AttemptReconnect());
        // } catch (InterruptedException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }

        while (true) {
            try {
                if (!this.portOpen){
                    connectionMadeFlag = this.initSerialCommunication();
                }
                else if(connectionMadeFlag){
                    this.autoHome();
                    connectionMadeFlag = false;
                }
                else
                    sendGcode();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }catch( IOException | NullPointerException  e) {
                System.out.println("No Connection. Entering reconnect loop...");
                statusLabel.setText("Attempting to reconnect to Guest Book...");
                while(!this.AttemptReconnect());
                connectionMadeFlag = true;
            } 
        }
    }

    public void sendGcode() throws InterruptedException, IOException, NullPointerException{
        synchronized (gCodeMessages) {
            // if(){
            // noOkayCount++;
            // }
            while (gCodeMessages.isEmpty() || !messageListener.readyToSend()) {
                gCodeMessages.wait(MESSAGE_DELAY);
            }
            GcodeMessage gMsg = gCodeMessages.getFirst();
            String gStr = gMsg.asString();
            if (portOpen) {
                writeToSerial(gStr);
                System.out.print("<CONNECTED> ");
            }
            if (PRINT_GCODE)
                System.out.println(gStr);
            if (PRINT_ENGLISH)
                gMsg.printMoveInfo();
            gCodeMessages.removeFirst();
            gCodeMessages.notifyAll();
        }
    }

    public boolean AttemptReconnect() {
        reconnectCount++;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Reconnect Attempt #" + reconnectCount);
        boolean result = initSerialCommunication();
        if (result == true){
            reconnectCount = 0;
        }
        return result;
    }
    public boolean initSerialCommunication() {
        SerialPort ports[] = SerialPort.getCommPorts();
        if (ports.length == 0)
            return false;
        serialPort = ports[0];

        serialPort.setComPortParameters(115200, 8, 1, 0);
        // sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block
        // until bytes can be written

        if (serialPort.openPort()) {
            System.out.println("Port is open");
            portOpen = true;

        } else {
            System.out.println("Failed to open port :(");
            return false;
        }

        serialPort.addDataListener(messageListener);
        return true;
    }

    public void programGRBL() throws IOException, InterruptedException {
        this.disableGcode();
        GRBL_Setting_Strings GSS_Obj = new GRBL_Setting_Strings();

        int num = GSS_Obj.size();
        for (int ii = 0; ii < num; ++ii) {
            String str = GSS_Obj.at(ii);
            writeToSerial(str);
            Thread.sleep(100);
        }
        this.enableGcode();
    }

    public void query() throws IOException, InterruptedException, NullPointerException {
        serialPort.getOutputStream().write('?');
        serialPort.getOutputStream().flush();
        System.out.println("Sent ?");

        Thread.sleep(1000);
    }

    public void unlock() throws IOException, InterruptedException {
        writeToSerial("$X");
        Thread.sleep(1000);
    }

    public void autoHome() throws IOException, InterruptedException, NullPointerException {
        this.enableGcode();
        writeToSerial("$H");
        Thread.sleep(1000);
    }

    private void writeToSerial(String str) throws IOException, NullPointerException {
        for (int ii = 0; ii < str.length(); ++ii) {
            serialPort.getOutputStream().write(str.charAt(ii));
        }
        serialPort.getOutputStream().write('\n');
        // serialPort.getOutputStream().flush();
        statusLabel.setText(str);
        messageListener.readyToSend(false);
        // lastCommandTime = System.currentTimeMillis();
    }

    public void disableGcode() {
        // gcodeEnabled = false;
    }

    public void enableGcode() {
        // gcodeEnabled = true;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    private final class MessageListener implements SerialPortMessageListener {
        private boolean readyFlag = true;

        // private boolean grblResponded = false;
        // private int ackedMessages = 0;
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }

        @Override
        public byte[] getMessageDelimiter() {
            return new byte[] { (byte) 0x0D };
        }

        // { (byte)0x0B, (byte)0x65 }
        @Override
        public boolean delimiterIndicatesEndOfMessage() {
            return true;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] delimitedMessage = event.getReceivedData();
            System.out.println("Received: " + parseMessage(delimitedMessage));
        }

        String parseMessage(byte[] msg) {
            char c = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < msg.length; ++i) {
                c = ((char) msg[i]);
                sb.append(c);
            }
            String msgStr = sb.toString();
            if (msgStr.contains("ok")) {
                readyToSend(true);
                // ackedMessages++;
            } else if (msgStr.contains("error")) {
                readyToSend(false);
            } else if (msgStr.contains("[MSG:'$H'|'$X' to unlock]")) {
                grblResponded = true;
            }
            statusLabel.setText(msgStr);
            return msgStr;
        }

        public boolean readyToSend() {
            return readyFlag;
        }

        public void readyToSend(boolean rdy) {
            readyFlag = rdy;
        }
    }
    // public class DisconnectedException extends Exception { 
    //     public DisconnectedException(){};
    //     public DisconnectedException(String errorMessage) {
    //         super(errorMessage);
    //     }
    // }
}