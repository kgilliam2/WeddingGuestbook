package drawingapp;

import java.util.LinkedList;

import javax.swing.JLabel;

/**
 * GcodeGenerator
 * This will maintain a buffer of gcode messages to send in queue/enqueue fashion.
 */
public class GcodeGenerator implements Runnable{
// public class GcodeGenerator implements Thread{
    private Thread t;
    private String threadName;
    private CoordinateMessageList coordinatesQueue;// = new CoordinateMessageList();
    private LinkedList<String> gCodeMessages;// = new LinkedList<String>();
    private String nextGcodeString;
    private static int MAX_GCODE_CAPACITY;
    private static final int MESSAGE_DELAY = 10;
//    public static float yMinBuff;
//    public static float yMaxBuff;
//    public static float xMinBuff;
//    public static float xMaxBuff;
//    private float mmPerPixelX, mmPerPixelY;
//    private float maxTravelX, maxTravelY;
    private pixelToPaperTransform TX;
    private final float FEED_RATE = 8000;
    private JLabel posLabel;
    
    
    GcodeGenerator(String name,CoordinateMessageList sharedCoordsQueue, LinkedList<String> sharedGcodeQueue, int maxGcodeCapacity){
        threadName = name;
        coordinatesQueue = sharedCoordsQueue;
        gCodeMessages = sharedGcodeQueue;
        MAX_GCODE_CAPACITY = maxGcodeCapacity;
        TX = new pixelToPaperTransform();
        // System.out.println("Creating " + threadName );

    }
	public pixelToPaperTransform coordinateSystem() {
		return this.TX;
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
                        // if(coordinatesQueue.penStateChanged()){
                        //     Thread.sleep(400);
                        // }
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
                gCodeMessages.wait(MESSAGE_DELAY);
            }
            gCodeMessages.addLast(gCodeStr);
            // messagesAddedCount++;
            // System.out.println("Generated GCode: [" + 
            //      + messagesAddedCount + ", " + gCodeMessages.size() + "]: " + gCodeStr );
            // Thread.sleep(SLEEP_TIME);
            gCodeMessages.notify();
        }
    }
    private String parseCoordinatesToGcode(CoordinateMessage msg){
        float cX = TX.transformXCoordinate(msg.currentX());
        float cY = TX.transformYCoordinate(msg.currentY());
        
        float cZ = msg.isPenDown() ? -1 : 1;
        
        posLabel.setText("X: " + msg.currentX() + "Y: " + msg.currentY());
        StringBuilder sBuilder = new StringBuilder();
        //it's in mm

        sBuilder.append("$J =");
        sBuilder.append(" G90"); //Absolute distances
        // sBuilder.append(" G91"); //Incremental distances
        sBuilder.append(" G21"); //Millimeter mode
        sBuilder.append(" X");
        sBuilder.append(String.format("%.3f", cX));
        sBuilder.append(" Y");
        sBuilder.append(String.format("%.3f", cY));
        sBuilder.append(" Z");
        sBuilder.append(String.format("%.3f", cZ));
        sBuilder.append(" F");
        sBuilder.append(FEED_RATE);
        sBuilder.append("\n");
        
        return sBuilder.toString();
    }

    public String getNextGcodeString(){
        return nextGcodeString;
    }
    
	public JLabel getPosLabel() {
		return posLabel;
	}
	public void setPosLabel(JLabel posLabel) {
		this.posLabel = posLabel;
	}
	

    public static class pixelToPaperTransform {
    	private static float xMinPaper, xMaxPaper, yMinPaper, yMaxPaper;
    	private static float numPixelsX, numPixelsY;
//    	private float mmPerPixelX, mmPerPixelY;
    	
    	public void setPaperLimits(float xMin, float xMax, float yMin, float yMax) {
    		pixelToPaperTransform.xMinPaper = xMin;
    		pixelToPaperTransform.xMaxPaper = xMax;
    		pixelToPaperTransform.yMinPaper = yMin;
    		pixelToPaperTransform.yMaxPaper = yMax;
    	}
    	public void setPixelLimits(float numPixelsX, float numPixelsY) {
    		pixelToPaperTransform.numPixelsX = numPixelsX;
    		pixelToPaperTransform.numPixelsY = numPixelsY;
    	}
    	public float mmPerPixelX() {
    		float maxPaperTravel = xMaxPaper - xMinPaper;
    		return maxPaperTravel/numPixelsX;
    	}
    	public float mmPerPixelY() {
    		float maxPaperTravel = yMaxPaper - yMinPaper;
    		return maxPaperTravel/numPixelsY;
    	}
    	public float transformXCoordinate(float xPixelCoordinate) {
    		float xPaperCoordinate = mmPerPixelX()*xPixelCoordinate + xMinPaper;
    		return xPaperCoordinate;
    	}
    	public float transformYCoordinate(float yPixelCoordinate) {
    		float yPaperCoordinate  = mmPerPixelY()*yPixelCoordinate + yMinPaper;
    		return yPaperCoordinate;
    	}
    }

}