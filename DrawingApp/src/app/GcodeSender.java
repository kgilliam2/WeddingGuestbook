package app;

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
    // private final int MESSAGE_DELAY = 1000;
    SerialPort comPorts[];// = SerialPort.getCommPorts()[0];
    SerialPort comPort;
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

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    public void sendGcode() throws InterruptedException {
        synchronized (gCodeMessages) {
            while (gCodeMessages.isEmpty()) {
                // System.out.println("Queue is empty " +
                // Thread.currentThread().getName() +
                // " is waiting , size: " + gCodeMessages.size());
                gCodeMessages.wait(1000);
            }
            // Thread.sleep(MESSAGE_DELAY);
            String gstr = gCodeMessages.getFirst();
            gCodeMessages.removeFirst();
            System.out.println("Sent GCode: " + gstr);
            gCodeMessages.notifyAll();
        }

    }

    public void initSerialCommunication() {
        comPorts = SerialPort.getCommPorts();
        comPort = comPorts[0];
        comPort.openPort();
        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE){
                    return;
                }
                // else if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_WRITTEN){
                //     System.out.println("All bytes were successfully transmitted!");
                // }
                byte[] newData = new byte[comPort.bytesAvailable()];
                int numRead = comPort.readBytes(newData, newData.length);
                System.out.println("Read " + numRead + " bytes.");
            }
        });
    }
}