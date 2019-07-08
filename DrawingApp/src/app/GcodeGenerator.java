package app;

import java.util.LinkedList;

/**
 * GcodeGenerator
 * This will maintain a buffer of gcode messages to send in queue/enqueue fashion.
 */
public class GcodeGenerator implements Runnable{
    private Thread t;
    private String threadName;
    private CoordinateMessageList coordinatesQueue;// = new CoordinateMessageList();
    private LinkedList<String> gCodeMessages;// = new LinkedList<String>();
    // private bool newCoordinatesFlag = false;
    private String nextGcodeString;
    private final int MAX_GCODE_CAPACITY;
    private int messagesAddedCount = 0;
    private final int SLEEP_TIME = 100;

    GcodeGenerator(String name,CoordinateMessageList sharedCoordsQueue, LinkedList<String> sharedGcodeQueue, int maxGcodeCapacity){
        threadName = name;
        coordinatesQueue = sharedCoordsQueue;
        gCodeMessages = sharedGcodeQueue;
        MAX_GCODE_CAPACITY = maxGcodeCapacity;
        System.out.println("Creating " + threadName );

    }
    public void run(){
        System.out.println("Running " + threadName);
        while(coordinatesQueue.CoordinatesAvailable()){
            try {
                //do thread stuff
                CoordinateMessage msg = coordinatesQueue.getNextCoordinates();
                nextGcodeString = parseCoordinatesToGcode(msg);
                addGcodeToQueue(nextGcodeString);
                System.out.println("Threading is happening [" + threadName + "]");
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
        
        System.out.println("Exiting thread.");
    }
    public void start(){
        System.out.println("Starting " + threadName); 
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    private void addGcodeToQueue(String gCodeStr) throws InterruptedException {
        synchronized (gCodeMessages){
            while(gCodeMessages.size() == MAX_GCODE_CAPACITY){
                System.out.println("GCode Queue at capacity.");
                gCodeMessages.wait();
            }

            Thread.sleep(SLEEP_TIME);
            gCodeMessages.addLast(gCodeStr);
            messagesAddedCount++;
            System.out.println("GCode added to queue [" + 
                 + messagesAddedCount + "]: " + gCodeStr );
            gCodeMessages.notify();
        }
    }
    private String parseCoordinatesToGcode(CoordinateMessage msg){

        return "";
    }
}