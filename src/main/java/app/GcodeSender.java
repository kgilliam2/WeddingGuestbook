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
    private final int MESSAGE_DELAY = 20;
    private SerialPort serialPort;
    private JLabel statusLabel;
    private boolean portOpen = false;
    public boolean grblResponded;
    public int reconnectCount = 0;

    private static final boolean PRINT_GCODE = true;
    private static final boolean PRINT_ENGLISH = false;

    // private long tic, toc;
    // private int sentMessages = 0;
    // private int queueLength = 0;
    private long lastCommandTime = 0;
    private long currentTime = 0;
    private boolean homed = false;

    // private boolean discoFlag = true;
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
        // this.autoHome();
        // } catch (IOException | NullPointerException e1) {
        // portOpen = false;
        // System.out.println("No Connection. Entering reconnect loop...");
        // while(!this.AttemptReconnect());
        // } catch (InterruptedException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        lastCommandTime = System.nanoTime() / 1000000;

        while (true) {
            try {
                if (!this.portOpen) {
                    // statusLabel.setText("Connecting to Guest Book...");
                    this.initSerialCommunication();
                    this.autoHome();
                } else if (messageListener.getConnectionMadeFlag()) {
                    this.autoHome();
                    // this.query();
                    messageListener.clearConnectionMadeFlag();
                } else
                    sendGcode();
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            } catch (IOException | DisconnectedException | NullPointerException e) {
                if (this.portOpen)
                    serialPort.closePort();
                    e.printStackTrace();
                System.out.println("No Connection. Entering reconnect loop...");
                while (!this.AttemptReconnect()) {}
            }
        }
    }

    public void sendGcode() throws InterruptedException, IOException, NullPointerException, DisconnectedException {
        synchronized (gCodeMessages) {
            // if(){

            // }
            if (gCodeMessages.isEmpty()) {
                gCodeMessages.wait(MESSAGE_DELAY);
                return;
            }
            if (!messageListener.readyToSend()) {
                messageListener.incrementNoOkayCount();
                gCodeMessages.wait(MESSAGE_DELAY);
                if (messageListener.getNoOkayCount() > 200) {
                    messageListener.resetNoOkayCount();
                    throw new DisconnectedException();
                }
                return;
            }
            // currentTime = System.nanoTime() / 1000000;
            // if (!homed) {
            // if ((currentTime - lastCommandTime) > 20000) {
            // // statusLabel.setText("!homed");
            // autoHome();
            // return;
            // }
            // } else {
            // if ((currentTime - lastCommandTime) > 600000) {
            // // statusLabel.setText("homed wtf");
            // autoHome();
            // return;
            // }
            // }
            if (messageListener.getConnectionMadeFlag()) {
                autoHome();
                return;
            }

            GcodeMessage gMsg = gCodeMessages.getFirst();
            String gStr = gMsg.asString();
            if (portOpen) {
                writeToSerial(gStr);
                homed = false;
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
            statusLabel.setText("Attempting to reconnect to Guest Book...");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Reconnect Attempt #" + reconnectCount);
        statusLabel.setText("Reconnect Attempt #" + reconnectCount);
        boolean result = initSerialCommunication();
        if (result == true) {
            reconnectCount = 0;
            messageListener.setConnectionMadeFlag();
            try {
                Thread.sleep(1000);
                statusLabel.setText("Connected!");

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
        lastCommandTime = System.nanoTime() / 1000000;
        homed = true;
        Thread.sleep(1000);
    }

    public void unlock() throws IOException, InterruptedException {
        writeToSerial("$X");
        Thread.sleep(1000);
    }

    public void autoHome() throws IOException, InterruptedException, NullPointerException {
        this.enableGcode();
        writeToSerial("$H");
        homed = true;
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
        lastCommandTime = System.nanoTime() / 1000000;
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
        private boolean connectionMadeFlag = false;
        private int noOkayCount = 0;

        // private boolean grblResponded = false;
        // private int ackedMessages = 0;
        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
        }
        public void resetNoOkayCount(){
            this.noOkayCount = 0;
        }
        public int getNoOkayCount() {
            return noOkayCount;
        }

        public void incrementNoOkayCount() {
            this.noOkayCount++;
        }

        public boolean getConnectionMadeFlag() {
            return connectionMadeFlag;
        }

        public void setConnectionMadeFlag() {
            this.connectionMadeFlag = true;
        }

        public void clearConnectionMadeFlag() {
            this.connectionMadeFlag = false;
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
            String msg = parseMessage(delimitedMessage);
            System.out.println("Received: " + msg);
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
                resetNoOkayCount();
            } else if (msgStr.contains("error")) {
                readyToSend(false);
            } else if (msgStr.contains("[MSG:'$H'|'$X' to unlock]")) {
                grblResponded = true;
                connectionMadeFlag = true;
            }
            // statusLabel.setText(msgStr);
            return msgStr;
        }

        public boolean readyToSend() {
            return readyFlag;
        }

        public void readyToSend(boolean rdy) {
            readyFlag = rdy;
        }
    }
    public class DisconnectedException extends Exception {
    public DisconnectedException(){
        super ("You've been disconnected.");
    };
    public DisconnectedException(String errorMessage) {
    super(errorMessage);
    }
    }
}