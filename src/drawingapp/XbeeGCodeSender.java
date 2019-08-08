package drawingapp;

import com.digi.xbee.api.WiFiDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBeeProtocol;

public class XbeeGCodeSender {

     /* Constants */
    private static final String PORT = "COM1";
    private static final int BAUD_RATE = 9600;

    private XBeeDevice myDevice;

    public void openXbeeComs(){
        myDevice = new XBeeDevice(PORT, BAUD_RATE);
        try {
            myDevice.open();
            System.out.println(" >> Success");
        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        } finally {
            myDevice.close();
        }
    }

    public void SendString(String str){
        byte[] dataToSend = str.getBytes();
        try {
            System.out.format("Sending broadcast data: '%s'", new String(dataToSend));

            if (myDevice.getXBeeProtocol() == XBeeProtocol.XBEE_WIFI) {
                myDevice.close();
                myDevice = new WiFiDevice(PORT, BAUD_RATE);
                myDevice.open();
                ((WiFiDevice)myDevice).sendBroadcastIPData(0x2616, dataToSend);
            } else
                myDevice.sendBroadcastData(dataToSend);

            System.out.println(" >> Success");

        } catch (XBeeException e) {
            System.out.println(" >> Error");
            e.printStackTrace();
            System.exit(1);
        } 
    }
}