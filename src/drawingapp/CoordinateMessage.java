/**
 * CoordinateMessage
 */
package drawingapp;

public class CoordinateMessage {

    private int X, Y, Z;
    private CoordinateMessage prevMsg;
    private PenStates penState;
    private float moveTime = 0;
    CoordinateMessage(PenStates penState, int cX, int cY) {
        this.X = cX;
        this.Y = cY;
        setPenPosition(penState);
    }

    public void setPreviousCoordinates(CoordinateMessage msg) {
        this.prevMsg = msg;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }

    public boolean penIsUp() {
        return this.penState == PenStates.PEN_UP;
    }

    public boolean penIsDown() {
        return this.penState != PenStates.PEN_DOWN;
    }

    public PenStates getPenState() {
        return this.penState;
    }

    public void setPenPosition(PenStates penState) {
        this.Z = penState == PenStates.PEN_UP ? -1 : 1;
        this.penState = penState;
    }

    public int DeltaX() {
        if (this.prevMsg != null)
            return this.prevMsg.getX() - this.X;
        else
            return this.X;
    }

    public int DeltaY() {
        if (this.prevMsg != null)
            return this.prevMsg.getY() - this.Y;
        else
            return this.Y;
    }

    public int moveDistance() {
        int dX, dY;

        dX = DeltaX();
        dY = DeltaY();
        return (int) Math.sqrt(dX * dX + dY * dY);
    }

}