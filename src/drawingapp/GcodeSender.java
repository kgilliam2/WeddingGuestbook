package drawingapp;

import java.io.IOException;
import java.util.LinkedList;
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
    private final int MESSAGE_DELAY = 5;
    SerialPort comPorts[];// = SerialPort.getCommPorts()[0];
    SerialPort serialPort;
//    String portStr = "/dev/ttyUSB0";
//    String portStr = 
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
                // do thread stuff
                // System.out.println("Threading is happening [" + threadName + "]");
            } catch (Exception e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }

        // System.out.println("Exiting thread.");
    }

    public void initSerialCommunication() {
    	SerialPort ports[] = SerialPort.getCommPorts();
    	serialPort = ports[0];
//        serialPort = SerialPort.getCommPort();
        serialPort.setComPortParameters(115200, 8, 1, 0);
        // sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block
        // until bytes can be written

        if (serialPort.openPort()) {
            System.out.println("Port is open :)");

        } else {
            System.out.println("Failed to open port :(");
            return;
        }

        serialPort.addDataListener(messageListener);
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
            gCodeMessages.removeFirst();
            char[] gcChar = gstr.toCharArray();
            for(int i = 0; i < gcChar.length; ++i){
                serialPort.getOutputStream().write(gcChar[i]);
            }
            serialPort.getOutputStream().write('\n');
            serialPort.getOutputStream().flush();
            messageListener.readyToSend(false);
            System.out.println("Sent: " + gstr);
            gCodeMessages.notifyAll();
        }

    }

    

    public void query() throws IOException, InterruptedException {
        serialPort.getOutputStream().write('?');
        serialPort.getOutputStream().flush();
        System.out.println("Sent ?");
        Thread.sleep(1000);
    }

    public void unlock() throws IOException, InterruptedException  {
        serialPort.getOutputStream().write('$');
        serialPort.getOutputStream().write('X');
        serialPort.getOutputStream().write('\n');
        serialPort.getOutputStream().flush();
        System.out.println("Sent: $X");
        Thread.sleep(1000);
    }

    public void autoHome() throws IOException, InterruptedException  {
        serialPort.getOutputStream().write('$');
        serialPort.getOutputStream().write('H');
        serialPort.getOutputStream().write('\n');
        serialPort.getOutputStream().flush();
        System.out.println("Sent: $X");
        Thread.sleep(1000);
    }
    private final class MessageListener implements SerialPortMessageListener {
        private boolean readyFlag = true;
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
            if (msgStr.equals("\nok\r")){
                readyToSend(true);
                return "ok\n";
            }
                
            return msgStr;
        }

        public boolean readyToSend(){
            return readyFlag;
        }
        public void readyToSend(boolean rdy){
            readyFlag = rdy;
        }
        // @Override
        // public void serialEvent(SerialPortEvent event) {
        // byte[] newData = event.getReceivedData();
        // System.out.println("Received data of size: " + newData.length);
        // char c = 0;
        // for (int i = 0; i < newData.length; ++i){
        // c = (char) newData[i];
        // System.out.print(c);
        // }
        // System.out.println("\n");
        // }
    }

    
}