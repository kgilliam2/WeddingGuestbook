/**
 * CoordinateMessage
 */
package drawingapp;

public class CoordinateMessage {

    private int X, Y, Z;
    private PenStates penState;

    CoordinateMessage(PenStates penState, int cX, int cY){
        this.X = cX;
        this.Y = cY;
        setPenPosition(penState);
    }
    public int currentX(){
        return X;
    }
    public int currentY(){
        return Y;
    }
	public int currentZ() {
		return Z;
    }
    public boolean penIsUp(){
        return this.penState == PenStates.PEN_UP;
    }
    public boolean penIsDown(){
        return this.penState != PenStates.PEN_DOWN;
    }
    public PenStates getPenState(){
        return this.penState;
    }
	public void setPenPosition(PenStates penState) {
        this.Z = penState == PenStates.PEN_UP ? -1 : 1;
        this.penState = penState;
	}
}