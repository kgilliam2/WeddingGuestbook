package main.java.app;

/**
 * GcodeMessage
 */
public class GcodeMessage {

    private float moveSeconds = -1;
    private String gCodeString = "";
    private CoordinateMessage cMsg;
    private static final float MAX_FEED_RATE_MM_PER_MIN = 8000; // mm per minute
    private static final String JOG_DISTANCE_MODE_STR = "G90"; // Absolute distances ("G91" for Incremental distances)
    private static final String JOG_DISTANCE_UNITS_STR = "G21"; // Millimeter mode
    private static final float PEN_LIFT_TIME_SECONDS = (float) 3.0;

    public GcodeMessage() {
        this.parseCoordinateMessage();
    }

    public GcodeMessage(String msg) {
        this.gCodeString = msg;
    }

    public GcodeMessage(CoordinateMessage cMsg) {
        this.cMsg = cMsg;
        this.parseCoordinateMessage();
        this.setMoveSeconds(this.MoveMillimeters() / MAX_FEED_RATE_MM_PER_MIN * 60);
    }

    private void parseCoordinateMessage() {
        float cX;
        float cY;
        float cZ;
        if (cMsg == null) {
            cX = 0;
            cY = 0;
            cZ = CoordinateMessage.getZ(PenStates.PEN_UP);
        } else {
            cX = PixelToPaperTransform.transformXCoordinate(cMsg.getX());
            cY = PixelToPaperTransform.transformYCoordinate(cMsg.getY());
            cZ = cMsg.getZ();
        }
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(GcodeMessage.jogConfigString());
        sBuilder.append(" X");
        sBuilder.append(String.format("%.3f", cX));
        sBuilder.append(" Y");
        sBuilder.append(String.format("%.3f", cY));
        sBuilder.append(" Z");
        sBuilder.append(String.format("%.3f", cZ));
        sBuilder.append(" F");
        sBuilder.append(MAX_FEED_RATE_MM_PER_MIN);
        this.gCodeString = sBuilder.toString();
    }

    public static GcodeMessage generatePenLiftGcode(CoordinateMessage cMsg) {
        StringBuilder sBuilder = new StringBuilder();
        float cZ = cMsg.getZ();
        sBuilder.append(GcodeMessage.jogConfigString());
        sBuilder.append(" Z");
        sBuilder.append(String.format("%.3f", cZ));
        sBuilder.append(" F");
        sBuilder.append(MAX_FEED_RATE_MM_PER_MIN);
        GcodeMessage gMsg = new GcodeMessage(sBuilder.toString());
        gMsg.setMoveSeconds(PEN_LIFT_TIME_SECONDS);
        return gMsg;
    }

    public float getMoveTime() {
        return this.moveSeconds;
    }

    public float MoveMillimeters() {
        if (this.cMsg != null)
            return cMsg.moveDistancePixels() * PixelToPaperTransform.mmPerPixel();

        else
            return -9999;
    }

    // public float MoveMinutes() {
    //     return this.MoveMillimeters() / MAX_FEED_RATE_MM_PER_MIN;
    // }

    public float MoveSeconds() {
        return this.moveSeconds;
    }

    public String asString() {
        return this.gCodeString;
    }

    public PenStates getPenState() {
        if (this.cMsg != null)
            return this.cMsg.getPenState();
        else
            return PenStates.PEN_UP;
    }

    private static String jogConfigString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("$J = ");
        sBuilder.append(JOG_DISTANCE_MODE_STR);
        sBuilder.append(" ");
        sBuilder.append(JOG_DISTANCE_UNITS_STR);
        return sBuilder.toString();
    }

    public float getMoveSeconds() {
        return this.moveSeconds;
    }

    public void setMoveSeconds(float moveSeconds) {
        this.moveSeconds = moveSeconds;
    }

    public void printMoveInfo() {
        String penStateString = (this.getPenState() == PenStates.PEN_UP) ? "up" : "dn";
        System.out.print("<Pen State [up/dn]>: " + penStateString);
        System.out.print("; <Distance [mm]>: " + this.MoveMillimeters());
        System.out.println("; <Duration [s]>: " + this.moveSeconds);
    }
}
