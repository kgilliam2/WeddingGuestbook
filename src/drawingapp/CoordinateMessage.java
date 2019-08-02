/**
 * CoordinateMessage
 */
package drawingapp;

public class CoordinateMessage {

    private int X, Y; //, prevX, prevY;
    private boolean penDown;

    CoordinateMessage(int cX, int cY, boolean penDown){
        this.X = cX;
        this.Y = cY;
        setPenDown(penDown);
    }
    public int currentX(){
        return X;
    }
    public int currentY(){
        return Y;
    }
    // public int previousX(){
    //     return prevX;
    // }
    // public int previousY(){
    //     return prevY;
    // }
    // public int deltaX(){
    //     return currX - prevX;
    // }
    // public int deltaY(){
    //     return currY - prevY;
    // }
	public boolean isPenDown() {
		return penDown;
	}
	public void setPenDown(boolean penDown) {
		this.penDown = penDown;
	}
}