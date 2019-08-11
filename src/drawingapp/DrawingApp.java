package drawingapp;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// import java.awt.event.FocusEvent;
// import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
// import javax.swing.FullScreenFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.LinkedList;
import drawingapp.CoordinateMessageList;

public class DrawingApp {
    // boolean flag = false;
    public static final boolean DEVELOPER_MODE = true;
    public static final float Y_MIN_BUFFER_MM = 10;
    public static final float Y_MAX_BUFFER_MM = 10;
    public static final float X_MIN_BUFFER_MM = 5;
    public static final float X_MAX_BUFFER_MM = 5;
    public static final float MAX_TRAVEL_X = 800;
    public static final float MAX_TRAVEL_Y = 300;
    public static GcodeGenerator gcg;
    public static GcodeSender gcs;

    DrawArea drawArea;
    Button clearButton = new Button("CLEAR");
    Button homeButton = new Button("HOME");
    Button progButton = new Button("PROGRAM");

    Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();

    static LinkedList<GcodeMessage> sharedGcodeQueue = new LinkedList<GcodeMessage>();
    static CoordinateMessageList coordsQueue = new CoordinateMessageList();

    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == clearButton) {
                drawArea.clear();
            } else if (e.getSource() == homeButton) {
                // btnConnectActionPerformed(e);
                try {   
                    gcs.autoHome();
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getSource() == progButton){
                    try {
                    gcs.programGRBL();
                } catch (IOException | InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    };

    public static void main(String[] args) {

        gcg = new GcodeGenerator("generator", coordsQueue, sharedGcodeQueue, 1000);
        gcs = new GcodeSender("Sender", sharedGcodeQueue);
        gcs.initSerialCommunication();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DrawingApp app = new DrawingApp();
                app.setupGUI();
                // statusLabel.setText(gcg.getNextGcodeString());
            }
        });
        gcg.start();
        gcs.start();
        
        if(!DEVELOPER_MODE) {
        	try {
				Thread.sleep(3000);
				gcs.autoHome();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private void setupGUI() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        JFrame frame = new JFrame("App Title");
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        int displayWidth, displayHeight, drawWidth, drawHeight, topHeight, bottomHeight;
        Container content;


        JLabel statusLabel = new JLabel();
        JLabel posLabel = new JLabel();
        
        gcs.setStatusLabel(statusLabel);
        gcg.setPosLabel(posLabel);
        
        statusLabel.setFont(new Font("Courier", Font.PLAIN, 20));
        statusLabel.setForeground(Color.white);

        displayWidth = (int) displaySize.getWidth();
        displayHeight = (int) displaySize.getHeight();
        float heightWidthRatio = MAX_TRAVEL_Y/MAX_TRAVEL_X;
        drawWidth = displayWidth;
        drawHeight = (int)(displayWidth * heightWidthRatio);

        PixelToPaperTransform.setPixelLimits(drawWidth, drawHeight);
        PixelToPaperTransform.setPaperLimits(
            X_MIN_BUFFER_MM, MAX_TRAVEL_X - X_MAX_BUFFER_MM, 
            MAX_TRAVEL_Y - Y_MAX_BUFFER_MM, Y_MIN_BUFFER_MM);
            
        topHeight = (displayHeight - drawHeight) / 2;
        bottomHeight = displayHeight - drawHeight - topHeight; //topHeight;// - statusLabel.getHeight();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        drawArea = new DrawArea(coordsQueue, drawWidth, drawHeight);

     // Set up the top panel
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        Dimension minTopSize = new Dimension(0, 0);
        Dimension prefTopSize = new Dimension(displayWidth, topHeight);
        Dimension maxTopSize = new Dimension(displayWidth, topHeight);
        topPanel.setBackground(Color.BLACK);
        topPanel.setMaximumSize(maxTopSize);
        topPanel.add(new Box.Filler(minTopSize, prefTopSize, maxTopSize));

        // Set up bottom panel
        Dimension minBottomSize = new Dimension(0, 0);
        Dimension prefBottomSize = new Dimension(displayWidth, bottomHeight);
        Dimension maxBottomSize = new Dimension(displayWidth, bottomHeight);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
//        bottomPanel.setOpaque(false);
        bottomPanel.setMaximumSize(maxBottomSize);
        bottomPanel.add(new Box.Filler(minBottomSize, prefBottomSize, maxBottomSize));

        if (DEVELOPER_MODE) {
            clearButton.setBackground(Color.white);
            clearButton.setFont(new Font("Arial", Font.PLAIN, 30));
            homeButton.setBackground(Color.white);
            homeButton.setFont(new Font("Arial", Font.PLAIN, 30));
            progButton.setBackground(Color.white);
            progButton.setFont(new Font("Arial", Font.PLAIN, 30));

            topPanel.add(clearButton);
            topPanel.add(homeButton);
            topPanel.add(progButton);

        	bottomPanel.add(posLabel, BorderLayout.LINE_END);
            posLabel.setText("Pen Position");
            posLabel.setForeground(Color.white);
            
            statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 25));
            statusLabel.setText("Status Text");
            bottomPanel.add(statusLabel, BorderLayout.LINE_START);
        } else {
        	
//        	frame.setUndecorated(true);
        	// Transparent 16 x 16 pixel cursor image.
            BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            // Create a new blank cursor.
            Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
            // Set the blank cursor to the JFrame.
            content.setCursor(blankCursor);
//            frame.addFocusListener(new FocusListener() {
//
//                @Override
//                public void focusGained(FocusEvent arg0) {
//                    frame.setAlwaysOnTop(true);
//                }
//
//                @Override
//                public void focusLost(FocusEvent arg0) {
//                    frame.setAlwaysOnTop(false);
//                }
//            });
        }
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setState(JFrame.MAXIMIZED_BOTH);
        device.setFullScreenWindow(frame);
        
        // Add everything to frame
        content.add(topPanel, BorderLayout.PAGE_START);
        content.add(bottomPanel, BorderLayout.PAGE_END);
//        content.add(midPanel, BorderLayout.CENTER);
        content.add(drawArea, BorderLayout.CENTER);
        // frame.pack();
        clearButton.addActionListener(actionListener);
        homeButton.addActionListener(actionListener);
        progButton.addActionListener(actionListener);
        frame.setVisible(true);
        
    }

}