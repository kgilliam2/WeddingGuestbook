package main.java.app;

import java.awt.BorderLayout;
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
import javax.swing.JButton;
import javax.swing.JFrame;
// import javax.swing.FullScreenFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.util.LinkedList;
import main.java.app.CoordinateMessageList;

public class DrawingApp {
    // boolean flag = false;
    public static final boolean DEVELOPER_MODE = false;
    public static final boolean WAIT_FOR_CONNECT = false;
    public static final float Y_MIN_BUFFER_MM = 0;
    public static final float Y_MAX_BUFFER_MM = 10;
    public static final float X_MIN_BUFFER_MM = 5;
    public static final float X_MAX_BUFFER_MM = 5;
    public static final float MAX_TRAVEL_X = 800;
    public static final float MAX_TRAVEL_Y = 300;
    public static GcodeGenerator gcg;
    public static GcodeSender gcs;
    public long lastPressTime = 0;
    public long newPressTime = 0;
    public int exitPressCount = 0;
    JFrame frame = new JFrame("App Title");
    JPanel topPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();

    DrawArea drawArea;
    JButton loadButton = new JButton("LOAD");
    JButton saveButton = new JButton("SAVE");
    JButton clearButton = new JButton("CLEAR");
    JButton homeButton = new JButton("HOME");
    JButton progButton = new JButton("PROGRAM");
    JButton exitButton = new JButton("EXIT");

    Dimension displaySize = Toolkit.getDefaultToolkit().getScreenSize();

