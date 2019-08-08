package drawingapp;

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
    private LinkedList<String> gCodeMessages = new LinkedList<String>();
    MessageListener messageListener = new MessageListener();
    private final int MESSAGE_DELAY = 10;
    private SerialPort serialPort;
    private JLabel statusLabel;
    // private int sentMessages = 0;
    // private int queueLength = 0;
    // private long lastCommandTime = 0;
    // private boolean gcodeEnabled = false;

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }

    //
    // String portStr =
    GcodeSender(String name, LinkedList<String> sharedQueue) {
        threadName = name;
        gCodeMessages = sharedQueue;
        // System.out.println("Creating " + threadName );
    }

    public void run() {
        System.out.println("Running " + threadName);
        while (true) {
            try {
                sendGcode();
            } catch (Exception e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
    }

    public boolean initSerialCommunication() {
        String portStr = "/dev/ttyUSB0";
        serialPort = SerialPort.getCommPort(portStr);
        // SerialPort ports[] = SerialPort.getCommPorts();
        // if (ports.length == 0) return false;
        // serialPort = ports[0];
        // serialPort = SerialPort.getCommPort();
        serialPort.setComPortParameters(115200, 8, 1, 0);
        // sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block
        // until bytes can be written

        if (serialPort.openPort()) {
            System.out.println("Port is open");

        } else {
            System.out.println("Failed to open port :(");
            return false;
        }

        serialPort.addDataListener(messageListener);
        return true;
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void sendGcode() throws InterruptedException, IOException {
        synchronized (gCodeMessages) {
            while (gCodeMessages.isEmpty() || !messageListener.readyToSend()) {
                gCodeMessages.wait(MESSAGE_DELAY);
            }
            // Thread.sleep(MESSAGE_DELAY);
            String gstr = gCodeMessages.getFirst();
            writeToSerial(gstr);
            // sentMessages++;
            gCodeMessages.removeFirst();
            gCodeMessages.notifyAll();
        }

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

    public void query() throws IOException, InterruptedException {
        serialPort.getOutputStream().write('?');
        serialPort.getOutputStream().flush();
        System.out.println("Sent ?");

        Thread.sleep(1000);
    }

    public void unlock() throws IOException, InterruptedException {
        writeToSerial("$X");
        Thread.sleep(1000);
    }

    public void autoHome() throws IOException, InterruptedException {
        this.enableGcode();
        writeToSerial("$H");
        Thread.sleep(1000);
    }

    private void writeToSerial(String str) throws IOException {
        for (int ii = 0; ii < str.length(); ++ii) {
            serialPort.getOutputStream().write(str.charAt(ii));
        }
        serialPort.getOutputStream().write('\n');
        // serialPort.getOutputStream().flush();

        System.out.println("Sent: " + str);
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

    private final class MessageListener implements SerialPortMessageListener {
        private boolean readyFlag = true;

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

}