/**
 * CoordinateMessage
 */
package drawingapp;

public class CoordinateMessage {

    private int currX, currY, prevX, prevY;
    private boolean penDown;

    CoordinateMessage(int cX, int cY, int pX, int pY, boolean penDown){
        currX = cX;
        currY = cY;
        prevX = pX;
        prevY = pY;
        setPenDown(penDown);
    }
    public int currentX(){
        return currX;
    }
    public int currentY(){
        return currY;
    }
    public int previousX(){
        return prevX;
    }
    public int previousY(){
        return prevY;
    }
    public int deltaX(){
        return currX - prevX;
    }
    public int deltaY(){
        return currY - prevY;
    }
	public boolean isPenDown() {
		return penDown;
	}
	public void setPenDown(boolean penDown) {
		this.penDown = penDown;
	}
}