    static LinkedList<GcodeMessage> sharedGcodeQueue = new LinkedList<GcodeMessage>();
    static CoordinateMessageList coordsQueue = new CoordinateMessageList();

    ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == clearButton) {
                try {
                    drawArea.save();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                drawArea.clear();
                try {
                    gcs.autoHome();
                } catch (NullPointerException | IOException | InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else if (e.getSource() == homeButton) {
                // btnConnectActionPerformed(e);
                try {
                    gcs.autoHome();
                } catch (IOException | InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else if (e.getSource() == progButton) {
                try {
                    gcs.programGRBL();
                } catch (IOException | InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else if (e.getSource() == saveButton) {
                try {
                    drawArea.save();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } else if (e.getSource() == loadButton) {
                try {
                    drawArea.load();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
            else if (e.getSource() == exitButton) {
                lastPressTime = newPressTime;
                newPressTime = System.nanoTime();
                long elapsed_millis = (newPressTime - lastPressTime)/1000000;
                if (elapsed_millis < 500) {
                    exitPressCount++;
                }
                else{
                    exitPressCount = 0;
                    hideDebugButtons();
                }
                if(exitPressCount > 10){
                    System.exit(0);
                } else if (exitPressCount == 5){
                    showDebugButtons();
                }
                // System.out.println(exitPressCount);
            }
        }
    };

    public static void main(String[] args) {

        gcg = new GcodeGenerator("generator", coordsQueue, sharedGcodeQueue, 1000);
        gcs = new GcodeSender("Sender", sharedGcodeQueue);
        // if (WAIT_FOR_CONNECT)
        //     while(!gcs.initSerialCommunication() );
        // else
        //     gcs.initSerialCommunication();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DrawingApp app = new DrawingApp();
                app.setupGUI();
                // toc = System.nanoTime() / 1000000;
                // while ((toc - tic) > 5000) {toc = System.nanoTime() / 1000000;}
                // statusLabel.setText(gcg.getNextGcodeString());
            }
        });
        gcg.start();
        gcs.start();

        // if (!DEVELOPER_MODE) {
        //     try {
        //         Thread.sleep(3000);

        //     } catch (InterruptedException e) {
        //         // TODO Auto-generated catch block
        //         e.printStackTrace();
        //     } 
        // }
    }

    private void setupGUI() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();

        int displayWidth, displayHeight, drawWidth, drawHeight, topHeight, bottomHeight;
        Container content;

        frame.setResizable(false);
        JLabel statusLabel = new JLabel();
        JLabel posLabel = new JLabel();
        JLabel welcomeLabel = new JLabel();
        // JLabel step1_Label = new JLabel();
        // JLabel step2_Label = new JLabel();
        // JPanel stepsPanel = new JPanel();

        gcs.setStatusLabel(statusLabel);
        gcg.setPosLabel(posLabel);

        statusLabel.setFont(new Font("Courier", Font.PLAIN, 20));
        statusLabel.setForeground(Color.GRAY);

        welcomeLabel.setFont(new Font("Didot", Font.PLAIN, 110));
        welcomeLabel.setForeground(Color.white);
        welcomeLabel.setText("Welcome!");
        // step1_Label.setFont(new Font("Didot", Font.BOLD, 70));
        // step1_Label.setForeground(Color.white);
        // step1_Label.setText("Please sign in here, then pull some paper through for the next person.");
        // step2_Label.setFont(new Font("Didot", Font.BOLD, 70));
        // step2_Label.setForeground(Color.white);
        // step2_Label.setText("Press \"Clear\" to clean the slate!");

        // stepsPanel.setLayout(new BoxLayout(stepsPanel, BoxLayout.PAGE_AXIS));
        // stepsPanel.setBackground(Color.BLACK);
        // stepsPanel.add(step1_Label);
        // stepsPanel.add(step2_Label);
        // stepsPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        displayWidth = (int) displaySize.getWidth();
        displayHeight = (int) displaySize.getHeight();
        float heightWidthRatio = MAX_TRAVEL_Y / MAX_TRAVEL_X;
        drawWidth = displayWidth;
        drawHeight = (int) (displayWidth * heightWidthRatio);

        PixelToPaperTransform.setPixelLimits(drawWidth, drawHeight);
        PixelToPaperTransform.setPaperLimits(X_MIN_BUFFER_MM, MAX_TRAVEL_X - X_MAX_BUFFER_MM,
                MAX_TRAVEL_Y - Y_MAX_BUFFER_MM, Y_MIN_BUFFER_MM);

        topHeight = (displayHeight - drawHeight) / 2;
        bottomHeight = displayHeight - drawHeight - topHeight; // topHeight;// - statusLabel.getHeight();

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
        topPanel.add(welcomeLabel);
        topPanel.add(new Box.Filler(minTopSize, prefTopSize, maxTopSize));
        
        // Set up bottom panel
        Dimension minBottomSize = new Dimension(0, 0);
        Dimension prefBottomSize = new Dimension(displayWidth, bottomHeight);
        Dimension maxBottomSize = new Dimension(displayWidth, bottomHeight);
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(Color.BLACK);
        // bottomPanel.setOpaque(false);
        bottomPanel.setMaximumSize(maxBottomSize);
        bottomPanel.add(new Box.Filler(minBottomSize, prefBottomSize, maxBottomSize));

        // Set up the Buttons panel
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        // buttonsPanel.setLayout(new FlowLayout());
        // Dimension minButtonsSize = new Dimension(0, 0);
        // Dimension prefButtonsSize = new Dimension(displayWidth, topHeight);
        // Dimension maxButtonsSize = new Dimension(displayWidth, topHeight);
        buttonsPanel.setBackground(Color.BLACK);
        // buttonsPanel.setMaximumSize(maxButtonsSize);
        // buttonsPanel.add(new Box.Filler(minButtonsSize, prefButtonsSize, maxButtonsSize));
         // buttonsPanel.add
         loadButton.setBackground(Color.white);
         loadButton.setFont(new Font("Arial", Font.PLAIN, 30));
         loadButton.setVisible(false);
         saveButton.setBackground(Color.white);
         saveButton.setFont(new Font("Arial", Font.PLAIN, 30));
         saveButton.setVisible(false);
         homeButton.setBackground(Color.white);
         homeButton.setFont(new Font("Arial", Font.PLAIN, 30));
         homeButton.setVisible(false);
         progButton.setBackground(Color.white);
         progButton.setFont(new Font("Arial", Font.PLAIN, 30));
         progButton.setVisible(false);
         topPanel.add(loadButton);
         topPanel.add(saveButton);
         topPanel.add(homeButton);
         topPanel.add(progButton);
         // frame.setUndecorated(true);
         // Transparent 16 x 16 pixel cursor image.
         BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
         // Create a new blank cursor.
         Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0),
                 "blank cursor");
         // Set the blank cursor to the JFrame.
         content.setCursor(blankCursor);
        if (DEVELOPER_MODE) {
            showDebugButtons();
        } 

        statusLabel.setFont(new Font(statusLabel.getFont().getName(), Font.PLAIN, 25));
        statusLabel.setText("Status Text");
        // stepsPanel.add(statusLabel);
        bottomPanel.add(statusLabel, BorderLayout.LINE_START);
        // bottomPanel.add(stepsPanel, BorderLayout.LINE_START);
        // Border emptyBorder = BorderFactory.createEmptyBorder();
        clearButton.setBackground(Color.BLACK);
        clearButton.setForeground(Color.WHITE);
        // clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setFont(new Font("Didot", Font.PLAIN, 70));
        // clearButton.setPreferredSize(new Dimension(800, 300));
        buttonsPanel.add(clearButton);
        exitButton.setFont(new Font("Didot", Font.PLAIN, 50));
        exitButton.setBorderPainted(false);
        exitButton.setBackground(Color.BLACK);
        exitButton.setForeground(Color.BLACK);
        exitButton.setFocusPainted(false);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        buttonsPanel.add(exitButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        bottomPanel.add(buttonsPanel, BorderLayout.LINE_END);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setState(JFrame.MAXIMIZED_BOTH);
        device.setFullScreenWindow(frame);

        // Add everything to frame
        content.add(topPanel, BorderLayout.PAGE_START);
        content.add(bottomPanel, BorderLayout.PAGE_END);
        // content.add(midPanel, BorderLayout.CENTER);
        content.add(drawArea, BorderLayout.CENTER);
        // frame.pack();
        loadButton.addActionListener(actionListener);
        saveButton.addActionListener(actionListener);
        clearButton.addActionListener(actionListener);
        homeButton.addActionListener(actionListener);
        progButton.addActionListener(actionListener);
        exitButton.addActionListener(actionListener);
        frame.setVisible(true);
    }
    private void showDebugButtons(){
        loadButton.setVisible(true);
        loadButton.setEnabled(true);
        saveButton.setVisible(true);
        saveButton.setEnabled(true);
        homeButton.setVisible(true);
        homeButton.setEnabled(true);
        progButton.setVisible(true);
        progButton.setEnabled(true);
        // frame.setVisible(true);
    }
    private void hideDebugButtons(){
        loadButton.setVisible(false);
        loadButton.setEnabled(false);
        saveButton.setVisible(false);
        saveButton.setEnabled(false);
        homeButton.setVisible(false);
        homeButton.setEnabled(false);
        progButton.setVisible(false);
        progButton.setEnabled(false);
        // frame.setVisible(true);
    }
}