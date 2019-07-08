package app;

import java.util.LinkedList;

/**
 * GcodeGenerator
 * This will maintain a buffer of gcode messages to send in queue/enqueue fashion.
 */
// public class GcodeGenerator implements Runnable{
public class GcodeGenerator extends Thread{
    private Thread t;
    private String threadName;
    private CoordinateMessageList coordinatesQueue;// = new CoordinateMessageList();
    private LinkedList<String> gCodeMessages;// = new LinkedList<String>();
    // private bool newCoordinatesFlag = false;
    private String nextGcodeString;
    private final int MAX_GCODE_CAPACITY;
    private int messagesAddedCount = 0;
    // private final int SLEEP_TIME = 1000;

    GcodeGenerator(String name,CoordinateMessageList sharedCoordsQueue, LinkedList<String> sharedGcodeQueue, int maxGcodeCapacity){
        threadName = name;
        coordinatesQueue = sharedCoordsQueue;
        gCodeMessages = sharedGcodeQueue;
        MAX_GCODE_CAPACITY = maxGcodeCapacity;
        // System.out.println("Creating " + threadName );

    }
    public void run(){
        System.out.println("Running " + threadName);
        // while(coordinatesQueue.CoordinatesAvailable()){
        while(true){
            try {
                //do thread stuff
                synchronized(coordinatesQueue){
                    if(coordinatesQueue.CoordinatesAvailable()){
                        CoordinateMessage msg = coordinatesQueue.getNextCoordinates();
                        nextGcodeString = parseCoordinatesToGcode(msg);
                        addGcodeToQueue(nextGcodeString);
                        // System.out.println("Coordinates Available");
                        coordinatesQueue.wait();
                    }
                }
                

                // System.out.println("Threading is happening [" + threadName + "]");
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
        
       // System.out.println("Exiting thread.");
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
            gCodeMessages.addLast(gCodeStr);
            messagesAddedCount++;
            System.out.println("Generated GCode: [" + 
                 + messagesAddedCount + ", " + gCodeMessages.size() + "]: " + gCodeStr );
            // Thread.sleep(SLEEP_TIME);
            gCodeMessages.notify();
        }
    }
    private String parseCoordinatesToGcode(CoordinateMessage msg){
        int cX = msg.currentX();
        int cY = msg.currentY();

        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("X");
        sBuilder.append(cX);
        sBuilder.append(" Y");
        sBuilder.append(cY);
        sBuilder.append("/n");
        return sBuilder.toString();
    }
}