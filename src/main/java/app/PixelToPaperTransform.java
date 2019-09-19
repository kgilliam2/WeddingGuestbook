package main.java.app;

/**
 * PixelToPaperTransform
 */
public class PixelToPaperTransform {
    private static float xMinPaper, xMaxPaper, yMinPaper, yMaxPaper;
    private static float numPixelsX, numPixelsY;
    // private float mmPerPixelX, mmPerPixelY;

    public static void setPaperLimits(float xMin, float xMax, float yMin, float yMax) {
        PixelToPaperTransform.xMinPaper = xMin;
        PixelToPaperTransform.xMaxPaper = xMax;
        PixelToPaperTransform.yMinPaper = yMin;
        PixelToPaperTransform.yMaxPaper = yMax;
    }

    public static void setPixelLimits(float numPixelsX, float numPixelsY) {
        PixelToPaperTransform.numPixelsX = numPixelsX;
        PixelToPaperTransform.numPixelsY = numPixelsY;
    }

    public static float mmPerPixelX() {
        float maxPaperTravel = xMaxPaper - xMinPaper;
        return maxPaperTravel / numPixelsX;
    }

    public static float mmPerPixelY() {
        float maxPaperTravel = yMaxPaper - yMinPaper;
        return maxPaperTravel / numPixelsY;
    }

    public static float mmPerPixel() {
        return Math.max(mmPerPixelX(), mmPerPixelY());
    }

    public static float transformXCoordinate(float xPixelCoordinate) {
        float xPaperCoordinate = mmPerPixelX() * xPixelCoordinate + xMinPaper;
        return xPaperCoordinate;
    }

    public static float transformYCoordinate(float yPixelCoordinate) {
        float yPaperCoordinate = mmPerPixelY() * yPixelCoordinate + yMinPaper;
        return yPaperCoordinate;
    }
}