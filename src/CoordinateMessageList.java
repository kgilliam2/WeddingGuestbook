package drawingapp;

import java.util.LinkedList;

/**
 * CoordinateMessageList
 */

public class CoordinateMessageList {

    private LinkedList<CoordinateMessage> coordsList = new LinkedList<CoordinateMessage>();

    public void addCoordinatesToList(int X, int Y, boolean penDown){
        int prevX = coordsList.getLast().currentX();
        int prevY = coordsList.getLast().currentY();
        boolean prevPenDown = coordsList.getLast().isPenDown();
        CoordinateMessage cmsg = new CoordinateMessage(X, Y, prevX, prevY, prevPenDown);
        coordsList.addLast(cmsg);
    }
//    public void addCoordinatesToList(CoordinateMessage msg){
//        coordsList.addLast(msg);
//    }

    public int getSize() {
        return coordsList.size();
    }
    public boolean CoordinatesAvailable(){
        if (this.getSize() > 0) return true;
        else return false;

    }
    public CoordinateMessage getNextCoordinates(){

        CoordinateMessage nextMessage = coordsList.getLast();
        coordsList.removeLast();
        return nextMessage;
    }
	public void addCoordinatesToList(CoordinateMessage msg) {
		coordsList.addLast(msg);
	}
}