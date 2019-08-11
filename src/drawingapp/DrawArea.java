package drawingapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.BasicStroke;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
// import javax.swing.JLabel;

import drawingapp.CoordinateMessageList;

/**
 * DrawArea
 */

public class DrawArea extends JComponent {

    private static final long serialVersionUID = 1L;
    private BufferedImage image;
    private Graphics2D g2d;
    private int currX, currY, prevX, prevY, newX, newY;
    private int areaWidth, areaHeight;
    CoordinateMessageList coordsQueue;
    private PenStates penState = PenStates.PEN_UP;
    private boolean dragCorrectFlag = false;

    public DrawArea(CoordinateMessageList sharedQueue, int width, int height) {
        coordsQueue = sharedQueue;
        areaWidth = width;
        areaHeight = height;

        setDoubleBuffered(true);
        addMouseListener(new CustomMouseListener());
        addMouseMotionListener(new CustomMouseMotionListener());
    }

    protected void paintComponent(Graphics g) {
        if (image == null) {
            // Create image
            // image = createImage(areaWidth, areaHeight);
            image = new BufferedImage(areaWidth, areaHeight, BufferedImage.TYPE_INT_RGB);
            g2d = (Graphics2D) image.getGraphics();
            g2d.setStroke(new BasicStroke(3));
            // Enable antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Clear drawing area
            clear();

        }
        g.drawImage(image, 0, 0, null);
    }

    public void clear() {
        g2d.setPaint(Color.white);
        // Draw a white rectangle which fills entire area
        g2d.fillRect(0, 0, getSize().width, getSize().height);
        g2d.setPaint(Color.black);
        repaint();
    }

    public void red() {
        g2d.setPaint(Color.red);
    }

    public void black() {
        g2d.setPaint(Color.black);
    }

    public void green() {
        g2d.setPaint(Color.black);
    }

    public void blue() {
        g2d.setPaint(Color.blue);
    }

    public void save() throws IOException {
        // dPanel.paintAll(g2d);
        if (ImageIO.write(image, "png", new File("./images/output_image.png"))) {
            System.out.println("-- saved");
        }
    }

    public void load() throws IOException {
        image = ImageIO.read(new File("./images/output_image.png"));
        // g2d.drawImage(image, null, areaWidth, areaHeight);
        g2d = (Graphics2D) image.getGraphics();
        g2d.setStroke(new BasicStroke(3));
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setPaint(Color.black);
        repaint();
    }

    class CustomMouseListener implements MouseListener {
        // private JLabel statusLabel;
        public void mouseClicked(MouseEvent e) {
            // statusLabel.setText("Mouse Clicked: ("+e.getX()+", "+e.getY() +")");
            setNewX(e.getX());
            setNewY(e.getY());
            // penState = PenStates.PEN_DOWN;
            // addCoordinateMessage();
            if (newX >= 0 && newX <= areaWidth)
                prevX = newX;
            if (newY >= 0 && newY <= areaHeight)
                prevY = newY;
            // addCoordinateMessage();
            // penState = PenStates.PEN_UP;
            // addCoordinateMessage();
        }

        public void mousePressed(MouseEvent e) {
            setNewX(e.getX());
            setNewY(e.getY());
            penState = PenStates.PEN_DOWN;
            addCoordinateMessage();
            if (newX >= 0 && newX <= areaWidth)
                prevX = newX;
            if (newY >= 0 && newY <= areaHeight)
                prevY = newY;
            addCoordinateMessage();
            dragCorrectFlag = true;
        }

        public void mouseReleased(MouseEvent e) {
            penState = PenStates.PEN_UP;
            dragCorrectFlag = false;
            addCoordinateMessage();
        }

        public void mouseEntered(MouseEvent e) {
            setNewX(e.getX());
            setNewY(e.getY());
            penState = PenStates.PEN_UP;
            addCoordinateMessage();
            if (newX >= 0 && newX <= areaWidth)
                prevX = newX;
            if (newY >= 0 && newY <= areaHeight)
                prevY = newY;
            addCoordinateMessage();
            dragCorrectFlag = false;
        }

        public void mouseExited(MouseEvent e) {
            // penDown = false;
            // addCoordinateMessage();
            dragCorrectFlag = false;
        }
    }

    class CustomMouseMotionListener implements MouseMotionListener {

        public void mouseMoved(MouseEvent e) {
            setNewX(e.getX());
            setNewY(e.getY());
            if (moveDistance() < 200) {
                return;
            }
            if (newX >= 0 && newX <= areaWidth) {
                currX = newX;
            } else {
                currX = newX < 0 ? 0 : areaWidth;
            }
            if (newY >= 0 && newY <= areaHeight) {
                currY = newY;
            } else {
                currY = newY < 0 ? 0 : areaHeight;
            }

            addCoordinateMessage();
            // addCoordinateMessage();
        }

        public void mouseDragged(MouseEvent e) {
            setNewX(e.getX());
            setNewY(e.getY());

            penState = PenStates.PEN_DOWN;
            addCoordinateMessage();

            if (newX >= 0 && newX <= areaWidth) {
                currX = newX;
            } else {
                currX = newX < 0 ? 0 : areaWidth;
            }
            if (newY >= 0 && newY <= areaHeight) {
                currY = newY;
            } else {
                currY = newY < 0 ? 0 : areaHeight;
            }

            addCoordinateMessage();
            if (dragCorrectFlag) {
                if (g2d != null) {
                    g2d.drawLine(prevX, prevY, currX, currY);
                    repaint();
                    prevX = currX;
                    prevY = currY;
                }
            }
            dragCorrectFlag = true;
        }
    }

    public void addCoordinateMessage() {
        synchronized (coordsQueue) {
            coordsQueue.addCoordinatesToList(penState, currX, currY);
            coordsQueue.notify();
        }

    }

    public int getCurrX() {
        return currX;
    }

    public void setCurrX(int currX) {
        this.currX = currX;
    }

    public int getCurrY() {
        return currY;
    }

    public void setCurrY(int currY) {
        this.currY = currY;
    }

    public int getPrevX() {
        return prevX;
    }

    public void setPrevX(int prevX) {
        this.prevX = prevX;
    }

    public int getPrevY() {
        return prevY;
    }

    public void setPrevY(int prevY) {
        this.prevY = prevY;
    }

    public int getNewX() {
        return newX;
    }

    public void setNewX(int newX) {
        this.newX = newX;
    }

    public int getNewY() {
        return newY;
    }

    public void setNewY(int newY) {
        this.newY = newY;
    }

    private int moveDistance() {
        int dX, dY;
        dX = currX - newX;
        dY = currY - newY;
        return (int) Math.sqrt(dX * dX + dY * dY);
    }
}