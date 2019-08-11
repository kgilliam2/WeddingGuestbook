package drawingapp;

import java.util.LinkedList;

import javax.swing.JLabel;

/**
 * GcodeGenerator This will maintain a buffer of gcode messages to send in
 * queue/enqueue fashion.
 */

public class GcodeGenerator implements Runnable {
    private Thread t;
    private String threadName;
    private CoordinateMessageList coordinatesQueue;// = new CoordinateMessageList();
    private LinkedList<GcodeMessage> gCodeMessages;// = new LinkedList<String>();
    private static int MAX_GCODE_CAPACITY;
    private static final int MESSAGE_DELAY = 10;
    private JLabel posLabel;

    GcodeGenerator(String name, CoordinateMessageList sharedCoordsQueue, LinkedList<GcodeMessage> sharedGcodeQueue,
            int maxGcodeCapacity) {
        threadName = name;
        coordinatesQueue = sharedCoordsQueue;
        gCodeMessages = sharedGcodeQueue;
        MAX_GCODE_CAPACITY = maxGcodeCapacity;
        
    }

    public void run() {
        System.out.println("Running " + threadName);
        while (true) {
            try {
                // do thread stuff
                synchronized (coordinatesQueue) {
                    if (coordinatesQueue.CoordinatesAvailable()) {
                        CoordinateMessage cMsg = coordinatesQueue.popNextCoordinates();
                        if (coordinatesQueue.penStateChanged())
                            addGcodeToQueue(GcodeMessage.generatePenLiftGcode(cMsg));
                        addGcodeToQueue(new GcodeMessage(cMsg));
                        coordinatesQueue.wait();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    private void addGcodeToQueue(GcodeMessage gMsg) throws InterruptedException {
        synchronized (gCodeMessages) {
            while (gCodeMessages.size() == MAX_GCODE_CAPACITY) {
                System.out.println("GCode Queue at capacity.");
                gCodeMessages.wait(MESSAGE_DELAY);
            }
            if (gMsg.getMoveSeconds() > 0.0) {
                gCodeMessages.addLast(gMsg);
                gCodeMessages.notify();
            }

        }
    }

    public JLabel getPosLabel() {
        return posLabel;
    }

    public void setPosLabel(JLabel posLabel) {
        this.posLabel = posLabel;
    }

}