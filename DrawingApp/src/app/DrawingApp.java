package app;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

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
    public static final boolean DEBUG_MODE = true;
    Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();

    static LinkedList<String> sharedQueue = new LinkedList<String>();
    static CoordinateMessageList coordsQueue = new CoordinateMessageList();

    public static void main(String[] args) {

        GcodeGenerator gcg = new GcodeGenerator("generator", coordsQueue, sharedQueue, 1000);
        GcodeSender gcs = new GcodeSender("Sender", sharedQueue);
        gcg.setPriority(2);
        gcs.setPriority(1);

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

        int displayWidth, displayHeight, drawWidth, drawHeight, topHeight, bottomHeight;
        Container content;
        final DrawArea drawArea;
        JLabel statusLabel = new JLabel();
        Button clearButton = new Button("Clear");

        // int density = Toolkit.getDefaultToolkit().getScreenResolution();
        displayWidth = (int) displaySize.getWidth();
        displayHeight = (int) displaySize.getHeight();
        drawWidth = displayWidth;
        drawHeight = displayWidth / 2;

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        content = frame.getContentPane();
        content.setLayout(new BorderLayout());
        drawArea = new DrawArea(coordsQueue, drawWidth, drawHeight);
        
        if (DEVELOPER_MODE && !DEBUG_MODE)  {
            frame.setSize(new Dimension(displayWidth, drawHeight));
            content.add(drawArea, BorderLayout.CENTER);
        }
        else if (DEBUG_MODE)  {
            frame.setSize(new Dimension(displayWidth, drawHeight));
            content.add(drawArea, BorderLayout.CENTER);
        }
        else {

            topHeight = displayHeight - drawHeight;
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
            topPanel.add(new Box.Filler(minTopSize, prefTopSize, maxTopSize));
            topPanel.setMaximumSize(maxTopSize);
            // Set up bottom panel
            statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 25));
            statusLabel.setText("Status Text");
            Dimension minBottomSize = new Dimension(0, 0);
            Dimension prefBottomSize = new Dimension(displayWidth, bottomHeight);
            Dimension maxBottomSize = new Dimension(displayWidth, bottomHeight);
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.PAGE_AXIS));
            bottomPanel.add(statusLabel);
            bottomPanel.add(new Box.Filler(minBottomSize, prefBottomSize, maxBottomSize));
            bottomPanel.setBackground(Color.BLACK);
            bottomPanel.setMaximumSize(maxBottomSize);

            // set up the drawArea background
            Dimension minDrawSize = new Dimension(drawWidth, drawHeight);
            Dimension prefDrawSize = new Dimension(drawWidth, drawWidth);
            Dimension maxDrawSize = new Dimension(drawWidth, drawHeight);
            Box.Filler fillerBox = new Box.Filler(minDrawSize, prefDrawSize, maxDrawSize);
            JPanel midPanel = new JPanel();

            midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.LINE_AXIS));
            midPanel.add(fillerBox);
            midPanel.setOpaque(true);
            // drawArea.setPreferredSize(prefDrawSize);
            midPanel.setBackground(Color.GREEN);

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

        frame.setVisible(true);
    }
}