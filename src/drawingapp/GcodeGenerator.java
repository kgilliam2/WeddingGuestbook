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
    private static final float PEN_LIFT_DELAY = (float)0.3; //seconds
    // private static final String JOG_COMMAND_FORMATTER = "$J ="
    private pixelToPaperTransform TX;
    private final float MAX_FEED_RATE = 8000; //mm per minute
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
                        
                        CoordinateMessage msg = coordinatesQueue.popNextCoordinates();
                        int moveDistance = msg.moveDistance();
                        float moveTime = moveDistance / MAX_FEED_RATE;
                        System.out.println("Move Distance: " + moveDistance + "; Move Time: " + moveTime);
                        if(coordinatesQueue.penStateChanged()) {
                            nextGcodeString = generatePenLiftGcode(msg);
                            addGcodeToQueue(nextGcodeString);
                            // nextGcodeString = generateDelayGcode(PEN_LIFT_DELAY);
                            // addGcodeToQueue(nextGcodeString);
                        } 
                        else 
                            nextGcodeString = parseCoordinatesToGcode(msg);
                        addGcodeToQueue(nextGcodeString);
                        coordinatesQueue.wait();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Thread interrupted.");
                e.printStackTrace();
            }
        }
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
            gCodeMessages.notify();
        }
    }
    private String generatePenLiftGcode(CoordinateMessage msg){
        StringBuilder sBuilder = new StringBuilder();
        float cZ = msg.getZ();
        sBuilder.append("$J =");
        sBuilder.append(" G90"); //Absolute distances
        // sBuilder.append(" G91"); //Incremental distances
        sBuilder.append(" G21"); //Millimeter mode

        sBuilder.append(" Z");
        sBuilder.append(String.format("%.3f", cZ));
        sBuilder.append(" F");
        sBuilder.append(MAX_FEED_RATE/200);
        // sBuilder.append("\n");
        return sBuilder.toString();
    }
    private String generateDelayGcode(float delay){
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("G4 P");
        sBuilder.append(String.format("%1.2f", delay));
        // sBuilder.append("\n");
        return sBuilder.toString();
    }
    private String parseCoordinatesToGcode(CoordinateMessage msg){
        float cX = TX.transformXCoordinate(msg.getX());
        float cY = TX.transformYCoordinate(msg.getY());
        float cZ = msg.getZ();
        
        posLabel.setText("X: " + msg.getX() + "Y: " + msg.getY());
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
        sBuilder.append(MAX_FEED_RATE);
        // sBuilder.append("\n");
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
        public float mmPerPixel(){
            return Math.max(this.mmPerPixelX(), this.mmPerPixelY());
        }
    	public float transformXCoordinate(float xPixelCoordinate) {
    		float xPaperCoordinate = mmPerPixelX()*xPixelCoordinate + xMinPaper;
    		return xPaperCoordinate;
    	}
    	public float transformYCoordinate(float yPixelCoordinate) {
    		float yPaperCoordinate  = mmPerPixelY()*yPixelCoordinate + yMinPaper;
    		return yPaperCoordinate;
        }
        // public float getMoveDuration(CoordinateMessage msg){

        //     return (float)0.0;
        // }
    }

}