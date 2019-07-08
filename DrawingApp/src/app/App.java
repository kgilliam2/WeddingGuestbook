package app;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.util.LinkedList;
import app.CoordinateMessageList;
/**
 * app
 */
public class App {
    boolean flag = false;
    public static void main(String[] args) {
       
        
        
        LinkedList<String> sharedQueue = new LinkedList<String>();
        CoordinateMessageList coordsQueue = new CoordinateMessageList();
        GcodeGenerator gcg = new GcodeGenerator("generator", coordsQueue, sharedQueue, 1000);
        GcodeSender gcs = new GcodeSender("Sender", sharedQueue);
        gcg.start();
        gcs.start();

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                

                JFrame frame = new JFrame("App Title");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(2500, 1250);
                frame.setVisible(true);
                Container content = frame.getContentPane();
                content.setLayout(new BorderLayout());
                final DrawArea drawArea = new DrawArea(coordsQueue);
                content.add(BorderLayout.CENTER, drawArea);
                // 
            }
        });
        
        
    }
}