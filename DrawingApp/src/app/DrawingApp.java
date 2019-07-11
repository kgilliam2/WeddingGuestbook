package app;

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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
// import javax.swing.FullScreenFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.util.LinkedList;
import app.CoordinateMessageList;

/**
 * app
 */

public class DrawingApp {
    // boolean flag = false;
    public static final boolean DEVELOPER_MODE = true;

    DrawArea drawArea;
    Button clearButton = new Button("Clear");
    Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();

    static LinkedList<String> sharedQueue = new LinkedList<String>();
    static CoordinateMessageList coordsQueue = new CoordinateMessageList();
    static SerialCommunicator serialComm = new SerialCommunicator();

    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == clearButton) {
                drawArea.clear();
            }
            else {
                btnConnectActionPerformed(e);
            }
        }
    };

    public static void main(String[] args) {

        GcodeGenerator gcg = new GcodeGenerator("generator", coordsQueue, sharedQueue, 1000);
        GcodeSender gcs = new GcodeSender("Sender", sharedQueue);

        gcg.setMaxTravelX(800);
        gcg.setMaxTravelY(300);
        gcg.setDrawWidth(2736);
        gcg.setDrawHeight(1368);
        
        serialComm.initSerialCommunication();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DrawingApp app = new DrawingApp();
                app.setupGUI();
                // statusLabel.setText(gcg.getNextGcodeString());
            }
        });
        gcg.start();
        gcs.start();

    }

    private void setupGUI() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        JFrame frame = new JFrame("App Title");
        // JLabel statusLabel = new JLabel();
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        //comment
        int displayWidth, displayHeight, drawWidth, drawHeight, topHeight, bottomHeight;
        Container content;

        JLabel statusLabel = new JLabel();

        // int density = Toolkit.getDefaultToolkit().getScreenResolution();
        displayWidth = (int) displaySize.getWidth();
        displayHeight = (int) displaySize.getHeight();
        drawWidth = displayWidth;
        drawHeight = displayWidth / 2;
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        drawArea = new DrawArea(coordsQueue, drawWidth, drawHeight);
        // Transparent 16 x 16 pixel cursor image.
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        // Create a new blank cursor.
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

        // Set the blank cursor to the JFrame.
       
        if (DEVELOPER_MODE) {
            frame.setSize(new Dimension(displayWidth, drawHeight));
            content.add(drawArea, BorderLayout.CENTER);
        } else {

            topHeight = (displayHeight - drawHeight) / 2;
            bottomHeight = topHeight;// - statusLabel.getHeight();

            // frame.setSize(new Dimension(displayWidth, drawHeight + topHeight +
            // bottomHeight));
            frame.setUndecorated(true);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setState(JFrame.MAXIMIZED_BOTH);
            device.setFullScreenWindow(frame);
            // frame.setPreferredSize(frame.getGraphicsConfiguration().getBounds().getSize());
            frame.setResizable(true);
            // frame.setAlwaysOnTop(true);
            // frame.pack();

            // drawArea.setSize(new Dimension(drawWidth, drawHeight));

            // Set up the top panel
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
            Dimension minTopSize = new Dimension(0, 0);
            Dimension prefTopSize = new Dimension(displayWidth, topHeight);
            Dimension maxTopSize = new Dimension(displayWidth, topHeight);
            topPanel.setBackground(Color.BLACK);
            topPanel.add(clearButton);
            topPanel.setMaximumSize(maxTopSize);
            topPanel.add(new Box.Filler(minTopSize, prefTopSize, maxTopSize));

            // Set up bottom panel
            statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 25));
            statusLabel.setText("Status Text");
            Dimension minBottomSize = new Dimension(0, 0);
            Dimension prefBottomSize = new Dimension(displayWidth, bottomHeight);
            Dimension maxBottomSize = new Dimension(displayWidth, bottomHeight);
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
            bottomPanel.setBackground(Color.BLACK);
            bottomPanel.setMaximumSize(maxBottomSize);
            bottomPanel.add(new Box.Filler(minBottomSize, prefBottomSize, maxBottomSize));
            bottomPanel.add(statusLabel);
            // set up the drawArea background
            Dimension minDrawSize = new Dimension(drawWidth, drawHeight);
            Dimension prefDrawSize = new Dimension(drawWidth, drawWidth);
            Dimension maxDrawSize = new Dimension(drawWidth, drawHeight);
            Box.Filler fillerBox = new Box.Filler(minDrawSize, prefDrawSize, maxDrawSize);
            JPanel midPanel = new JPanel();

            midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.LINE_AXIS));
            midPanel.setOpaque(true);
            // drawArea.setPreferredSize(prefDrawSize);
            midPanel.setBackground(Color.GREEN);
            midPanel.add(fillerBox);
            // Add everything to frame
            content.add(topPanel, BorderLayout.PAGE_START);
            content.add(midPanel, BorderLayout.CENTER);
            content.add(drawArea, BorderLayout.CENTER);
            content.add(bottomPanel, BorderLayout.PAGE_END);
            frame.addFocusListener(new FocusListener() {

                @Override
                public void focusGained(FocusEvent arg0) {
                    frame.setAlwaysOnTop(true);
                }

                @Override
                public void focusLost(FocusEvent arg0) {
                    frame.setAlwaysOnTop(false);
                }
            });
            // frame.pack();
        }
        content.setCursor(blankCursor);
        clearButton.addActionListener(actionListener);
        frame.setVisible(true);
    }

    private void btnConnectActionPerformed(ActionEvent e) {
        serialComm.connect();
        if (serialComm.isConnected() == true)
        {
            if (serialComm.initIOStream() == true)
            {
                serialComm.initListener();
            }
        }
    }
}