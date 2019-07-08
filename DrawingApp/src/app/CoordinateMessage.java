/**
 * CoordinateMessage
 */
package app;

public class CoordinateMessage {

    private int currX, currY, prevX, prevY;

    CoordinateMessage(int cX, int cY, int pX, int pY){
        currX = cX;
        currY = cY;
        prevX = pX;
        prevY = pY;
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
}