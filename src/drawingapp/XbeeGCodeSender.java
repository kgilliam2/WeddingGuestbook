package drawingapp;

import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JLabel;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.models.XBeeProtocol;

public class XbeeGCodeSender implements Runnable {

    /* Constants */
    private Thread t;
    private String threadName;
    private final int MESSAGE_DELAY = 10;
    private static final String PORT = "/dev/ttyUSB0";
    private static final int BAUD_RATE = 9600;
    private LinkedList<GcodeMessage> gCodeMessages = new LinkedList<GcodeMessage>();
    private MyDataReceiveListener dataListener = new MyDataReceiveListener();
    private XBeeDevice myLocalXbee;
    private JLabel statusLabel;
    
    XbeeGCodeSender(String name, LinkedList<GcodeMessage> sharedGcodeQueue) {
        threadName = name;
        gCodeMessages = sharedGcodeQueue;
        myLocalXbee = new XBeeDevice(PORT, BAUD_RATE);
        myLocalXbee.addDataListener(dataListener);
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
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

    public void openXbeeComs() {

        try {
            myLocalXbee.open();
            System.out.println(" >> Success");
        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        } finally {
            myLocalXbee.close();
        }
    }

    public void sendGcode() throws InterruptedException, IOException {
        synchronized (gCodeMessages) {
            while (gCodeMessages.isEmpty()) {
                gCodeMessages.wait(MESSAGE_DELAY);
            }
            GcodeMessage gMsg = gCodeMessages.getFirst();
            String gStr = gMsg.asString();
            // Send the string
            BroadcastString(gStr);
            gCodeMessages.removeFirst();
            gCodeMessages.notifyAll();
        }
    }

    public void BroadcastString(String str) {
        byte[] dataToSend = str.getBytes();
        try {
            System.out.format("Sending broadcast data: '%s'", new String(dataToSend));
            myLocalXbee.sendBroadcastData(dataToSend);
            System.out.println(" >> Success");

        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void autoHome() throws IOException, InterruptedException {
        // this.enableGcode();
        BroadcastString("$H");
        Thread.sleep(1000);
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }

    public void setStatusLabel(JLabel statusLabel) {
        this.statusLabel = statusLabel;
    }
    private class MyDataReceiveListener implements IDataReceiveListener {
        private boolean readyFlag = true;

        @Override
        public void dataReceived(XBeeMessage xMsg) {
            String address = xMsg.getDevice().get64BitAddress().toString();
            String msgString = xMsg.getDataString();
            System.out.println("Received data from " + address + ": " + parseMessage(msgString.getBytes()));
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