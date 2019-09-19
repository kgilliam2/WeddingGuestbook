package main.java.app;

import java.util.LinkedList;

/**
 * CoordinateMessageList
 */

public class CoordinateMessageList {

    private LinkedList<CoordinateMessage> coordsList = new LinkedList<CoordinateMessage>();
    private boolean bPenStateChanged;

    public void addCoordinatesToList(PenStates penState, int X, int Y) {
        PenStates prevPenState;
        CoordinateMessage prevMsg = this.getLast();
        if (coordsList.size() > 0)
            prevPenState = coordsList.getLast().getPenState();
        else
            prevPenState = PenStates.PEN_UP;

        bPenStateChanged = prevPenState != penState;
        CoordinateMessage msg = new CoordinateMessage(penState, X, Y);
        if (prevMsg != null)
            msg.setPreviousCoordinates(prevMsg);
        coordsList.addLast(msg);

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

    public CoordinateMessage getLast() {
        if (this.coordsList.size() > 0)
            return this.coordsList.getLast();
        else
            return null;
    }

    public CoordinateMessage popNextCoordinates() {

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