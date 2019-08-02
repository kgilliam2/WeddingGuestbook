package drawingapp;

import java.util.LinkedList;

/**
 * CoordinateMessageList
 */

public class CoordinateMessageList {

    private LinkedList<CoordinateMessage> coordsList = new LinkedList<CoordinateMessage>();
    private boolean bPenStateChanged;

    public void addCoordinatesToList(int X, int Y, boolean penDown) {
        if (coordsList.size() > 0) {
            int prevX = coordsList.getLast().currentX();
            int prevY = coordsList.getLast().currentY();
            boolean prevPenDown = coordsList.getLast().isPenDown();
            if (prevPenDown != penDown) {
                CoordinateMessage msg1 = new CoordinateMessage(prevX, prevY, penDown);
                
                for(int ii = 1; ii < 1000; ++ii){
                    System.out.println("DROPPING THE PEN MOTHERFUCKEERRRRSSSSSS");
                    coordsList.addLast(msg1);
                }
                
                // try {
                //     Thread.sleep(300);
                // } catch (InterruptedException e) {
                //     // TODO Auto-generated catch block
                //     e.printStackTrace();
                // }
            }
        }
        CoordinateMessage msg2 = new CoordinateMessage(X, Y, penDown);
        coordsList.addLast(msg2);

    }
    // public void addCoordinatesToList(CoordinateMessage msg){
    // coordsList.addLast(msg);
    // }

    public int getSize() {
        return coordsList.size();
    }

    public boolean CoordinatesAvailable() {
        if (this.getSize() > 0)
            return true;
        else
            return false;

    }

    public CoordinateMessage getNextCoordinates() {

        CoordinateMessage nextMessage = coordsList.getLast();
        coordsList.removeLast();
        return nextMessage;
    }

    public boolean penStateChanged() {
        if (this.bPenStateChanged) {
            this.bPenStateChanged = false;
            return true;
        }
        return false;
    }

    public void addCoordinatesToList(CoordinateMessage msg) {
        coordsList.addLast(msg);
    }
}