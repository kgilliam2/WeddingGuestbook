package drawingapp;

/**
 * GcodeMessage
 */
public class GcodeMessage {

    private float moveTime;
    private String gCodeString;

    public float getMoveTime(){
        return this.moveTime;
    }
    public void setMoveTime(float mt){
        this.moveTime = mt;
    }
}