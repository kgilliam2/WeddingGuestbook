package drawingapp;

import java.util.LinkedList;

/**
 * CoordinateMessageList
 */

public class CoordinateMessageList {

    private LinkedList<CoordinateMessage> coordsList = new LinkedList<CoordinateMessage>();
    private boolean bPenStateChanged;

    public void addCoordinatesToList(PenStates penState, int X, int Y) {
        PenStates prevPenState;
        if (coordsList.size() > 0)
            prevPenState = coordsList.getLast().getPenState();
        else
            prevPenState = PenStates.PEN_UP;
            
        bPenStateChanged = prevPenState != penState;
        CoordinateMessage msg2 = new CoordinateMessage(penState, X, Y);
        coordsList.addLast(msg2);

    }
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
            return this.bPenStateChanged;
    }

    public void addCoordinatesToList(CoordinateMessage msg) {
        coordsList.addLast(msg);
    }
}